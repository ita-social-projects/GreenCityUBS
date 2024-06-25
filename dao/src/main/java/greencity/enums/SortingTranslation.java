package greencity.enums;

import java.util.Set;

/**
 * This interface defines methods for obtaining sorting-related information for
 * enums that represent different statuses.
 *
 * @param <T> the type represent different statuses.
 */
public interface SortingTranslation <T extends Enum<T>> {
    /**
     * Method returns the sort order value of the current enum constant.
     *
     * @return {@link int}.
     */
    int getSortOrder();

    /**
     * Returns a set of all constants sorted in ascending order according to their
     * sort order values.
     *
     * @return a sorted {@link Set} of enum constants.
     */
    Set<T> getSortedTranslations();

    /**
     * Returns the enum constant that represents a default or "other" status.
     *
     * @return the enum constant representing the "OTHER" status.
     */
    T getOtherStatus();
}
