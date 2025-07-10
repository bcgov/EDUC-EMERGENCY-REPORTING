package ca.bc.gov.educ.reporting.school.api.endpoint.v1;

import ca.bc.gov.educ.reporting.school.api.constants.v1.URL;
import ca.bc.gov.educ.reporting.school.api.struct.v1.School;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(URL.BASE_URL)
public interface SchoolReportingAPIEndpoint {

  @GetMapping("/schools")
  @PreAuthorize("hasAuthority('SCOPE_READ_SDC_MINISTRY_REPORTS')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  @Transactional(readOnly = true)
  @Tag(name = "School Entity", description = "Endpoints for school entity.")
  @Schema(name = "School", implementation = School.class)
  List<School> getAllSchools();
}
