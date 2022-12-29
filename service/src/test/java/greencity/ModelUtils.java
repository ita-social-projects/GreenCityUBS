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
import greencity.dto.LocationsDtos;
import greencity.dto.OptionForColumnDTO;
import greencity.dto.TariffsForLocationDto;
import greencity.dto.address.AddressDto;
import greencity.dto.address.AddressInfoDto;
import greencity.dto.bag.AdditionalBagInfoDto;
import greencity.dto.bag.BagDto;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.bag.BagInfoDto;
import greencity.dto.bag.BagMappingDto;
import greencity.dto.bag.BagOrderDto;
import greencity.dto.bag.BagTransDto;
import greencity.dto.bag.EditAmountOfBagDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.CourierUpdateDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.*;
import greencity.dto.location.AddLocationTranslationDto;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.location.LocationSummaryDto;
import greencity.dto.location.LocationToCityDto;
import greencity.dto.location.LocationTranslationDto;
import greencity.dto.location.LocationsDto;
import greencity.dto.location.RegionTranslationDto;
import greencity.dto.notification.BodyDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationScheduleDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateLocalizedDto;
import greencity.dto.notification.SenderInfoDto;
import greencity.dto.notification.TitleDto;
import greencity.dto.notification.UpdateNotificationTemplatesDto;
import greencity.dto.order.AdminCommentDto;
import greencity.dto.order.AssignEmployeesForOrderDto;
import greencity.dto.order.AssignForOrderEmployee;
import greencity.dto.order.BigOrderTableDTO;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.dto.order.DetailsOrderInfoDto;
import greencity.dto.order.EcoNumberDto;
import greencity.dto.order.EditPriceOfOrder;
import greencity.dto.order.EmployeeOrderPositionDTO;
import greencity.dto.order.ExportDetailsDto;
import greencity.dto.order.ExportDetailsDtoUpdate;
import greencity.dto.order.GroupedOrderDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderAddressDtoResponse;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderClientDto;
import greencity.dto.order.OrderDetailInfoDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderDetailStatusRequestDto;
import greencity.dto.order.OrderDto;
import greencity.dto.order.OrderFondyClientDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.order.ReadAddressByOrderDto;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.dto.order.SenderLocation;
import greencity.dto.order.UpdateAllOrderPageDto;
import greencity.dto.order.UpdateOrderDetailDto;
import greencity.dto.order.UpdateOrderPageAdminDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.OverpaymentInfoRequestDto;
import greencity.dto.payment.PaymentInfoDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseDtoLiqPay;
import greencity.dto.payment.PaymentTableInfoDto;
import greencity.dto.position.PositionDto;
import greencity.dto.service.AddServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.EditServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.service.ServiceTranslationDto;
import greencity.dto.tariff.EditTariffServiceDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.tariff.TariffTranslationDto;
import greencity.dto.tariff.TariffsInfoDto;
import greencity.dto.user.AddBonusesToUserDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.dto.violation.AddingViolationsToUserDto;
import greencity.dto.violation.UpdateViolationToUserDto;
import greencity.dto.violation.ViolationDetailInfoDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import greencity.entity.order.BigOrderTableViews;
import greencity.entity.order.Certificate;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Courier;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.Service;
import greencity.entity.order.ServiceTranslation;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.parameters.CustomTableView;
import greencity.entity.schedule.NotificationSchedule;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.AddressStatus;
import greencity.enums.CancellationReason;
import greencity.enums.CertificateStatus;
import greencity.enums.CourierLimit;
import greencity.enums.CourierStatus;
import greencity.enums.EmployeeStatus;
import greencity.enums.LocationStatus;
import greencity.enums.MinAmountOfBag;
import greencity.enums.NotificationType;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.util.Bot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static greencity.enums.NotificationReceiverType.SITE;
import static greencity.enums.ViolationLevel.MAJOR;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class ModelUtils {

    public static final Order TEST_ORDER = createOrder();
    public static final Address TEST_ADDRESS = createAddress2();
    public static final OrderAddressDtoResponse TEST_ORDER_ADDRESS_DTO_RESPONSE = createOrderAddressDtoResponse();
    public static final OrderAddressExportDetailsDtoUpdate TEST_ORDER_ADDRESS_DTO_UPDATE =
        createOrderAddressDtoUpdate();
    public static final List<Payment> TEST_PAYMENT_LIST = createPaymentList();
    public static final OrderDetailStatusDto ORDER_DETAIL_STATUS_DTO = createOrderDetailStatusDto();
    public static final List<BagMappingDto> TEST_BAG_MAPPING_DTO_LIST = createBagMappingDtoList();
    public static final BagTransDto TEST_BAG_TRANS_DTO = createBagTransDto();
    public static final BagTranslation TEST_BAG_TRANSLATION = createBagTranslation();
    public static final List<BagTranslation> TEST_BAG_TRANSLATION_LIST = singletonList(TEST_BAG_TRANSLATION);
    public static final Bag TEST_BAG = createBag();
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
    public static final NotificationParameter TEST_NOTIFICATION_PARAMETER = createNotificationParameter();
    public static final Violation TEST_VIOLATION = createTestViolation();
    public static final Pageable TEST_PAGEABLE_NOTIFICATION_TEMPLATE = PageRequest.of(0, 5, Sort.by("id").descending());
    public static final NotificationTemplate TEST_NOTIFICATION_TEMPLATE = createNotificationTemplate();
    public static final Pageable TEST_PAGEABLE = PageRequest.of(0, 5, Sort.by("notificationTime").descending());
    public static final NotificationShortDto TEST_NOTIFICATION_SHORT_DTO = createNotificationShortDto();
    public static final List<NotificationShortDto> TEST_NOTIFICATION_SHORT_DTO_LIST =
        List.of(TEST_NOTIFICATION_SHORT_DTO);
    public static final PageableDto<NotificationShortDto> TEST_DTO = createPageableDto();
    public static final Order TEST_ORDER_UPDATE_POSITION = createOrder2();
    public static final List<EmployeeOrderPosition> TEST_EMPLOYEE_ORDER_POSITION = createEmployeeOrderPositionList();
    public static final EmployeePositionDtoResponse TEST_EMPLOYEE_POSITION_DTO_RESPONSE =
        createEmployeePositionDtoResponse();
    public static final Position TEST_POSITION = createPosition();
    public static final Employee TEST_EMPLOYEE = createEmployee();
    public static final User TEST_USER = createUser();
    public static final List<UserNotification> TEST_USER_NOTIFICATION_LIST = createUserNotificationList();
    public static final Page<UserNotification> TEST_PAGE =
        new PageImpl<>(TEST_USER_NOTIFICATION_LIST, TEST_PAGEABLE, TEST_USER_NOTIFICATION_LIST.size());
    public static final AdditionalBagInfoDto TEST_ADDITIONAL_BAG_INFO_DTO = createAdditionalBagInfoDto();
    public static final List<AdditionalBagInfoDto> TEST_ADDITIONAL_BAG_INFO_DTO_LIST = createAdditionalBagInfoDtoList();
    public static final Map<String, Object> TEST_MAP_ADDITIONAL_BAG = createMap();
    public static final List<Map<String, Object>> TEST_MAP_ADDITIONAL_BAG_LIST =
        Collections.singletonList(TEST_MAP_ADDITIONAL_BAG);
    public static final NotificationDto TEST_NOTIFICATION_DTO = createNotificationDto();
    public static final UpdateOrderPageAdminDto UPDATE_ORDER_PAGE_ADMIN_DTO = updateOrderPageAdminDto();
    public static final Page<NotificationTemplate> TEST_NOTIFICATION_TEMPLATE_PAGE = getNotificationTemplatePageable();
    public static final NotificationTemplate TEST_TEMPLATE = getNotificationTemplate();
    public static final NotificationSchedule NOTIFICATION_SCHEDULE = new NotificationSchedule();
    public static final NotificationScheduleDto NOTIFICATION_SCHEDULE_DTO =
        new NotificationScheduleDto().setCron("0 0 18 * * ?");
    public static final NotificationTemplateDto TEST_NOTIFICATION_TEMPLATE_DTO = getNotificationTemplateDto();
    public static final List<NotificationTemplateDto> TEST_NOTIFICATION_TEMPLATE_LIST =
        List.of(TEST_NOTIFICATION_TEMPLATE_DTO);
    public static final PageableDto<NotificationTemplateDto> TEST_TEMPLATE_DTO = templateDtoPageableDto();
    public static final CourierUpdateDto UPDATE_COURIER_DTO = getUpdateCourierDto();

    public static CourierUpdateDto getUpdateCourierDto() {
        return CourierUpdateDto.builder()
            .courierId(1L)
            .nameUk("Test")
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
        return OrderResponseDto.builder()
            .additionalOrders(new HashSet<>(List.of("232-534-634")))
            .bags(Collections.singletonList(new BagDto(3, 999)))
            .locationId(1L)
            .orderComment("comment")
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .shouldBePaid(true)
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
            .address(Address.builder()
                .id(1L)
                .user(null)
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

    public static UBSuser getUBSuserWithoutSender() {
        return UBSuser.builder()
            .firstName("oleh")
            .lastName("ivanov")
            .email("mail@mail.ua")
            .id(1L)
            .phoneNumber("067894522")
            .address(Address.builder()
                .id(1L)
                .user(null)
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
                .address(Address.builder()
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
                    .user(User.builder().id(1L).build())
                    .build())
                .build())
            .user(User.builder().id(1L).recipientName("Yuriy").recipientSurname("Gerasum").uuid("UUID").build())
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

    public static Order getOrderWithoutAddress() {
        return Order.builder()
            .id(1L)
            .counterOrderPaymentId(0L)
            .ubsUser(UBSuser.builder()
                .firstName("oleh")
                .lastName("ivanov")
                .email("mail@mail.ua")
                .id(1L)
                .phoneNumber("067894522")
                .build())
            .user(User.builder().id(1L).recipientName("Yuriy").recipientSurname("Gerasum").build())
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
                    .address(Address.builder()
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
            .points(10)
            .build();
    }

    public static Certificate getCertificate() {
        return Certificate.builder()
            .code("1111-1234")
            .certificateStatus(CertificateStatus.ACTIVE)
            .points(10)
            .expirationDate(LocalDate.now().plusMonths(1))
            .creationDate(LocalDate.now())
            .order(null)
            .build();
    }

    public static Certificate getCertificate2() {
        return Certificate.builder()
            .code("1111-1234")
            .certificateStatus(CertificateStatus.ACTIVE)
            .points(600)
            .expirationDate(LocalDate.now().plusMonths(1))
            .creationDate(LocalDate.now())
            .order(null)
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

    public static OrderClientDto getOrderClientDto() {
        return OrderClientDto.builder()
            .id(1L)
            .orderStatus(OrderStatus.DONE)
            .amount(350L)
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
                .build()))
            .receivingStations(List.of(ReceivingStationDto.builder()
                .id(1L)
                .name("Петрівка")
                .build()))
            .build();
    }

    public static EmployeeDto getEmployeeDto() {
        return EmployeeDto.builder()
            .id(1L)
            .firstName("Петро")
            .lastName("Петренко")
            .phoneNumber("+380935577455")
            .email("test@gmail.com")
            .employeePositions(List.of(PositionDto.builder()
                .id(1L)
                .name("Водій")
                .build()))
            .receivingStations(List.of(ReceivingStationDto.builder()
                .id(1L)
                .name("Петрівка")
                .build()))
            .courier(getCourierDto())
            .location(LocationsDtos.builder()
                .locationId(1L)
                .nameEn("location")
                .nameUk("локація")
                .build())
            .tariffs(Set.of(getTariffsInfoDto()))
            .build();
    }

    public static EmployeeSignUpDto getEmployeeSignUpDto() {
        return EmployeeSignUpDto.builder()
            .name("testname")
            .email("test@gmail.com")
            .isUbs(true)
            .build();
    }

    public static TariffsInfoDto getTariffsInfoDto() {
        return TariffsInfoDto.builder()
            .id(1L)
            .maxAmountOfBags(20L)
            .maxPriceOfOrder(20000L)
            .minAmountOfBags(2L)
            .minPriceOfOrder(500L)
            .tariffLocations(Set.of(TariffLocation.builder()
                .location(getLocation())
                .build()))
            .receivingStations(List.of(getReceivingStationDto()))
            .courier(getCourierDto())
            .build();
    }

    public static EmployeeDto getEmployeeDtoWithReceivingStations() {
        return EmployeeDto.builder()
            .id(1L)
            .firstName("Петро")
            .lastName("Петренко")
            .phoneNumber("+380935577455")
            .email("test@gmail.com")
            .employeePositions(List.of(PositionDto.builder()
                .id(1L)
                .name("Водій")
                .build()))
            .receivingStations(List.of(ReceivingStationDto.builder()
                .id(1L)
                .name("Петрівка")
                .build()))
            .build();
    }

    public static Employee getEmployee() {
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
                .build()))
            .tariffInfos(Set.of(getTariffsInfo()))
            .imagePath("path")
            .id(1L)
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
            .build();
    }

    public static User getUserProfile() {
        return User.builder()
            .recipientName("Dima")
            .recipientSurname("Petrov")
            .recipientPhone("0666051373")
            .recipientEmail("petrov@gmail.com")
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

    public static PersonalDataDto getPersonalDataDto2() {
        return PersonalDataDto.builder()
            .id(1L)
            .firstName("Dima")
            .lastName("Petrov")
            .phoneNumber("0666051373")
            .email("mail@mail.ua")
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
            .houseNumber("7")
            .street("Gorodotska")
            .coordinates(Coordinates.builder().latitude(2.3).longitude(5.6).build())
            .district("Zaliznuchnuy")
            .city("Lviv")
            .actual(false)
            .build();
    }

    public static Address address() {
        return Address.builder()
            .id(addressDto().getId())
            .city(addressDto().getCity())
            .district(addressDto().getDistrict())
            .street(addressDto().getStreet())
            .coordinates(addressDto().getCoordinates())
            .entranceNumber(addressDto().getEntranceNumber())
            .houseNumber(addressDto().getHouseNumber())
            .houseCorpus(addressDto().getHouseCorpus())
            .actual(addressDto().getActual())
            .addressStatus(AddressStatus.DELETED)
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
            .build();
    }

    public static PositionDto getPositionDto() {
        return PositionDto.builder()
            .id(1L)
            .name("Водій")
            .build();
    }

    public static ReceivingStation getReceivingStation() {
        return ReceivingStation.builder()
            .id(1L)
            .name("Петрівка")
            .createDate(LocalDate.EPOCH)
            .createdBy(getUser())
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
            16, 13, 00, 00);
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
            16, 13, 00, 00);
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
            16, 13, 00, 00);
        return ViolationDetailInfoDto.builder()
            .orderId(1L)
            .addedByUser("Alan Po")
            .violationLevel(MAJOR)
            .description("violation1")
            .images(new ArrayList<>())
            .violationDate(localdatetime)
            .build();
    }

    public static OverpaymentInfoRequestDto getOverpaymentInfoRequestDto() {
        return OverpaymentInfoRequestDto.builder()
            .overpayment(200L)
            .bonuses(300L)
            .comment(AppConstant.ENROLLMENT_TO_THE_BONUS_ACCOUNT)
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
            .addresses(singletonList(address()))
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
                .address(Address.builder()
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
                    .user(User.builder().id(1L).build())
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

    public static BagTranslation getBagTranslation() {
        return BagTranslation.builder()
            .id(1L)
            .bag(Bag.builder().id(1).capacity(120).price(350).location(Location.builder()
                .id(1L)
                .locationStatus(LocationStatus.ACTIVE)
                .build())
                .build())
            .nameEng("a")
            .name("Useless paper")
            .build();
    }

    public static BagOrderDto getBagOrderDto() {
        return BagOrderDto.builder()
            .bagId(1)
            .price(350)
            .capacity(120)
            .bagAmount(1)
            .name("Useless paper")
            .nameEng("a")
            .build();
    }

    public static Order getFormedOrder() {
        return Order.builder()
            .id(1L)
            .events(List.of(new Event(1L, LocalDateTime.now(),
                "Roman", "Roman", new Order())))
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
            .confirmedQuantity(Map.of(1, 1))
            .exportedQuantity(Map.of(1, 1))
            .pointsToUse(100)
            .user(User.builder().id(1L).currentPoints(100).build())
            .build();
    }

    public static Order getCanceledPaidOrder() {
        return Order.builder()
            .id(1L)
            .events(List.of(new Event(1L, LocalDateTime.now(),
                "Roman", "Roman", new Order())))
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
                "Roman", "Roman", new Order())))
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
                "Roman", "Roman", new Order())))
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
                "Roman", "Roman", new Order())))
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
        Map<PositionDto, List<EmployeeNameIdDto>> allPositionsEmployees = new HashMap<>();
        Map<PositionDto, String> currentPositionEmployees = new HashMap<>();
        String value = getEmployee().getFirstName() + " " + getEmployee().getLastName();
        allPositionsEmployees.put(getPositionDto(), new ArrayList<>(List.of(EmployeeNameIdDto.builder()
            .id(getPositionDto().getId())
            .name(value)
            .build())));
        currentPositionEmployees.put(getPositionDto(), value);
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
            .address(createAddress())
            .build();
    }

    private static Address createAddress() {
        return Address.builder()
            .id(2L)
            .build();
    }

    private static Address createAddress2() {
        return Address.builder()
            .id(2L)
            .houseNumber("1")
            .entranceNumber("3")
            .district("Syhiv")
            .street("Stys")
            .houseCorpus("2")
            .city("cc")
            .region("cc")
            .build();
    }

    private static OrderAddressExportDetailsDtoUpdate createOrderAddressDtoUpdate() {
        return OrderAddressExportDetailsDtoUpdate.builder()
            .addressId(1L)
            .addressHouseNumber("1")
            .addressEntranceNumber("3")
            .addressDistrict("Syhiv")
            .addressStreet("Stys")
            .addressHouseCorpus("2")
            .addressCity("s")
            .addressRegion("s")
            .build();
    }

    private static OrderAddressDtoResponse createOrderAddressDtoResponse() {
        return OrderAddressDtoResponse.builder()
            .houseNumber("1")
            .entranceNumber("3")
            .district("Syhiv")
            .street("Stys")
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
            .paymentStatus(TEST_PAYMENT_LIST.get(0).getPaymentStatus().name())
            .date(orderDate)
            .build();
    }

    private static BagInfoDto createBagInfoDto() {
        return BagInfoDto.builder()
            .id(1)
            .capacity(4)
            .build();
    }

    private static BagTransDto createBagTransDto() {
        return BagTransDto.builder()
            .name("test")
            .build();
    }

    private static List<BagMappingDto> createBagMappingDtoList() {
        return Collections.singletonList(
            BagMappingDto.builder()
                .amount(4)
                .build());
    }

    private static Bag createBag() {
        return Bag.builder()
            .id(2)
            .fullPrice(100)
            // .price(100)
            .build();
    }

    private static OrderDetailInfoDto createOrderDetailInfoDto() {
        return OrderDetailInfoDto.builder()
            .amount(5)
            .capacity(4)
            .build();
    }

    private static BagTranslation createBagTranslation() {
        return BagTranslation.builder()
            .id(4L)
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
            .addresses(singletonList(address()))
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

    private static EmployeePositionDtoResponse createEmployeePositionDtoResponse() {
        return EmployeePositionDtoResponse.builder()
            .orderId(1L)
            .employeeOrderPositionDTOS(createEmployeePositionDto())
            .build();
    }

    private static List<EmployeeOrderPositionDTO> createEmployeePositionDto() {
        return List.of(
            EmployeeOrderPositionDTO.builder()
                .name("Test Test")
                .positionId(2L)
                .build());
    }

    private static List<EmployeeOrderPosition> createEmployeeOrderPositionList() {
        return List.of(
            EmployeeOrderPosition.builder()
                .id(1L)
                .employee(createEmployee())
                .position(createPosition())
                .order(createOrder2())
                .build());
    }

    private static Position createPosition() {
        return Position.builder()
            .id(2L)
            .build();
    }

    private static Order createOrder2() {
        return Order.builder()
            .id(2L)
            .user(User.builder().id(1L).recipientName("Yuriy").recipientSurname("Gerasum").build())
            .build();
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
            .title("Test")
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

    private static PageableDto<NotificationTemplateDto> templateDtoPageableDto() {
        return new PageableDto<>(
            TEST_NOTIFICATION_TEMPLATE_LIST,
            1,
            0,
            1);
    }

    private static NotificationTemplate createNotificationTemplate() {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setTitle("Test");
        notificationTemplate.setNotificationType(NotificationType.UNPAID_ORDER);
        notificationTemplate.setNotificationReceiverType(SITE);
        notificationTemplate.setBody("Test");
        // notificationTemplate.set;

        return notificationTemplate;
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
        notification.setNotificationType(NotificationType.UNPAID_ORDER);
        return List.of(
            notification);
    }

    private static Violation createTestViolation() {
        return Violation.builder().description("violation description").build();
    }

    private static NotificationParameter createNotificationParameter() {
        return NotificationParameter.builder()
            .key("violationDescription")
            .value("violation description")
            .build();
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

        return parameters;
    }

    private static Set<NotificationParameter> createNotificationParameterSet2() {
        Set<NotificationParameter> parameters = new HashSet<>();

        parameters.add(NotificationParameter.builder().key("returnedPayment")
            .value(String.valueOf(200L)).build());

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
        notification.setNotificationType(NotificationType.UNPAID_ORDER);
        return notification;
    }

    private static NotificationDto createNotificationDto() {
        return NotificationDto.builder()
            .title("Test")
            .body("Test")
            .build();
    }

    public static NotificationDto createViolationNotificationDto() {
        return NotificationDto.builder()
            .title("Test")
            .body("Test")
            .images(emptyList())
            .build();
    }

    public static List<TariffTranslationDto> getTariffTranslationDto() {
        return List.of(TariffTranslationDto.builder()
            .description("Description")
            .descriptionEng("DescriptionEng")
            .nameEng("a")
            .name("Test")
            .build());
    }

    public static AddServiceDto addServiceDto() {
        return AddServiceDto.builder()
            .commission(50)
            .capacity(100)
            .price(100)
            .tariffTranslationDtoList(getTariffTranslationDto())
            .locationId(1L)
            .build();
    }

    public static AssignEmployeesForOrderDto assignEmployeesForOrderDto() {
        return AssignEmployeesForOrderDto.builder()
            .orderId(1L)
            .employeesList(List.of(AssignForOrderEmployee.builder()
                .employeeId(1L)
                .build(),
                AssignForOrderEmployee.builder()
                    .employeeId(1L)
                    .build(),
                AssignForOrderEmployee.builder()
                    .employeeId(1L)
                    .build()))
            .build();
    }

    public static AssignEmployeesForOrderDto assignEmployeeForOrderDto() {
        return AssignEmployeesForOrderDto.builder()
            .orderId(1L)
            .employeesList(List.of(AssignForOrderEmployee.builder()
                .employeeId(1L)
                .build()))
            .build();
    }

    public static Optional<Bag> getBag() {
        return Optional.of(Bag.builder()
            .id(1)
            .capacity(120)
            .commission(50)
            .price(120)
            .fullPrice(170)
            .location(Location.builder().id(1L).locationStatus(LocationStatus.ACTIVE).build())
            .createdAt(LocalDate.now())
            .createdBy("User")
            .bagTranslations(List.of(BagTranslation.builder()
                .description("Description")
                .descriptionEng("DescriptionEng")
                .id(1L).build()))
            .minAmountOfBags(MinAmountOfBag.INCLUDE)
            .build());
    }

    public static EditTariffServiceDto getEditTariffServiceDto() {
        return EditTariffServiceDto.builder()
            .name("Бавовняна сумка")
            .capacity(120)
            .price(120)
            .commission(50)
            .description("Description")
            .langCode("ua")
            .build();

    }

    public static BagTranslation getBagTranslationForEditMethod() {
        return BagTranslation.builder()
            .id(1L)
            .bag(getBag().get())
            .nameEng("a")
            .name("Бавовняна сумка")
            .description("Description")
            .descriptionEng("DescriptionEng")
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
            .build();
    }

    public static List<Location> getLocationForSummary() {
        return List.of(Location.builder()
            .locationStatus(LocationStatus.ACTIVE)
            .nameEn("Kyiv")
            .nameUk("Київ")
            .coordinates(Coordinates.builder()
                .longitude(3.21d)
                .latitude(5.43d).build())
            .region(getRegionForMapper())
            .id(1L)
            .build(),
            Location.builder()
                .locationStatus(LocationStatus.ACTIVE)
                .nameEn("Bucha")
                .nameUk("Буча")
                .coordinates(Coordinates.builder()
                    .longitude(4.32d)
                    .latitude(4.21d).build())
                .region(getRegionForMapper())
                .id(11L)
                .build());
    }

    public static Courier getCourier() {
        return Courier.builder()
            .id(1L)
            .courierStatus(CourierStatus.ACTIVE)
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

    public static List<BagTranslation> getBagTransaltion() {
        return List.of(BagTranslation.builder()
            .description("Description")
            .descriptionEng("DescriptionEng")
            .name("Test")
            .nameEng("a")
            .build());
    }

    public static Bag getTariffBag() {
        return Bag.builder().price(100)
            .commission(50)
            .fullPrice(150)
            .capacity(100)
            .createdAt(LocalDate.now())
            .createdBy("Taras Ivanov")
            .location(getLocation())
            .minAmountOfBags(MinAmountOfBag.INCLUDE)
            .bagTranslations(getBagTransaltion()).build();
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

    public static PaymentResponseDtoLiqPay getPaymentResponceDto() {
        return PaymentResponseDtoLiqPay.builder()
            .data("Test Data")
            .signature("Test Signature").build();
    }

    public static CreateServiceDto getCreateServiceDto() {
        return CreateServiceDto.builder()
            .capacity(120)
            .commission(50)
            .price(100)
            .serviceTranslationDtoList(List.of(getServiceTranslationDto()))
            .courierId(1L)
            .build();
    }

    public static EditServiceDto getEditServiceDto() {
        return EditServiceDto.builder()
            .capacity(120)
            .commission(50)
            .locationId(1L)
            .price(100)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .name("Name")
            .nameEng("NameEng")
            .build();

    }

    public static Service getService() {
        User user = ModelUtils.getUser();
        return Service.builder()
            .capacity(120)
            .basePrice(100)
            .commission(50)
            .fullPrice(150)
            .createdAt(LocalDate.now())
            .createdBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .courier(getCourier())
            .serviceTranslations(List.of(getServiceTranslation()))
            .build();
    }

    public static Service getEditedService() {
        User user = ModelUtils.getUser();
        return Service.builder()
            .id(1L)
            .capacity(120)
            .basePrice(100)
            .commission(50)
            .fullPrice(150)
            .editedAt(LocalDate.now())
            .editedBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .serviceTranslations(getServiceTranslationList())
            .courier(getCourier())
            .build();
    }

    public static ServiceTranslation getServiceTranslation() {
        return ServiceTranslation.builder()
            .name("Name")
            .description("Description")
            .nameEng("NameEng")
            .descriptionEng("DescriptionEng")
            .build();
    }

    public static CreateCourierDto getCreateCourierDto() {
        return CreateCourierDto.builder()
            .nameUk("nameUa")
            .nameEn("nameEn")
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

    public static List<Bag> getBaglist() {
        return List.of(Bag.builder()
            .id(1)
            .price(100)
            .capacity(10)
            .commission(21)
            .fullPrice(20)
            .build(),
            Bag.builder()
                .id(2)
                .price(100)
                .capacity(10)
                .commission(21)
                .fullPrice(21)
                .build());
    }

    public static List<Bag> getBag2list() {
        return List.of(Bag.builder()
            .id(1)
            .price(100)
            .capacity(10)
            .commission(21)
            .fullPrice(20)
            .build());
    }

    public static List<Bag> getBag3list() {
        return List.of(Bag.builder()
            .id(1)
            .price(100)
            .capacity(10)
            .commission(21)
            .fullPrice(2000)
            .build(),
            Bag.builder()
                .id(2)
                .price(100)
                .capacity(10)
                .commission(21)
                .fullPrice(2100)
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

    public static BagInfoDto getBagInfoDto() {
        return BagInfoDto.builder()
            .id(1)
            .name("name")
            .nameEng("name")
            .price(100)
            .capacity(10)
            .build();
    }

    public static PaymentTableInfoDto getPaymentTableInfoDto() {
        return PaymentTableInfoDto.builder()
            .paidAmount(200L)
            .unPaidAmount(0L)
            .paymentInfoDtos(List.of(getInfoPayment().setAmount(10L)))
            .overpayment(800L)
            .build();
    }

    public static PaymentInfoDto getInfoPayment() {
        return PaymentInfoDto.builder()
            .comment("ddd")
            .id(1L)
            .amount(1000L)
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

    public static OrderFondyClientDto getOrderFondyClientDto() {
        return OrderFondyClientDto.builder()
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
                .recipientId(2L)
                .recipientName("aaaaa")
                .recipientPhoneNumber("085555")
                .recipientEmail("yura@333gmail.com")
                .build())
            .addressExportDetailsDto(OrderAddressExportDetailsDtoUpdate
                .builder()
                .addressId(1L)
                .addressDistrict("aaaaaaa")
                .addressStreet("aaaaa")
                .addressEntranceNumber("12")
                .addressHouseCorpus("123")
                .addressHouseNumber("121")
                .addressCity("dsfsdf")
                .addressRegion("sdfsdfsd")
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

    public static List<ServiceTranslation> getServiceTranslationList() {
        return List.of(ServiceTranslation.builder()
            .description("Description")
            .name("Name")
            .nameEng("NameEng")
            .descriptionEng("DescriptionEng")
            .id(1L)
            .service(Service.builder()
                .id(1L)
                .capacity(120)
                .basePrice(100)
                .commission(50)
                .fullPrice(150)
                .courier(getCourier())
                .createdAt(LocalDate.now())
                .createdBy("Taras Ivanov")
                .build())
            .build());
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
            .id(1)
            .minAmountOfBags(MinAmountOfBag.INCLUDE)
            .location(Location
                .builder()
                .id(1L)
                .locationStatus(LocationStatus.ACTIVE)
                .build())
            .bagTranslations(List.of(getBagTranslation()))
            .build();
    }

    public static Bag bagDto2() {
        return Bag.builder()
            .id(1)
            .minAmountOfBags(MinAmountOfBag.EXCLUDE)
            .location(Location
                .builder()
                .id(1L)
                .locationStatus(LocationStatus.ACTIVE)
                .build())
            .build();
    }

    public static BagTranslation bagTranslationDto() {
        return BagTranslation
            .builder()
            .id(1L)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .bag(Bag.builder().id(1).minAmountOfBags(MinAmountOfBag.EXCLUDE)
                .location(Location.builder()
                    .id(1L)
                    .locationStatus(LocationStatus.ACTIVE)
                    .build())
                .build())
            .name("Name")
            .nameEng("NameEng")
            .build();
    }

    public static GetServiceDto getServiceDto() {
        User user = getUser();
        return GetServiceDto.builder()
            .id(1L)
            .name("Name")
            .nameEng("NameEng")
            .capacity(120)
            .price(100)
            .commission(50)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .fullPrice(150)
            .editedAt(LocalDate.now())
            .editedBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .courierId(1L)
            .build();
    }

    public static ServiceTranslationDto getServiceTranslationDto() {
        return ServiceTranslationDto.builder()
            .description("Description")
            .descriptionEng("DescriptionEng")
            .nameEng("NameEng")
            .name("Name")
            .build();
    }

    public static EditPriceOfOrder getEditPriceOfOrder() {
        return EditPriceOfOrder.builder()
            .maxPriceOfOrder(500000L)
            .minPriceOfOrder(300L)
            .locationId(1L)
            .build();
    }

    public static EditAmountOfBagDto getAmountOfBagDto() {
        return EditAmountOfBagDto.builder()
            .maxAmountOfBigBags(99L)
            .minAmountOfBigBags(2L)
            .locationId(1L)
            .build();
    }

    public static GetServiceDto getAllInfoAboutService() {
        User user = getUser();
        return GetServiceDto.builder()
            .name("Name")
            .nameEng("NameEng")
            .capacity(120)
            .price(100)
            .commission(50)
            .description("Description")
            .descriptionEng("DescriptionEng")
            .fullPrice(150)
            .id(1L)
            .createdAt(LocalDate.now())
            .createdBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .courierId(1L)
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

    public static Bag bagDtoClient() {
        return Bag.builder()
            .id(1)
            .minAmountOfBags(MinAmountOfBag.INCLUDE)
            .price(1)
            .fullPrice(1)
            .commission(2)
            .location(Location
                .builder()
                .id(1L)
                .build())
            .build();
    }

    public static NotificationTemplate getNotificationTemplate() {
        return new NotificationTemplate()
            .setId(1L)
            .setBody("test")
            .setTitle("test")
            .setNotificationType(NotificationType.UNPAID_ORDER)
            .setNotificationReceiverType(SITE)
            .setLanguageCode("ua");
    }

    public static NotificationTemplateDto getNotificationTemplateDto() {
        return new NotificationTemplateDto()
            .setId(1L)
            .setBody("test")
            .setTitle("test")
            .setNotificationType("UNPAID_ORDER")
            .setSchedule(NOTIFICATION_SCHEDULE_DTO);
    }

    public static PageableDto<NotificationTemplateLocalizedDto> getNotificationTemplateLocalizedDto() {
        return new PageableDto<>(List.of(NotificationTemplateLocalizedDto.builder()
            .id(1L)
            .title(TitleDto.builder().enTitle("test").uaTitle("test").build())
            .body(BodyDto.builder().bodyEn("test").bodyUa("test").build())
            .notificationType("UNPAID_ORDER")
            .build()), 1, 0, 1);
    }

    public static Page<NotificationTemplate> getNotificationTemplatePageable() {
        return new PageImpl<>(List.of(new NotificationTemplate()
            .setId(1L)
            .setBody("test")
            .setTitle("test")
            .setNotificationType(NotificationType.UNPAID_ORDER)
            .setNotificationReceiverType(SITE)
            .setLanguageCode("ua")),
            TEST_PAGEABLE_NOTIFICATION_TEMPLATE,
            1);
    }

    public static UserProfileUpdateDto updateUserProfileDto() {
        return UserProfileUpdateDto.builder()
            .recipientName("Taras")
            .recipientSurname("Ivanov")
            .recipientPhone("962473289")
            .addressDto(addressDtoList())
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

    public static Region getRegionForMapper() {
        return Region.builder()
            .id(1L)
            .ukrName("Київська область")
            .enName("Kyiv region")
            .build();
    }

    public static Region getRegionForSummary() {
        return Region.builder()
            .id(1L)
            .ukrName("Київська область")
            .enName("Kyiv region")
            .locations(getLocationForSummary())
            .build();
    }

    public static LocationInfoDto getInfoAboutLocationDto() {
        return LocationInfoDto.builder()
            .regionId(1L)
            .regionTranslationDtos(getRegionTranslationsDto())
            .locationsDto(getLocationsDto()).build();
    }

    public static List<LocationToCityDto> getCitiesInUa() {
        return List.of(
            LocationToCityDto.builder().cityName("Київ").cityId(1L)
                .coordinates(CoordinatesDto.builder().longitude(3.21d).latitude(5.43d).build()).build(),
            LocationToCityDto.builder().cityName("Буча").cityId(11L)
                .coordinates(CoordinatesDto.builder().longitude(4.32d).latitude(4.21d).build()).build());
    }

    public static List<LocationToCityDto> getCitiesInEn() {
        return List.of(
            LocationToCityDto.builder().cityName("Kyiv").cityId(1L)
                .coordinates(CoordinatesDto.builder().longitude(3.21d).latitude(5.43d).build()).build(),
            LocationToCityDto.builder().cityName("Bucha").cityId(11L)
                .coordinates(CoordinatesDto.builder().longitude(4.32d).latitude(4.21d).build()).build());
    }

    public static LocationSummaryDto getInfoAboutLocationSummaryDto() {
        return LocationSummaryDto.builder()
            .regionId(1L)
            .nameUa("Київська область")
            .nameEn("Kyiv region")
            .citiesEn(getCitiesInEn())
            .citiesUa(getCitiesInUa())
            .build();
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

    public static PaymentResponseDto getPaymentResponseDto() {
        return PaymentResponseDto.builder()
            .order_id("1_1_1")
            .payment_id(2)
            .currency("a")
            .amount(1)
            .order_status("approved")
            .response_status("failure")
            .sender_cell_phone("sss")
            .sender_account("ss")
            .masked_card("s")
            .card_type("s")
            .response_code(2)
            .response_description("ddd")
            .order_time("s")
            .settlement_date("21.12.2014")
            .fee(null)
            .payment_system("s")
            .sender_email("s")
            .payment_id(2)
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
            .setSettlement("Київ")
            .setSettlementEn("Kyiv")
            .setDistrict("Шевченківський")
            .setDistrictEn("Shevchenkivs'kyi")
            .setAddress("Січових Стрільців, 37, 1, 1")
            .setAddressEn("Sichovyh Stril'tsiv, 37, 1, 1")
            .setCommentToAddressForClient("coment")
            .setBagAmount("3")
            .setTotalOrderSum(500L)
            .setOrderCertificateCode("5489-2789")
            .setGeneralDiscount(100L)
            .setAmountDue(0L)
            .setCommentForOrderByClient("commentForOrderByClient")
            .setTotalPayment(200L)
            .setDateOfExport(LocalDate.of(2021, 12, 8))
            .setTimeOfExport("from 15:59:52 to 15:59:52")
            .setIdOrderFromShop("3245678765")
            .setReceivingStationId(1L)
            .setResponsibleLogicManId(1L)
            .setResponsibleDriverId(1L)
            .setResponsibleCallerId(1L)
            .setResponsibleNavigatorId(1L)
            .setCommentsForOrder("commentsForOrder")
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
            .setSettlement(new SenderLocation().setUa("Київ").setEn("Kyiv"))
            .setDistrict(new SenderLocation().setUa("Шевченківський").setEn("Shevchenkivs'kyi"))
            .setAddress(
                new SenderLocation().setUa("Січових Стрільців, 37, 1, 1").setEn("Sichovyh Stril'tsiv, 37, 1, 1"))
            .setCommentToAddressForClient("coment")
            .setBagsAmount("3")
            .setTotalOrderSum(500L)
            .setOrderCertificateCode("5489-2789")
            .setGeneralDiscount(100L)
            .setAmountDue(0L)
            .setCommentForOrderByClient("commentForOrderByClient")
            .setTotalPayment(200L)
            .setDateOfExport("2021-12-08")
            .setTimeOfExport("from 15:59:52 to 15:59:52")
            .setIdOrderFromShop("3245678765")
            .setReceivingStation("1")
            .setResponsibleLogicMan("1")
            .setResponsibleDriver("1")
            .setResponsibleCaller("1")
            .setResponsibleNavigator("1")
            .setCommentsForOrder("commentsForOrder")
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
            .setSettlement(new SenderLocation().setEn(null).setUa(null))
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

    public static Order getOrderForGetOrderStatusData2Test() {
        Map<Integer, Integer> hashMap = new HashMap<>();
        hashMap.put(1, 1);
        hashMap.put(2, 1);

        return Order.builder()
            .id(1L)
            .amountOfBagsOrdered(hashMap)
            .confirmedQuantity(hashMap)
            .exportedQuantity(hashMap)
            .pointsToUse(100)
            .orderStatus(OrderStatus.DONE)
            .payment(Lists.newArrayList(Payment.builder()
                .paymentId("1L")
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
                .address(Address.builder()
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
                    .user(User.builder().id(1L).build())
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
            .imageReasonNotTakingBags(List.of("foto"))

            .tariffsInfo(TariffsInfo.builder()
                .courier(Courier.builder()
                    .id(1L)
                    .build())
                .id(1L)
                .tariffLocations(Set.of(TariffLocation.builder()
                    .id(1L)
                    .build()))
                .maxAmountOfBigBags(2L)
                .maxPriceOfOrder(500000L)
                .minAmountOfBigBags(99L)
                .minPriceOfOrder(500L)
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

    public static Order getOrdersStatusAdjustmentDto() {
        return Order.builder()
            .id(1L)
            .payment(List.of(Payment.builder().id(1L).build()))
            .user(User.builder().id(1L).build())
            .imageReasonNotTakingBags(List.of("ss"))
            .reasonNotTakingBagDescription("aa")
            .orderStatus(OrderStatus.ADJUSTMENT)
            .counterOrderPaymentId(1L)
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
            .certificates(Set.of(getCertificate2()))
            .pointsToUse(100)
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
            .build();
    }

    public static Order getOrdersStatusCanseledDto() {
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
            .addressStreet("s")
            .addressCity("ss")
            .addressDistrict("s")
            .addressHouseCorpus("ss")
            .addressEntranceNumber("ss")
            .addressRegion("ss")
            .addressHouseNumber("ss")
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
            .orderId(List.of(1L))
            .newValue("1")
            .build();
    }

    public static List<Bot> botList() {
        List<Bot> botList = new ArrayList<>();
        botList.add(new Bot()
            .setType("TELEGRAM")
            .setLink("https://t.me/ubs_test_bot?start=87df9ad5-6393-441f-8423-8b2e770b01a8"));
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
                .address(Address.builder()
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
                    .user(User.builder().id(1L).build())
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
            .creator("Taras")
            .build();
    }

    public static TariffsInfo getTariffsInfo() {
        return TariffsInfo.builder()
            .id(1L)
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .maxAmountOfBigBags(20L)
            .maxPriceOfOrder(20000L)
            .minAmountOfBigBags(2L)
            .minPriceOfOrder(500L)
            .tariffLocations(Set.of(TariffLocation.builder()
                .location(getLocation())
                .build()))
            .receivingStationList(Set.of(getReceivingStation()))
            .courier(getCourier())
            .build();
    }

    public static UpdateNotificationTemplatesDto getUpdateNotificationTemplatesDto() {
        return UpdateNotificationTemplatesDto.builder()
            .body("test")
            .notificationType(NotificationType.UNPAID_ORDER.toString())
            .build();
    }

    public static NotificationTemplateLocalizedDto getNotificationTemplateLocalizeDto() {
        return NotificationTemplateLocalizedDto.builder()
            .id(1L)
            .notificationType(NotificationType.UNPAID_ORDER.toString())
            .notificationReceiverType(SITE.toString())
            .build();
    }

    public static OrdersDataForUserDto getOrderStatusDto() {
        SenderInfoDto senderInfoDto = SenderInfoDto.builder()
            .senderName("TestName")
            .senderSurname("TestSurName")
            .senderPhone("38099884433")
            .senderEmail("test@mail.com")
            .build();

        CertificateDto certificateDto = CertificateDto.builder()
            .points(300)
            .dateOfUse(LocalDate.now())
            .expirationDate(LocalDate.now())
            .code("200")
            .certificateStatus("ACTIVE")
            .build();

        AddressInfoDto addressInfoDto = AddressInfoDto.builder()
            .addressStreet("StreetTest")
            .addressDistinct("AdressDistinctTest")
            .addressRegion("TestRegion")
            .addressComment("TestComment")
            .addressCity("TestCity")
            .addressStreetEng("StreetEng")
            .addressDistinctEng("DisticntEng")
            .addressCityEng("CityEng")
            .addressRegionEng("RegionEng")
            .build();

        BagForUserDto bagForUserDto = new BagForUserDto();
        bagForUserDto.setTotalPrice(900);
        bagForUserDto.setCount(3);
        bagForUserDto.setCapacity(200);
        bagForUserDto.setFullPrice(300);
        bagForUserDto.setService("Безпечні Відходи");
        bagForUserDto.setServiceEng("Safe Waste");

        return OrdersDataForUserDto.builder()
            .id(1L)
            .dateForm(LocalDateTime.of(22, 10, 12, 14, 55))
            .datePaid(LocalDateTime.now())
            .amountBeforePayment(500d)
            .bonuses(100d)
            .orderFullPrice(400d)
            .orderComment("abc")
            .paymentStatus(PaymentStatus.PAID.toString())
            .sender(senderInfoDto)
            .address(addressInfoDto)
            .paymentStatusEng(PaymentStatus.PAID.toString())
            .orderStatus(OrderStatus.FORMED.toString())
            .orderStatusEng(OrderStatus.FORMED.toString())
            .bags(List.of(bagForUserDto))
            .certificate(List.of(certificateDto))
            .additionalOrders(Collections.emptySet())
            .build();
    }

    public static TariffsInfo getTariffInfo() {
        return TariffsInfo.builder()
            .id(1L)
            .courier(ModelUtils.getCourier())
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
            .locationStatus(LocationStatus.ACTIVE)
            .creator(ModelUtils.getUser())
            .createdAt(LocalDate.of(2022, 10, 20))
            .maxAmountOfBigBags(100L)
            .minAmountOfBigBags(2L)
            .maxPriceOfOrder(50000L)
            .minPriceOfOrder(500L)
            .orders(Collections.emptyList())
            .receivingStationList(Set.of(ReceivingStation.builder()
                .name("receivingStation")
                .createdBy(ModelUtils.createUser())
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
            .locationStatus(LocationStatus.ACTIVE)
            .creator(ModelUtils.getUser())
            .createdAt(LocalDate.of(2022, 10, 20))
            .maxAmountOfBigBags(100L)
            .minAmountOfBigBags(5L)
            .maxPriceOfOrder(50000L)
            .minPriceOfOrder(500L)
            .orders(List.of(ModelUtils.getOrder()))
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

    public static String getSuccessfulFondyResponse() {
        return "{\n" +
            "  \"response\":{\n" +
            "    \"response_status\":\"success\",\n" +
            "    \"checkout_url\":\"https://pay.fondy.eu/checkout?token=afcb21aef707b1fea2565b66bac7dc41d7833390\"\n" +
            "  }\n" +
            "}";
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

    public static OrderAddressDtoRequest getTestOrderAddressDtoRequest() {
        return OrderAddressDtoRequest.builder()
            .id(0L)
            .region("fake region")
            .searchAddress("fake address")
            .city("fake street")
            .district("fake district")
            .entranceNumber("1")
            .houseNumber("13")
            .houseCorpus("1")
            .street("fake street name")
            .streetEn("fake street name")
            .coordinates(new Coordinates(50.5555555d, 50.5555555d))
            .cityEn("fake street")
            .districtEn("fake district")
            .regionEn("fake region")
            .build();
    }

    public static OrderAddressDtoRequest getTestOrderAddressLocationDto() {
        return OrderAddressDtoRequest.builder()
            .id(0L)
            .region("fake region")
            .city("fake street")
            .district("fake district")
            .entranceNumber("1")
            .houseNumber("13")
            .houseCorpus("1")
            .street("fake street name")
            .streetEn("fake street name")
            .coordinates(new Coordinates(50.5555555d, 50.5555555d))
            .cityEn("fake street")
            .districtEn("fake district")
            .regionEn("fake region")
            .build();
    }

    public static User getUserForCreate() {
        return User.builder()
            .id(1L)
            .addresses(List.of(Address.builder().id(7L).city("fake street").cityEn("fake street")
                .district("fake district").districtEn("fake district").region("fake region").regionEn("fake region")
                .street("fake street name").streetEn("fake street name").houseNumber("13")
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
                    .city("Lviv")
                    .district("Zaliznuchnuy")
                    .entranceNumber("7a")
                    .houseCorpus("2")
                    .houseNumber("7")
                    .street("Gorodotska")
                    .coordinates(new Coordinates(2.3, 5.6))
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

    public static CourierTranslationDto getCourierTranslationDto() {
        return CourierTranslationDto.builder()
            .nameUk("Test")
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

    public static NotificationScheduleDto getInfoAboutNotificationScheduleDto() {
        return NotificationScheduleDto.builder()
            .cron("test")
            .build();
    }

    public static NotificationSchedule getInfoAboutNotificationSchedule() {
        return NotificationSchedule.builder()
            .notificationType(NotificationType.TEST)
            .cron("test")
            .build();
    }
}
