package ca.bc.gov.educ.reporting.school.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {ReportingApiResourceApplication.class})
@ActiveProfiles("testWebclient")
@AutoConfigureMockMvc
public class BaseSchoolReportingAPITest {

    @BeforeEach
    public void before() {

  }

  @AfterEach
  public void resetState() {

  }
}
