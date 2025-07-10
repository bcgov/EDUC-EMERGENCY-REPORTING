package ca.bc.gov.educ.reporting.school.api.service.v1;

import ca.bc.gov.educ.reporting.school.api.rest.RestUtils;
import ca.bc.gov.educ.reporting.school.api.struct.v1.School;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SchoolReportingAPIService {

    private final RestUtils restUtils;

    @Autowired
    public SchoolReportingAPIService(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    public List<School> getAllSchoolsWithHeadcounts() {
        Map<String, School> schools = restUtils.getCachedSchools();
        Map<String, Long> headcounts = restUtils.getCachedHeadCounts();

        return schools.values().stream()
                .map(school -> {
                    Long count = headcounts.getOrDefault(school.getSchoolId(), 0L);
                    school.setHeadCount(String.valueOf(count));  // convert long to string
                    return school;
                })
                .sorted(Comparator.comparing(School::getDisplayName))
                .collect(Collectors.toList());
    }
}
