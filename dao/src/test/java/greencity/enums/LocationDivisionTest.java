package greencity.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LocationDivisionTest {

    @Test
    public void testRegionLevelId() {
        Assertions.assertEquals(1, LocationDivision.REGION.getLevelId());
    }

    @Test
    public void testDistrictInRegionLevelId() {
        Assertions.assertEquals(2, LocationDivision.DISTRICT_IN_REGION.getLevelId());
    }

    @Test
    public void testLocalCommunityLevelId() {
        Assertions.assertEquals(3, LocationDivision.LOCAL_COMMUNITY.getLevelId());
    }

    @Test
    public void testCityLevelId() {
        Assertions.assertEquals(4, LocationDivision.CITY.getLevelId());
    }

    @Test
    public void testDistrictInCityLevelId() {
        Assertions.assertEquals(5, LocationDivision.DISTRICT_IN_CITY.getLevelId());
    }
}
