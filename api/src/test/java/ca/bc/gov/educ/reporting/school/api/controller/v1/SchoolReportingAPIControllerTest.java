package ca.bc.gov.educ.reporting.school.api.controller.v1;

import ca.bc.gov.educ.reporting.school.api.BaseSchoolReportingAPITest;
import ca.bc.gov.educ.reporting.school.api.constants.v1.URL;
import ca.bc.gov.educ.reporting.school.api.rest.RestUtils;
import ca.bc.gov.educ.reporting.school.api.service.v1.SchoolReportingAPIService;
import ca.bc.gov.educ.reporting.school.api.struct.v1.School;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

public class SchoolReportingAPIControllerTest extends BaseSchoolReportingAPITest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    SchoolReportingAPIService service;


    protected static final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();


    @Test
    void getAllSchools_shouldReturnSchoolsWithHeadcounts() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_SDC_MINISTRY_REPORTS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);
        School school = new School();
        school.setSchoolId("123");
        school.setDisplayName("Test School");
        school.setHeadCount("42");

        Mockito.when(service.getAllSchoolsWithHeadcounts()).thenReturn(List.of(school));
        var resultActions1 = this.mockMvc.perform(get(URL.BASE_URL + "/schools").with(mockAuthority).contentType(APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].schoolId").value("123"))
                .andExpect(jsonPath("$[0].headCount").value("42"));
        List<School> summary1 = objectMapper.readValue(
                resultActions1.andReturn().getResponse().getContentAsByteArray(),
                new TypeReference<List<School>>() {}
        );
        assertThat(summary1).isNotNull();
        }
   @Test
    void getAllSchools_shouldReturnEmptyListWhenNoSchoolsExist() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_SDC_MINISTRY_REPORTS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        Mockito.when(service.getAllSchoolsWithHeadcounts()).thenReturn(Collections.emptyList());
        var resultActions = this.mockMvc.perform(get(URL.BASE_URL + "/schools").with(mockAuthority).contentType(APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        List<School> summary = objectMapper.readValue(
                resultActions.andReturn().getResponse().getContentAsByteArray(),
                new TypeReference<List<School>>() {}
        );
        assertThat(summary).isEmpty();
    }
    @Test
    void getAllSchools_shouldReturnSchoolsWithNullHeadcounts() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_SDC_MINISTRY_REPORTS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);
        School school = new School();
        school.setSchoolId("456");
        school.setDisplayName("School Without Headcount");
        school.setHeadCount(null);

        Mockito.when(service.getAllSchoolsWithHeadcounts()).thenReturn(List.of(school));
        var resultActions = this.mockMvc.perform(get(URL.BASE_URL + "/schools").with(mockAuthority).contentType(APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].schoolId").value("456"))
                .andExpect(jsonPath("$[0].headCount").isEmpty());
        List<School> summary = objectMapper.readValue(
                resultActions.andReturn().getResponse().getContentAsByteArray(),
                new TypeReference<List<School>>() {}
        );
        assertThat(summary).isNotNull();
        assertThat(summary.get(0).getHeadCount()).isNull();
    }

}
