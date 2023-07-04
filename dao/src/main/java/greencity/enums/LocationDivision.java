package greencity.enums;


enum LocationDivision {
    REGION(1, "Область або АРК"),
    DISTRICT_IN_REGION(2, "Район в області або в АРК"),
    LOCAL_COMMUNITY(3, "Територіальна громада"),
    CITY(4, "Місто або село"),
    DISTRICT_IN_CITY(5, "Район в місті");
    int levelId;
    String nameUa;

    LocationDivision(int levelId, String nameUa) {
        this.levelId = levelId;
        this.nameUa = nameUa;
    }
}
