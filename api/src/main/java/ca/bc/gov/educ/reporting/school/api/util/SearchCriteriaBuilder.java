package ca.bc.gov.educ.reporting.school.api.util;

import java.time.Year;
import java.util.List;

import java.time.Year;
import java.util.List;

public class SearchCriteriaBuilder {

    public static List<SearchCriteriaGroup> septemberCollectionsFromLastYear() {
        int lastYear = Year.now().getValue() - 1;

        String startDate = lastYear + "-01-01";
        String endDate = lastYear + "-12-31";
        String betweenValue = startDate + "," + endDate;

        // collectionTypeCode = SEPTEMBER
        SearchCriterion collectionTypeCode = new SearchCriterion(
                "collectionTypeCode", "SEPTEMBER", "eq", "STRING", null
        );

        // openDate between lastYear-01-01 and lastYear-12-31
        SearchCriterion openDateBetween = new SearchCriterion(
                "openDate", betweenValue, "btn", "DATE", "AND"
        );

        return List.of(
                new SearchCriteriaGroup(null, List.of(collectionTypeCode)),
                new SearchCriteriaGroup("AND", List.of(openDateBetween))
        );
    }
}




