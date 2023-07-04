package greencity.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocationDivisionTest {

    @Test
    void testRegionLevelId() {
        Assertions.assertEquals(1, LocationDivision.REGION.getLevelId());
    }

    @Test
    void testDistrictInRegionLevelId() {
        Assertions.assertEquals(2, LocationDivision.DISTRICT_IN_REGION.getLevelId());
    }

    @Test
    void testLocalCommunityLevelId() {
        Assertions.assertEquals(3, LocationDivision.LOCAL_COMMUNITY.getLevelId());
    }

    @Test
    void testCityLevelId() {
        Assertions.assertEquals(4, LocationDivision.CITY.getLevelId());
    }

    @Test
    void testDistrictInCityLevelId() {
        Assertions.assertEquals(5, LocationDivision.DISTRICT_IN_CITY.getLevelId());
    }
}
