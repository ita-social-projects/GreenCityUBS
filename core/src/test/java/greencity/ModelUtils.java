package greencity;

import greencity.dto.BagDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ModelUtils {
    public static Principal getPrincipal() {
        return () -> "test@gmail.com";
    }

    public static OrderResponseDto getOrderResponceDto() {
        return OrderResponseDto.builder()
            .additionalOrders(new HashSet<>(Arrays.asList("232534634")))
            .bags(Collections.singletonList(new BagDto(3, 999)))
            .orderComment("comment")
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .personalData(PersonalDataDto.builder()
                .firstName("Anton")
                .lastName("Antonov")
                .id(13L)
                .email("mail@mail.ua")
                .district("frankivskiy")
                .addressComment("near mall")
                .city("Lviv")
                .houseNumber("4R")
                .entranceNumber("5")
                .phoneNumber("067894522")
                .street("Levaya")
                .houseCorpus(null)
                .build())
            .build();
    }
}
