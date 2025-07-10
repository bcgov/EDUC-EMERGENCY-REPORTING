package ca.bc.gov.educ.reporting.school.api.controller.v1;


import ca.bc.gov.educ.reporting.school.api.endpoint.v1.SchoolReportingAPIEndpoint;
import ca.bc.gov.educ.reporting.school.api.service.v1.SchoolReportingAPIService;
import ca.bc.gov.educ.reporting.school.api.struct.v1.School;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class SchoolReportingAPIController implements SchoolReportingAPIEndpoint {

    private final SchoolReportingAPIService schoolReportingAPIService;

    public SchoolReportingAPIController(SchoolReportingAPIService schoolReportingAPIService) {
        this.schoolReportingAPIService = schoolReportingAPIService;
    }


    @Override
    public List<School> getAllSchools() {
      return schoolReportingAPIService.getAllSchoolsWithHeadcounts();
  }

}
