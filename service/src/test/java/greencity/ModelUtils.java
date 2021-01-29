package greencity;

import greencity.dto.BagDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import greencity.entity.lang.Language;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import java.util.Collections;

public class ModelUtils {

    public static Language getLanguage() {
        return new Language(1L, "en");
    }

    public static OrderResponseDto getOrderResponceDto() {
        return OrderResponseDto.builder()
            .additionalOrder("232-534-634")
            .bags(Collections.singletonList(new BagDto(3, 999)))
            .orderComment("comment")
            .cerfiticate("")
            .pointsToUse(700)
            .personalData(PersonalDataDto.builder()
                .firstName("oleh")
                .lastName("ivanov")
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

    public static UBSuser getUBSuser() {
        return UBSuser.builder()
            .firstName("oleh")
            .lastName("ivanov")
            .email("mail@mail.ua")
            .id(1L)
            .phoneNumber("067894522")
            .userAddress(Address.builder()
                .id(1L)
                .city("Lviv")
                .street("Levaya")
                .district("frankivskiy")
                .entranceNumber("5")
                .comment("near mall")
                .houseCorpus(null)
                .houseNumber("4R")
                .build())
            .build();
    }

}
