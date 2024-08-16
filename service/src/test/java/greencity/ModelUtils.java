package greencity;

import com.google.common.collect.Lists;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import greencity.constant.AppConstant;
import greencity.dto.AddNewTariffDto;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.LocationsDtos;
import greencity.dto.OptionForColumnDTO;
import greencity.dto.RegionDto;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.address.AddressDto;
import greencity.dto.bag.AdditionalBagInfoDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.bag.BagInfoDto;
import greencity.dto.bag.BagLimitDto;
import greencity.dto.bag.BagMappingDto;
import greencity.dto.bag.BagTranslationDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.CourierUpdateDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.GetReceivingStationDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.AddEmployeeDto;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.employee.EmployeeNameDto;
import greencity.dto.employee.EmployeeNameIdDto;
import greencity.dto.employee.EmployeePositionDtoRequest;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.employee.UpdateResponsibleEmployeeDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.location.AddLocationTranslationDto;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.location.LocationTranslationDto;
import greencity.dto.location.LocationsDto;
import greencity.dto.location.RegionTranslationDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.LocationDto;
import greencity.dto.notification.AddNotificationPlatformDto;
import greencity.dto.notification.AddNotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateMainInfoDto;
import greencity.dto.notification.NotificationTemplateUpdateInfoDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.dto.order.AdminCommentDto;
import greencity.dto.order.BigOrderTableDTO;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.dto.order.DetailsOrderInfoDto;
import greencity.dto.order.EcoNumberDto;
import greencity.dto.order.ExportDetailsDto;
import greencity.dto.order.ExportDetailsDtoUpdate;
import greencity.dto.order.GroupedOrderDto;
import greencity.dto.order.NotTakenOrderReasonDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderAddressDtoResponse;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderDetailInfoDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderDetailStatusRequestDto;
import greencity.dto.order.OrderDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.ReadAddressByOrderDto;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.dto.order.SenderLocation;
import greencity.dto.order.UpdateAllOrderPageDto;
import greencity.dto.order.UpdateOrderDetailDto;
import greencity.dto.order.UpdateOrderPageAdminDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.PaymentInfoDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentTableInfoDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.position.PositionDto;
import greencity.dto.position.PositionWithTranslateDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.service.GetTariffServiceDto;
import greencity.dto.service.ServiceDto;
import greencity.dto.service.TariffServiceDto;
import greencity.dto.table.ColumnWidthDto;
import greencity.dto.tariff.EditTariffDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.dto.tariff.GetTariffLimitsDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.tariff.SetTariffLimitsDto;
import greencity.dto.user.AddBonusesToUserDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.dto.useragreement.UserAgreementDto;
import greencity.dto.violation.AddingViolationsToUserDto;
import greencity.dto.violation.UpdateViolationToUserDto;
import greencity.dto.violation.ViolationDetailInfoDto;
import greencity.entity.TariffsInfoRecievingEmployee;
import greencity.entity.coords.Coordinates;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Bag;
import greencity.entity.order.BigOrderTableViews;
import greencity.entity.order.Certificate;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Courier;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.Refund;
import greencity.entity.order.Service;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.parameters.CustomTableView;
import greencity.entity.table.TableColumnWidthForEmployee;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeFilterView;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.entity.viber.ViberBot;
import greencity.enums.AddressStatus;
import greencity.enums.BagStatus;
import greencity.enums.CancellationReason;
import greencity.enums.CertificateStatus;
import greencity.enums.CourierLimit;
import greencity.enums.CourierStatus;
import greencity.enums.EmployeeStatus;
import greencity.enums.LocationStatus;
import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationTime;
import greencity.enums.NotificationTrigger;
import greencity.enums.NotificationType;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.TariffStatus;
import greencity.enums.UserCategory;
import greencity.util.Bot;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import static greencity.enums.NotificationReceiverType.EMAIL;
import static greencity.enums.NotificationReceiverType.MOBILE;
import static greencity.enums.NotificationReceiverType.SITE;
import static greencity.enums.NotificationStatus.ACTIVE;
import static greencity.enums.NotificationTime.AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID;
import static greencity.enums.NotificationTrigger.ORDER_NOT_PAID_FOR_3_DAYS;
import static greencity.enums.NotificationType.UNPAID_ORDER;
import static greencity.enums.ViolationLevel.MAJOR;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class ModelUtils {
    private static final Clock fixedClock =
        Clock.fixed(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)), ZoneId.systemDefault());

    public static final String TEST_EMAIL = "test@gmail.com";
    public static final String TEST_UUID = "1ab2c3-d4e5f6";
    public static final Order TEST_ORDER = createOrder();
    public static final OrderAddressDtoResponse TEST_ORDER_ADDRESS_DTO_RESPONSE = createOrderAddressDtoResponse();
    public static final OrderAddressExportDetailsDtoUpdate TEST_ORDER_ADDRESS_DTO_UPDATE =
        createOrderAddressDtoUpdate();
    public static final List<Payment> TEST_PAYMENT_LIST = createPaymentList();
    public static final OrderDetailStatusDto ORDER_DETAIL_STATUS_DTO = createOrderDetailStatusDto();
    public static final List<BagMappingDto> TEST_BAG_MAPPING_DTO_LIST = createBagMappingDtoList();
    public static final Bag TEST_BAG = createBag(1);
    public static final OrderBag TEST_ORDER_BAG = createOrderBag();
    public static final BagForUserDto TEST_BAG_FOR_USER_DTO = createBagForUserDto();
    public static final BagInfoDto TEST_BAG_INFO_DTO = createBagInfoDto();
    public static final List<Bag> TEST_BAG_LIST = singletonList(TEST_BAG);
    public static final List<OrderDetailInfoDto> TEST_ORDER_DETAILS_INFO_DTO_LIST =
        singletonList(createOrderDetailInfoDto());
    public static final OrderAddressDtoRequest TEST_ORDER_ADDRESS_DTO_REQUEST = createOrderDtoRequest();
    public static final Order TEST_ORDER_2 = createTestOrder2();
    public static final Order TEST_ORDER_3 = createTestOrder3();
    public static final Order TEST_ORDER_4 = createTestOrder4();
    public static final Order TEST_ORDER_5 = createTestOrder5();
    public static final Set<NotificationParameter> TEST_NOTIFICATION_PARAMETER_SET = createNotificationParameterSet();
    public static final Set<NotificationParameter> TEST_NOTIFICATION_PARAMETER_SET2 = createNotificationParameterSet2();
    public static final UserNotification TEST_USER_NOTIFICATION = createUserNotification();
    public static final UserNotification TEST_USER_NOTIFICATION_2 = createUserNotification2();
    public static final UserNotification TEST_USER_NOTIFICATION_3 = createUserNotificationForViolation();
    public static final UserNotification TEST_USER_NOTIFICATION_4 = createUserNotification4();
    public static final UserNotification TEST_USER_NOTIFICATION_5 = createUserNotification5();
    public static final UserNotification TEST_USER_NOTIFICATION_6 = createUserNotificationForViolation6();
    public static final UserNotification TEST_USER_NOTIFICATION_7 = createUserNotificationForViolation7();
    public static final Violation TEST_VIOLATION = createTestViolation();
    public static final NotificationTemplate TEST_NOTIFICATION_TEMPLATE = createNotificationTemplate();
    public static final NotificationTemplateDto TEST_NOTIFICATION_TEMPLATE_DTO = createNotificationTemplateDto();

    public static final NotificationTemplateWithPlatformsUpdateDto TEST_NOTIFICATION_TEMPLATE_UPDATE_DTO =
        createNotificationTemplateWithPlatformsUpdateDto();

    public static final NotificationTemplateWithPlatformsDto TEST_NOTIFICATION_TEMPLATE_WITH_PLATFORMS_DTO =
        createNotificationTemplateWithPlatformsDto();
    public static final Pageable TEST_PAGEABLE = PageRequest.of(0, 5, Sort.by("notificationTime").descending());
    public static final Pageable TEST_NOTIFICATION_PAGEABLE = PageRequest.of(0, 5, Sort.by("id").descending());
    public static final NotificationShortDto TEST_NOTIFICATION_SHORT_DTO = createNotificationShortDto();
    public static final List<NotificationShortDto> TEST_NOTIFICATION_SHORT_DTO_LIST =
        List.of(TEST_NOTIFICATION_SHORT_DTO);
    public static final PageableDto<NotificationShortDto> TEST_DTO = createPageableDto();
    public static final Employee TEST_EMPLOYEE = createEmployee();
    public static final User TEST_USER = createUser();
    public static final List<UserNotification> TEST_USER_NOTIFICATION_LIST = createUserNotificationList();
    public static final Page<UserNotification> TEST_PAGE =
        new PageImpl<>(TEST_USER_NOTIFICATION_LIST, TEST_PAGEABLE, TEST_USER_NOTIFICATION_LIST.size());

    public static final Page<NotificationTemplate> TEMPLATE_PAGE =
        new PageImpl<>(List.of(TEST_NOTIFICATION_TEMPLATE), TEST_PAGEABLE, 1L);
    public static final AdditionalBagInfoDto TEST_ADDITIONAL_BAG_INFO_DTO = createAdditionalBagInfoDto();
    public static final List<AdditionalBagInfoDto> TEST_ADDITIONAL_BAG_INFO_DTO_LIST = createAdditionalBagInfoDtoList();
    public static final Map<String, Object> TEST_MAP_ADDITIONAL_BAG = createMap();
    public static final List<Map<String, Object>> TEST_MAP_ADDITIONAL_BAG_LIST =
        Collections.singletonList(TEST_MAP_ADDITIONAL_BAG);
    public static final NotificationDto TEST_NOTIFICATION_DTO = createNotificationDto();
    public static final UpdateOrderPageAdminDto UPDATE_ORDER_PAGE_ADMIN_DTO = updateOrderPageAdminDto();
    public static final CourierUpdateDto UPDATE_COURIER_DTO = getUpdateCourierDto();
    public static final List<Bag> TEST_BAG_LIST2 = Arrays.asList(createBag(1), createBag(2), createBag(3));

    public static final String KYIV_REGION_EN = "Kyiv Oblast";
    public static final String KYIV_REGION_UA = "Київська область";

    public static EmployeeFilterView getEmployeeFilterView() {
        return getEmployeeFilterViewWithPassedIds(1L, 5L, 10L);
    }

    public static EmployeeFilterView getEmployeeFilterViewWithPassedIds(
        Long employeeId, Long positionId, Long tariffsInfoId) {
        return EmployeeFilterView.builder()
            .employeeId(employeeId)
            .positionId(positionId)
            .positionName("Водій")
            .positionNameEn("Driver")
            .tariffsInfoId(tariffsInfoId)
            .firstName("First Name")
            .lastName("Last Name")
            .phoneNumber("Phone Number")
            .email("employee@gmail.com")
            .employeeStatus("ACTIVE")
            .image("Image")
            .regionId(15L)
            .regionNameEn("Kyiv region")
            .regionNameUk("Київська область")
            .courierId(20L)
            .receivingStationId(25L)
            .receivingStationName("Receiving station")
            .courierNameEn("Test")
            .courierNameUk("Тест")
            .locationId(30L)
            .locationNameEn("Kyiv")
            .locationNameUk("Київ")
            .build();
    }

    public static List<EmployeeFilterView> getEmployeeFilterViewListForOneEmployeeWithDifferentPositions(
        Long employeeId, Long tariffsInfoId) {
        return List.of(
            getEmployeeFilterViewWithPassedIds(employeeId, 3L, tariffsInfoId),
            getEmployeeFilterViewWithPassedIds(employeeId, 5L, tariffsInfoId),
            getEmployeeFilterViewWithPassedIds(employeeId, 7L, tariffsInfoId));
    }

    public static List<Employee> getEmployeeListForGetAllMethod() {
        return List.of(
            Employee.builder()
                .id(1L)
                .firstName("First Name")
                .lastName("Last Name")
                .phoneNumber("Phone Number")
                .email("employee@gmail.com")
                .employeeStatus(EmployeeStatus.ACTIVE)
                .employeePosition(new HashSet<>())
                .tariffs(List.of())
                .imagePath("path")
                .tariffs(List.of(getTariffInfo()))
                .tariffsInfoReceivingEmployees(new ArrayList<>())
                .build());
    }

    public static GetEmployeeDto getEmployeeDtoWithoutPositionsAndTariffsForGetAllMethod() {
        var getEmployeeDto = getEmployeeDto();
        getEmployeeDto.setEmployeePositions(new ArrayList<>());
        getEmployeeDto.setTariffs(new ArrayList<>());
        return getEmployeeDto;
    }

    public static GetEmployeeDto getEmployeeDto() {
        return GetEmployeeDto.builder()
            .id(1L)
            .firstName("First Name")
            .lastName("Last Name")
            .phoneNumber("Phone Number")
            .email("employee@gmail.com")
            .employeeStatus("ACTIVE")
            .image("Image")
            .build();
    }

    public static GetReceivingStationDto getReceivingStationDto2() {
        return GetReceivingStationDto.builder()
            .stationId(25L)
            .name("Receiving station")
            .build();
    }

    public static GetTariffInfoForEmployeeDto getTariffInfoForEmployeeDto2() {
        return GetTariffInfoForEmployeeDto.builder()
            .id(10L)
            .region(getRegionDto())
            .courier(getCourierTranslationDto(20L))
            .build();
    }

    public static CourierUpdateDto getUpdateCourierDto() {
        return CourierUpdateDto.builder()
            .courierId(1L)
            .nameUk("Тест")
            .nameEn("Test")
            .build();
    }

    public static DetailsOrderInfoDto getTestDetailsOrderInfoDto() {
        return DetailsOrderInfoDto.builder()
            .capacity("One")
            .sum("Two")
            .build();
    }

    public static Optional<Order> getOrderWithEvents() {
        return Optional.of(Order.builder()
            .id(1L)
            .events(List.of(Event.builder()
                .id(1L)
                .authorName("Igor")
                .eventDate(LocalDateTime.now())
                .authorName("Igor")
                .build(),
                Event.builder()
                    .id(1L)
                    .authorName("Igor")
                    .eventDate(LocalDateTime.now())
                    .authorName("Igor")
                    .build(),
                Event.builder()
                    .id(1L)
                    .authorName("Igor")
                    .eventDate(LocalDateTime.now())
                    .authorName("Igor")
                    .build()))
            .user(getTestUser())
            .build());
    }

    public static List<Event> getListOfEvents() {
        return List.of(Event.builder()
            .id(1L)
            .authorName("Igor")
            .eventDate(LocalDateTime.now())
            .authorName("Igor")
            .order(new Order())
            .build(),
            Event.builder()
                .id(1L)
                .authorName("Igor")
                .eventDate(LocalDateTime.now())
                .authorName("Igor")
                .order(new Order())
                .build(),
            Event.builder()
                .id(1L)
                .authorName("Igor")
                .eventDate(LocalDateTime.now())
                .authorName("Igor")
                .order(new Order())
                .build());
    }

    public static OrderResponseDto getOrderResponseDto() {
        return getOrderResponseDto(true);
    }

    public static OrderResponseDto getOrderResponseDto(boolean shouldBePaid) {
        return OrderResponseDto.builder()
            .addressId(1L)
            .additionalOrders(new HashSet<>(List.of("232-534-634")))
            .bags(Collections.singletonList(new BagDto(3, 999)))
            .locationId(1L)
            .orderComment("comment")
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .shouldBePaid(shouldBePaid)
            .personalData(PersonalDataDto.builder()
                .senderEmail("test@email.ua")
                .senderPhoneNumber("+380974563223")
                .senderLastName("TestLast")
                .senderFirstName("TestFirst")
                .firstName("oleh")
                .lastName("ivanov")
                .id(13L)
                .email("mail@mail.ua")
                .phoneNumber("067894522")
                .ubsUserId(1L)
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
            .senderEmail("test@email.ua")
            .senderPhoneNumber("+380974563223")
            .senderLastName("TestLast")
            .senderFirstName("TestFirst")
            .orderAddress(OrderAddress.builder()
                .id(1L)
                .houseNumber("1a")
                .actual(true)
                .entranceNumber("str")
                .district("3a")
                .houseCorpus("2a")
                .city("Kiev")
                .street("Gorodotska")
                .coordinates(Coordinates.builder()
                    .longitude(2.2)
                    .latitude(3.2)
                    .build())
                .addressComment(null).build())
            .orders(List.of(Order.builder().id(1L).build()))
            .build();
    }

    public static UBSuser getUBSuserWithoutOrderAddress() {
        return UBSuser.builder()
            .firstName("oleh")
            .lastName("ivanov")
            .email("mail@mail.ua")
            .id(1L)
            .phoneNumber("067894522")
            .senderEmail("test@email.ua")
            .senderPhoneNumber("+380974563223")
            .senderLastName("TestLast")
            .senderFirstName("TestFirst")
            .orders(List.of(Order.builder().id(1L).build()))
            .build();
    }

    public static UBSuser getUBSuserWithoutSender() {
        return UBSuser.builder()
            .firstName("oleh")
            .lastName("ivanov")
            .email("mail@mail.ua")
            .id(1L)
            .phoneNumber("067894522")
            .orderAddress(OrderAddress.builder()
                .id(1L)
                .houseNumber("1a")
                .actual(true)
                .entranceNumber("str")
                .district("3a")
                .houseCorpus("2a")
                .city("Kiev")
                .street("Gorodotska")
                .coordinates(Coordinates.builder()
                    .longitude(2.2)
                    .latitude(3.2)
                    .build())
                .addressComment(null).build())
            .orders(List.of(Order.builder().id(1L).build()))
            .build();
    }

    public static User getTestUser() {
        return User.builder()
            .id(1L)
            .orders(Lists.newArrayList(getOrder()))
            .changeOfPointsList(Lists.newArrayList(getChangeOfPoints()))
            .currentPoints(getChangeOfPoints().getAmount())
            .orders(Lists.newArrayList(getOrder()))
            .recipientName("Alan")
            .recipientSurname("Po")
            .uuid("abc")
            .build();
    }

    public static ChangeOfPoints getChangeOfPoints() {
        return ChangeOfPoints.builder()
            .id(1L)
            .amount(0)
            .order(getOrder())
            .date(LocalDateTime.now())
            .build();
    }

    public static Order getOrder() {
        return Order.builder()
            .id(1L)
            .orderDate(LocalDateTime.of(2023, 10, 20, 14, 58))
            .payment(Lists.newArrayList(Payment.builder()
                .id(1L)
                .paymentId("1")
                .amount(20000L)
                .currency("UAH")
                .comment("avb")
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .ubsUser(UBSuser.builder()
                .firstName("oleh")
                .lastName("ivanov")
                .email("mail@mail.ua")
                .id(1L)
                .phoneNumber("067894522")
                .orderAddress(OrderAddress.builder()
                    .id(1L)
                    .city("Lviv")
                    .street("Levaya")
                    .district("frankivskiy")
                    .entranceNumber("5")
                    .addressComment("near mall")
                    .houseCorpus("1")
                    .houseNumber("4")
                    .coordinates(Coordinates.builder()
                        .latitude(49.83)
                        .longitude(23.88)
                        .build())
                    .build())
                .build())
            .user(User.builder()
                .id(1L)
                .recipientName("Yuriy")
                .recipientSurname("Gerasum")
                .uuid("UUID")
                .build())
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .adminComment("Admin")
            .cancellationComment("cancelled")
            .receivingStation(ReceivingStation.builder()
                .id(1L)
                .name("Саперно-Слобідська")
                .build())
            .cancellationReason(CancellationReason.OUT_OF_CITY)
            .imageReasonNotTakingBags(List.of("foto"))
            .orderPaymentStatus(OrderPaymentStatus.UNPAID)
            .additionalOrders(new HashSet<>(Arrays.asList("1111111111", "2222222222")))
            .events(new ArrayList<>())
            .build();
    }

    public static Order getOrderWithTariffAndLocation() {
        return Order.builder()
            .id(1L)
            .payment(Lists.newArrayList(Payment.builder()
                .id(1L)
                .paymentId("1")
                .amount(20000L)
                .currency("UAH")
                .comment("avb")
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .ubsUser(UBSuser.builder()
                .firstName("oleh")
                .lastName("ivanov")
                .email("mail@mail.ua")
                .id(1L)
                .phoneNumber("067894522")
                .orderAddress(OrderAddress.builder()
                    .id(1L)
                    .city("Lviv")
                    .street("Levaya")
                    .district("frankivskiy")
                    .entranceNumber("5")
                    .addressComment("near mall")
                    .houseCorpus("1")
                    .houseNumber("4")
                    .location(getLocation())
                    .coordinates(Coordinates.builder()
                        .latitude(49.83)
                        .longitude(23.88)
                        .build())
                    .build())
                .build())
            .tariffsInfo(getTariffInfo())
            .user(User.builder()
                .id(1L)
                .recipientName("Yuriy")
                .recipientSurname("Gerasum")
                .uuid("UUID")
                .build())
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .adminComment("Admin")
            .cancellationComment("cancelled")
            .receivingStation(ReceivingStation.builder()
                .id(1L)
                .name("Саперно-Слобідська")
                .build())
            .cancellationReason(CancellationReason.OUT_OF_CITY)
            .imageReasonNotTakingBags(List.of("foto"))
            .orderPaymentStatus(OrderPaymentStatus.UNPAID)
            .additionalOrders(new HashSet<>(Arrays.asList("1111111111", "2222222222")))
            .build();
    }

    public static Order getOrderExportDetails() {
        return Order.builder()
            .id(1L)
            .deliverFrom(LocalDateTime.of(1997, 12, 4, 15, 40, 24))
            .dateOfExport(LocalDate.of(1997, 12, 4))
            .deliverTo(LocalDateTime.of(1990, 12, 11, 19, 30, 30))
            .receivingStation(ReceivingStation.builder()
                .id(1L)
                .name("Саперно-Слобідська")
                .build())
            .user(User.builder().id(1L).recipientName("Yuriy").recipientSurname("Gerasum").build())
            .build();
    }

    public static Order getOrderExportDetailsWithNullValues() {
        return Order.builder()
            .id(1L)
            .user(User.builder().id(1L).recipientName("Yuriy").recipientSurname("Gerasum").build())
            .build();
    }

    public static Order getOrderWithoutPayment() {
        return Order.builder()
            .id(1L)
            .payment(Collections.emptyList())
            .certificates(Collections.emptySet())
            .pointsToUse(500)
            .build();
    }

    public static OrderDto getOrderDto() {
        return OrderDto.builder()
            .firstName("oleh")
            .lastName("ivanov")
            .address("frankivskiy Levaya 4")
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

    public static ExportDetailsDto getExportDetails() {
        return ExportDetailsDto.builder()
            .dateExport("1997-12-04T15:40:24")
            .timeDeliveryFrom("1997-12-04T15:40:24")
            .timeDeliveryTo("1990-12-11T19:30:30")
            .receivingStationId(1L)
            .allReceivingStations(List.of(getReceivingStationDto(), getReceivingStationDto()))
            .build();
    }

    public static ExportDetailsDtoUpdate getExportDetailsRequestToday() {
        return ExportDetailsDtoUpdate.builder()
            .dateExport(String.valueOf(LocalDate.now()))
            .timeDeliveryFrom(String.valueOf(LocalDateTime.now()))
            .timeDeliveryTo(String.valueOf(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)))
            .receivingStationId(1L)
            .build();
    }

    public static ExportDetailsDtoUpdate getExportDetailsRequest() {
        return ExportDetailsDtoUpdate.builder()
            .dateExport("1997-12-04T15:40:24")
            .timeDeliveryFrom("1997-12-04T15:40:24")
            .timeDeliveryTo("1990-12-11T19:30:30")
            .receivingStationId(1L)
            .build();
    }

    public static Set<Coordinates> getCoordinatesSet() {
        Set<Coordinates> set = new HashSet<>();
        set.add(Coordinates.builder()
            .latitude(49.894)
            .longitude(24.107)
            .build());
        set.add(Coordinates.builder()
            .latitude(49.771)
            .longitude(23.909)
            .build());
        set.add(Coordinates.builder()
            .latitude(49.801)
            .longitude(24.164)
            .build());
        set.add(Coordinates.builder()
            .latitude(49.854)
            .longitude(24.069)
            .build());
        set.add(Coordinates.builder()
            .latitude(49.796)
            .longitude(24.931)
            .build());
        set.add(Coordinates.builder()
            .latitude(49.812)
            .longitude(24.035)
            .build());
        set.add(Coordinates.builder()
            .latitude(49.871)
            .longitude(24.029)
            .build());
        set.add(Coordinates.builder()
            .latitude(49.666)
            .longitude(24.013)
            .build());
        set.add(Coordinates.builder()
            .latitude(49.795)
            .longitude(24.052)
            .build());
        set.add(Coordinates.builder()
            .latitude(49.856)
            .longitude(24.049)
            .build());
        set.add(Coordinates.builder()
            .latitude(49.862)
            .longitude(24.039)
            .build());
        return set;
    }

    public static List<Order> getOrdersToGroupThem() {
        List<Order> orders = new ArrayList<>();
        long id = 0L;
        long userId = 10L;
        for (Coordinates coordinates : getCoordinatesSet()) {
            orders.add(Order.builder()
                .id(++id)
                .ubsUser(UBSuser.builder()
                    .id(++userId)
                    .orderAddress(OrderAddress.builder()
                        .coordinates(coordinates)
                        .build())
                    .build())
                .build());
        }
        return orders;
    }

    public static List<GroupedOrderDto> getGroupedOrders() {
        List<GroupedOrderDto> list = new ArrayList<>();
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(75)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.854)
                .longitude(24.069)
                .build(),
                OrderDto.builder()
                    .latitude(49.856)
                    .longitude(24.049)
                    .build(),
                OrderDto.builder()
                    .latitude(49.862)
                    .longitude(24.039)
                    .build()))
            .build());

        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.812)
                .longitude(24.035)
                .build()))
            .build());

        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.795)
                .longitude(24.052)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.796)
                .longitude(24.931)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.871)
                .longitude(24.029)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.894)
                .longitude(24.107)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.666)
                .longitude(24.013)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.771)
                .longitude(23.909)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.801)
                .longitude(24.164)
                .build()))
            .build());
        return list;
    }

    public static List<GroupedOrderDto> getGroupedOrdersWithLiters() {
        List<GroupedOrderDto> list = new ArrayList<>();
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(75)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.854)
                .longitude(24.069)
                .build()))
            .build());

        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.812)
                .longitude(24.035)
                .build()))
            .build());

        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.795)
                .longitude(24.052)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.796)
                .longitude(24.931)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.871)
                .longitude(24.029)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.894)
                .longitude(24.107)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.666)
                .longitude(24.013)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.856)
                .longitude(24.049)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.862)
                .longitude(24.039)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.771)
                .longitude(23.909)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.801)
                .longitude(24.164)
                .build()))
            .build());
        return list;
    }

    public static List<GroupedOrderDto> getGroupedOrdersFor60LitresLimit() {
        List<GroupedOrderDto> list = new ArrayList<>();
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(50)
            .groupOfOrders(List.of(
                OrderDto.builder()
                    .latitude(49.856)
                    .longitude(24.049)
                    .build(),
                OrderDto.builder()
                    .latitude(49.862)
                    .longitude(24.039)
                    .build()))
            .build());

        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.854)
                .longitude(24.069)
                .build()))
            .build());

        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.812)
                .longitude(24.035)
                .build()))
            .build());

        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.795)
                .longitude(24.052)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.796)
                .longitude(24.931)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.871)
                .longitude(24.029)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.894)
                .longitude(24.107)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.666)
                .longitude(24.013)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.771)
                .longitude(23.909)
                .build()))
            .build());
        list.add(GroupedOrderDto.builder()
            .amountOfLitres(25)
            .groupOfOrders(List.of(OrderDto.builder()
                .latitude(49.801)
                .longitude(24.164)
                .build()))
            .build());
        return list;
    }

    public static CertificateDtoForSearching getCertificateDtoForSearching() {
        return CertificateDtoForSearching.builder()
            .code("1111-1234")
            .certificateStatus(CertificateStatus.ACTIVE)
            .points(10)
            .expirationDate(LocalDate.now().plusMonths(1))
            .creationDate(LocalDate.now())
            .orderId(1L)
            .build();
    }

    public static CertificateDtoForAdding getCertificateDtoForAdding() {
        return CertificateDtoForAdding.builder()
            .code("1111-1234")
            .monthCount(0)
            .initialPointsValue(10)
            .points(0)
            .build();
    }

    public static Certificate getActiveCertificateWith10Points() {
        return Certificate.builder()
            .code("1111-1234")
            .certificateStatus(CertificateStatus.ACTIVE)
            .order(getOrder())
            .points(10)
            .expirationDate(LocalDate.now().plusMonths(1))
            .creationDate(LocalDate.now())
            .order(null)
            .build();
    }

    public static Certificate getActiveCertificateWith600Points() {
        return Certificate.builder()
            .code("1111-1234")
            .certificateStatus(CertificateStatus.ACTIVE)
            .order(getOrder())
            .points(600)
            .expirationDate(LocalDate.now().plusMonths(1))
            .creationDate(LocalDate.now())
            .build();
    }

    public static Certificate getUsedCertificateWith600Points() {
        return Certificate.builder()
            .code("1111-1234")
            .certificateStatus(CertificateStatus.USED)
            .order(getOrder())
            .points(600)
            .expirationDate(LocalDate.now().plusMonths(1))
            .creationDate(LocalDate.now())
            .build();
    }

    public static AddingViolationsToUserDto getAddingViolationsToUserDto() {
        return AddingViolationsToUserDto.builder()
            .orderID(1L)
            .violationDescription("String string string")
            .violationLevel("low")
            .build();
    }

    public static UpdateViolationToUserDto getUpdateViolationToUserDto() {
        List<String> listImages = new ArrayList<>();
        listImages.add("");
        return UpdateViolationToUserDto.builder()
            .orderID(1L)
            .violationDescription("String1 string1 string1")
            .violationLevel("low")
            .imagesToDelete(listImages)
            .build();
    }

    public static Order getOrderDoneByUser() {
        return Order.builder()
            .id(1L)
            .orderStatus(OrderStatus.CONFIRMED)
            .payment(singletonList(Payment.builder()
                .id(1L)
                .amount(350L)
                .build()))
            .orderDate(LocalDateTime.of(2021, 5, 15, 10, 20, 5))
            .amountOfBagsOrdered(Collections.singletonMap(1, 2))
            .build();
    }

    public static AddEmployeeDto getAddEmployeeDto() {
        return AddEmployeeDto.builder()
            .firstName("Петро")
            .lastName("Петренко")
            .phoneNumber("+380935577455")
            .email("test@gmail.com")
            .employeePositions(List.of(PositionDto.builder()
                .id(1L)
                .name("Водій")
                .nameEn("Driver")
                .build()))
            .receivingStations(List.of(ReceivingStationDto.builder()
                .id(1L)
                .name("Петрівка")
                .build()))
            .build();
    }

    public static EmployeeWithTariffsDto getEmployeeWithTariffsDto() {
        return EmployeeWithTariffsDto.builder()
            .employeeDto(EmployeeDto.builder()
                .id(1L)
                .firstName("Петро")
                .lastName("Петренко")
                .phoneNumber("+380935577455")
                .email("test@gmail.com")
                .image("path")
                .employeeStatus(EmployeeStatus.ACTIVE)
                .employeePositions(List.of(PositionDto.builder()
                    .id(1L)
                    .name("Водій")
                    .nameEn("Driver")
                    .build()))
                .build())
            .tariffs(List.of(GetTariffInfoForEmployeeDto.builder()
                .id(1L)
                .hasChat(true)
                .region(RegionDto.builder()
                    .regionId(1L)
                    .nameUk("Київська область")
                    .nameEn("Kyiv region")
                    .build())
                .locationsDtos(List.of(LocationsDtos.builder()
                    .locationId(1L)
                    .nameUk("Київ")
                    .nameEn("Kyiv")
                    .build()))
                .receivingStationDtos(List.of(GetReceivingStationDto.builder()
                    .stationId(1L)
                    .name("Петрівка")
                    .build()))
                .courier(CourierTranslationDto.builder()
                    .id(1L)
                    .nameEn("Test")
                    .nameUk("Тест")
                    .build())
                .build()))
            .build();
    }

    public static GetTariffInfoForEmployeeDto getTariffInfoForEmployeeDto() {
        return GetTariffInfoForEmployeeDto
            .builder()
            .id(1L)
            .region(RegionDto.builder()
                .regionId(1L)
                .nameEn("Kyiv region")
                .nameUk("Київська область")
                .build())
            .locationsDtos(List.of(getLocationsDtos(1L)))
            .receivingStationDtos(List.of(getGetReceivingStationDto()))
            .courier(getCourierTranslationDto(1L))
            .build();
    }

    public static GetTariffInfoForEmployeeDto getTariffInfoForEmployeeDtoWithUnknownRegion() {
        return GetTariffInfoForEmployeeDto
            .builder()
            .id(1L)
            .region(RegionDto.builder()
                .regionId(0L)
                .nameEn(AppConstant.UNKNOWN_ENG)
                .nameUk(AppConstant.UNKNOWN_UA)
                .build())
            .locationsDtos(List.of(getLocationsDtos(1L)))
            .receivingStationDtos(List.of(getGetReceivingStationDto()))
            .courier(getCourierTranslationDto(1L))
            .build();
    }

    public static LocationsDtos getLocationsDtos(Long id) {
        return LocationsDtos.builder()
            .locationId(id)
            .nameUk("Київ")
            .nameEn("Kyiv")
            .build();
    }

    public static GetReceivingStationDto getGetReceivingStationDto() {
        return GetReceivingStationDto
            .builder()
            .stationId(1L)
            .name("Петрівка")
            .build();
    }

    public static Employee getEmployee() {
        return Employee.builder()
            .id(1L)
            .firstName("Петро")
            .lastName("Петренко")
            .phoneNumber("+380935577455")
            .email("test@gmail.com")
            .uuid("Test")
            .employeeStatus(EmployeeStatus.ACTIVE)
            .employeePosition(Set.of(Position.builder()
                .id(1L)
                .name("Водій")
                .nameEn("Driver")
                .build()))
            .tariffs(List.of(TariffsInfo.builder()
                .id(1L)
                .service(new Service())
                .build()))
            .imagePath("path")
            .build();
    }

    public static Employee getEmployeeWithTariffs() {
        return Employee.builder()
            .id(1L)
            .firstName("Петро")
            .lastName("Петренко")
            .phoneNumber("+380935577455")
            .email("test@gmail.com")
            .employeeStatus(EmployeeStatus.ACTIVE)
            .employeePosition(Set.of(Position.builder()
                .id(1L)
                .name("Водій")
                .nameEn("Driver")
                .build()))
            .imagePath("path")
            .tariffs(List.of(getTariffInfo()))
            .tariffsInfoReceivingEmployees(List.of(TariffsInfoRecievingEmployee.builder()
                .tariffsInfo(getTariffInfo())
                .hasChat(true)
                .build()))
            .build();
    }

    public static List<Employee> getEmployeeList() {
        return List.of(
            Employee.builder()
                .id(1L)
                .firstName("Петро")
                .lastName("Петренко")
                .phoneNumber("+380935577455")
                .email("test@gmail.com")
                .employeeStatus(EmployeeStatus.ACTIVE)
                .employeePosition(Set.of(Position.builder()
                    .id(6L)
                    .name("Супер адмін")
                    .nameEn("Super admin")
                    .build()))
                .tariffs(List.of())
                .imagePath("path")
                .tariffs(List.of(getTariffInfo()))
                .build());
    }

    public static Employee getEmployeeForUpdateEmailCheck() {
        return Employee.builder()
            .id(1L)
            .firstName("Петро")
            .lastName("Петренко")
            .phoneNumber("+380935577455")
            .email("test1@gmail.com")
            .employeeStatus(EmployeeStatus.ACTIVE)
            .employeePosition(Set.of(Position.builder()
                .id(1L)
                .name("Водій")
                .nameEn("Driver")
                .build()))
            .imagePath("path")
            .build();
    }

    public static Employee getFullEmployee() {
        return Employee.builder()
            .id(1L)
            .firstName("Петро")
            .lastName("Петренко")
            .phoneNumber("+380935577455")
            .email("test@gmail.com")
            .imagePath("path")
            .employeeStatus(EmployeeStatus.ACTIVE)
            .employeePosition(Set.of(Position.builder()
                .id(1L)
                .name("Водій")
                .nameEn("Driver")
                .build()))
            .tariffs(List.of(TariffsInfo.builder()
                .id(1L)
                .service(getService())
                .courier(getCourier())
                .tariffLocations(Set.of(getTariffLocation()))
                .receivingStationList(Set.of(getReceivingStation()))
                .build()))
            .tariffs(List.of(TariffsInfo.builder()
                .id(1L)
                .service(getService())
                .courier(getCourier())
                .tariffLocations(Set.of(getTariffLocation()))
                .build()))
            .build();
    }

    public static TariffLocation getTariffLocation() {
        return TariffLocation
            .builder()
            .id(1L)
            .tariffsInfo(getTariffsInfo())
            .location(getLocation())
            .locationStatus(LocationStatus.ACTIVE)
            .build();
    }

    public static TariffLocation getTariffLocation2() {
        return TariffLocation
            .builder()
            .id(1L)
            .tariffsInfo(
                TariffsInfo.builder()
                    .id(2L)
                    .build())
            .location(getLocation())
            .locationStatus(LocationStatus.ACTIVE)
            .build();
    }

    public static List<TariffLocation> getTariffLocationList() {
        return List.of(getTariffLocation(),
            TariffLocation
                .builder()
                .id(2L)
                .locationStatus(LocationStatus.ACTIVE)
                .build());
    }

    public static EmployeeWithTariffsIdDto getEmployeeWithTariffsIdDto() {
        return EmployeeWithTariffsIdDto
            .builder()
            .employeeDto(EmployeeDto.builder()
                .id(1L)
                .firstName("Петро")
                .lastName("Петренко")
                .phoneNumber("+380935577455")
                .email("test@gmail.com")
                .image("path")
                .employeePositions(List.of(PositionDto.builder()
                    .id(1L)
                    .name("Водій")
                    .nameEn("Driver")
                    .build()))
                .build())
            .tariffs(null)
            .build();
    }

    private static RegionDto getRegionDto() {
        return RegionDto.builder()
            .regionId(15L)
            .nameEn("Kyiv region")
            .nameUk("Київська область")
            .build();
    }

    public static UserInfoDto getUserInfoDto() {
        return UserInfoDto.builder()
            .customerName("Alan")
            .customerSurName("Maym")
            .customerPhoneNumber("091546745")
            .customerEmail("wayn@email.com")
            .recipientName("Anatolii")
            .recipientSurName("Petyrov")
            .recipientPhoneNumber("095123456")
            .recipientEmail("anatolii.andr@gmail.com")
            .totalUserViolations(4)
            .userViolationForCurrentOrder(1)
            .build();
    }

    public static Order getOrderDetails() {
        return Order.builder()
            .id(1L)
            .user(User.builder()
                .id(1L)
                .recipientName("Alan")
                .recipientSurname("Maym")
                .recipientPhone("091546745")
                .recipientEmail("wayn@email.com")
                .violations(4).build())
            .ubsUser(UBSuser.builder()
                .id(1L)
                .firstName("Anatolii")
                .lastName("Petyrov")
                .phoneNumber("095123456")
                .email("anatolii.andr@gmail.com")
                .senderFirstName("Anatolii")
                .senderLastName("Petyrov")
                .senderPhoneNumber("095123456")
                .senderEmail("anatolii.andr@gmail.com")
                .build())
            .build();
    }

    public static Order getOrderDetailsWithoutSender() {
        return Order.builder()
            .id(1L)
            .user(User.builder()
                .id(1L)
                .recipientName("Alan")
                .recipientSurname("Maym")
                .recipientPhone("091546745")
                .recipientEmail("wayn@email.com")
                .violations(4).build())
            .ubsUser(UBSuser.builder()
                .id(1L)
                .firstName("Anatolii")
                .lastName("Petyrov")
                .phoneNumber("095123456")
                .email("anatolii.andr@gmail.com")
                .build())
            .build();
    }

    public static UbsCustomersDtoUpdate getUbsCustomersDtoUpdate() {
        return UbsCustomersDtoUpdate.builder()
            .recipientId(1L)
            .recipientName("Anatolii Petyrov")
            .recipientEmail("anatolii.andr@gmail.com")
            .recipientPhoneNumber("095123456").build();
    }

    public static List<AddressDto> addressDtoList() {
        List<AddressDto> list = new ArrayList<>();
        list.add(AddressDto.builder()
            .id(1L)
            .entranceNumber("7a")
            .houseCorpus("2")
            .houseNumber("7")
            .street("Gorodotska")
            .coordinates(Coordinates.builder().latitude(2.3).longitude(5.6).build())
            .district("Zaliznuchnuy")
            .city("Lviv")
            .actual(false)
            .placeId("place_id")
            .build());
        list.add(AddressDto.builder().id(2L)
            .entranceNumber("9a")
            .houseCorpus("2")
            .houseNumber("7")
            .street("Shevchenka")
            .coordinates(Coordinates.builder().latitude(3.3).longitude(6.6).build())
            .district("Zaliznuchnuy")
            .city("Lviv")
            .actual(false)
            .placeId("place_id")
            .build());
        return list;
    }

    public static List<AddressDto> addressDtoListWithNullPlaceId() {
        List<AddressDto> list = new ArrayList<>();
        list.add(AddressDto.builder()
            .id(1L)
            .entranceNumber("7a")
            .houseCorpus("2")
            .houseNumber("7")
            .street("Gorodotska")
            .coordinates(Coordinates.builder().latitude(2.3).longitude(5.6).build())
            .district("Zaliznuchnuy")
            .city("Lviv")
            .actual(false)
            .build());
        list.add(AddressDto.builder().id(2L)
            .entranceNumber("9a")
            .houseCorpus("2")
            .houseNumber("7")
            .street("Shevchenka")
            .coordinates(Coordinates.builder().latitude(3.3).longitude(6.6).build())
            .district("Zaliznuchnuy")
            .city("Lviv")
            .actual(false)
            .build());
        return list;
    }

    public static UserProfileDto userProfileDto() {
        return UserProfileDto.builder()
            .recipientName("Dima")
            .recipientSurname("Petrov")
            .recipientPhone("0666051373")
            .recipientEmail("petrov@gmail.com")
            .telegramIsNotify(true)
            .viberIsNotify(false)
            .build();
    }

    public static User getUserProfile() {
        return User.builder()
            .recipientName("Dima")
            .recipientSurname("Petrov")
            .recipientPhone("0666051373")
            .recipientEmail("petrov@gmail.com")
            .telegramBot(getTelegramBotNotifyTrue())
            .build();
    }

    public static TelegramBot getTelegramBotNotifyTrue() {
        return TelegramBot.builder()
            .id(1L)
            .chatId(111111L)
            .isNotify(true)
            .build();
    }

    public static TelegramBot getTelegramBotNotifyFalse() {
        return TelegramBot.builder()
            .id(1L)
            .chatId(111111L)
            .isNotify(false)
            .build();
    }

    public static ViberBot getViberBotNotifyTrue() {
        return ViberBot.builder()
            .id(1L)
            .chatId("111111L")
            .isNotify(true)
            .build();
    }

    public static ViberBot getViberBotNotifyFalse() {
        return ViberBot.builder()
            .id(1L)
            .chatId("111111L")
            .isNotify(false)
            .build();
    }

    public static UserProfileUpdateDto getUserProfileUpdateDto() {
        User user = getUserWithBotNotifyTrue();
        return UserProfileUpdateDto.builder().addressDto(addressDtoList())
            .recipientName(user.getRecipientName()).recipientSurname(user.getRecipientSurname())
            .recipientPhone(user.getRecipientPhone())
            .alternateEmail("test@email.com")
            .telegramIsNotify(true)
            .viberIsNotify(true)
            .build();
    }

    public static UserProfileUpdateDto getUserProfileUpdateDtoWithBotsIsNotifyFalse() {
        User user = getUserWithBotNotifyTrue();
        return UserProfileUpdateDto.builder().addressDto(addressDtoList())
            .recipientName(user.getRecipientName()).recipientSurname(user.getRecipientSurname())
            .recipientPhone(user.getRecipientPhone())
            .alternateEmail("test@email.com")
            .telegramIsNotify(false)
            .viberIsNotify(false)
            .build();
    }

    public static PersonalDataDto getPersonalDataDto() {
        return PersonalDataDto.builder()
            .id(1L)
            .firstName("Max")
            .lastName("B")
            .phoneNumber("09443332")
            .email("dsd@gmail.com")
            .build();
    }

    public static User getUserPersonalData() {
        return User.builder()
            .id(1L)
            .recipientName("Max")
            .recipientSurname("B")
            .recipientPhone("09443332")
            .recipientEmail("dsd@gmail.com")
            .build();
    }

    public static OptionForColumnDTO getOptionForColumnDTO() {
        return OptionForColumnDTO.builder()
            .key("1")
            .en("en")
            .ua("en")
            .build();
    }

    public static ReceivingStationDto getOptionReceivingStationDto() {
        return ReceivingStationDto.builder()
            .id(1L)
            .name("en")
            .build();
    }

    public static List<Address> addressList() {
        List<Address> list = new ArrayList<>();
        list.add(Address.builder()
            .id(1L)
            .entranceNumber("7a")
            .houseCorpus("2")
            .houseNumber("7")
            .street("Gorodotska")
            .coordinates(Coordinates.builder().latitude(2.3).longitude(5.6).build())
            .district("Zaliznuchnuy")
            .city("Lviv")
            .actual(false)
            .build());
        list.add(Address.builder().id(2L)
            .entranceNumber("9a")
            .houseCorpus("2")
            .houseNumber("7")
            .street("Shevchenka")
            .coordinates(Coordinates.builder().latitude(3.3).longitude(6.6).build())
            .district("Zaliznuchnuy")
            .city("Lviv")
            .actual(false)
            .build());
        return list;
    }

    public static AddressDto addressDto() {
        return AddressDto.builder()
            .id(1L)
            .entranceNumber("7a")
            .houseCorpus("2")
            .houseNumber("25")
            .street("Street")
            .coordinates(Coordinates.builder()
                .latitude(50.4459068)
                .longitude(30.4477005)
                .build())
            .district("Distinct")
            .city("City")
            .actual(false)
            .build();
    }

    public static AddressDto addressWithEmptyPlaceIdDto() {
        return AddressDto.builder()
            .id(1L)
            .entranceNumber("7a")
            .houseCorpus("2")
            .houseNumber("25")
            .street("Street")
            .streetEn("StreetEn")
            .coordinates(Coordinates.builder()
                .latitude(0.0)
                .longitude(0.0)
                .build())
            .district("Distinct")
            .city("City")
            .cityEn("CityEn")
            .actual(false)
            .build();
    }

    public static AddressDto addressWithKyivRegionDto() {
        return AddressDto.builder()
            .id(1L)
            .entranceNumber("7a")
            .houseCorpus("2")
            .houseNumber("25")
            .street("Street")
            .coordinates(Coordinates.builder()
                .latitude(50.4459068)
                .longitude(30.4477005)
                .build())
            .district("Distinct")
            .regionEn(KYIV_REGION_EN)
            .region(KYIV_REGION_UA)
            .city("City")
            .cityEn("Kyiv")
            .actual(false)
            .build();
    }

    public static Address getAddress() {
        return Address.builder()
            .id(1L)
            .region("Region")
            .city("City")
            .street("Street")
            .district("Distinct")
            .houseNumber("25")
            .houseCorpus("2")
            .entranceNumber("7a")
            .addressComment("Address Comment")
            .actual(false)
            .addressStatus(AddressStatus.NEW)
            .coordinates(Coordinates.builder()
                .latitude(50.4459068)
                .longitude(30.4477005)
                .build())
            .regionEn("RegionEng")
            .cityEn("Kyiv")
            .streetEn("StreetEng")
            .districtEn("DistinctEng")
            .build();
    }

    public static Address getAddressTrue() {
        return Address.builder()
            .id(1L)
            .region("Region")
            .city("City")
            .street("Street")
            .district("Distinct")
            .houseNumber("25")
            .houseCorpus("2")
            .entranceNumber("7a")
            .addressComment("Address Comment")
            .actual(true)
            .addressStatus(AddressStatus.NEW)
            .coordinates(Coordinates.builder()
                .latitude(50.3072388)
                .longitude(30.3316833)
                .build())
            .regionEn("RegionEng")
            .cityEn("Boiarka")
            .streetEn("StreetEng")
            .districtEn("DistinctEng")
            .build();
    }

    public static Address getAddress(long id) {
        return Address.builder()
            .id(id)
            .region("Вінницька")
            .city("Вінниця")
            .street("Street")
            .district("Distinct")
            .houseNumber("25")
            .houseCorpus("2")
            .entranceNumber("7a")
            .addressComment("Address Comment")
            .actual(false)
            .addressStatus(AddressStatus.NEW)
            .coordinates(Coordinates.builder()
                .latitude(50.4459068)
                .longitude(30.4477005)
                .build())
            .regionEn("RegionEng")
            .cityEn("CityEng")
            .streetEn("StreetEng")
            .districtEn("DistinctEng")
            .build();
    }

    public static LocationDto getLocationApiDto() {
        return LocationDto.builder()
            .locationNameMap(Map.of("name", "Вінниця", "name_en", "Vinnytsa"))
            .build();
    }

    public static List<LocationDto> getLocationApiDtoList() {
        LocationDto locationDto1 = LocationDto.builder()
            .locationNameMap(Map.of("name", "Вінниця", "name_en", "Vinnytsa"))
            .build();
        return singletonList(locationDto1);
    }

    public static DistrictDto getDistrictDto() {
        return DistrictDto.builder()
            .nameUa("Вінниця")
            .nameEn("Vinnytsa")
            .build();
    }

    public static AddressDto getAddressDto(long id) {
        return AddressDto.builder()
            .id(id)
            .region("Вінницька")
            .city("Вінниця")
            .street("Street")
            .district("Distinct")
            .houseNumber("25")
            .houseCorpus("2")
            .entranceNumber("7a")
            .addressComment("Address Comment")
            .actual(false)
            .coordinates(Coordinates.builder()
                .latitude(50.4459068)
                .longitude(30.4477005)
                .build())
            .regionEn("RegionEng")
            .cityEn("CityEng")
            .streetEn("StreetEng")
            .districtEn("DistinctEng")
            .addressRegionDistrictList(singletonList(getDistrictDto()))
            .build();
    }

    public static OrderAddress getOrderAddress() {
        return OrderAddress.builder()
            .region("Region")
            .city("City")
            .street("Street")
            .district("Distinct")
            .houseNumber("25")
            .houseCorpus("2")
            .entranceNumber("7a")
            .addressComment("Address Comment")
            .actual(false)
            .addressStatus(AddressStatus.NEW)
            .coordinates(Coordinates.builder()
                .latitude(50.4459068)
                .longitude(30.4477005)
                .build())
            .regionEn("RegionEng")
            .cityEn("CityEng")
            .streetEn("StreetEng")
            .districtEn("DistinctEng")
            .build();
    }

    public static UbsCustomersDto getUbsCustomersDto() {
        return UbsCustomersDto.builder()
            .name("Ivan Michalov")
            .email("michalov@gmail.com")
            .phoneNumber("095531111")
            .build();
    }

    public static Position getPosition() {
        return Position.builder()
            .id(1L)
            .name("Водій")
            .nameEn("Driver")
            .build();
    }

    public static PositionDto getPositionDto(Long id) {
        return PositionDto.builder()
            .id(id)
            .name("Водій")
            .nameEn("Driver")
            .build();
    }

    public static ReceivingStation getReceivingStation() {
        return ReceivingStation.builder()
            .id(1L)
            .name("Петрівка")
            .createDate(LocalDate.EPOCH)
            .createdBy(getEmployee())
            .build();
    }

    public static ReceivingStationDto getReceivingStationDto() {
        return ReceivingStationDto.builder()
            .id(1L)
            .name("Петрівка")
            .build();
    }

    public static List<ReceivingStation> getReceivingList() {
        return List.of(ReceivingStation.builder().id(1L).build());
    }

    public static Violation getViolation() {
        LocalDateTime localdatetime = LocalDateTime.of(
            2021, Month.MARCH,
            16, 13, 0, 0);
        return Violation.builder()
            .id(1L)
            .order(Order.builder()
                .id(1L).user(ModelUtils.getTestUser()).build())
            .violationLevel(MAJOR)
            .description("violation1")
            .violationDate(localdatetime)
            .images(new LinkedList<>())
            .addedByUser(getTestUser())
            .build();
    }

    public static Violation getViolation2() {
        LocalDateTime localdatetime = LocalDateTime.of(
            2021, Month.MARCH,
            16, 13, 0, 0);
        return Violation.builder()
            .id(1L)
            .order(Order.builder()
                .id(1L).user(ModelUtils.getTestUser()).build())
            .violationLevel(MAJOR)
            .description("violation1")
            .violationDate(localdatetime)
            .images(List.of("as", "s"))
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
            .images(new ArrayList<>())
            .violationDate(localdatetime)
            .build();
    }

    public static OrderPaymentDetailDto getOrderPaymentDetailDto() {
        return OrderPaymentDetailDto.builder()
            .amount(95000L + 1000 + 70000)
            .certificates(-1000)
            .pointsToUse(-70000)
            .amountToPay(95000L)
            .currency("UAH")
            .build();
    }

    public static Payment getPayment() {
        return Payment.builder()
            .id(1L)
            .paymentStatus(PaymentStatus.PAID)
            .amount(95000L)
            .currency("UAH")
            .orderStatus("approved")
            .responseStatus("approved")
            .order(getOrder())
            .paymentId("1")
            .settlementDate(LocalDate.now().toString())
            .fee(0L)
            .build();
    }

    public static User getUser() {
        return User.builder()
            .id(1L)
            .addresses(singletonList(getAddress()))
            .recipientEmail("someUser@gmail.com")
            .recipientPhone("962473289")
            .recipientSurname("Ivanov")
            .uuid("87df9ad5-6393-441f-8423-8b2e770b01a8")
            .recipientName("Taras")
            .uuid("uuid")
            .ubsUsers(getUbsUsers())
            .currentPoints(100)
            .build();
    }

    public static User getUserWithBotNotifyTrue_AddressTrue() {
        return User.builder()
            .id(1L)
            .addresses(singletonList(getAddressTrue()))
            .recipientEmail("someUser@gmail.com")
            .recipientPhone("962473289")
            .recipientSurname("Ivanov")
            .uuid("87df9ad5-6393-441f-8423-8b2e770b01a8")
            .recipientName("Taras")
            .uuid("uuid")
            .ubsUsers(getUbsUsers())
            .currentPoints(100)
            .telegramBot(getTelegramBotNotifyTrue())
            .build();
    }

    public static User getUserWithBotNotifyTrue() {
        return User.builder()
            .id(1L)
            .addresses(singletonList(getAddress()))
            .recipientEmail("someUser@gmail.com")
            .recipientPhone("962473289")
            .recipientSurname("Ivanov")
            .uuid("87df9ad5-6393-441f-8423-8b2e770b01a8")
            .recipientName("Taras")
            .uuid("uuid")
            .ubsUsers(getUbsUsers())
            .currentPoints(100)
            .telegramBot(getTelegramBotNotifyTrue())
            .build();
    }

    public static User getUserWithBotNotifyFalse() {
        return User.builder()
            .id(1L)
            .addresses(singletonList(getAddress()))
            .recipientEmail("someUser@gmail.com")
            .recipientPhone("962473289")
            .recipientSurname("Ivanov")
            .uuid("87df9ad5-6393-441f-8423-8b2e770b01a8")
            .recipientName("Taras")
            .uuid("uuid")
            .ubsUsers(getUbsUsers())
            .currentPoints(100)
            .telegramBot(getTelegramBotNotifyFalse())
            .build();
    }

    public static Set<UBSuser> getUbsUsers() {
        Set<UBSuser> ubSusers = new HashSet<>();
        ubSusers.add(UBSuser.builder().id(1L).build());
        return ubSusers;
    }

    public static Payment getManualPayment() {
        return Payment.builder()
            .settlementDate("02-08-2021")
            .amount(500L)
            .paymentStatus(PaymentStatus.PAID)
            .paymentId("1l")
            .receiptLink("somelink.com")
            .currency("UAH")
            .imagePath("")
            .order(getOrder())
            .build();
    }

    public static ManualPaymentRequestDto getManualPaymentRequestDto() {
        return ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021")
            .amount(500L)
            .receiptLink("link")
            .paymentId("1")
            .imagePath("fdhgh")
            .build();
    }

    public static Order getOrderTest() {
        return Order.builder()
            .id(1L)
            .orderStatus(OrderStatus.FORMED)
            .payment(singletonList(Payment.builder()
                .id(1L)
                .amount(350L)
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .ubsUser(UBSuser.builder()
                .firstName("oleh")
                .lastName("ivanov")
                .email("mail@mail.ua")
                .senderEmail("test@email.ua")
                .senderPhoneNumber("+380974563223")
                .senderLastName("TestLast")
                .senderFirstName("TestFirst")
                .id(1L)
                .phoneNumber("067894522")
                .orderAddress(OrderAddress.builder()
                    .id(1L)
                    .city("Lviv")
                    .street("Levaya")
                    .district("frankivskiy")
                    .entranceNumber("5")
                    .addressComment("near mall")
                    .houseCorpus(null)
                    .houseNumber("4R")
                    .coordinates(Coordinates.builder()
                        .latitude(49.83)
                        .longitude(23.88)
                        .build())
                    .build())
                .build())
            .certificates(Collections.emptySet())
            .cancellationComment("Garbage disappeared")
            .cancellationReason(CancellationReason.OTHER)
            .pointsToUse(700)
            .user(User.builder()
                .id(1L)
                .recipientName("Yuriy")
                .recipientSurname("Gerasum")
                .build())
            .build();
    }

    public static Order getFormedOrder() {
        return Order.builder()
            .id(1L)
            .events(List.of(new Event(1L, LocalDateTime.now(),
                "Roman", "Roman", "Roman", "Roman", new Order())))
            .orderStatus(OrderStatus.FORMED)
            .payment(singletonList(Payment.builder()
                .id(1L)
                .amount(350L)
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .orderDate(LocalDateTime.of(2021, 5, 15, 10, 20, 5))
            .amountOfBagsOrdered(Collections.singletonMap(1, 2))
            .exportedQuantity(Collections.singletonMap(1, 1))
            .amountOfBagsOrdered(Map.of(1, 1))
            .confirmedQuantity(Map.of(1, 0))
            .exportedQuantity(Map.of(1, 1))
            .pointsToUse(100)
            .user(User.builder().id(1L).currentPoints(100).build())
            .build();
    }

    public static Order getCanceledPaidOrder() {
        return Order.builder()
            .id(1L)
            .events(List.of(new Event(1L, LocalDateTime.now(),
                "Roman", "Roman", "Roman", "Roman", new Order())))
            .orderStatus(OrderStatus.CANCELED)
            .payment(singletonList(Payment.builder()
                .id(1L)
                .amount(350L)
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .orderDate(LocalDateTime.of(2021, 5, 15, 10, 20, 5))
            .amountOfBagsOrdered(Collections.singletonMap(1, 2))
            .exportedQuantity(Collections.singletonMap(1, 1))
            .amountOfBagsOrdered(Map.of(1, 1))
            .confirmedQuantity(Map.of(1, 1))
            .exportedQuantity(Map.of(1, 1))
            .pointsToUse(100)
            .user(User.builder().id(1L).currentPoints(100).build())
            .build();
    }

    public static Order getAdjustmentPaidOrder() {
        return Order.builder()
            .id(1L)
            .events(List.of(new Event(1L, LocalDateTime.now(),
                "Roman", "Roman", "Roman", "Roman", new Order())))
            .orderStatus(OrderStatus.ADJUSTMENT)
            .payment(singletonList(Payment.builder()
                .id(1L)
                .amount(300000L)
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .orderDate(LocalDateTime.of(2021, 5, 15, 10, 20, 5))
            .amountOfBagsOrdered(Collections.singletonMap(1, 2))
            .exportedQuantity(Collections.singletonMap(1, 1))
            .amountOfBagsOrdered(Map.of(1, 1))
            .confirmedQuantity(Map.of(1, 1))
            .exportedQuantity(Map.of(2, 2))
            .pointsToUse(100)
            .user(User.builder().id(1L).currentPoints(100).build())
            .build();
    }

    public static Order getFormedHalfPaidOrder() {
        return Order.builder()
            .id(1L)
            .events(List.of(new Event(1L, LocalDateTime.now(),
                "Roman", "Roman", "Roman", "Roman", new Order())))
            .orderStatus(OrderStatus.FORMED)
            .payment(singletonList(Payment.builder()
                .id(1L)
                .amount(100L)
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .orderDate(LocalDateTime.of(2021, 5, 15, 10, 20, 5))
            .amountOfBagsOrdered(Collections.singletonMap(1, 1))
            .exportedQuantity(Collections.singletonMap(1, 1))
            .amountOfBagsOrdered(Map.of(1, 1))
            .confirmedQuantity(Map.of(1, 1))
            .exportedQuantity(Map.of(1, 1))
            .pointsToUse(50)
            .user(User.builder().id(1L).currentPoints(500).build())
            .build();
    }

    public static Order getCanceledHalfPaidOrder() {
        return Order.builder()
            .id(1L)
            .events(List.of(new Event(1L, LocalDateTime.now(),
                "Roman", "Roman", "Roman", "Roman", new Order())))
            .orderStatus(OrderStatus.CANCELED)
            .payment(singletonList(Payment.builder()
                .id(1L)
                .amount(1000L)
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .orderDate(LocalDateTime.of(2021, 5, 15, 10, 20, 5))
            .amountOfBagsOrdered(Collections.singletonMap(1, 1))
            .exportedQuantity(Collections.singletonMap(1, 1))
            .amountOfBagsOrdered(Map.of(1, 1))
            .confirmedQuantity(Map.of(1, 1))
            .exportedQuantity(Map.of(1, 1))
            .pointsToUse(50)
            .user(User.builder().id(1L).currentPoints(500).build())
            .build();
    }

    public static OrderDetailStatusRequestDto getTestOrderDetailStatusRequestDto() {
        return OrderDetailStatusRequestDto.builder()
            .orderStatus("FORMED")
            .adminComment("all good")
            .orderPaymentStatus("PAID").build();
    }

    public static OrderDetailStatusDto getTestOrderDetailStatusDto() {
        return OrderDetailStatusDto.builder()
            .orderStatus("FORMED")
            .paymentStatus("PAID")
            .date("15-05-2021")
            .build();
    }

    public static EmployeeOrderPosition getEmployeeOrderPosition() {
        return EmployeeOrderPosition.builder()
            .id(1L)
            .order(getOrder())
            .position(getPosition())
            .employee(getEmployee())
            .build();
    }

    public static EmployeePositionDtoRequest getEmployeePositionDtoRequest() {
        Long positionId = 1L;
        Map<PositionDto, List<EmployeeNameIdDto>> allPositionsEmployees = new HashMap<>();
        Map<PositionDto, String> currentPositionEmployees = new HashMap<>();
        String value = getEmployee().getFirstName() + " " + getEmployee().getLastName();
        allPositionsEmployees.put(getPositionDto(positionId), new ArrayList<>(List.of(EmployeeNameIdDto.builder()
            .id(positionId)
            .name(value)
            .build())));
        currentPositionEmployees.put(getPositionDto(positionId), value);
        return EmployeePositionDtoRequest.builder()
            .orderId(1L)
            .allPositionsEmployees(allPositionsEmployees)
            .currentPositionEmployees(currentPositionEmployees)
            .build();
    }

    private static Order createOrder() {
        return Order.builder()
            .id(1L)
            .orderStatus(OrderStatus.FORMED)
            .ubsUser(createUbsUser())
            .user(User.builder().id(1L).recipientName("Yuriy").recipientSurname("Gerasum").build())
            .orderDate(LocalDateTime.of(2021, 8, 5, 21, 47, 5))
            .build();
    }

    private static UBSuser createUbsUser() {
        return UBSuser.builder()
            .id(10L)
            .orderAddress(createAddress())
            .build();
    }

    private static OrderAddress createAddress() {
        return OrderAddress.builder()
            .id(2L)
            .build();
    }

    private static OrderAddressExportDetailsDtoUpdate createOrderAddressDtoUpdate() {
        return OrderAddressExportDetailsDtoUpdate.builder()
            .addressId(1L)
            .addressHouseNumber("1")
            .addressEntranceNumber("3")
            .addressDistrict("District")
            .addressDistrictEng("DistrictEng")
            .addressStreet("Street")
            .addressStreetEng("StreetEng")
            .addressHouseCorpus("2")
            .addressCity("City")
            .addressCityEng("CityEng")
            .addressRegion("Region")
            .addressRegionEng("RegionEng")
            .build();
    }

    private static OrderAddressDtoResponse createOrderAddressDtoResponse() {
        return OrderAddressDtoResponse.builder()
            .houseNumber("1")
            .entranceNumber("3")
            .district("District")
            .districtEng("DistrictEng")
            .street("Street")
            .streetEng("StreetEng")
            .houseCorpus("2")
            .build();
    }

    private static List<Payment> createPaymentList() {
        return List.of(
            Payment.builder()
                .id(1L)
                .paymentStatus(PaymentStatus.PAID)
                .amount(100L)
                .build(),
            Payment.builder()
                .id(2L)
                .paymentStatus(PaymentStatus.PAID)
                .amount(50L)
                .build());
    }

    private static OrderDetailStatusDto createOrderDetailStatusDto() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String orderDate = TEST_ORDER.getOrderDate().toLocalDate().format(formatter);

        return OrderDetailStatusDto.builder()
            .orderStatus(TEST_ORDER.getOrderStatus().name())
            .paymentStatus(TEST_PAYMENT_LIST.getFirst().getPaymentStatus().name())
            .date(orderDate)
            .build();
    }

    private static BagInfoDto createBagInfoDto() {
        return BagInfoDto.builder()
            .id(1)
            .capacity(20)
            .name("Name")
            .nameEng("NameEng")
            .price(1000.00)
            .build();
    }

    private static List<BagMappingDto> createBagMappingDtoList() {
        return Collections.singletonList(
            BagMappingDto.builder()
                .amount(4)
                .build());
    }

    private static Bag createBag(int id) {
        Bag bag = Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(id)
            .name("Name")
            .nameEng("NameEng")
            .capacity(20)
            .price(100_00L)
            .commission(0L)
            .fullPrice(100_00L)
            .description("some_description")
            .descriptionEng("some_eng_description")
            .limitIncluded(true)
            .createdAt(LocalDate.now())
            .createdBy(Employee.builder()
                .id(1L)
                .build())
            .build();
        return bag.setFullPrice(100000L);
    }

    private static OrderBag createOrderBag() {
        return OrderBag.builder()
            .id(1L)
            .name("Name")
            .nameEng("NameEng")
            .capacity(20)
            .price(100_00L)
            .order(createOrder())
            .bag(createBag(1))
            .build();
    }

    private static BagForUserDto createBagForUserDto() {
        return BagForUserDto.builder()
            .service("Name")
            .serviceEng("NameEng")
            .capacity(20)
            .fullPrice(100.0)
            .count(22)
            .totalPrice(2200.0)
            .build();
    }

    private static OrderDetailInfoDto createOrderDetailInfoDto() {
        return OrderDetailInfoDto.builder()
            .amount(5)
            .capacity(4)
            .build();
    }

    public static OrderCancellationReasonDto getCancellationDto() {
        return OrderCancellationReasonDto.builder()
            .cancellationReason(CancellationReason.OTHER)
            .cancellationComment("Garbage disappeared")
            .build();
    }

    private static OrderAddressDtoRequest createOrderDtoRequest() {
        return OrderAddressDtoRequest.builder()
            .id(13L).city("Kyiv").district("Svyatoshyn")
            .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Peremohy av.")
            .actual(true).coordinates(new Coordinates(12.5, 34.5))
            .build();
    }

    public static User getUserWithLastLocation() {
        Location location = new Location();
        location.setLocationStatus(LocationStatus.ACTIVE);
        return User.builder()
            .id(1L)
            .addresses(singletonList(getAddress()))
            .recipientEmail("someUser@gmail.com")
            .recipientPhone("962473289")
            .recipientSurname("Ivanov")
            .uuid("87df9ad5-6393-441f-8423-8b2e770b01a8")
            .recipientName("Taras")
            .build();
    }

    public static List<Location> getLocationList() {
        return List.of(Location.builder()
            .locationStatus(LocationStatus.ACTIVE)
            .nameEn("Kyiv")
            .id(1L)
            .nameUk("Київ")
            .region(getRegion())
            .coordinates(getCoordinates())
            .build());
    }

    public static List<Location> getLocationList2() {
        return List.of(Location.builder()
            .id(1L)
            .region(
                Region.builder()
                    .id(1L)
                    .build())
            .build(),
            Location.builder()
                .id(2L)
                .region(
                    Region.builder()
                        .id(2L)
                        .build())
                .build());
    }

    private static Employee createEmployee() {
        return Employee.builder()
            .id(1L)
            .firstName("Test")
            .lastName("Test")
            .build();
    }

    private static Map<String, Object> createMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("Test", new Object());
        return map;
    }

    private static List<AdditionalBagInfoDto> createAdditionalBagInfoDtoList() {
        return Collections.singletonList(createAdditionalBagInfoDto());
    }

    private static AdditionalBagInfoDto createAdditionalBagInfoDto() {
        return AdditionalBagInfoDto.builder()
            .recipientEmail("test@mail.com")
            .build();
    }

    private static User createUser() {
        return User.builder()
            .id(1L)
            .uuid("Test")
            .recipientEmail("test@mail.com")
            .build();
    }

    public static Set<CoordinatesDto> getCoordinatesDtoSet() {
        Set<CoordinatesDto> set = new HashSet<>();
        set.add(CoordinatesDto.builder()
            .latitude(49.83)
            .longitude(23.88)
            .build());
        return set;
    }

    private static NotificationShortDto createNotificationShortDto() {
        return NotificationShortDto.builder()
            .id(1L)
            .orderId(1L)
            .title("Title")
            .notificationTime(LocalDateTime.of(2021, 9, 17, 20, 26, 10))
            .read(false)
            .build();
    }

    private static PageableDto<NotificationShortDto> createPageableDto() {
        return new PageableDto<>(
            TEST_NOTIFICATION_SHORT_DTO_LIST,
            1,
            0,
            1);
    }

    public static NotificationTemplateWithPlatformsUpdateDto createNotificationTemplateWithPlatformsUpdateDto() {
        return NotificationTemplateWithPlatformsUpdateDto.builder()
            .notificationTemplateUpdateInfo(createNotificationTemplateUpdateInfoDto())
            .platforms(List.of(
                createNotificationPlatformDto()))
            .build();
    }

    private static NotificationTemplateWithPlatformsDto createNotificationTemplateWithPlatformsDto() {
        return NotificationTemplateWithPlatformsDto.builder()
            .notificationTemplateMainInfoDto(createNotificationTemplateMainInfoDto())
            .platforms(List.of(createNotificationPlatformDto()))
            .build();
    }

    private static NotificationPlatformDto createNotificationPlatformDto() {
        return NotificationPlatformDto.builder()
            .id(1L)
            .receiverType(SITE)
            .nameEng("NameEng")
            .body("Body")
            .bodyEng("BodyEng")
            .status(ACTIVE)
            .build();
    }

    private static NotificationTemplateDto createNotificationTemplateDto() {
        return NotificationTemplateDto.builder()
            .id(1L)
            .notificationTemplateMainInfoDto(createNotificationTemplateMainInfoDto())
            .build();
    }

    private static NotificationTemplateMainInfoDto createNotificationTemplateMainInfoDto() {
        return NotificationTemplateMainInfoDto.builder()
            .type(UNPAID_ORDER)
            .trigger(ORDER_NOT_PAID_FOR_3_DAYS)
            .triggerDescription("Trigger")
            .triggerDescriptionEng("TriggerEng")
            .time(AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID)
            .timeDescription("Description")
            .timeDescriptionEng("DescriptionEng")
            .schedule("0 0 18 * * ?")
            .title("Title")
            .titleEng("TitleEng")
            .notificationStatus(ACTIVE)
            .build();
    }

    private static NotificationTemplateUpdateInfoDto createNotificationTemplateUpdateInfoDto() {
        return NotificationTemplateUpdateInfoDto.builder()
            .type(UNPAID_ORDER)
            .trigger(ORDER_NOT_PAID_FOR_3_DAYS)
            .time(AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID)
            .schedule("0 0 18 * * ?")
            .title("Title")
            .titleEng("TitleEng")
            .build();
    }

    public static NotificationTemplate createNotificationTemplate() {
        return NotificationTemplate.builder()
            .id(1L)
            .notificationType(UNPAID_ORDER)
            .trigger(ORDER_NOT_PAID_FOR_3_DAYS)
            .time(AT_6PM_3DAYS_AFTER_ORDER_FORMED_NOT_PAID)
            .schedule("0 0 18 * * ?")
            .title("Title")
            .titleEng("TitleEng")
            .notificationStatus(ACTIVE)
            .userCategory(UserCategory.ALL_USERS)
            .notificationPlatforms(List.of(
                createNotificationPlatform(SITE),
                createNotificationPlatform(EMAIL),
                createNotificationPlatform(MOBILE)))
            .build();
    }

    public static NotificationPlatform createNotificationPlatform(
        NotificationReceiverType receiverType) {
        return NotificationPlatform.builder()
            .id(1L)
            .body("Body")
            .bodyEng("BodyEng")
            .notificationReceiverType(receiverType)
            .notificationStatus(ACTIVE)
            .build();
    }

    public static AddNotificationTemplateWithPlatformsDto createAddNotificationTemplateWithPlatforms() {
        return AddNotificationTemplateWithPlatformsDto.builder()
            .schedule("0 0 18 * * ?")
            .title("Title")
            .titleEng("TitleEng")
            .userCategory(UserCategory.ALL_USERS)
            .platforms(List.of(
                createAddNotificationPlatform(SITE),
                createAddNotificationPlatform(EMAIL),
                createAddNotificationPlatform(MOBILE)))
            .build();
    }

    public static AddNotificationPlatformDto createAddNotificationPlatform(
        NotificationReceiverType receiverType) {
        return AddNotificationPlatformDto.builder()
            .body("Body")
            .bodyEng("BodyEng")
            .notificationReceiverType(receiverType)
            .build();
    }

    private static List<UserNotification> createUserNotificationList() {
        UserNotification notification = new UserNotification();
        notification.setId(1L);
        notification.setNotificationTime(LocalDateTime.of(2021, 9, 17, 20, 26, 10));
        notification.setRead(false);
        notification.setUser(TEST_USER);
        notification.setOrder(Order.builder()
            .id(1L)
            .build());
        notification.setNotificationType(UNPAID_ORDER);
        return List.of(
            notification);
    }

    private static Violation createTestViolation() {
        return Violation.builder().description("violation description").build();
    }

    private static Order createTestOrder4() {
        return Order.builder().id(46L).user(User.builder().id(42L).build())
            .orderDate(LocalDateTime.now())
            .build();
    }

    private static Order createTestOrder5() {
        return Order.builder().id(45L).user(User.builder().id(42L).build())
            .orderDate(LocalDateTime.now()).pointsToUse(200)
            .build();
    }

    public static UserNotification createUserNotificationForViolation() {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.VIOLATION_THE_RULES);
        userNotification.setUser(TEST_ORDER_4.getUser());
        userNotification.setOrder(TEST_ORDER_4);

        return userNotification;
    }

    public static UserNotification createUserNotificationForViolationWithParameters() {
        Set<NotificationParameter> parameter = new HashSet<>();
        parameter.add(NotificationParameter.builder()
            .key("violationDescription")
            .value("Description")
            .build());
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.VIOLATION_THE_RULES);
        userNotification.setUser(TEST_ORDER_4.getUser());
        userNotification.setOrder(TEST_ORDER_4);
        userNotification.setParameters(parameter);

        return userNotification;
    }

    public static UserNotification createUserNotificationForViolation6() {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.CHANGED_IN_RULE_VIOLATION_STATUS);
        userNotification.setUser(TEST_ORDER_4.getUser());
        userNotification.setOrder(TEST_ORDER_4);

        return userNotification;
    }

    public static UserNotification createUserNotificationForViolation7() {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER);
        userNotification.setUser(TEST_ORDER_4.getUser());
        userNotification.setOrder(TEST_ORDER_4);

        return userNotification;
    }

    private static UserNotification createUserNotification2() {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.ACCRUED_BONUSES_TO_ACCOUNT);
        userNotification.setUser(TEST_ORDER_3.getUser());
        userNotification.setOrder(TEST_ORDER_3);
        return userNotification;
    }

    private static UserNotification createUserNotification5() {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.BONUSES_FROM_CANCELLED_ORDER);
        userNotification.setUser(TEST_ORDER_5.getUser());
        userNotification.setOrder(TEST_ORDER_5);
        return userNotification;
    }

    private static Set<NotificationParameter> createNotificationParameterSet() {
        Set<NotificationParameter> parameters = new HashSet<>();

        parameters.add(NotificationParameter.builder().key("overpayment")
            .value(String.valueOf(2L)).build());
        parameters.add(NotificationParameter.builder().key("realPackageNumber")
            .value(String.valueOf(0)).build());
        parameters.add(NotificationParameter.builder().key("paidPackageNumber")
            .value(String.valueOf(0)).build());
        parameters.add(NotificationParameter.builder().key("orderNumber")
            .value("45").build());
        return parameters;
    }

    private static Set<NotificationParameter> createNotificationParameterSet2() {
        Set<NotificationParameter> parameters = new HashSet<>();

        parameters.add(NotificationParameter.builder().key("returnedPayment")
            .value(String.valueOf(200L)).build());
        parameters.add(NotificationParameter.builder().key("orderNumber")
            .value("45").build());
        return parameters;
    }

    private static Order createTestOrder3() {
        return Order.builder().id(45L).user(User.builder().id(42L).build())
            .confirmedQuantity(new HashMap<>())
            .exportedQuantity(new HashMap<>())
            .amountOfBagsOrdered(new HashMap<>())
            .orderStatus(OrderStatus.ADJUSTMENT)
            .orderPaymentStatus(OrderPaymentStatus.PAID)
            .orderDate(LocalDateTime.now())
            .build();
    }

    private static UserNotification createUserNotification() {
        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.ORDER_IS_PAID);
        notification.setUser(TEST_ORDER_2.getUser());
        notification.setOrder(TEST_ORDER_2);

        return notification;
    }

    private static Order createTestOrder2() {
        return Order.builder().id(43L).user(User.builder().id(42L).build())
            .orderPaymentStatus(OrderPaymentStatus.PAID).orderDate(LocalDateTime.now()).build();
    }

    private static UserNotification createUserNotification4() {
        UserNotification notification = new UserNotification();
        notification.setId(1L);
        notification.setUser(User.builder()
            .uuid("test")
            .build());
        notification.setRead(false);
        notification.setParameters(null);
        notification.setNotificationType(UNPAID_ORDER);
        return notification;
    }

    private static NotificationDto createNotificationDto() {
        return NotificationDto.builder()
            .title("Title")
            .body("Body")
            .build();
    }

    public static NotificationDto createViolationNotificationDto() {
        return NotificationDto.builder()
            .title("Title")
            .body("Body")
            .images(emptyList())
            .build();
    }

    public static TariffServiceDto TariffServiceDto() {
        return TariffServiceDto.builder()
            .capacity(20)
            .price(100.0)
            .commission(50.0)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .name("name")
            .nameEng("nameEng")
            .build();
    }

    public static GetTariffServiceDto getGetTariffServiceDto() {
        return GetTariffServiceDto.builder()
            .id(1)
            .capacity(20)
            .price(100.0)
            .commission(50.0)
            .fullPrice(150.0)
            .limitIncluded(false)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .name("name")
            .nameEng("nameEng")
            .build();
    }

    public static Optional<Bag> getOptionalBag() {
        return Optional.of(Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(1)
            .capacity(120)
            .commission(50_00L)
            .price(120_00L)
            .fullPrice(170_00L)
            .createdAt(LocalDate.now())
            .createdBy(getEmployee())
            .editedBy(getEmployee())
            .description("Description")
            .descriptionEng("DescriptionEng")
            .limitIncluded(false)
            .build());
    }

    public static OrderBag getOrderBag2() {
        return OrderBag.builder()
            .id(2L)
            .capacity(2200)
            .price(22000_00L)
            .name("name")
            .nameEng("name eng")
            .amount(20)
            .bag(getBag2())
            .order(getOrder())
            .build();
    }

    public static Bag getBag() {
        return Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(1)
            .capacity(120)
            .commission(50_00L)
            .price(120_00L)
            .fullPrice(120_00L)
            .createdAt(LocalDate.now())
            .createdBy(getEmployee())
            .editedBy(getEmployee())
            .limitIncluded(true)
            .tariffsInfo(getTariffInfo())
            .build();
    }

    public static Bag getBag2() {
        return Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(2)
            .capacity(120)
            .commission(50_00L)
            .price(120_00L)
            .fullPrice(2200000L)
            .createdAt(LocalDate.now())
            .createdBy(getEmployee())
            .editedBy(getEmployee())
            .limitIncluded(true)
            .tariffsInfo(getTariffInfo())
            .build();
    }

    public static OrderBag getOrderBag() {
        return OrderBag.builder()
            .id(1L)
            .capacity(120)
            .price(120_00L)
            .name("name")
            .nameEng("name eng")
            .amount(1)
            .bag(getBag())
            .order(getOrder())
            .build();
    }

    public static OrderBag getOrderBagWithConfirmedAmount() {
        return OrderBag.builder()
            .id(1L)
            .capacity(120)
            .price(120_00L)
            .name("name")
            .nameEng("name eng")
            .amount(1)
            .confirmedQuantity(2)
            .bag(getBag())
            .order(getOrder())
            .build();
    }

    public static OrderBag getOrderBagWithExportedAmount() {
        return OrderBag.builder()
            .id(1L)
            .capacity(120)
            .price(120_00L)
            .name("name")
            .nameEng("name eng")
            .amount(1)
            .confirmedQuantity(2)
            .exportedQuantity(2)
            .bag(getBag())
            .order(getOrder())
            .build();
    }

    public static Bag getBagDeleted() {
        return Bag.builder()
            .id(1)
            .capacity(120)
            .commission(50_00L)
            .price(120_00L)
            .fullPrice(170_00L)
            .createdAt(LocalDate.now())
            .createdBy(getEmployee())
            .editedBy(getEmployee())
            .description("Description")
            .descriptionEng("DescriptionEng")
            .limitIncluded(true)
            .status(BagStatus.DELETED)
            .tariffsInfo(getTariffInfo())
            .build();
    }

    public static Bag getBagForOrder() {
        return Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(3)
            .capacity(120)
            .commission(50_00L)
            .price(350_00L)
            .fullPrice(400_00L)
            .createdAt(LocalDate.now())
            .createdBy(getEmployee())
            .editedBy(getEmployee())
            .description("Description")
            .descriptionEng("DescriptionEng")
            .limitIncluded(true)
            .tariffsInfo(getTariffInfo())
            .build();
    }

    public static TariffServiceDto getTariffServiceDto() {
        return TariffServiceDto.builder()
            .name("Бавовняна сумка")
            .capacity(20)
            .price(100.0)
            .commission(50.0)
            .description("Description")
            .build();

    }

    public static Bag getEditedBag() {
        return Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(1)
            .capacity(20)
            .price(100_00L)
            .fullPrice(150_00L)
            .commission(50_00L)
            .name("Бавовняна сумка")
            .description("Description")
            .createdAt(LocalDate.now())
            .createdBy(getEmployee())
            .editedBy(getEmployee())
            .editedAt(LocalDate.now())
            .limitIncluded(true)
            .status(BagStatus.ACTIVE)
            .tariffsInfo(getTariffInfo())
            .build();

    }

    public static Location getLocation() {
        return Location.builder()
            .id(1L)
            .locationStatus(LocationStatus.ACTIVE)
            .nameEn("Kyiv")
            .nameUk("Київ")
            .coordinates(Coordinates.builder()
                .longitude(3.34d)
                .latitude(1.32d).build())
            .region(getRegionForMapper())
            .orderAddresses(new ArrayList<>())
            .build();
    }

    public static Location getLocationDeactivated() {
        return Location.builder()
            .id(1L)
            .locationStatus(LocationStatus.DEACTIVATED)
            .nameEn("Kyiv")
            .nameUk("Київ")
            .coordinates(Coordinates.builder()
                .longitude(3.34d)
                .latitude(1.32d).build())
            .region(getRegionForMapper())
            .orderAddresses(new ArrayList<>())
            .build();
    }

    public static Courier getCourier() {
        return Courier.builder()
            .id(1L)
            .courierStatus(CourierStatus.ACTIVE)
            .nameUk("Тест")
            .nameEn("Test")
            .build();
    }

    public static Courier getDeactivatedCourier() {
        return Courier.builder()
            .id(1L)
            .courierStatus(CourierStatus.DEACTIVATED)
            .nameUk("Тест")
            .nameEn("Test")
            .build();
    }

    public static CourierDto getCourierDto() {
        return CourierDto.builder()
            .courierId(1L)
            .courierStatus("ACTIVE")
            .nameUk("Тест")
            .nameEn("Test")
            .build();
    }

    public static Bag getTariffBag() {
        return Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(1)
            .capacity(20)
            .price(100_00L)
            .commission(50_00L)
            .fullPrice(150_00L)
            .createdAt(LocalDate.now())
            .createdBy(getEmployee())
            .description("Description")
            .descriptionEng("DescriptionEng")
            .name("name")
            .nameEng("nameEng")
            .limitIncluded(false)
            .tariffsInfo(getTariffInfo())
            .build();
    }

    public static BagTranslationDto getBagTranslationDto() {
        return BagTranslationDto.builder()
            .id(1)
            .capacity(20)
            .price(150.0)
            .name("name")
            .nameEng("nameEng")
            .limitedIncluded(false)
            .build();
    }

    public static Bag getNewBag() {
        return Bag.builder()
            .status(BagStatus.ACTIVE)
            .capacity(20)
            .price(100_00L)
            .commission(50_00L)
            .fullPrice(150_00L)
            .createdAt(LocalDate.now())
            .createdBy(getEmployee())
            .limitIncluded(false)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .name("name")
            .nameEng("nameEng")
            .build();
    }

    public static AdminCommentDto getAdminCommentDto() {
        return AdminCommentDto.builder()
            .orderId(1L)
            .adminComment("Admin")
            .build();
    }

    public static EcoNumberDto getEcoNumberDto() {
        return EcoNumberDto.builder()
            .ecoNumber(new HashSet<>(Arrays.asList("1111111111", "3333333333")))
            .build();
    }

    public static ServiceDto getServiceDto() {
        return ServiceDto.builder()
            .name("Name")
            .nameEng("NameEng")
            .price(100.0)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .build();
    }

    public static GetServiceDto getGetServiceDto() {
        return GetServiceDto.builder()
            .id(1L)
            .name("Name")
            .nameEng("NameEng")
            .price(100.0)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .build();

    }

    public static Service getService() {
        Employee employee = ModelUtils.getEmployee();
        return Service.builder()
            .id(1L)
            .price(100_00L)
            .createdAt(LocalDate.now())
            .createdBy(employee)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .name("Name")
            .nameEng("NameEng")
            .build();
    }

    public static Service getNewService() {
        Employee employee = ModelUtils.getEmployee();
        return Service.builder()
            .price(100_00L)
            .createdAt(LocalDate.now())
            .createdBy(employee)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .name("Name")
            .nameEng("NameEng")
            .build();
    }

    public static Service getEditedService() {
        Employee employee = ModelUtils.getEmployee();
        return Service.builder()
            .id(1L)
            .name("Name")
            .nameEng("NameEng")
            .price(100_00L)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .editedAt(LocalDate.now())
            .editedBy(employee)
            .build();
    }

    public static CreateCourierDto getCreateCourierDto() {
        return CreateCourierDto.builder()
            .nameUk("Тест")
            .nameEn("Test")
            .build();
    }

    public static CounterOrderDetailsDto getcounterOrderDetailsDto() {
        return CounterOrderDetailsDto.builder()
            .totalAmount(22.02D)
            .totalConfirmed(12.34D)
            .totalExported(32.2D)
            .sumAmount(35.3D)
            .sumConfirmed(31.54D)
            .sumExported(366.44D)
            .certificateBonus(23.4D)
            .bonus(54.32D)
            .totalSumAmount(1.32D)
            .totalSumConfirmed(32.6D)
            .totalSumExported(73.1D)
            .orderComment("test")
            .certificate(List.of("fds"))
            .numberOrderFromShop(Set.of("dsd"))
            .build();
    }

    public static Order getOrderUserFirst() {
        return Order.builder()
            .id(1L)
            .exportedQuantity(Collections.singletonMap(1, 1))
            .amountOfBagsOrdered(Map.of(1, 1))
            .confirmedQuantity(Map.of(1, 1))
            .exportedQuantity(Map.of(1, 1))
            .pointsToUse(100)
            .build();
    }

    public static Order getOrderUserSecond() {
        return Order.builder()
            .id(2L)
            .exportedQuantity(Collections.singletonMap(1, 1))
            .amountOfBagsOrdered(Map.of(1, 1))
            .confirmedQuantity(Map.of(1, 1))
            .exportedQuantity(Map.of(1, 1))
            .pointsToUse(100)
            .build();

    }

    public static List<Bag> getBag1list() {
        return List.of(Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(1)
            .price(100_00L)
            .capacity(20)
            .commission(50_00L)
            .fullPrice(170_00L)
            .name("name")
            .nameEng("nameEng")
            .limitIncluded(false)
            .build());
    }

    public static List<Bag> getBaglist() {
        return List.of(Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(1)
            .price(100_00L)
            .capacity(10)
            .commission(21_00L)
            .fullPrice(20_00L)
            .build(),
            Bag.builder()
                .status(BagStatus.ACTIVE)
                .id(2)
                .price(100_00L)
                .capacity(10)
                .commission(21_00L)
                .fullPrice(21_00L)
                .build());
    }

    public static List<Bag> getBag4list() {
        return List.of(Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(1)
            .price(100_00L)
            .capacity(10)
            .commission(20_00L)
            .fullPrice(120_00L)
            .name("name")
            .nameEng("nameEng")
            .limitIncluded(false)
            .build(),
            Bag.builder()
                .status(BagStatus.ACTIVE)
                .id(2)
                .price(100_00L)
                .capacity(10)
                .commission(20_00L)
                .fullPrice(120_00L)
                .name("name")
                .nameEng("nameEng")
                .limitIncluded(false)
                .build());
    }

    public static List<Certificate> getCertificateList() {
        return List.of(Certificate.builder()
            .code("uuid")
            .certificateStatus(CertificateStatus.ACTIVE)
            .creationDate(LocalDate.now())
            .points(999)
            .build());
    }

    public static OrderStatusTranslation getStatusTranslation() {
        return OrderStatusTranslation.builder().id(6L).statusId(2L).name("name").build();
    }

    public static List<OrderStatusTranslation> getOrderStatusTranslations() {
        return List.of(OrderStatusTranslation.builder()
            .id(1L)
            .statusId(6L)
            .name("Order DONE")
            .build(),
            OrderStatusTranslation.builder()
                .id(2L)
                .statusId(7L)
                .name("Order NOT TAKEN OUT")
                .build(),
            OrderStatusTranslation.builder().id(3L)
                .statusId(8L)
                .name("Order CANCELLED")
                .build());
    }

    public static List<OrderPaymentStatusTranslation> getOrderStatusPaymentTranslations() {
        return List.of(OrderPaymentStatusTranslation.builder()
            .id(1L)
            .translationValue("тест")
            .translationsValueEng("test")
            .orderPaymentStatusId(1L)
            .build(),
            OrderPaymentStatusTranslation.builder()
                .id(2L)
                .translationValue("тест2")
                .translationsValueEng("test2")
                .orderPaymentStatusId(2L)
                .build(),
            OrderPaymentStatusTranslation.builder()
                .id(3L)
                .translationValue("тест3")
                .translationsValueEng("test3")
                .orderPaymentStatusId(3L)
                .build(),
            OrderPaymentStatusTranslation.builder()
                .id(4L)
                .translationValue("тест4")
                .translationsValueEng("test4")
                .orderPaymentStatusId(4L)
                .build());
    }

    public static BagInfoDto getBagInfoDto() {
        return BagInfoDto.builder()
            .id(1)
            .name("name")
            .nameEng("name")
            .price(100.)
            .capacity(10)
            .build();
    }

    public static PaymentTableInfoDto getPaymentTableInfoDto() {
        return PaymentTableInfoDto.builder()
            .paidAmount(200d)
            .unPaidAmount(0d)
            .paymentInfoDtos(List.of(getInfoPayment().setAmount(10d)))
            .overpayment(800d)
            .build();
    }

    public static PaymentTableInfoDto getPaymentTableInfoDto2() {
        return PaymentTableInfoDto.builder()
            .paidAmount(0d)
            .unPaidAmount(0d)
            .paymentInfoDtos(Collections.emptyList())
            .overpayment(400d)
            .build();
    }

    public static PaymentInfoDto getInfoPayment() {
        return PaymentInfoDto.builder()
            .comment("ddd")
            .id(1L)
            .amount(10d)
            .build();
    }

    public static OrderPaymentStatusTranslation getOrderPaymentStatusTranslation() {
        return OrderPaymentStatusTranslation.builder()
            .id(1L)
            .orderPaymentStatusId(1L)
            .translationValue("Абв")
            .translationsValueEng("Abc")
            .build();
    }

    public static OrderWayForPayClientDto getOrderWayForPayClientDto() {
        return OrderWayForPayClientDto.builder()
            .orderId(1L)
            .pointsToUse(100)
            .build();
    }

    public static Order getOrderCount() {
        return Order.builder()
            .id(1L)
            .pointsToUse(1)
            .counterOrderPaymentId(2L)
            .orderPaymentStatus(OrderPaymentStatus.HALF_PAID)
            .payment(Lists.newArrayList(Payment.builder()
                .paymentId("1L")
                .amount(200L)
                .currency("UAH")
                .settlementDate("20.02.1990")
                .comment("avb")
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .build();
    }

    public static Order getOrderCountWithPaymentStatusPaid() {
        return Order.builder()
            .id(1L)
            .pointsToUse(1)
            .counterOrderPaymentId(2L)
            .orderPaymentStatus(OrderPaymentStatus.PAID)
            .build();
    }

    public static UpdateOrderPageAdminDto updateOrderPageAdminDto() {
        return UpdateOrderPageAdminDto.builder()
            .generalOrderInfo(OrderDetailStatusRequestDto
                .builder()
                .orderStatus(String.valueOf(OrderStatus.CONFIRMED))
                .orderPaymentStatus(String.valueOf(PaymentStatus.PAID))
                .adminComment("aaa")
                .build())
            .userInfoDto(UbsCustomersDtoUpdate
                .builder()
                .recipientId(1L)
                .recipientName("Anatolii Petyrov")
                .recipientPhoneNumber("095123456")
                .recipientEmail("anatolii.andr@gmail.com")
                .build())
            .addressExportDetailsDto(OrderAddressExportDetailsDtoUpdate
                .builder()
                .addressId(1L)
                .addressDistrict("District")
                .addressDistrictEng("DistrictEng")
                .addressStreet("Street")
                .addressStreetEng("StreetEng")
                .addressEntranceNumber("12")
                .addressHouseCorpus("123")
                .addressHouseNumber("121")
                .addressCity("City")
                .addressCityEng("CityEng")
                .addressRegion("Region")
                .addressRegionEng("RegionEng")
                .build())
            .ecoNumberFromShop(EcoNumberDto.builder()
                .ecoNumber(Set.of("1111111111"))
                .build())
            .exportDetailsDto(ExportDetailsDtoUpdate
                .builder()
                .dateExport("1997-12-04T15:40:24")
                .timeDeliveryFrom("1997-12-04T15:40:24")
                .timeDeliveryTo("1990-12-11T19:30:30")
                .receivingStationId(1L)
                .build())
            .orderDetailDto(
                UpdateOrderDetailDto.builder()
                    .amountOfBagsConfirmed(Map.ofEntries(Map.entry(1, 1)))
                    .amountOfBagsExported(Map.ofEntries(Map.entry(1, 1)))
                    .build())
            .updateResponsibleEmployeeDto(List.of(UpdateResponsibleEmployeeDto.builder()
                .positionId(2L)
                .employeeId(2L)
                .build()))
            .build();
    }

    public static UpdateOrderPageAdminDto updateOrderPageAdminDtoWithNullFields() {
        return UpdateOrderPageAdminDto.builder()
            .generalOrderInfo(OrderDetailStatusRequestDto
                .builder()
                .orderStatus(String.valueOf(OrderStatus.DONE))
                .build())
            .exportDetailsDto(ExportDetailsDtoUpdate
                .builder()
                .dateExport(null)
                .timeDeliveryFrom(null)
                .timeDeliveryTo(null)
                .receivingStationId(null)
                .build())
            .build();
    }

    public static UpdateOrderPageAdminDto updateOrderPageAdminDtoWithStatusCanceled() {
        return UpdateOrderPageAdminDto.builder()
            .generalOrderInfo(OrderDetailStatusRequestDto
                .builder()
                .orderStatus(String.valueOf(OrderStatus.FORMED))
                .build())
            .exportDetailsDto(ExportDetailsDtoUpdate
                .builder()
                .dateExport(null)
                .timeDeliveryFrom(null)
                .timeDeliveryTo(null)
                .receivingStationId(null)
                .build())
            .build();
    }

    public static UpdateOrderPageAdminDto updateOrderPageAdminDtoWithStatusBroughtItHimself() {
        return UpdateOrderPageAdminDto.builder()
            .generalOrderInfo(OrderDetailStatusRequestDto
                .builder()
                .orderStatus(String.valueOf(OrderStatus.DONE))
                .build())
            .exportDetailsDto(ExportDetailsDtoUpdate
                .builder()
                .dateExport("2023-12-16T16:30")
                .timeDeliveryFrom("2023-12-16T19:00")
                .timeDeliveryTo("2023-12-16T20:30")
                .receivingStationId(2L)
                .build())
            .build();
    }

    public static UpdateOrderPageAdminDto updateOrderPageAdminDtoWithStatusFormed() {
        return UpdateOrderPageAdminDto.builder()
            .generalOrderInfo(OrderDetailStatusRequestDto
                .builder()
                .orderStatus(String.valueOf(OrderStatus.FORMED))
                .build())
            .exportDetailsDto(ExportDetailsDtoUpdate
                .builder()
                .dateExport(null)
                .timeDeliveryFrom(null)
                .timeDeliveryTo(null)
                .receivingStationId(null)
                .build())
            .build();
    }

    public static Location getLocationDto() {
        return Location.builder()
            .id(1L)
            .locationStatus(LocationStatus.DEACTIVATED)
            .nameUk("Київ")
            .nameEn("Kyiv")
            .build();
    }

    public static Bag bagDto() {
        return Bag.builder()
            .status(BagStatus.ACTIVE)
            .id(1)
            .limitIncluded(false)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .name("Test")
            .nameEng("a")
            .createdBy(getEmployee())
            .editedBy(getEmployee())
            .build();
    }

    public static OrderStatusTranslation getOrderStatusTranslation() {
        return OrderStatusTranslation
            .builder()
            .statusId(1L)
            .id(1L)
            .name("ua")
            .build();
    }

    public static Order getOrdersDto() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.CANCELED)
            .counterOrderPaymentId(1L)
            .build();
    }

    public static UserProfileUpdateDto updateUserProfileDto() {
        return UserProfileUpdateDto.builder()
            .recipientName("Taras")
            .recipientSurname("Ivanov")
            .recipientPhone("962473289")
            .addressDto(addressDtoList())
            .telegramIsNotify(true)
            .viberIsNotify(false)
            .build();
    }

    public static List<Region> getAllRegion() {
        return List.of(Region.builder()
            .id(1L)
            .ukrName("Київська область")
            .enName("Kyiv region")
            .locations(getLocationList())
            .build());
    }

    public static List<RegionTranslationDto> getRegionTranslationsDto() {
        return List.of(
            RegionTranslationDto.builder().languageCode("ua").regionName("Київська область").build(),
            RegionTranslationDto.builder().regionName("Kyiv region").languageCode("en").build());
    }

    public static List<LocationCreateDto> getLocationCreateDtoList() {
        return List.of(LocationCreateDto.builder()
            .addLocationDtoList(getAddLocationTranslationDtoList())
            .regionTranslationDtos(getRegionTranslationsDto())
            .longitude(3.34d)
            .latitude(1.32d)
            .build());
    }

    public static List<AddLocationTranslationDto> getAddLocationTranslationDtoList() {
        return List.of(
            AddLocationTranslationDto.builder().locationName("Київ").languageCode("ua").build(),
            AddLocationTranslationDto.builder().locationName("Kyiv").languageCode("en").build());
    }

    public static Region getRegion() {
        return Region.builder()
            .id(1L)
            .ukrName("Київська область")
            .enName("Kyiv region")
            .locations(List.of(getLocation()))
            .build();
    }

    public static Region getUnknownRegion() {
        return Region.builder()
            .id(0L)
            .ukrName(AppConstant.UNKNOWN_UA)
            .enName(AppConstant.UNKNOWN_ENG)
            .locations(List.of(getLocation()))
            .build();
    }

    public static Region getRegionForMapper() {
        return Region.builder()
            .id(1L)
            .ukrName("Київська область")
            .enName("Kyiv region")
            .build();
    }

    public static LocationInfoDto getInfoAboutLocationDto() {
        return LocationInfoDto.builder()
            .regionId(1L)
            .regionTranslationDtos(getRegionTranslationsDto())
            .locationsDto(getLocationsDto()).build();
    }

    public static List<LocationsDto> getLocationsDto() {
        return List.of(LocationsDto.builder()
            .locationTranslationDtoList(getLocationTranslationDto())
            .locationStatus("ACTIVE")
            .longitude(3.34d)
            .latitude(1.32d)
            .build());
    }

    public static List<LocationTranslationDto> getLocationTranslationDto() {
        return List.of(LocationTranslationDto.builder()
            .locationName("Київ")
            .languageCode("ua")
            .build(),
            LocationTranslationDto.builder()
                .locationName("Kyiv")
                .languageCode("en").build());
    }

    public static List<CourierDto> getCourierDtoList() {
        return List.of(CourierDto.builder()
            .courierId(1L)
            .courierStatus("ACTIVE")
            .nameUk("Тест")
            .nameEn("Test")
            .build());
    }

    public static Location getLocationForCreateRegion() {
        return Location.builder()
            .locationStatus(LocationStatus.ACTIVE)
            .nameUk("Київ").nameEn("Kyiv")
            .coordinates(Coordinates.builder()
                .longitude(3.34d)
                .latitude(1.32d).build())
            .region(Region.builder().id(1L).enName("Kyiv region").ukrName("Київська область").build())
            .build();
    }

    public static BigOrderTableViews getBigOrderTableViews() {
        return new BigOrderTableViews()
            .setId(3333L)
            .setOrderStatus("FORMED")
            .setOrderPaymentStatus("PAID")
            .setOrderDate(LocalDate.of(2021, 12, 8))
            .setPaymentDate(LocalDate.of(2021, 12, 8))
            .setClientName("Uliana Стан")
            .setClientPhoneNumber("+380996755544")
            .setClientEmail("motiy14146@ecofreon.com")
            .setSenderName("Uliana Стан")
            .setSenderPhone("996755544")
            .setSenderEmail("motiy14146@ecofreon.com")
            .setViolationsAmount(1)
            .setRegion("Київська область")
            .setRegionEn("Kyivs'ka oblast")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Шевченківський")
            .setDistrictEn("Shevchenkivs'kyi")
            .setAddress("Січових Стрільців, 37, 1, 1")
            .setAddressEn("Sichovyh Stril'tsiv, 37, 1, 1")
            .setCommentToAddressForClient("coment")
            .setBagAmount("3")
            .setTotalOrderSum(50000L)
            .setOrderCertificateCode("5489-2789")
            .setGeneralDiscount(100L)
            .setAmountDue(0L)
            .setCommentForOrderByClient("commentForOrderByClient")
            .setCommentForOrderByAdmin("commentForOrderByAdmin")
            .setTotalPayment(20000L)
            .setDateOfExport(LocalDate.of(2021, 12, 8))
            .setTimeOfExport("from 15:59:52 to 15:59:52")
            .setIdOrderFromShop("3245678765")
            .setReceivingStationId(1L)
            .setResponsibleLogicManId(1L)
            .setResponsibleDriverId(1L)
            .setResponsibleCallerId(1L)
            .setResponsibleNavigatorId(1L)
            .setIsBlocked(true)
            .setBlockedBy("Blocked Test");
    }

    public static BigOrderTableDTO getBigOrderTableDto() {
        return new BigOrderTableDTO()
            .setId(3333L)
            .setOrderStatus("FORMED")
            .setOrderPaymentStatus("PAID")
            .setOrderDate("2021-12-08")
            .setPaymentDate("2021-12-08")
            .setClientName("Uliana Стан")
            .setClientPhone("+380996755544")
            .setClientEmail("motiy14146@ecofreon.com")
            .setSenderName("Uliana Стан")
            .setSenderPhone("996755544")
            .setSenderEmail("motiy14146@ecofreon.com")
            .setViolationsAmount(1)
            .setRegion(new SenderLocation().setUa("Київська область").setEn("Kyivs'ka oblast"))
            .setCity(new SenderLocation().setUa("Київ").setEn("Kyiv"))
            .setDistrict(new SenderLocation().setUa("Шевченківський").setEn("Shevchenkivs'kyi"))
            .setAddress(
                new SenderLocation().setUa("Січових Стрільців, 37, 1, 1").setEn("Sichovyh Stril'tsiv, 37, 1, 1"))
            .setCommentToAddressForClient("coment")
            .setBagsAmount("3")
            .setTotalOrderSum(500.)
            .setOrderCertificateCode("5489-2789")
            .setGeneralDiscount(100L)
            .setAmountDue(0.)
            .setCommentForOrderByClient("commentForOrderByClient")
            .setCommentsForOrder("commentForOrderByAdmin")
            .setTotalPayment(200.)
            .setDateOfExport("2021-12-08")
            .setTimeOfExport("from 15:59:52 to 15:59:52")
            .setIdOrderFromShop("3245678765")
            .setReceivingStation("1")
            .setResponsibleLogicMan("1")
            .setResponsibleDriver("1")
            .setResponsibleCaller("1")
            .setResponsibleNavigator("1")
            .setIsBlocked(true)
            .setBlockedBy("Blocked Test");
    }

    public static BigOrderTableDTO getBigOrderTableDtoByDateNullTest() {
        return new BigOrderTableDTO()
            .setOrderDate("")
            .setPaymentDate("")
            .setDateOfExport("")
            .setReceivingStation("")
            .setResponsibleCaller("")
            .setResponsibleDriver("")
            .setResponsibleLogicMan("")
            .setResponsibleNavigator("")
            .setRegion(new SenderLocation().setEn(null).setUa(null))
            .setCity(new SenderLocation().setEn(null).setUa(null))
            .setDistrict(new SenderLocation().setEn(null).setUa(null))
            .setAddress(new SenderLocation().setEn(null).setUa(null));
    }

    public static BigOrderTableViews getBigOrderTableViewsByDateNullTest() {
        return new BigOrderTableViews()
            .setOrderDate(null)
            .setPaymentDate(null)
            .setDateOfExport(null)
            .setReceivingStation(null)
            .setResponsibleCaller(null)
            .setResponsibleDriver(null)
            .setResponsibleLogicMan(null)
            .setResponsibleNavigator(null);
    }

    public static Map<Integer, Integer> getAmount() {
        Map<Integer, Integer> hashMap = new HashMap<>();
        hashMap.put(1, 1);
        hashMap.put(2, 1);
        return hashMap;
    }

    public static Order getOrderForGetOrderStatusData2Test() {
        Map<Integer, Integer> hashMap = new HashMap<>();
        hashMap.put(1, 1);
        hashMap.put(2, 1);

        return Order.builder()
            .orderBags(Arrays.asList(getOrderBag(), getOrderBag2()))
            .id(1L)
            .amountOfBagsOrdered(hashMap)
            .confirmedQuantity(hashMap)
            .exportedQuantity(hashMap)
            .pointsToUse(100)
            .orderStatus(OrderStatus.DONE)
            .payment(Lists.newArrayList(Payment.builder()
                .paymentId("1L")
                .amount(200_00L)
                .currency("UAH")
                .settlementDate("20.02.1990")
                .comment("avb")
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .ubsUser(UBSuser.builder()
                .firstName("oleh")
                .lastName("ivanov")
                .email("mail@mail.ua")
                .id(1L)
                .phoneNumber("067894522")
                .orderAddress(OrderAddress.builder()
                    .id(1L)
                    .city("Lviv")
                    .street("Levaya")
                    .district("frankivskiy")
                    .entranceNumber("5")
                    .addressComment("near mall")
                    .houseCorpus("1")
                    .houseNumber("4")
                    .coordinates(Coordinates.builder()
                        .latitude(49.83)
                        .longitude(23.88)
                        .build())
                    .build())
                .build())
            .user(User.builder().id(1L).recipientName("Yuriy").recipientSurname("Gerasum").currentPoints(100).build())
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .adminComment("Admin")
            .cancellationComment("cancelled")
            .receivingStation(ReceivingStation.builder()
                .id(1L)
                .name("Саперно-Слобідська")
                .build())
            .orderPaymentStatus(OrderPaymentStatus.PAID)
            .cancellationReason(CancellationReason.OUT_OF_CITY)
            .writeOffStationSum(50_00L)
            .imageReasonNotTakingBags(List.of("foto"))

            .tariffsInfo(TariffsInfo.builder()
                .courier(Courier.builder()
                    .id(1L)
                    .build())
                .id(1L)
                .tariffLocations(Set.of(TariffLocation.builder()
                    .id(1L)
                    .build()))
                .max(99L)
                .min(2L)
                .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
                .build())

            .build();
    }

    public static Order getOrderForGetOrderStatusEmptyPriceDetails() {
        return Order.builder()
            .id(1L)
            .amountOfBagsOrdered(new HashMap<>())
            .confirmedQuantity(new HashMap<>())
            .exportedQuantity(new HashMap<>())
            .pointsToUse(100)
            .orderStatus(OrderStatus.DONE)
            .build();
    }

    public static Order getOrderWithoutExportedBags() {
        Map<Integer, Integer> hashMap = new HashMap<>();
        hashMap.put(1, 1);
        hashMap.put(2, 1);
        return Order.builder()
            .id(1L)
            .amountOfBagsOrdered(hashMap)
            .confirmedQuantity(hashMap)
            .exportedQuantity(new HashMap<>())
            .pointsToUse(100)
            .certificates(Collections.emptySet())
            .orderStatus(OrderStatus.CONFIRMED)
            .user(User.builder().id(1L).currentPoints(100).build())
            .writeOffStationSum(50_00L)
            .payment(Lists.newArrayList(Payment.builder()
                .paymentId("1L")
                .amount(20000L)
                .currency("UAH")
                .settlementDate("20.02.1990")
                .comment("avb")
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .build();
    }

    public static Order getOrdersStatusAdjustmentDto2() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.ADJUSTMENT)
            .counterOrderPaymentId(1L)
            .certificates(Set.of(getActiveCertificateWith600Points()))
            .pointsToUse(100)
            .writeOffStationSum(50_00L)
            .build();
    }

    public static Order getOrdersStatusConfirmedDto() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.CONFIRMED)
            .counterOrderPaymentId(1L)
            .build();
    }

    public static Order getOrdersStatusFormedDto() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.FORMED)
            .counterOrderPaymentId(1L)
            .pointsToUse(100)
            .confirmedQuantity(Map.of(1, 1))
            .exportedQuantity(Map.of(1, 1))
            .amountOfBagsOrdered(Map.of(1, 1))
            .build();
    }

    public static Order getOrdersStatusFormedDto2() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.FORMED)
            .counterOrderPaymentId(1L)
            .pointsToUse(100)
            .confirmedQuantity(Map.of(1, 3))
            .exportedQuantity(Map.of(1, 1))
            .amountOfBagsOrdered(Map.of(1, 1))
            .build();
    }

    public static Order getOrdersStatusNotTakenOutDto() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.NOT_TAKEN_OUT)
            .counterOrderPaymentId(1L)
            .build();
    }

    public static Order getOrdersStatusOnThe_RouteDto() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.ON_THE_ROUTE)
            .counterOrderPaymentId(1L)
            .build();
    }

    public static Order getOrdersStatusBROUGHT_IT_HIMSELFDto() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.BROUGHT_IT_HIMSELF)
            .counterOrderPaymentId(1L)
            .build();
    }

    public static Order getOrdersStatusDoneDto() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.DONE)
            .counterOrderPaymentId(1L)
            .certificates(Set.of(Certificate.builder()
                .points(0)
                .build()))
            .pointsToUse(0)
            .build();
    }

    public static Order getOrdersStatusCanceledDto() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.CANCELED)
            .counterOrderPaymentId(1L)
            .build();
    }

    public static OrderAddressExportDetailsDtoUpdate getOrderAddressExportDetailsDtoUpdate() {
        return OrderAddressExportDetailsDtoUpdate.builder()
            .addressId(1L)
            .addressStreet("Street")
            .addressStreetEng("StreetEng")
            .addressCity("City")
            .addressCityEng("City")
            .addressDistrict("District")
            .addressDistrictEng("DistrictEng")
            .addressHouseCorpus("12")
            .addressEntranceNumber("2")
            .addressRegion("Region")
            .addressRegionEng("RegionEng")
            .addressHouseNumber("123")
            .build();

    }

    public static CustomTableView getCustomTableView() {
        return CustomTableView.builder()
            .id(1L)
            .uuid("uuid1")
            .titles("title")
            .build();
    }

    public static ReadAddressByOrderDto getReadAddressByOrderDto() {
        return ReadAddressByOrderDto.builder()
            .street("Levaya")
            .district("frankivskiy")
            .entranceNumber("5")
            .houseCorpus("1")
            .houseNumber("4")
            .comment("helo")
            .build();
    }

    public static RequestToChangeOrdersDataDto getRequestToChangeOrdersDataDTO() {
        return RequestToChangeOrdersDataDto.builder()
            .columnName("orderStatus")
            .orderIdsList(List.of(1L))
            .newValue("1")
            .build();
    }

    public static RequestToChangeOrdersDataDto getRequestToAddAdminCommentForOrder() {
        return RequestToChangeOrdersDataDto.builder()
            .columnName("adminComment")
            .orderIdsList(List.of(1L))
            .newValue("Admin Comment")
            .build();
    }

    public static List<Bot> botList() {
        List<Bot> botList = new ArrayList<>();
        botList.add(new Bot()
            .setType("TELEGRAM")
            .setLink("https://telegram.me/ubs_test_bot?start=87df9ad5-6393-441f-8423-8b2e770b01a8"));
        botList.add(new Bot()
            .setType("VIBER")
            .setLink("viber://pa?chatURI=ubstestbot1&context=87df9ad5-6393-441f-8423-8b2e770b01a8"));
        return botList;
    }

    public static UpdateAllOrderPageDto updateAllOrderPageDto() {
        return UpdateAllOrderPageDto.builder()
            .orderId(List.of(1L))
            .exportDetailsDto(ExportDetailsDtoUpdate
                .builder()
                .dateExport("1997-12-04T15:40:24")
                .timeDeliveryFrom("1997-12-04T15:40:24")
                .timeDeliveryTo("1990-12-11T19:30:30")
                .receivingStationId(1L)
                .build())
            .updateResponsibleEmployeeDto(List.of(UpdateResponsibleEmployeeDto.builder()
                .positionId(2L)
                .employeeId(2L)
                .build()))
            .build();
    }

    public static Order getOrder2() {
        return Order.builder()
            .id(1L)
            .payment(Lists.newArrayList(Payment.builder()
                .paymentId("1")
                .amount(20000L)
                .currency("UAH")
                .settlementDate("20.02.1990")
                .comment("avb")
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .ubsUser(UBSuser.builder()
                .firstName("oleh")
                .lastName("ivanov")
                .email("mail@mail.ua")
                .id(1L)
                .phoneNumber("067894522")
                .orderAddress(OrderAddress.builder()
                    .id(1L)
                    .city("Lviv")
                    .street("Levaya")
                    .district("frankivskiy")
                    .entranceNumber("5")
                    .addressComment("near mall")
                    .houseCorpus("1")
                    .houseNumber("4")
                    .coordinates(Coordinates.builder()
                        .latitude(49.83)
                        .longitude(23.88)
                        .build())
                    .build())
                .build())
            .user(User.builder().id(1L).recipientName("Yuriy").recipientSurname("Gerasum").build())
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .adminComment("Admin")
            .cancellationComment("cancelled")
            .receivingStation(ReceivingStation.builder()
                .id(1L)
                .name("Саперно-Слобідська")
                .build())
            .orderPaymentStatus(OrderPaymentStatus.PAID)
            .cancellationReason(CancellationReason.OUT_OF_CITY)
            .imageReasonNotTakingBags(List.of("foto"))
            .orderPaymentStatus(OrderPaymentStatus.UNPAID)
            .additionalOrders(new HashSet<>())
            .sumTotalAmountWithoutDiscounts(20000L)
            .build();
    }

    public static AddBonusesToUserDto getAddBonusesToUserDto() {
        return AddBonusesToUserDto.builder()
            .paymentId("5")
            .receiptLink("test")
            .settlementdate("test")
            .amount(1000L)
            .build();
    }

    public static GetTariffsInfoDto getAllTariffsInfoDto() {
        Location location = getLocation();
        return GetTariffsInfoDto.builder()
            .cardId(1L)
            .locationInfoDtos(List.of(LocationsDtos.builder()
                .locationId(location.getId())
                .nameEn(location.getNameEn())
                .nameUk(location.getNameUk()).build()))
            .courierDto(getCourierDto())
            .createdAt(LocalDate.of(22, 2, 12))
            .creator(EmployeeNameDto.builder()
                .email("sss@gmail.com").build())
            .build();
    }

    public static GetTariffLimitsDto getGetTariffLimitsDto() {
        return GetTariffLimitsDto.builder()
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .max(20L)
            .min(2L)
            .build();
    }

    public static TariffsInfo getTariffsInfo() {
        return TariffsInfo.builder()
            .id(1L)
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .max(20L)
            .min(2L)
            .tariffLocations(Set.of(TariffLocation.builder()
                .location(getLocation())
                .build()))
            .receivingStationList(Set.of(getReceivingStation()))
            .courier(getCourier())
            .service(getService())
            .build();
    }

    public static TariffsInfo getTariffsInfoActive() {
        return TariffsInfo.builder()
            .id(1L)
            .tariffStatus(TariffStatus.ACTIVE)
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .max(20L)
            .min(2L)
            .tariffLocations(Set.of(TariffLocation.builder()
                .location(getLocation())
                .build()))
            .receivingStationList(Set.of(getReceivingStation()))
            .courier(getCourier())
            .service(getService())
            .bags(getBag4list())
            .build();
    }

    public static TariffsInfo getTariffsInfoDeactivated() {
        return TariffsInfo.builder()
            .id(1L)
            .tariffStatus(TariffStatus.DEACTIVATED)
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .max(20L)
            .min(2L)
            .tariffLocations(Set.of(TariffLocation.builder()
                .location(getLocation())
                .build()))
            .receivingStationList(Set.of(getReceivingStation()))
            .courier(getCourier())
            .service(getService())
            .bags(getBag4list())
            .build();
    }

    public static TariffsInfo getTariffWithUknownRegionInfo() {
        return TariffsInfo.builder()
            .id(1L)
            .courier(ModelUtils.getCourier())
            .courierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
            .tariffLocations(Set.of(TariffLocation.builder()
                .tariffsInfo(ModelUtils.getTariffInfoWithLimitOfBags())
                .locationStatus(LocationStatus.ACTIVE)
                .location(Location.builder().id(1L)
                    .locationStatus(LocationStatus.ACTIVE)
                    .region(ModelUtils.getUnknownRegion())
                    .nameUk("Київ")
                    .nameEn("Kyiv")
                    .coordinates(ModelUtils.getCoordinates())
                    .build())
                .build()))
            .tariffStatus(TariffStatus.ACTIVE)
            .creator(ModelUtils.getEmployee())
            .createdAt(LocalDate.of(2022, 10, 20))
            .max(6000L)
            .min(500L)
            .orders(Collections.emptyList())
            .receivingStationList(Set.of(ReceivingStation.builder()
                .id(1L)
                .name("Петрівка")
                .createdBy(ModelUtils.createEmployee())
                .build()))
            .build();
    }

    public static TariffsInfo getTariffsInfoWithStatusNew() {
        return TariffsInfo.builder()
            .id(1L)
            .courierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
            .tariffLocations(Set.of(TariffLocation.builder()
                .tariffsInfo(ModelUtils.getTariffInfoWithLimitOfBags())
                .location(Location.builder().id(1L)
                    .region(ModelUtils.getRegion())
                    .nameUk("Київ")
                    .nameEn("Kyiv")
                    .coordinates(ModelUtils.getCoordinates())
                    .build())
                .build()))
            .tariffStatus(TariffStatus.NEW)
            .creator(ModelUtils.getEmployee())
            .createdAt(LocalDate.of(2022, 10, 20))
            .orders(Collections.emptyList())
            .receivingStationList(Set.of(ReceivingStation.builder()
                .id(1L)
                .name("Петрівка")
                .createdBy(ModelUtils.createEmployee())
                .build()))
            .build();
    }

    public static TariffsInfo getTariffInfoWithLimitOfBags() {
        return TariffsInfo.builder()
            .id(1L)
            .courier(ModelUtils.getCourier())
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .tariffLocations(Set.of(TariffLocation.builder()
                .location(Location.builder().id(1L)
                    .region(ModelUtils.getRegion())
                    .nameUk("Київ")
                    .nameEn("Kyiv")
                    .coordinates(ModelUtils.getCoordinates())
                    .build())
                .build()))
            .tariffStatus(TariffStatus.ACTIVE)
            .creator(ModelUtils.getEmployee())
            .createdAt(LocalDate.of(2022, 10, 20))
            .max(100L)
            .min(5L)
            .orders(List.of(ModelUtils.getOrder()))
            .build();
    }

    public static TariffsInfo getTariffInfoWithLimitOfBagsAndMaxLessThanCountOfBigBag() {
        return TariffsInfo.builder()
            .id(1L)
            .courier(ModelUtils.getCourier())
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .tariffLocations(Set.of(TariffLocation.builder()
                .location(Location.builder().id(1L)
                    .region(ModelUtils.getRegion())
                    .nameUk("Київ")
                    .nameEn("Kyiv")
                    .coordinates(ModelUtils.getCoordinates())
                    .build())
                .build()))
            .tariffStatus(TariffStatus.ACTIVE)
            .creator(ModelUtils.getEmployee())
            .createdAt(LocalDate.of(2022, 10, 20))
            .max(10L)
            .min(5L)
            .orders(List.of(ModelUtils.getOrder()))
            .build();
    }

    public static TariffsInfo getTariffInfo() {
        return TariffsInfo.builder()
            .id(1L)
            .courier(ModelUtils.getCourier())
            .courierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
            .tariffLocations(Set.of(TariffLocation.builder()
                .tariffsInfo(ModelUtils.getTariffInfoWithLimitOfBags())
                .locationStatus(LocationStatus.ACTIVE)
                .location(Location.builder().id(1L)
                    .locationStatus(LocationStatus.ACTIVE)
                    .region(ModelUtils.getRegion())
                    .nameUk("Київ")
                    .nameEn("Kyiv")
                    .coordinates(ModelUtils.getCoordinates())
                    .build())
                .build()))
            .tariffStatus(TariffStatus.ACTIVE)
            .creator(ModelUtils.getEmployee())
            .createdAt(LocalDate.of(2022, 10, 20))
            .max(6000L)
            .min(500L)
            .orders(Collections.emptyList())
            .receivingStationList(Set.of(ReceivingStation.builder()
                .id(1L)
                .name("Петрівка")
                .createdBy(ModelUtils.createEmployee())
                .build()))
            .build();
    }

    public static AddNewTariffDto getAddNewTariffDto() {
        return AddNewTariffDto.builder()
            .courierId(1L)
            .locationIdList(List.of(1L))
            .receivingStationsIdList(List.of(1L))
            .regionId(1L)
            .build();
    }

    public static EditTariffDto getEditTariffDto() {
        return EditTariffDto.builder()
            .locationIds(List.of(1L))
            .receivingStationIds(List.of(1L))
            .courierId(1L)
            .build();
    }

    public static EditTariffDto getEditTariffDtoWithoutCourier() {
        return EditTariffDto.builder()
            .locationIds(List.of(1L))
            .receivingStationIds(List.of(1L))
            .build();
    }

    public static EditTariffDto getEditTariffDtoWith2Locations() {
        return EditTariffDto.builder()
            .locationIds(List.of(1L, 2L))
            .receivingStationIds(List.of(1L))
            .build();
    }

    public static List<GeocodingResult> getGeocodingResultWithKyivRegion() {
        List<GeocodingResult> geocodingResults = new ArrayList<>();

        GeocodingResult geocodingResult1 = new GeocodingResult();

        Geometry geometry = new Geometry();
        geometry.location = new LatLng(50.5555555d, 50.5555555d);

        AddressComponent locality = new AddressComponent();
        locality.longName = "fake street";
        locality.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        AddressComponent streetNumber = new AddressComponent();
        streetNumber.longName = "13";
        streetNumber.types = new AddressComponentType[] {AddressComponentType.STREET_NUMBER};

        AddressComponent region = new AddressComponent();
        region.longName = "Kyiv";
        region.types = new AddressComponentType[] {AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1};

        AddressComponent sublocality = new AddressComponent();
        sublocality.longName = "fake district";
        sublocality.types = new AddressComponentType[] {AddressComponentType.SUBLOCALITY};

        AddressComponent route = new AddressComponent();
        route.longName = "fake street name";
        route.types = new AddressComponentType[] {AddressComponentType.ROUTE};

        geocodingResult1.addressComponents = new AddressComponent[] {
            locality,
            streetNumber,
            region,
            sublocality,
            route
        };

        geocodingResult1.formattedAddress = "fake address";
        geocodingResult1.geometry = geometry;

        GeocodingResult geocodingResult2 = new GeocodingResult();

        AddressComponent locality2 = new AddressComponent();
        locality2.longName = "fake street";
        locality2.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        AddressComponent streetNumber2 = new AddressComponent();
        streetNumber2.longName = "13";
        streetNumber2.types = new AddressComponentType[] {AddressComponentType.STREET_NUMBER};

        AddressComponent region2 = new AddressComponent();
        region2.longName = "місто Київ";
        region2.types = new AddressComponentType[] {AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1};

        AddressComponent sublocality2 = new AddressComponent();
        sublocality2.longName = "fake district";
        sublocality2.types = new AddressComponentType[] {AddressComponentType.SUBLOCALITY};

        AddressComponent route2 = new AddressComponent();
        route2.longName = "fake street name";
        route2.types = new AddressComponentType[] {AddressComponentType.ROUTE};

        geocodingResult2.addressComponents = new AddressComponent[] {
            locality2,
            streetNumber2,
            region2,
            sublocality2,
            route2
        };

        geocodingResult2.formattedAddress = "fake address 2";
        geocodingResult2.geometry = geometry;

        geocodingResults.add(geocodingResult1);
        geocodingResults.add(geocodingResult2);
        return geocodingResults;
    }

    public static List<GeocodingResult> getGeocodingResult() {
        List<GeocodingResult> geocodingResults = new ArrayList<>();

        GeocodingResult geocodingResult1 = new GeocodingResult();

        Geometry geometry = new Geometry();
        geometry.location = new LatLng(50.5555555d, 50.5555555d);

        AddressComponent locality = new AddressComponent();
        locality.longName = "fake street";
        locality.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        AddressComponent streetNumber = new AddressComponent();
        streetNumber.longName = "13";
        streetNumber.types = new AddressComponentType[] {AddressComponentType.STREET_NUMBER};

        AddressComponent region = new AddressComponent();
        region.longName = "fake region";
        region.types = new AddressComponentType[] {AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1};

        AddressComponent sublocality = new AddressComponent();
        sublocality.longName = "fake district";
        sublocality.types = new AddressComponentType[] {AddressComponentType.SUBLOCALITY};

        AddressComponent route = new AddressComponent();
        route.longName = "fake street name";
        route.types = new AddressComponentType[] {AddressComponentType.ROUTE};

        geocodingResult1.addressComponents = new AddressComponent[] {
            locality,
            streetNumber,
            region,
            sublocality,
            route
        };

        geocodingResult1.formattedAddress = "fake address";
        geocodingResult1.geometry = geometry;

        GeocodingResult geocodingResult2 = new GeocodingResult();

        AddressComponent locality2 = new AddressComponent();
        locality2.longName = "fake street";
        locality2.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        AddressComponent streetNumber2 = new AddressComponent();
        streetNumber2.longName = "13";
        streetNumber2.types = new AddressComponentType[] {AddressComponentType.STREET_NUMBER};

        AddressComponent region2 = new AddressComponent();
        region2.longName = "fake region";
        region2.types = new AddressComponentType[] {AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1};

        AddressComponent sublocality2 = new AddressComponent();
        sublocality2.longName = "fake district";
        sublocality2.types = new AddressComponentType[] {AddressComponentType.SUBLOCALITY};

        AddressComponent route2 = new AddressComponent();
        route2.longName = "fake street name";
        route2.types = new AddressComponentType[] {AddressComponentType.ROUTE};

        geocodingResult2.addressComponents = new AddressComponent[] {
            locality2,
            streetNumber2,
            region2,
            sublocality2,
            route2
        };

        geocodingResult2.formattedAddress = "fake address 2";
        geocodingResult2.geometry = geometry;

        geocodingResults.add(geocodingResult1);
        geocodingResults.add(geocodingResult2);

        return geocodingResults;
    }

    public static CreateAddressRequestDto getAddressRequestDto() {
        return CreateAddressRequestDto.builder()
            .addressComment("fdsfs")
            .searchAddress("fake street name, 13, fake street, 02000")
            .district("fdsfds")
            .districtEn("dsadsad")
            .region("regdsad")
            .regionEn("regdsaden")
            .houseNumber("1")
            .houseCorpus("2")
            .entranceNumber("3")
            .placeId("place_id")
            .build();
    }

    public static CreateAddressRequestDto getAddressRequestToSaveDto() {
        return CreateAddressRequestDto.builder()
            .addressComment("fdsfs2")
            .searchAddress("fake street name2, 132, fake street2, 020002")
            .district("Район")
            .districtEn("District")
            .region("regdsad2")
            .regionEn("regdsaden2")
            .houseNumber("12")
            .houseCorpus("22")
            .entranceNumber("32")
            .placeId("place_id")
            .build();
    }

    public static CreateAddressRequestDto getAddressRequestToSaveDto_WithoutDistricts() {
        return CreateAddressRequestDto.builder()
            .addressComment("fdsfs2")
            .searchAddress("fake street name2, 132, fake street2, 020002")
            .region("regdsad2")
            .regionEn("regdsaden2")
            .houseNumber("12")
            .houseCorpus("22")
            .entranceNumber("32")
            .placeId("place_id")
            .build();
    }

    public static CreateAddressRequestDto getAddressRequestWithEmptyPlaceIdDto() {
        return CreateAddressRequestDto.builder()
            .addressComment("fdsfs")
            .searchAddress("fake street name, 13, fake street, 02000")
            .district("fdsfds")
            .districtEn("dsadsad")
            .region("regdsad")
            .regionEn("regdsaden")
            .houseNumber("1")
            .houseCorpus("2")
            .entranceNumber("3")
            .placeId("")
            .street("street")
            .streetEn("streetEn")
            .city("city")
            .cityEn("cityEn")
            .build();
    }

    public static CreateAddressRequestDto getAddressRequestWithEmptyPlaceIdToSaveDto() {
        return CreateAddressRequestDto.builder()
            .addressComment("fdsfs1")
            .searchAddress("fake street name, 13, fake street, 02000")
            .district("fdsfds1")
            .districtEn("dsadsad1")
            .region("regdsad1")
            .regionEn("regdsaden1")
            .houseNumber("11")
            .houseCorpus("21")
            .entranceNumber("31")
            .placeId("")
            .street("street1")
            .streetEn("streetEn1")
            .city("city1")
            .cityEn("cityEn1")
            .build();
    }

    public static CreateAddressRequestDto getAddressWithKyivRegionRequestDto() {
        return CreateAddressRequestDto.builder()
            .addressComment("fdsfs")
            .searchAddress("fake street name, 13, fake street, 02000")
            .district("fdsfds")
            .districtEn("dsadsad")
            .regionEn(KYIV_REGION_EN)
            .region(KYIV_REGION_UA)
            .houseNumber("1")
            .houseCorpus("2")
            .entranceNumber("3")
            .placeId("place_id")
            .build();
    }

    public static CreateAddressRequestDto getAddressWithKyivRegionToSaveRequestDto() {
        return CreateAddressRequestDto.builder()
            .addressComment("fdsfs1")
            .searchAddress("fake street name, 13, fake street, 02000")
            .district("fdsfds1")
            .districtEn("dsadsad1")
            .regionEn(KYIV_REGION_EN)
            .region(KYIV_REGION_UA)
            .houseNumber("11")
            .houseCorpus("21")
            .entranceNumber("31")
            .placeId("place_id")
            .build();
    }

    public static OrderAddressDtoRequest getTestOrderAddressDtoRequest() {
        return OrderAddressDtoRequest.builder()
            .id(0L)
            .region("fake region")
            .searchAddress("fake street name, 13, fake street, 02000")
            .city("fake street")
            .district("Район")
            .districtEn("District")
            .entranceNumber("1")
            .houseNumber("13")
            .houseCorpus("1")
            .street("fake street name")
            .streetEn("fake street name")
            .coordinates(new Coordinates(50.5555555d, 50.5555555d))
            .cityEn("fake street")
            .regionEn("fake region")
            .placeId("place_id")
            .build();
    }

    public static OrderAddressDtoRequest getTestOrderAddressDtoRequestWithNullPlaceId() {
        return OrderAddressDtoRequest.builder()
            .id(0L)
            .region("fake region")
            .searchAddress("fake street name, 13, fake street, 02000")
            .city("fake street")
            .district("Район")
            .districtEn("District")
            .entranceNumber("1")
            .houseNumber("13")
            .houseCorpus("1")
            .street("fake street name")
            .streetEn("fake street name")
            .coordinates(new Coordinates(50.5555555d, 50.5555555d))
            .cityEn("fake street")
            .regionEn("fake region")
            .build();
    }

    public static OrderAddressDtoRequest getTestOrderAddressLocationDto() {
        return getTestOrderAddressLocationDto(true);
    }

    public static OrderAddressDtoRequest getTestOrderAddressLocationDto(boolean withDistrictRegionHouse) {
        return OrderAddressDtoRequest.builder()
            .id(0L)
            .region(withDistrictRegionHouse ? "fake region" : null)
            .city("fake street")
            .district(withDistrictRegionHouse ? "fake district" : null)
            .entranceNumber("1")
            .houseNumber("13")
            .houseCorpus("1")
            .street("fake street name")
            .streetEn("fake street name")
            .coordinates(new Coordinates(50.5555555d, 50.5555555d))
            .cityEn("fake street")
            .districtEn(withDistrictRegionHouse ? "fake district" : null)
            .regionEn(withDistrictRegionHouse ? "fake region" : null)
            .placeId("place_id")
            .build();
    }

    public static User getUserForCreate() {
        return getUserForCreate(AddressStatus.IN_ORDER);
    }

    public static User getUserForCreate(AddressStatus addressStatus) {
        return User.builder()
            .id(1L)
            .addresses(List.of(Address.builder()
                .id(7L)
                .city("fake street")
                .cityEn("fake street")
                .district("Район")
                .districtEn("District")
                .region("fake region")
                .regionEn("fake region")
                .street("fake street name")
                .streetEn("fake street name")
                .houseNumber("13")
                .addressStatus(addressStatus)
                .coordinates(new Coordinates(50.5555555, 50.5555555)).build()))
            .recipientEmail("someUser@gmail.com")
            .recipientPhone("962473289")
            .recipientSurname("Ivanov")
            .uuid("87df9ad5-6393-441f-8423-8b2e770b01a8")
            .recipientName("Taras")
            .uuid("uuid")
            .ubsUsers(getUbsUsers())
            .currentPoints(100)
            .build();
    }

    public static OrderWithAddressesResponseDto getAddressDtoResponse() {
        return OrderWithAddressesResponseDto.builder()
            .addressList(List.of(
                AddressDto.builder()
                    .id(1L)
                    .city("City")
                    .district("Район")
                    .districtEn("District")
                    .entranceNumber("7a")
                    .houseCorpus("2")
                    .houseNumber("25")
                    .street("Street")
                    .coordinates(Coordinates.builder()
                        .latitude(50.4459068)
                        .longitude(30.4477005)
                        .build())
                    .actual(false)
                    .build()))
            .build();
    }

    public static OrderWithAddressesResponseDto getOrderWithAddressesResponseDto() {
        return OrderWithAddressesResponseDto.builder()
            .addressList(List.of(
                AddressDto.builder()
                    .id(1L)
                    .city("City")
                    .cityEn("CityEn")
                    .district("Distinct")
                    .entranceNumber("7a")
                    .houseCorpus("2")
                    .houseNumber("25")
                    .street("Street")
                    .streetEn("StreetEn")
                    .coordinates(Coordinates.builder()
                        .latitude(0.0)
                        .longitude(0.0)
                        .build())
                    .actual(false)
                    .build()))
            .build();
    }

    public static TariffsForLocationDto getTariffsForLocationDto() {
        return TariffsForLocationDto.builder().build();
    }

    public static CertificateDto createCertificateDto() {
        return CertificateDto.builder()
            .points(300)
            .dateOfUse(LocalDate.now())
            .expirationDate(LocalDate.now())
            .code("200")
            .certificateStatus("ACTIVE")
            .build();
    }

    public static CourierTranslationDto getCourierTranslationDto(Long id) {
        return CourierTranslationDto.builder()
            .id(id)
            .nameUk("Тест")
            .nameEn("Test")
            .build();
    }

    public static List<Address> getMaximumAmountOfAddresses() {
        return List.of(new Address(), new Address(), new Address(), new Address());
    }

    public static List<String> getAllAuthorities() {
        return Collections.singletonList("SEE_CLIENTS_PAGE");
    }

    public static UserEmployeeAuthorityDto getUserEmployeeAuthorityDto() {
        return UserEmployeeAuthorityDto.builder()
            .employeeEmail("test@mail.com")
            .authorities(getAllAuthorities())
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithEmptyParams() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.empty())
            .citiesIds(Optional.empty())
            .stationsIds(Optional.empty())
            .courierId(Optional.empty())
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithRegion() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.of(List.of(1L)))
            .citiesIds(Optional.empty())
            .stationsIds(Optional.empty())
            .courierId(Optional.empty())
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithRegionAndCities() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.of(List.of(1L)))
            .citiesIds(Optional.of(List.of(1L, 11L)))
            .stationsIds(Optional.empty())
            .courierId(Optional.empty())
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithCourier() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.empty())
            .citiesIds(Optional.empty())
            .stationsIds(Optional.empty())
            .courierId(Optional.of(1L))
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithReceivingStations() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.empty())
            .citiesIds(Optional.empty())
            .stationsIds(Optional.of(List.of(1L, 12L)))
            .courierId(Optional.empty())
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithCourierAndReceivingStations() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.empty())
            .citiesIds(Optional.empty())
            .stationsIds(Optional.of(List.of(1L, 12L)))
            .courierId(Optional.of(1L))
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithCourierAndRegion() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.of(List.of(1L)))
            .citiesIds(Optional.empty())
            .stationsIds(Optional.empty())
            .courierId(Optional.of(1L))
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithRegionAndCityAndStation() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.of(List.of(1L)))
            .citiesIds(Optional.of(List.of(1L, 11L)))
            .stationsIds(Optional.of(List.of(1L, 12L)))
            .courierId(Optional.empty())
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithAllParams() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.of(List.of(1L)))
            .citiesIds(Optional.of(List.of(1L, 11L)))
            .stationsIds(Optional.of(List.of(1L, 12L)))
            .courierId(Optional.of(1L))
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithStatusActive() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.of(List.of(1L)))
            .citiesIds(Optional.of(List.of(1L)))
            .stationsIds(Optional.of(List.of(1L)))
            .courierId(Optional.of(1L))
            .activationStatus("Active")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithRegionAndReceivingStations() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.of(List.of(1L)))
            .citiesIds(Optional.empty())
            .stationsIds(Optional.of(List.of(1L, 12L)))
            .courierId(Optional.empty())
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithCourierAndRegionAndCities() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.of(List.of(1L)))
            .citiesIds(Optional.of(List.of(1L, 11L)))
            .stationsIds(Optional.empty())
            .courierId(Optional.of(1L))
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithCourierAndRegionAndReceivingStations() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.of(List.of(1L)))
            .citiesIds(Optional.empty())
            .stationsIds(Optional.of(List.of(1L, 12L)))
            .courierId(Optional.of(1L))
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithCities() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.empty())
            .citiesIds(Optional.of(List.of(1L, 11L)))
            .stationsIds(Optional.empty())
            .courierId(Optional.empty())
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithCitiesAndCourier() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.empty())
            .citiesIds(Optional.of(List.of(1L, 11L)))
            .stationsIds(Optional.empty())
            .courierId(Optional.of(1L))
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithCitiesAndReceivingStations() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.empty())
            .citiesIds(Optional.of(List.of(1L, 11L)))
            .stationsIds(Optional.of(List.of(1L, 12L)))
            .courierId(Optional.empty())
            .activationStatus("Deactivated")
            .build();
    }

    public static DetailsOfDeactivateTariffsDto getDetailsOfDeactivateTariffsDtoWithCitiesAndCourierAndReceivingStations() {
        return DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(Optional.empty())
            .citiesIds(Optional.of(List.of(1L, 11L)))
            .stationsIds(Optional.of(List.of(1L, 12L)))
            .courierId(Optional.of(1L))
            .activationStatus("Deactivated")
            .build();
    }

    public static BagLimitDto getBagLimitIncludedDtoTrue() {
        return BagLimitDto.builder()
            .id(1)
            .limitIncluded(true)
            .build();
    }

    public static BagLimitDto getBagLimitIncludedDtoFalse() {
        return BagLimitDto.builder()
            .id(1)
            .limitIncluded(false)
            .build();
    }

    public static SetTariffLimitsDto setTariffLimitsWithAmountOfBags() {
        return SetTariffLimitsDto.builder()
            .min(1L)
            .max(2L)
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .bagLimitDtoList(List.of(getBagLimitIncludedDtoTrue()))
            .build();
    }

    public static SetTariffLimitsDto setTariffLimitsWithNullAllTariffParamsAndFalseBagLimit() {
        return SetTariffLimitsDto.builder()
            .min(null)
            .max(null)
            .courierLimit(null)
            .bagLimitDtoList(List.of(getBagLimitIncludedDtoFalse()))
            .build();
    }

    public static SetTariffLimitsDto setTariffLimitsWithNullMinAndMaxAndFalseBagLimit() {
        return SetTariffLimitsDto.builder()
            .min(null)
            .max(null)
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .bagLimitDtoList(List.of(getBagLimitIncludedDtoFalse()))
            .build();
    }

    public static SetTariffLimitsDto setTariffsLimitWithSameMinAndMaxValue() {
        return SetTariffLimitsDto.builder()
            .min(2L)
            .max(2L)
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .bagLimitDtoList(List.of(getBagLimitIncludedDtoTrue()))
            .build();
    }

    public static SetTariffLimitsDto setTariffLimitsWithPriceOfOrder() {
        return SetTariffLimitsDto.builder()
            .min(100L)
            .max(200L)
            .courierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
            .bagLimitDtoList(List.of(getBagLimitIncludedDtoTrue()))
            .build();
    }

    public static SetTariffLimitsDto setTariffLimitsWithAmountOfBigBagsWhereMaxValueIsGreater() {
        return SetTariffLimitsDto.builder()
            .min(2L)
            .max(1L)
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .bagLimitDtoList(List.of(getBagLimitIncludedDtoTrue()))
            .build();
    }

    public static SetTariffLimitsDto setTariffLimitsWithPriceOfOrderWhereMaxValueIsGreater() {
        return SetTariffLimitsDto.builder()
            .min(200L)
            .max(100L)
            .courierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
            .bagLimitDtoList(List.of(getBagLimitIncludedDtoTrue()))
            .build();
    }

    public static UserPointsAndAllBagsDto getUserPointsAndAllBagsDto() {
        return new UserPointsAndAllBagsDto(
            List.of(
                BagTranslationDto.builder()
                    .id(1)
                    .name("name")
                    .capacity(20)
                    .price(150.)
                    .nameEng("nameEng")
                    .limitedIncluded(false)
                    .build()),
            100);
    }

    public static Order getOrderExportDetailsWithExportDate() {
        return Order.builder()
            .id(1L)
            .dateOfExport(LocalDate.of(2023, 2, 8))
            .user(User.builder().id(1L).recipientName("Admin").recipientSurname("Ubs").build())
            .build();
    }

    public static Order getOrderExportDetailsWithExportDateDeliverFrom() {
        return Order.builder()
            .id(1L)
            .dateOfExport(LocalDate.of(2023, 2, 8))
            .deliverFrom(LocalDateTime.of(2023, 2, 8, 15, 0))
            .user(User.builder().id(1L).recipientName("Admin").recipientSurname("Ubs").build())
            .build();
    }

    public static Order getOrderExportDetailsWithExportDateDeliverFromTo() {
        return Order.builder()
            .id(1L)
            .dateOfExport(LocalDate.of(2023, 2, 8))
            .deliverFrom(LocalDateTime.of(2023, 2, 8, 15, 0))
            .deliverTo(LocalDateTime.of(2023, 2, 8, 16, 30))
            .user(User.builder().id(1L).recipientName("Admin").recipientSurname("Ubs").build())
            .build();
    }

    public static Order getOrderExportDetailsWithDeliverFromTo() {
        Order order = getOrderExportDetailsWithNullValues();
        order.setDeliverTo(LocalDateTime.of(2023, 2, 8, 16, 30));
        order.setDeliverFrom(LocalDateTime.of(2023, 2, 8, 15, 0));
        return order;
    }

    public static UserProfileCreateDto getUserProfileCreateDto() {
        return UserProfileCreateDto.builder()
            .name("UbsProfile")
            .email("ubsuser@mail.com")
            .uuid("f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
            .build();
    }

    public static Order getTestNotTakenOrderReason() {
        return Order.builder()
            .id(1L)
            .orderStatus(OrderStatus.NOT_TAKEN_OUT)
            .reasonNotTakingBagDescription("Some description")
            .imageReasonNotTakingBags(List.of("image1", "image2"))
            .build();
    }

    public static NotTakenOrderReasonDto getNotTakenOrderReasonDto() {
        return NotTakenOrderReasonDto.builder()
            .description("Some description")
            .images(List.of("image1", "image2"))
            .build();
    }

    public static TableColumnWidthForEmployee getTestTableColumnWidth() {
        return TableColumnWidthForEmployee.builder()
            .employee(getEmployee())
            .address(50)
            .amountDue(60)
            .bagsAmount(150)
            .city(200)
            .build();
    }

    public static ColumnWidthDto getTestColumnWidthDto() {
        return ColumnWidthDto.builder()
            .address(100)
            .amountDue(20)
            .bagsAmount(500)
            .city(320)
            .clientPhone(340)
            .commentForOrderByClient(600)
            .build();
    }

    public static PositionAuthoritiesDto getPositionAuthoritiesDto() {
        return PositionAuthoritiesDto.builder()
            .positionId(List.of(1L))
            .authorities(List.of("Auth"))
            .build();
    }

    public static PositionWithTranslateDto getPositionWithTranslateDto(Long id) {
        Map<String, String> nameTranslations = new HashMap<>();
        nameTranslations.put("ua", "Водій");
        nameTranslations.put("en", "Driver");

        return PositionWithTranslateDto.builder()
            .id(id)
            .name(nameTranslations)
            .build();
    }

    public static Refund getRefund(Long id) {
        return Refund.builder().orderId(id).build();
    }

    public static UserPointsAndAllBagsDto getUserPointsAndAllBagsDtoWithQuantity() {
        return new UserPointsAndAllBagsDto(
            List.of(
                BagTranslationDto.builder()
                    .id(1)
                    .name("name")
                    .capacity(20)
                    .price(170.)
                    .nameEng("nameEng")
                    .limitedIncluded(false)
                    .quantity(2)
                    .build()),
            100);
    }

    public static Order getNotifyInternallyFormedOrder() {
        return Order.builder()
            .id(1L)
            .user(User.builder().id(1L).build())
            .deliverFrom(LocalDateTime.now(fixedClock).minusHours(3))
            .deliverTo(LocalDateTime.now(fixedClock).minusHours(2))
            .orderStatus(OrderStatus.ADJUSTMENT)
            .orderPaymentStatus(OrderPaymentStatus.PAID)
            .orderDate(LocalDateTime.now(fixedClock))
            .build();
    }

    public static NotificationTemplate getCustomNotificationTemplate() {
        return NotificationTemplate.builder()
            .id(1L)
            .isScheduleUpdateForbidden(false)
            .title("Заголовок")
            .titleEng("Title")
            .schedule("0 2 * * * *")
            .trigger(NotificationTrigger.CUSTOM)
            .time(NotificationTime.IMMEDIATELY)
            .userCategory(UserCategory.USERS_WITH_ORDERS_MADE_LESS_THAN_3_MONTHS)
            .build();
    }

    public static Employee getAdminEmployee() {
        return Employee.builder()
            .id(1L)
            .firstName("Петро")
            .lastName("Петренко")
            .phoneNumber("+380935577455")
            .email("test@gmail.com")
            .uuid("Test")
            .employeeStatus(EmployeeStatus.ACTIVE)
            .employeePosition(Set.of(Position.builder()
                .id(7L)
                .name("Адмін")
                .nameEn("Admin")
                .build()))
            .tariffs(List.of(TariffsInfo.builder()
                .id(1L)
                .service(new Service())
                .build()))
            .imagePath("path")
            .build();
    }

    public static PaymentResponseDto getPaymentResponseDto() {
        return PaymentResponseDto.builder()
            .merchantAccount("TestMerchant123")
            .orderReference("1_001")
            .merchantSignature("signature123")
            .amount("150")
            .currency("USD")
            .authCode("AUTH12345")
            .email("testuser@example.com")
            .phone("+1234567890")
            .createdDate("2024-07-23T12:00:00")
            .processingDate("2024-07-23T12:05:00")
            .cardPan("**** **** **** 1234")
            .cardType("Visa")
            .issuerBankCountry("USA")
            .issuerBankName("Test Bank")
            .recToken("rectoken123")
            .transactionStatus("Approved")
            .reason("None")
            .reasonCode("0")
            .fee("2.50")
            .paymentSystem("TestPaymentSystem")
            .acquirerBankName("Test Acquirer")
            .build();
    }

    public static Certificate getCertificate() {
        return Certificate.builder()
            .certificateStatus(CertificateStatus.ACTIVE)
            .points(100)
            .code("7777-7777")
            .creationDate(LocalDate.now().plusMonths(1))
            .build();
    }

    public static UserAgreement getUserAgreement() {
        return UserAgreement.builder()
            .id(1L)
            .textUa("Текст угоди українською")
            .textEn("Agreement text in English")
            .createdAt(LocalDateTime.now().minusDays(1))
            .author(getEmployee())
            .build();
    }

    public static UserAgreementDto getUserAgreementDto() {
        return UserAgreementDto.builder()
            .textUa("Текст угоди українською")
            .textEn("Agreement text in English")
            .build();
    }

    public static UserAgreementDetailDto getUserAgreementDetailDto() {
        return UserAgreementDetailDto.builder()
            .id(1L)
            .textUa("Текст угоди українською")
            .textEn("Agreement text in English")
            .authorEmail("test@gmail.com")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }
}
