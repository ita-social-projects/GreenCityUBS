package greencity.enums;

/**
 * Enum for representing various location divisions. Each enum instance
 * represents a different level of division, with an associated level ID and
 * Ukrainian name.
 */
public enum LocationDivision {
    /**
     * Represents a region or autonomous republic.
     */
    REGION(1, "Область або АРК"),

    /**
     * Represents a district within a region or autonomous republic.
     */
    DISTRICT_IN_REGION(2, "Район в області або в АРК"),

    /**
     * Represents a local community.
     */
    LOCAL_COMMUNITY(3, "Територіальна громада"),

    /**
     * Represents a city or village.
     */
    CITY(4, "Місто або село"),

    /**
     * Represents a district within a city.
     */
    DISTRICT_IN_CITY(5, "Район в місті");

    /**
     * ID for the location division level.
     */
    int levelId;

    /**
     * Ukrainian name for the location division.
     */
    String nameUa;

    /**
     * Constructor for creating a location division instance.
     *
     * @param levelId The ID for the location division level.
     * @param nameUa  The Ukrainian name for the location division.
     */
    LocationDivision(int levelId, String nameUa) {
        this.levelId = levelId;
        this.nameUa = nameUa;
    }

    /**
     * Returns the ID for the location division level.
     *
     * @return The ID for the location division level.
     */
    public int getLevelId() {
        return levelId;
    }
}
