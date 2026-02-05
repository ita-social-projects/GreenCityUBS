package greencity;

import greencity.dto.AddNewTariffDto;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.address.AddressDto;
import greencity.dto.address.UpdateAddressDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagLimitDto;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.EmployeeNameDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.location.AddLocationTranslationDto;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.RegionTranslationDto;
import greencity.dto.notification.AddNotificationPlatformDto;
import greencity.dto.notification.AddNotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateMainInfoDto;
import greencity.dto.notification.NotificationTemplateUpdateInfoDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.dto.order.ChangeOrderResponseDTO;
import greencity.dto.order.EcoNumberDto;
import greencity.dto.order.ExportDetailsDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderDetailStatusRequestDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.dto.order.UpdateAllOrderPageDto;
import greencity.dto.order.UpdateOrderPageAdminDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.service.GetTariffServiceDto;
import greencity.dto.service.ServiceDto;
import greencity.dto.service.TariffServiceDto;
import greencity.dto.tariff.EditTariffDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.tariff.SetTariffLimitsDto;
import greencity.dto.telegram.UpdateBotMessageRequestDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.dto.useragreement.UserAgreementDto;
import greencity.dto.violation.AddingViolationsToUserDto;
import greencity.dto.violation.ViolationDetailInfoDto;
import greencity.enums.CancellationReason;
import greencity.enums.CourierLimit;
import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationStatus;
import greencity.enums.NotificationTime;
import greencity.enums.NotificationTrigger;
import greencity.enums.NotificationType;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.PaymentSystem;
import greencity.enums.UserCategory;
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

import static greencity.enums.NotificationReceiverType.EMAIL;
import static greencity.enums.NotificationReceiverType.MOBILE;
import static greencity.enums.NotificationReceiverType.SITE;
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
            .additionalOrders(new HashSet<>(List.of("12345678")))
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
                .phoneNumber("+380678945221")
                .build())
            .addressId(1L)
            .tariffId(1L)
            .paymentSystem(PaymentSystem.WAY_FOR_PAY)
            .build();
    }

    public static OrderAddressDtoRequest getOrderAddressDtoRequest() {
        return OrderAddressDtoRequest.builder()
            .id(0L)
            .actual(false)
            .districtEn("Shevchenkivskyi")
            .districtUk("Шевченківський")
            .regionEn("Kyiv Oblast")
            .regionUk("Київська область")
            .houseNumber("25B")
            .entranceNumber("3")
            .houseCorpus("2A")
            .addressComment("Next to the park")
            .placeId("ChIJp0lN2HIRkEARuJ1pl_yMcc0")
            .coordinates(new CoordinatesDto(50.4501, 30.5234))
            .cityUk("Київ")
            .cityEn("Kyiv")
            .streetUk("Хрещатик")
            .streetEn("Khreshchatyk")
            .build();
    }

    public static List<AddressDto> addressDto() {
        List<AddressDto> list = new ArrayList<>();
        list.add(AddressDto.builder()
            .id(1L)
            .entranceNumber("7a")
            .houseCorpus("2")
            .houseNumber("7")
            .streetUk("Городоцька")
            .streetEn("Gorodotska")
            .coordinates(CoordinatesDto.builder().latitude(2.3).longitude(5.6).build())
            .districtUk("Залізничний")
            .districtEn("Zaliznuchnuy")
            .regionEn("Region")
            .regionUk("Регіон")
            .cityEn("Lviv")
            .cityUk("Львів")
            .addressRegionDistrictList(new ArrayList<>())
            .actual(false)
            .build());
        list.add(AddressDto.builder().id(2L)
            .entranceNumber("9a")
            .houseCorpus("2")
            .houseNumber("7")
            .streetUk("Шевченка")
            .streetEn("Shevchenka")
            .coordinates(CoordinatesDto.builder().latitude(3.3).longitude(6.6).build())
            .districtUk("Залізничний")
            .districtEn("Zaliznuchnuy")
            .regionEn("Region")
            .regionUk("Регіон")
            .cityUk("Львів")
            .cityEn("Lviv")
            .addressRegionDistrictList(new ArrayList<>())

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
            .build();
    }

    public static NotificationDto getNotificationDto() {
        return NotificationDto.builder()
            .title("Test")
            .body("Test")
            .build();
    }

    public static UbsCustomersDtoUpdate getUbsCustomersDtoUpdate() {
        return UbsCustomersDtoUpdate.builder()
            .customerId(2L)
            .customerName("Anatolii")
            .customerSurname("Petyrov")
            .customerPhoneNumber("+380951234561")
            .customerEmail("anatolii.andr@gmail.com")
            .build();
    }

    public static OrderDetailStatusDto getPaidOrderDetailStatusDto() {
        return getPaidOrderDetailStatusDto(PaymentStatus.PAID);
    }

    public static OrderDetailStatusDto getPaidOrderDetailStatusDto(PaymentStatus paymentStatus) {
        return OrderDetailStatusDto.builder()
            .paymentStatus(paymentStatus.name())
            .orderStatus(OrderStatus.CONFIRMED.name())
            .date(LocalDate.now())
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

    public static ReceivingStationDto getReceivingStationDto() {
        return ReceivingStationDto.builder()
            .id(1L)
            .name("Петрівка")
            .build();
    }

    public static ViolationDetailInfoDto getViolationDetailInfoDto() {
        LocalDateTime localdatetime = LocalDateTime.of(
            2021, Month.MARCH,
            16, 13, 0, 0);

        return ViolationDetailInfoDto.builder()
            .orderId(1L)
            .addedByUser("Alan Po")
            .violationLevel(MAJOR)
            .description("violation1")
            .violationDate(localdatetime)
            .build();
    }

    public static ManualPaymentRequestDto getManualPaymentRequestDto() {
        return ManualPaymentRequestDto.builder()
            .amount(500L)
            .settlementDate("2021-03-07")
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
            .commission(10.)
            .price(100.)
            .nameUk("Test")
            .nameEn("a")
            .descriptionUk("Description")
            .descriptionEn("DescriptionEng")
            .build();
    }

    public static ServiceDto getServiceDto() {
        return ServiceDto.builder()
            .nameUk("Name")
            .nameEn("NameEng")
            .price(100.)
            .descriptionUk("Description")
            .descriptionEn("DescriptionEng")
            .build();
    }

    public static GetTariffServiceDto getGetTariffServiceDto() {
        return GetTariffServiceDto.builder()
            .id(1)
            .nameUk("Бавовняна сумка")
            .capacity(120)
            .price(120.)
            .commission(50.)
            .descriptionUk("Description")
            .limitIncluded(true)
            .build();
    }

    public static EcoNumberDto getEcoNumberDto() {
        return EcoNumberDto.builder()
            .ecoNumber(Set.of("1111111111"))
            .build();
    }

    public static GetServiceDto getGetServiceDto() {
        return GetServiceDto.builder()
            .id(1L)
            .nameUk("Name")
            .nameEn("NameEng")
            .price(100.)
            .descriptionUk("Description")
            .descriptionEn("DescriptionEng")
            .build();
    }

    public static AddingPointsToUserDto getAddingPointsToUserDto() {
        return AddingPointsToUserDto.builder()
            .email("ddd@email.com")
            .additionalPoints(2)
            .build();
    }

    public static OrderWayForPayClientDto getOrderWayForPayClientDto() {
        return OrderWayForPayClientDto.builder()
            .orderId(1L)
            .pointsToUse(100)
            .certificates(Collections.emptySet())
            .build();
    }

    public static List<RegionTranslationDto> getRegionTranslationsDto() {
        return List.of(RegionTranslationDto.builder()
            .languageCode("uk")
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
            .languageCode("uk")
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
            .notificationTemplateUpdateInfo(getNotificationTemplateUpdateInfoDto())
            .platforms(List.of(
                getNotificationPlatformDto(NotificationReceiverType.SITE)))
            .build();
    }

    public static NotificationTemplateUpdateInfoDto getNotificationTemplateUpdateInfoDto() {
        return NotificationTemplateUpdateInfoDto.builder()
            .type(NotificationType.UNPAID_ORDER)
            .trigger(NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS)
            .time(NotificationTime.AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID)
            .schedule("0 0 18 * * ?")
            .titleUk("Неопачене замовлення")
            .titleEn("Unpaid order")
            .build();
    }

    public static NotificationTemplateMainInfoDto getNotificationTemplateMainInfoDto() {
        return NotificationTemplateMainInfoDto.builder()
            .type(NotificationType.UNPAID_ORDER)
            .trigger(NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS)
            .triggerDescriptionUk(NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS
                .getDescriptionUk())
            .triggerDescriptionEn(NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS
                .getDescriptionEn())
            .time(NotificationTime.AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID)
            .timeDescriptionUk(NotificationTime.AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID
                .getDescriptionUk())
            .timeDescriptionEn(NotificationTime.AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID
                .getDescriptionEn())
            .schedule("0 0 18 * * ?")
            .titleUk("Неопачене замовлення")
            .titleEn("Unpaid order")
            .notificationStatus(NotificationStatus.ACTIVE)
            .build();
    }

    public static NotificationPlatformDto getNotificationPlatformDto(
        NotificationReceiverType receiverType) {
        return NotificationPlatformDto.builder()
            .id(1L)
            .receiverType(receiverType)
            .nameEn("Site")
            .bodyUk("Body")
            .bodyEn("BodyEng")
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

    public static CreateAddressRequestDto getAddressRequestDto() {
        return CreateAddressRequestDto.builder()
            .districtEn("Shevchenkivskyi")
            .districtUk("Шевченківський")
            .regionEn("Kyiv Oblast")
            .regionUk("Київська область")
            .houseNumber("25B")
            .entranceNumber("3")
            .houseCorpus("2A")
            .addressComment("Next to the park")
            .placeId("ChIJp0lN2HIRkEARuJ1pl_yMcc0")
            .coordinates(new CoordinatesDto(50.4501, 30.5234))
            .cityUk("Київ")
            .cityEn("Kyiv")
            .streetUk("Хрещатик")
            .streetEn("Khreshchatyk")
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

    public static UpdateOrderPageAdminDto getUpdateOrderPageAdminDto() {
        return UpdateOrderPageAdminDto.builder()
            .generalOrderInfo(OrderDetailStatusRequestDto
                .builder()
                .orderStatus("NOT_TAKEN_OUT")
                .build())
            .notTakenOutReason("not taken out")
            .build();
    }

    public static AddNotificationTemplateWithPlatformsDto getAddNotificationTemplateWithPlatforms() {
        return AddNotificationTemplateWithPlatformsDto.builder()
            .schedule("0 0 18 * * ?")
            .titleUk("Title")
            .titleEn("TitleEng")
            .userCategory(UserCategory.ALL_USERS)
            .platforms(List.of(
                getAddNotificationPlatform(SITE),
                getAddNotificationPlatform(EMAIL),
                getAddNotificationPlatform(MOBILE)))
            .build();
    }

    public static AddNotificationPlatformDto getAddNotificationPlatform(
        NotificationReceiverType receiverType) {
        return AddNotificationPlatformDto.builder()
            .bodyUk("Body")
            .bodyEn("BodyEng")
            .notificationReceiverType(receiverType)
            .build();
    }

    public static PaymentResponseDto getPaymentResponseDto() {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setMerchantAccount("merchant123");
        dto.setOrderReference("order456");
        dto.setMerchantSignature("signature123");
        dto.setAmount("100.00");
        dto.setCurrency("USD");
        dto.setAuthCode("auth123");
        dto.setEmail("test@mail.com");
        dto.setPhone("+1234567890");
        dto.setCreatedDate("2023-07-19T12:00:00");
        dto.setProcessingDate("2023-07-19T12:05:00");
        dto.setCardPan("4111111111111111");
        dto.setCardType("VISA");
        dto.setIssuerBankCountry("USA");
        dto.setIssuerBankName("Bank of America");
        dto.setRecToken("token123");
        dto.setTransactionStatus("approved");
        dto.setReason("None");
        dto.setReasonCode("00");
        dto.setFee("1.00");
        dto.setPaymentSystem("card");
        dto.setAcquirerBankName("Chase");
        return dto;
    }

    public static final String TEST_AGREEMENT_TEXT_UK = "Текст угоди українською";
    public static final String TEST_AGREEMENT_TEXT_EN = "Agreement text in English";

    public static UserAgreementDto getUserAgreementDto() {
        return UserAgreementDto.builder()
            .textUk(TEST_AGREEMENT_TEXT_UK)
            .textEn(TEST_AGREEMENT_TEXT_EN)
            .build();
    }

    public static UserAgreementDetailDto getUserAgreementDetailDto() {
        return UserAgreementDetailDto.builder()
            .id(1L)
            .textUk(TEST_AGREEMENT_TEXT_UK)
            .textEn(TEST_AGREEMENT_TEXT_EN)
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    public static UpdateAddressDto getUpdateAddressDto() {
        OrderAddressExportDetailsDtoUpdate orderAddressDetails = OrderAddressExportDetailsDtoUpdate.builder()
            .id(1L)
            .districtUk("Деснянський район")
            .districtEn("Desnyans'kyi District")
            .streetUk("вулиця Шевченка")
            .streetEn("Shevchenka Street")
            .houseCorpus("2")
            .entranceNumber("1")
            .houseNumber("34")
            .cityUk("Київ")
            .cityEn("Kyiv")
            .regionUk("місто Київ")
            .regionEn("Kyiv city")
            .addressComment("Test comment for address №1")
            .coordinates(CoordinatesDto.builder()
                .latitude(50.4501)
                .longitude(30.5234)
                .build())
            .build();

        return UpdateAddressDto.builder()
            .orderAddressExportDetails(orderAddressDetails)
            .orderId(1L)
            .build();
    }

    /**
     * Returns a preconfigured CertificateDtoForAdding instance.
     *
     * <p>
     *
     * This method builds a CertificateDtoForAdding object using default values: 10
     * points, a month count of 1, an initial points value of 2000, and the
     * certificate code "4444-4444".
     * </p>
     *
     * @return a CertificateDtoForAdding instance with preset certificate values
     */
    public static CertificateDtoForAdding getCertificateDtoForAdding() {
        return CertificateDtoForAdding
            .builder()
            .points(10)
            .monthCount(1)
            .initialPointsValue(2000)
            .code("4444-4444")
            .build();
    }

    /**
     * Constructs an AddingViolationsToUserDto with default violation details.
     *
     * <p>
     *
     * This method creates an AddingViolationsToUserDto using its builder pattern,
     * setting the order ID to 1, the violation description to "Violation
     * description", and the violation level to "LOW".
     * </p>
     *
     * @return an AddingViolationsToUserDto instance populated with preset values
     */
    public static AddingViolationsToUserDto getAddingViolationsToUserDto() {
        return AddingViolationsToUserDto.builder()
            .orderID(1L)
            .violationDescription("Violation description")
            .violationLevel("LOW")
            .build();
    }

    public static UpdateBotMessageRequestDto getUpdateBotMessageRequestDto() {
        return UpdateBotMessageRequestDto
            .builder()
            .id(1L)
            .text("New bot message text")
            .build();
    }
}
