package ca.bc.gov.educ.reporting.school.api.rest;

import ca.bc.gov.educ.reporting.school.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.reporting.school.api.model.v1.PaginatedResponse;
import ca.bc.gov.educ.reporting.school.api.properties.ApplicationProperties;
import ca.bc.gov.educ.reporting.school.api.struct.v1.Collection;
import ca.bc.gov.educ.reporting.school.api.struct.v1.School;
import ca.bc.gov.educ.reporting.school.api.util.SearchCriteriaBuilder;
import ca.bc.gov.educ.reporting.school.api.util.SearchCriteriaGroup;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@Slf4j
public class RestUtils {

    public static final String PAGE_SIZE_VALUE = "1500";
    private final ReadWriteLock allSchoolLock = new ReentrantReadWriteLock();
    private final ReadWriteLock allHeadCountLock = new ReentrantReadWriteLock();
    private final Map<String, School> allSchoolMap = new ConcurrentHashMap<>();
    private final Map<String, Long> allHeadCountMap = new ConcurrentHashMap<>();
    private static final String CONTENT_TYPE = "Content-Type";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    @Value("${initialization.background.enabled}")
    private Boolean isBackgroundInitializationEnabled;

    @Getter
    private final ApplicationProperties props;

    @Autowired
    public RestUtils(WebClient webClient, final ApplicationProperties props) {
        this.webClient = webClient;
        this.props = props;
    }

    @PostConstruct
    public void init() {
        if (this.isBackgroundInitializationEnabled != null && this.isBackgroundInitializationEnabled) {
            ApplicationProperties.bgTask.execute(this::initialize);
        }
    }

    @Scheduled(cron = "${schedule.jobs.load.school.cron}")
    public void scheduledSchoolRefresh() {
        populateAllSchoolMap();
    }

    @Scheduled(cron = "${schedule.jobs.load.headcount.cron}")
    public void scheduledHeadcountRefresh() {
        populateHeadCountMap();
    }



    private void initialize() {
        this.populateAllSchoolMap();
        this.populateHeadCountMap();
    }

    private void populateHeadCountMap() {
        val writeLock = this.allHeadCountLock.writeLock();
        try {
            writeLock.lock();
            log.info("Calling SDC API via REST to get the September Collection");
            PaginatedResponse<Collection> collections = getCollections();
            if (collections.getContent().isEmpty()) {
                log.warn("No collections found.");
                return;
            }

            Collection collection = collections.getContent().get(0);

            if (StringUtils.isNotBlank(collection.getCollectionID())) {
                Map<String, Long> headcount = getHeadCount(collection.getCollectionID());
                allHeadCountMap.clear();
                allHeadCountMap.putAll(headcount);
                log.info("Loaded {} allHeadcounts to memory", this.allHeadCountMap.size());
            } else {
                log.warn("Collection ID is blank.");
            }
        } catch (Exception ex) {
            log.error("Unable to load map cache for allHeadCounts", ex);
        }
        finally {
            writeLock.unlock();
        }
    }

    @Retryable(
            retryFor = { Exception.class },
            noRetryFor = {EntityNotFoundException.class },
            backoff = @Backoff(multiplier = 2, delay = 2000)
    )
    private Map<String, Long> getHeadCount(String collectionID) {
        log.info("Calling SDC API to load Headcounts groups to memory");

        return this.webClient.get()
                .uri(this.props.getSdcApiURL() + "/ministryHeadcounts/allSchoolHeadcounts/" + collectionID)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Long>>() {})
                .doOnError(e -> log.error("Retry failed fetching headcount for collectionID={}", collectionID, e))
                .block();
    }

    @Recover
    public Map<String, Long> recover(Exception e, String collectionID) {
        log.error("Retry failed fetching headcount for collectionID={}", collectionID, e);
        // Handle recovery: return default/fallback value, throw, etc.
        return Collections.emptyMap();
    }


    public void populateAllSchoolMap() {
        val writeLock = this.allSchoolLock.writeLock();
        try {
            writeLock.lock();
            log.info("Calling Institute api via REST to load schools to memory");

            List<School> allSchools = getAllSchools();

            for (School school : allSchools) {
                this.allSchoolMap.put(school.getSchoolId(), school);
            }
        } catch (Exception ex) {
            log.error("Unable to load map cache for allSchool", ex);
            throw ex;
        } finally {
            writeLock.unlock();
        }
        log.info("Loaded {} allSchools to memory", this.allSchoolMap.size());
    }

    public List<School> getAllSchools() {
        return getAllSchoolsRecursively(0, new ArrayList<>());
    }

    public List<School> getAllSchoolsRecursively(int pageNumber, List<School> accumulator) {
        PaginatedResponse<School> response = getSchoolsPaginatedFromInstituteApi(pageNumber);
        if (response == null) {
            return accumulator;
        }
        accumulator.addAll(response.getContent());
        if (response.hasNext()) {
            return getAllSchoolsRecursively(response.nextPageable().getPageNumber(), accumulator);
        }
        return accumulator;
    }

    PaginatedResponse<School> getSchoolsPaginatedFromInstituteApi(int pageNumber) {
        int pageSize = Integer.parseInt(PAGE_SIZE_VALUE);
        try {
            String fullUrl = this.props.getInstituteApiURL()
                    + "/school/paginated"
                    + "?pageNumber=" + pageNumber
                    + "&pageSize=" + pageSize;
            return webClient.get()
                    .uri(fullUrl)
                    .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PaginatedResponse<School>>() {})
                    .block();
        } catch (Exception ex) {
            log.error("Error fetching schools on page {}", pageNumber, ex);
            return null;
        }
    }

    public PaginatedResponse<Collection> getCollections() throws JsonProcessingException {
        List<SearchCriteriaGroup> searchCriteriaList = SearchCriteriaBuilder.septemberCollectionsFromLastYear();
        String searchJson = objectMapper.writeValueAsString(searchCriteriaList);
        String encodedSearchJson = URLEncoder.encode(searchJson, StandardCharsets.UTF_8);

        int pageNumber = 0;
        int pageSize = 50;

        try {
            String fullUrl = this.props.getSdcApiURL()
                    + "/collection/paginated"
                    + "?pageNumber=" + pageNumber
                    + "&pageSize=" + pageSize
                    + "&searchCriteriaList=" + encodedSearchJson;
            return webClient.get()
                    .uri(fullUrl)
                    .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PaginatedResponse<Collection>>() {
                    })
                    .block();
        } catch (Exception ex) {
            log.error("Error fetching schools on page {}", pageNumber, ex);
            return null;
        }
    }

    public Map<String, School> getCachedSchools() {
        val readLock = allSchoolLock.readLock();
        try {
            readLock.lock();
            return Collections.unmodifiableMap(new HashMap<>(allSchoolMap));
        } finally {
            readLock.unlock();
        }
    }

    public Map<String, Long> getCachedHeadCounts() {
        val readLock = allHeadCountLock.readLock();
        try {
            readLock.lock();
            return Collections.unmodifiableMap(new HashMap<>(allHeadCountMap));
        } finally {
            readLock.unlock();
        }
    }



}
