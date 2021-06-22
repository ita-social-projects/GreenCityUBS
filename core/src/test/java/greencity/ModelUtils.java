package greencity;

import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.OrderStatus;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ModelUtils {
    public static Principal getPrincipal() {
        return () -> "test@gmail.com";
    }

    public static Principal getUuid() {
        return () -> "35467585763t4sfgchjfuyetf";
    }

    public static OrderResponseDto getOrderResponseDto() {
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
                .phoneNumber("067894522")
                .build())
            .addressId(1L)
            .build();
    }

    public static OrderAddressDtoRequest getOrderAddressDtoRequest() {
        return OrderAddressDtoRequest.builder()
            .id(0L)
            .entranceNumber("7a")
            .houseCorpus("2")
            .houseNumber("7")
            .street("Gorodotska")
            .coordinates(Coordinates.builder().latitude(2.3).longitude(5.6).build())
            .district("Zaliznuchnuy")
            .city("Lviv")
            .actual(false)
            .build();
    }

    public static AddressDto addressDto() {
        return AddressDto.builder()
            .id(1L)
            .entranceNumber("7a")
            .houseCorpus("2")
            .houseNumber("7")
            .street("Gorodotska")
            .coordinates(Coordinates.builder().latitude(2.3).longitude(5.6).build())
            .district("Zaliznuchnuy")
            .city("Lviv")
            .actual(false)
            .build();
    }

    public static UserProfileDto userProfileDto() {
        return UserProfileDto.builder()
            .recipientName("Dima")
            .recipientSurname("Petrov")
            .recipientPhone("666051373")
            .recipientEmail("petrov@gmail.com")
            .build();
    }

    public static OrderClientDto getOrderClientDto() {
        return OrderClientDto.builder()
            .id(1L)
            .orderStatus(OrderStatus.FORMED)
            .amount(450L)
            .build();
    }

    public static UserInfoDto getUserInfoDto() {
        return UserInfoDto.builder()
            .customerName("customer name")
            .customerPhoneNumber("1234")
            .customerEmail("test@gmail.com")
            .recipientName("recipient name")
            .customerPhoneNumber("321")
            .customerEmail("customer@gmail.com")
            .totalUserViolations(2)
            .userViolationForCurrentOrder(1)
            .build();
    }

    public static UbsCustomersDtoUpdate getUbsCustomersDtoUpdate() {
        return UbsCustomersDtoUpdate.builder()
            .id(1l)
            .recipientName("Anatolii Petyrov")
            .recipientPhoneNumber("095123456")
            .recipientEmail("anatolii.andr@gmail.com")
            .build();
    }

    public static UbsCustomersDto getUbsCustomersDto() {
        return UbsCustomersDto.builder()
            .name("Ivan Lipa")
            .email("lipa@gmail.com")
            .phoneNumber("096765432")
            .build();
    }
}
