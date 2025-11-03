package greencity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for representing various location divisions. Each enum instance
 * represents a different level of division, with an associated level ID
 */
@Getter
@RequiredArgsConstructor
public enum LocationDivision {
    /**
     * Represents a region or autonomous republic.
     */
    REGION(1),

    /**
     * Represents a district within a region or autonomous republic.
     */
    DISTRICT_IN_REGION(2),

    /**
     * Represents a local community.
     */
    LOCAL_COMMUNITY(3),

    /**
     * Represents a city or village.
     */
    CITY(4),

    /**
     * Represents a district within a city.
     */
    DISTRICT_IN_CITY(5);

    /**
     * ID for the location division level.
     */
    private final int levelId;
}
