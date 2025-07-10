package ca.bc.gov.educ.reporting.school.api.util;

import java.util.List;

public record SearchCriterion(
        String key,
        String value,
        String operation,
        String valueType,
        String condition
) {}

