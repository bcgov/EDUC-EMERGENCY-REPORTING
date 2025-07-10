package ca.bc.gov.educ.reporting.school.api.util;

import java.util.List;

public record SearchCriteriaGroup(
        String condition,
        List<SearchCriterion> searchCriteriaList
) {}
