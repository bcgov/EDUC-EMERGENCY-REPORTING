package ca.bc.gov.educ.reporting.school.api.properties;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Executor;

@Component
@Getter
public class ApplicationProperties {

  public static final Executor bgTask = new EnhancedQueueExecutor.Builder()
          .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("bg-task-executor-%d").build())
          .setCorePoolSize(1).setMaximumPoolSize(1).setKeepAliveTime(Duration.ofSeconds(60)).build();

  public static final String EMERGENCY_REPORTING_API = "EMERGENCY-REPORTING-API";
  public static final String CORRELATION_ID = "correlationID";
  public static final String API_NAME = "EMERGENCY-REPORTING-API";
  /**
   * The Stan url.
   */


  @Value("${url.api.institute}")
  private String instituteApiURL;

  @Value("${url.api.sdc}")
  private String sdcApiURL;

  @Value("${url.token}")
  private String tokenURL;

  @Value("${client.id}")
  private String clientID;

  @Value("${client.secret}")
  private String clientSecret;
}
