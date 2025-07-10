package ca.bc.gov.educ.reporting.school.api.service;

import ca.bc.gov.educ.reporting.school.api.service.v1.SchoolReportingAPIService;
import ca.bc.gov.educ.reporting.school.api.struct.v1.School;
import ca.bc.gov.educ.reporting.school.api.rest.RestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class SchoolReportingAPIServiceTest {

    @InjectMocks
    private SchoolReportingAPIService service;

    @Mock
    private RestUtils restUtils;

    @Test
    void getAllSchoolsWithHeadcounts_shouldReturnSchoolsWithCounts() {
        School school = new School();
        school.setSchoolId("123");
        school.setDisplayName("Test School");

        Mockito.when(restUtils.getCachedSchools()).thenReturn(Map.of("123", school));
        Mockito.when(restUtils.getCachedHeadCounts()).thenReturn(Map.of("123", 42L));

        List<School> result = service.getAllSchoolsWithHeadcounts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSchoolId()).isEqualTo("123");
        assertThat(result.get(0).getHeadCount()).isEqualTo("42");
    }
}
