package ca.bc.gov.educ.reporting.school.api.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper=false)
public class Collection implements Serializable {

  private static final long serialVersionUID = 1L;

  private String collectionID;

  @NotNull(message = "collectionStatusCode cannot be null")
  private String collectionStatusCode;

  @Size(max = 10)
  @NotNull(message = "collectionTypeCode cannot be null")
  private String collectionTypeCode;

  @NotNull(message = "open date cannot be null")
  private String openDate;

  @NotNull(message = "close date cannot be null")
  private String closeDate;

  @NotNull(message = "snapshot date cannot be null")
  private String snapshotDate;

  @NotNull(message = "submission due date cannot be null")
  private String submissionDueDate;

  @NotNull(message = "duplication resolution due date cannot be null")
  private String duplicationResolutionDueDate;

  @NotNull(message = "sign off due date cannot be null")
  private String signOffDueDate;

}
