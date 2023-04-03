package greencity;

import greencity.configuration.RedirectionConfigProp;
import greencity.dto.AddNewTariffDto;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.address.AddressDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagLimitDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.EmployeeNameDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.location.AddLocationTranslationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.RegionTranslationDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.dto.notification.NotificationTemplateMainInfoDto;
import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.order.AdminCommentDto;
import greencity.dto.order.ChangeOrderResponseDTO;
import greencity.dto.order.EcoNumberDto;
import greencity.dto.order.ExportDetailsDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderClientDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderFondyClientDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.dto.order.UpdateAllOrderPageDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.position.PositionDto;
import greencity.dto.service.ServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.service.GetTariffServiceDto;
import greencity.dto.service.TariffServiceDto;
import greencity.dto.tariff.EditTariffDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.tariff.SetTariffLimitsDto;
import greencity.dto.user.*;
import greencity.dto.violation.ViolationDetailInfoDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import greencity.enums.*;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static greencity.enums.ViolationLevel.MAJOR;

public class ModelUtils {

    public static Principal getPrincipal() {
        return () -> "test@gmail.com";
    }

    public static Principal getUuid() {
        return () -> "35467585763t4sfgchjfuyetf";
    }

    public static OrderResponseDto getOrderResponseDto() {
        return getOrderResponseDto(true);
    }

    public static OrderResponseDto getOrderResponseDto(boolean shouldBePaid) {
        return OrderResponseDto.builder()
            .additionalOrders(new HashSet<>(List.of("232534634")))
            .bags(Collections.singletonList(new BagDto(3, 999)))
            .orderComment("comment")
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .shouldBePaid(shouldBePaid)
            .personalData(PersonalDataDto.builder()
                .firstName("Anton")
                .lastName("Antonov")
                .id(13L)
                .email("mail@mail.ua")
                .phoneNumber("0678945221")
                .build())
            .addressId(1L)
            .locationId(1L)
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
            .region("Lvivskiy")
            .actual(false)
            .build();
    }

    public static List<AddressDto> addressDto() {
        List<AddressDto> list = new ArrayList<>();
        list.add(AddressDto.builder()
            .id(1L)
            .entranceNumber("7a")
            .houseCorpus("2")
            .houseNumber("7")
            .street("Городоцька")
            .streetEn("Gorodotska")
            .coordinates(Coordinates.builder().latitude(2.3).longitude(5.6).build())
            .district("Залізничний")
            .districtEn("Zaliznuchnuy")
            .regionEn("Region")
            .region("Регіон")
            .cityEn("Lviv")
            .city("Львів")
            .actual(false)
            .build());
        list.add(AddressDto.builder().id(2L)
            .entranceNumber("9a")
            .houseCorpus("2")
            .houseNumber("7")
            .street("Шевченка")
            .streetEn("Shevchenka")
            .coordinates(Coordinates.builder().latitude(3.3).longitude(6.6).build())
            .district("Залізничний")
            .districtEn("Zaliznuchnuy")
            .regionEn("Region")
            .region("Регіон")
            .city("Львів")
            .cityEn("Lviv")
            .actual(false)
            .build());
        return list;
    }

    public static UserProfileDto userProfileDto() {
        return UserProfileDto.builder()
            .recipientName("Dima")
            .recipientSurname("Petrov")
            .recipientPhone("666051373")
            .recipientEmail("petrov@gmail.com")
            .telegramIsNotify(true)
            .viberIsNotify(false)
            .build();
    }

    public static OrderClientDto getOrderClientDto() {
        return OrderClientDto.builder()
            .id(1L)
            .orderStatus(OrderStatus.FORMED)
            .amount(450L)
            .build();
    }

    public static NotificationDto getNotificationDto() {
        return NotificationDto.builder()
            .title("Test")
            .body("Test")
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
            .recipientId(2L)
            .recipientName("Anatolii")
            .recipientSurName("Petyrov")
            .recipientPhoneNumber("095123456")
            .recipientEmail("anatolii.andr@gmail.com")
            .build();
    }

    public static UbsCustomersDto getUbsCustomersDto() {
        return UbsCustomersDto.builder()
            .name("Ivan Petyrov")
            .email("lipa@gmail.com")
            .phoneNumber("096765432")
            .build();
    }

    public static OrderDetailStatusDto getOrderDetailStatusDto() {
        return getOrderDetailStatusDto(PaymentStatus.PAID);
    }

    public static OrderDetailStatusDto getOrderDetailStatusDto(PaymentStatus paymentStatus) {
        return OrderDetailStatusDto.builder()
            .paymentStatus(paymentStatus.name())
            .orderStatus(OrderStatus.CONFIRMED.name())
            .date(LocalDateTime.now().toString())
            .build();
    }

    public static ExportDetailsDto getOrderDetailExportDto() {
        return ExportDetailsDto.builder()
            .dateExport("20-12-2001")
            .timeDeliveryFrom("20:20:20")
            .timeDeliveryTo("20:20:20")
            .receivingStationId(1L)
            .allReceivingStations(List.of(getReceivingStationDto()))
            .build();
    }

    public static PositionDto getPositionDto() {
        return PositionDto.builder()
            .id(1L)
            .name("Водій")
            .build();
    }

    public static ReceivingStationDto getReceivingStationDto() {
        return ReceivingStationDto.builder()
            .id(1L)
            .name("Петрівка")
            .build();
    }

    public static ViolationDetailInfoDto getViolationDetailInfoDto() {
        LocalDateTime localdatetime = LocalDateTime.of(
            2021, Month.MARCH,
            16, 13, 00, 00);

        return ViolationDetailInfoDto.builder()
            .orderId(1L)
            .addedByUser("Alan Po")
            .violationLevel(MAJOR)
            .description("violation1")
            .violationDate(localdatetime)
            .build();
    }

    public static Address address() {
        List<Long> id = addressDto().stream().map(AddressDto::getId).collect(Collectors.toList());
        List<String> city = addressDto().stream().map(AddressDto::getCity).collect(Collectors.toList());
        List<String> street = addressDto().stream().map(AddressDto::getStreet).collect(Collectors.toList());
        List<String> district = addressDto().stream().map(AddressDto::getDistrict).collect(Collectors.toList());
        List<String> houseNumber = addressDto().stream().map(AddressDto::getHouseNumber).collect(Collectors.toList());
        List<String> entranceNumber =
            addressDto().stream().map(AddressDto::getEntranceNumber).collect(Collectors.toList());
        List<String> houseCorpus = addressDto().stream().map(AddressDto::getHouseCorpus).collect(Collectors.toList());
        List<Boolean> actual = addressDto().stream().map(AddressDto::getActual).collect(Collectors.toList());
        return Address.builder()
            .id(id.get(0))
            .city(String.valueOf(city))
            .district(String.valueOf(district))
            .street(String.valueOf(street))
            .coordinates(Coordinates.builder().latitude(2.3).longitude(5.6).build())
            .entranceNumber(String.valueOf(entranceNumber))
            .houseNumber(String.valueOf(houseNumber))
            .houseCorpus(String.valueOf(houseCorpus))
            .actual(Boolean.valueOf(String.valueOf(actual)))
            .addressStatus(AddressStatus.DELETED)
            .build();
    }

    public static ManualPaymentRequestDto getRequestDto() {
        return ManualPaymentRequestDto.builder()
            .amount(500L)
            .settlementdate("09-02-2021")
            .receiptLink("somelink.com")
            .paymentId("10l")
            .build();
    }

    public static OrderCancellationReasonDto getCancellationDto() {
        return OrderCancellationReasonDto.builder()
            .cancellationReason(CancellationReason.OTHER)
            .cancellationComment("Garbage disappeared")
            .build();
    }

    public static TariffServiceDto getTariffServiceDto() {
        return TariffServiceDto.builder()
            .capacity(120)
            .commission(10)
            .price(100)
            .name("Test")
            .nameEng("a")
            .description("Description")
            .descriptionEng("DescriptionEng")
            .build();
    }

    public static ServiceDto getServiceDto() {
        return ServiceDto.builder()
            .name("Name")
            .nameEng("NameEng")
            .price(100)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .build();
    }

    public static GetTariffServiceDto getGetTariffServiceDto() {
        return GetTariffServiceDto.builder()
            .id(1)
            .name("Бавовняна сумка")
            .capacity(120)
            .price(120)
            .commission(50)
            .description("Description")
            .limitIncluded(true)
            .build();
    }

    public static AdminCommentDto getAdminComment() {
        return AdminCommentDto.builder()
            .orderId(1L)
            .adminComment("Admin").build();
    }

    public static EcoNumberDto getEcoNumberDto() {
        return EcoNumberDto.builder()
            .ecoNumber(Set.of("1111111111"))
            .build();
    }

    public static GetServiceDto getGetServiceDto() {
        return GetServiceDto.builder()
            .id(1L)
            .name("Name")
            .nameEng("NameEng")
            .price(100)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .build();
    }

    public static PaymentResponseDto getPaymentResponseDto() {
        return PaymentResponseDto.builder()
            .order_id("1_1")
            .merchant_id(1)
            .actual_amount(1)
            .actual_currency("1")
            .amount(1)
            .build();
    }

    public static AddingPointsToUserDto getAddingPointsToUserDto() {
        return AddingPointsToUserDto.builder()
            .email("ddd@email.com")
            .additionalPoints(2)
            .build();
    }

    public static OrderFondyClientDto getOrderFondyClientDto() {
        return OrderFondyClientDto.builder()
            .orderId(1L)
            .pointsToUse(100)
            .certificates(Collections.emptySet())
            .build();
    }

    public static List<RegionTranslationDto> getRegionTranslationsDto() {
        return List.of(RegionTranslationDto.builder()
            .languageCode("ua")
            .regionName("Київська область")
            .build());
    }

    public static List<LocationCreateDto> getLocationCreateDtoList() {
        return List.of(LocationCreateDto.builder()
            .addLocationDtoList(getAddLocationTranslationDtoList())
            .regionTranslationDtos(getRegionTranslationsDto())
            .longitude(1.32d)
            .latitude(3.34)
            .build());
    }

    public static List<AddLocationTranslationDto> getAddLocationTranslationDtoList() {
        return List.of(AddLocationTranslationDto.builder()
            .locationName("Київ")
            .languageCode("ua")
            .build());
    }

    public static CreateCourierDto getCreateCourierDto() {
        return CreateCourierDto.builder()
            .nameEn("nameEn")
            .nameUk("nameUa")
            .build();
    }

    public static NotificationTemplateDto getNotificationTemplateDto() {
        return NotificationTemplateDto.builder()
            .id(1L)
            .notificationTemplateMainInfoDto(getNotificationTemplateMainInfoDto())
            .build();
    }

    public static NotificationTemplateWithPlatformsDto getNotificationTemplateWithPlatformsDto() {
        return NotificationTemplateWithPlatformsDto.builder()
            .notificationTemplateMainInfoDto(getNotificationTemplateMainInfoDto())
            .platforms(List.of(
                getNotificationPlatformDto(NotificationReceiverType.SITE)))
            .build();
    }

    public static NotificationTemplateWithPlatformsUpdateDto getNotificationTemplateWithPlatformsUpdateDto() {
        return NotificationTemplateWithPlatformsUpdateDto.builder()
            .type(NotificationType.UNPAID_ORDER)
            .trigger(NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS)
            .time(NotificationTime.AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID)
            .schedule("0 0 18 * * ?")
            .title("Неопачене замовлення")
            .titleEng("Unpaid order")
            .notificationStatus(NotificationStatus.ACTIVE)
            .platforms(List.of(
                getNotificationPlatformDto(NotificationReceiverType.SITE)))
            .build();
    }

    public static NotificationTemplateMainInfoDto getNotificationTemplateMainInfoDto() {
        return NotificationTemplateMainInfoDto.builder()
            .type(NotificationType.UNPAID_ORDER)
            .trigger(NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS)
            .triggerDescription(NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS
                .getDescription())
            .triggerDescriptionEng(NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS
                .getDescriptionEng())
            .time(NotificationTime.AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID)
            .timeDescription(NotificationTime.AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID
                .getDescription())
            .timeDescriptionEng(NotificationTime.AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID
                .getDescriptionEng())
            .schedule("0 0 18 * * ?")
            .title("Неопачене замовлення")
            .titleEng("Unpaid order")
            .notificationStatus(NotificationStatus.ACTIVE)
            .build();
    }

    public static NotificationPlatformDto getNotificationPlatformDto(
        NotificationReceiverType receiverType) {
        return NotificationPlatformDto.builder()
            .id(1L)
            .receiverType(receiverType)
            .nameEng("Site")
            .body("Body")
            .bodyEng("BodyEng")
            .status(NotificationStatus.ACTIVE)
            .build();
    }

    public static UpdateAllOrderPageDto getUpdateAllOrderPageDto() {
        return UpdateAllOrderPageDto.builder()
            .orderId(List.of(1L, 2L, 3L))
            .build();
    }

    public static RequestToChangeOrdersDataDto getRequestToChangeOrdersDataDTO() {
        return RequestToChangeOrdersDataDto.builder()
            .orderIdsList(List.of(1L))
            .columnName("name")
            .newValue("1")
            .build();
    }

    public static ChangeOrderResponseDTO getChangeOrderResponseDTO() {
        return ChangeOrderResponseDTO.builder()
            .httpStatus(HttpStatus.OK)
            .unresolvedGoalsOrderId(List.of(1L))
            .build();
    }

    public static AddBonusesToUserDto getAddBonusesToUserDto() {
        return AddBonusesToUserDto.builder()
            .paymentId("5")
            .receiptLink("test")
            .settlementdate("test")
            .amount(500L)
            .build();
    }

    public static GetTariffsInfoDto getAllTariffsInfoDto() {
        return GetTariffsInfoDto.builder()
            .cardId(1L)
            .courierDto(CourierDto.builder()
                .courierId(1L)
                .nameUk("Тест")
                .nameEn("Test")
                .build())
            .createdAt(LocalDate.of(22, 2, 12))
            .creator(EmployeeNameDto.builder()
                .firstName("Test")
                .lastName("Test")
                .build())
            .build();
    }

    public static AddNewTariffDto getAddNewTariffDto() {
        return AddNewTariffDto.builder()
            .regionId(1L)
            .courierId(1L)
            .locationIdList(List.of(1L))
            .receivingStationsIdList(List.of(1L))
            .build();
    }

    public static EditTariffDto getEditTariffDto() {
        return EditTariffDto.builder()
            .locationIds(List.of(1L))
            .receivingStationIds(List.of(1L))
            .build();
    }

    public static RedirectionConfigProp getRedirectionConfig() {
        return new RedirectionConfigProp()
            .setGreenCityClient("123")
            .setUserServerAddress("123");
    }

    public static CreateAddressRequestDto getAddressRequestDto() {
        return CreateAddressRequestDto.builder()
            .addressComment("fdsfs")
            .searchAddress("fake address")
            .district("fdsfds")
            .districtEn("dsadsad")
            .region("regdsad")
            .regionEn("regdsaden")
            .houseNumber("1")
            .houseCorpus("2")
            .entranceNumber("3")
            .build();
    }

    public static UserEmployeeAuthorityDto getUserEmployeeAuthorityDto() {
        return UserEmployeeAuthorityDto.builder()
            .authorities(Collections.singletonList("SEE_CLIENTS_PAGE"))
            .employeeEmail("test@mail.com")
            .build();
    }

    public static SetTariffLimitsDto setTariffLimitsWithAmountOfBags() {
        return SetTariffLimitsDto.builder()
            .min(1L)
            .max(2L)
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .bagLimitDtoList(List.of(
                BagLimitDto
                    .builder()
                    .id(1)
                    .limitIncluded(true)
                    .build()))
            .build();
    }

    public static UserProfileCreateDto getUserProfileCreateDto() {
        return UserProfileCreateDto.builder()
            .name("UbsProfile")
            .email("ubsuser@mail.com")
            .uuid("f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
            .build();
    }
}
