package greencity.dto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class UpdateCourierDtoTest {

    @Test
    void test() {
        UpdateCourierDto testEntity1 = new UpdateCourierDto();
        UpdateCourierDto testEntity2 = new UpdateCourierDto(1L, null);

        assertNotNull(testEntity1);
        assertNotNull(testEntity2);

        assertNotEquals(testEntity1, testEntity2);

        assertNotNull(UpdateCourierDto.builder()
            .courierId(2L)
            .courierTranslationDtos(new ArrayList<>())
            .build().toString());

    }
}
