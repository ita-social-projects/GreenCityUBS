package greencity.filters;

import lombok.Data;

/**
 * Used to set the period between two date, for next use to filter.
 */
@Data
public class DateFilter {
    private String from;
    private String to;
}
