package greencity.dto.location;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CoordinatesDtoTest {

    @Test
    void testEqualsSameObjectShouldReturnTrue() {
        CoordinatesDto coordinates1 = new CoordinatesDto();
        coordinates1.setLatitude(50.4501);
        coordinates1.setLongitude(30.5234);

        assertThat(coordinates1.equals(coordinates1)).isTrue();
    }

    @Test
    void testEqualsDifferentObjectWithSameValuesShouldReturnTrue() {
        CoordinatesDto coordinates1 = new CoordinatesDto();
        coordinates1.setLatitude(50.4501);
        coordinates1.setLongitude(30.5234);

        CoordinatesDto coordinates2 = new CoordinatesDto();
        coordinates2.setLatitude(50.4501);
        coordinates2.setLongitude(30.5234);

        assertThat(coordinates1.equals(coordinates2)).isTrue();
    }

    @Test
    void testEqualsDifferentObjectWithSmallDifferenceInLatitudeShouldReturnTrue() {
        CoordinatesDto coordinates1 = new CoordinatesDto();
        coordinates1.setLatitude(50.4501);
        coordinates1.setLongitude(30.5234);

        CoordinatesDto coordinates2 = new CoordinatesDto();
        coordinates2.setLatitude(50.4502);
        coordinates2.setLongitude(30.5234);

        assertThat(coordinates1.equals(coordinates2)).isTrue();
    }

    @Test
    void testEqualsDifferentObjectWithLargerDifferenceInLatitudeShouldReturnFalse() {
        CoordinatesDto coordinates1 = new CoordinatesDto();
        coordinates1.setLatitude(50.4501);
        coordinates1.setLongitude(30.5234);

        CoordinatesDto coordinates2 = new CoordinatesDto();
        coordinates2.setLatitude(50.455);
        coordinates2.setLongitude(30.5234);

        assertThat(coordinates1.equals(coordinates2)).isFalse();
    }

    @Test
    void testHashCodeSameObjectShouldReturnSameHashCode() {
        CoordinatesDto coordinates1 = new CoordinatesDto();
        coordinates1.setLatitude(50.4501);
        coordinates1.setLongitude(30.5234);

        assertEquals(coordinates1.hashCode(), coordinates1.hashCode());
    }

    @Test
    void testHashCodeDifferentObjectWithSameValuesShouldReturnSameHashCode() {
        CoordinatesDto coordinates1 = new CoordinatesDto();
        coordinates1.setLatitude(50.4501);
        coordinates1.setLongitude(30.5234);

        CoordinatesDto coordinates2 = new CoordinatesDto();
        coordinates2.setLatitude(50.4501);
        coordinates2.setLongitude(30.5234);

        assertEquals(coordinates1.hashCode(), coordinates2.hashCode());
    }

    @Test
    void testHashCodeDifferentObjectWithDifferentValuesShouldReturnDifferentHashCode() {
        CoordinatesDto coordinates1 = new CoordinatesDto();
        coordinates1.setLatitude(50.4501);
        coordinates1.setLongitude(30.5234);

        CoordinatesDto coordinates2 = new CoordinatesDto();
        coordinates2.setLatitude(51.1657);
        coordinates2.setLongitude(10.4515);

        assertNotEquals(coordinates1.hashCode(), coordinates2.hashCode());
    }
}
