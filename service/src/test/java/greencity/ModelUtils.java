package greencity;

import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Order;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ModelUtils {

    public static OrderResponseDto getOrderResponceDto() {
        return OrderResponseDto.builder()
            .additionalOrders(new HashSet<>(Arrays.asList("232-534-634")))
            .bags(Collections.singletonList(new BagDto(3, 999)))
            .orderComment("comment")
            .certificates(Collections.emptySet())
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

    public static Order getOrder() {
        return Order.builder()
            .ubsUser(UBSuser.builder()
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
                    .coordinates(Coordinates.builder()
                        .latitude(49.83)
                        .longitude(23.88)
                        .build())
                    .build())
                .build())
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .build();
    }

    public static OrderDto getOrderDto() {
        return OrderDto.builder()
            .firstName("oleh")
            .lastName("ivanov")
            .address("frankivskiy Levaya 4R")
            .addressComment("near mall")
            .phoneNumber("067894522")
            .latitude(49.83)
            .longitude(23.88)
            .build();
    }

    public static Coordinates getCoordinates() {
        return Coordinates.builder()
            .latitude(49.83)
            .longitude(23.88)
            .build();
    }

    public static CoordinatesDto getCoordinatesDto() {
        return CoordinatesDto.builder()
            .latitude(49.83)
            .longitude(23.88)
            .build();
    }

}
