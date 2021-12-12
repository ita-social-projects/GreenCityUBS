package greencity;

import com.google.common.collect.Lists;
import greencity.constant.AppConstant;
import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.*;
import greencity.entity.language.Language;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.LocationTranslation;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static greencity.entity.enums.NotificationReceiverType.SITE;
import static greencity.entity.enums.ViolationLevel.MAJOR;
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
    public static final Order GET_ORDER_DETAILS = getOrderDetails();
    public static final Order TEST_ORDER_2 = createTestOrder2();
    public static final Order TEST_ORDER_3 = createTestOrder3();
    public static final Order TEST_ORDER_4 = createTestOrder4();
    public static final Set<NotificationParameter> TEST_NOTIFICATION_PARAMETER_SET = createNotificationParameterSet();
    public static final UserNotification TEST_USER_NOTIFICATION = createUserNotification();
    public static final UserNotification TEST_USER_NOTIFICATION_2 = createUserNotification2();
    public static final UserNotification TEST_USER_NOTIFICATION_3 = createUserNotification3();
    public static final UserNotification TEST_USER_NOTIFICATION_4 = createUserNotification4();
    public static final NotificationParameter TEST_NOTIFICATION_PARAMETER = createNotificationParameter();
    public static final Violation TEST_VIOLATION = createTestViolation();
    public static final NotificationTemplate TEST_NOTIFICATION_TEMPLATE = createNotificationTemplate();
    public static final Pageable TEST_PAGEABLE = PageRequest.of(0, 5, Sort.by("notificationTime").descending());
    public static final List<UserNotification> TEST_USER_NOTIFICATION_LIST = createUserNotificationList();
    public static final Page<UserNotification> TEST_PAGE =
        new PageImpl<>(TEST_USER_NOTIFICATION_LIST, TEST_PAGEABLE, TEST_USER_NOTIFICATION_LIST.size());
    public static final NotificationShortDto TEST_NOTIFICATION_SHORT_DTO = createNotificationShortDto();
    public static final List<NotificationShortDto> TEST_NOTIFICATION_SHORT_DTO_LIST =
        List.of(TEST_NOTIFICATION_SHORT_DTO);
    public static final PageableDto<NotificationShortDto> TEST_DTO = createPageableDto();
    public static final List<String> TEST_ALL_LANGUAGE_CODE = createAllLanguageCode();
    public static final Order TEST_ORDER_UPDATE_POSITION = createOrder2();
    public static final List<EmployeeOrderPosition> TEST_EMPLOYEE_ORDER_POSITION = createEmployeeOrderPositionList();
    public static final EmployeePositionDtoResponse TEST_EMPLOYEE_POSITION_DTO_RESPONSE =
        createEmployeePositionDtoResponse();
    public static final Position TEST_POSITION = createPosition();
    public static final Employee TEST_EMPLOYEE = createEmployee();
    public static final User TEST_USER = createUser();
    public static final AdditionalBagInfoDto TEST_ADDITIONAL_BAG_INFO_DTO = createAdditionalBagInfoDto();
    public static final List<AdditionalBagInfoDto> TEST_ADDITIONAL_BAG_INFO_DTO_LIST = createAdditionalBagInfoDtoList();
    public static final Map<String, Object> TEST_MAP_ADDITIONAL_BAG = createMap();
    public static final List<Map<String, Object>> TEST_MAP_ADDITIONAL_BAG_LIST =
        Collections.singletonList(TEST_MAP_ADDITIONAL_BAG);
    public static final UpdateOrderDetailDto TEST_UPDATE_ORDER_DETAIL_DTO = createUpdateOrderDetailDto();
    public static final List<UpdateOrderDetailDto> TEST_UPDATE_ORDER_DETAIL_DTO_LIST =
        Collections.singletonList(TEST_UPDATE_ORDER_DETAIL_DTO);
    public static final NotificationDto TEST_NOTIFICATION_DTO = createNotificationDto();

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
            .additionalOrders(new HashSet<>(Arrays.asList("232-534-634")))
            .bags(Collections.singletonList(new BagDto(3, 999)))
            .orderComment("comment")
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .shouldBePaid(true)
            .personalData(PersonalDataDto.builder()
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
            .recipientName("Alan Po")
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
                .paymentId(1L)
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
            .receivingStation("C")
            .orderPaymentStatus(OrderPaymentStatus.PAID)
            .cancellationReason(CancellationReason.OUT_OF_CITY)
            .imageReasonNotTakingBags(List.of("foto"))
            .orderPaymentStatus(OrderPaymentStatus.UNPAID)
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
            .receivingStation("Petrivka")
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
            .receivingStation("Petrivka")
            .allReceivingStations(List.of("a", "b"))
            .build();
    }

    public static ExportDetailsDtoUpdate getExportDetailsRequest() {
        return ExportDetailsDtoUpdate.builder()
            .dateExport("1997-12-04T15:40:24")
            .timeDeliveryFrom("1997-12-04T15:40:24")
            .timeDeliveryTo("1990-12-11T19:30:30")
            .receivingStation("Petrivka")
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

    public static UserViolationMailDto getUserViolationMailDto() {
        return UserViolationMailDto.builder()
            .email("string@gmail.com")
            .name("string")
            .language("en")
            .violationDescription("String Description")
            .build();
    }

    public static AddingViolationsToUserDto getAddingViolationsToUserDto() {
        return AddingViolationsToUserDto.builder()
            .orderID(1L)
            .violationDescription("String string string")
            .violationLevel("low")
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
            .payment(singletonList(new Payment().builder()
                .id(1L)
                .amount(350L)
                .build()))
            .orderDate(LocalDateTime.of(2021, 5, 15, 10, 20, 5))
            .amountOfBagsOrdered(Collections.singletonMap(1, 2))
            .build();
    }

    public static OrderBagDto getOrderBagDto() {
        return OrderBagDto.builder()
            .id(1)
            .amount(3)
            .build();
    }

    public static AllPointsUserDto allPointsUserDto() {
        return AllPointsUserDto.builder()
            .userBonuses(100)
            .ubsUserBonuses(List.of(PointsForUbsUserDto.builder()
                .dateOfEnrollment(LocalDateTime.of(2017, 12, 25, 3, 0, 0, 0))
                .amount(50)
                .numberOfOrder(36874L).build(),
                PointsForUbsUserDto.builder()
                    .dateOfEnrollment(LocalDateTime.of(2017, 12, 25, 3, 0, 0, 0))
                    .amount(50)
                    .numberOfOrder(35478L).build()))
            .build();

    }

    public static PointsForUbsUserDto pointsForUbsUserDto() {
        return PointsForUbsUserDto.builder()
            .dateOfEnrollment(LocalDateTime.of(2017, 12, 25, 3, 0, 0, 0))
            .amount(700)
            .numberOfOrder(35478L).build();
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
            .receivingStation(Set.of(ReceivingStation.builder()
                .id(1L)
                .name("Петрівка")
                .build()))
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

    public static List<AddressDto> addressDto() {
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
            .id(id.get(1))
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
            .build();
    }

    public static ReceivingStationDto getReceivingStationDto() {
        return ReceivingStationDto.builder()
            .id(1L)
            .name("Петрівка")
            .build();
    }

    public static UbsTableCreationDto getUbsTableCreationDto() {
        return UbsTableCreationDto.builder().uuid("87df9ad5-6393-441f-8423-8b2e770b01a8").build();
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
            .images(new LinkedList<String>())
            .build();
    }

    public static ViolationDetailInfoDto getViolationDetailInfoDto() {
        LocalDateTime localdatetime = LocalDateTime.of(
            2021, Month.MARCH,
            16, 13, 00, 00);
        return ViolationDetailInfoDto.builder()
            .orderId(1L)
            .userName("Alan Po")
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
            .paymentId(1L)
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
            .uuid("abc")
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
            .amount(500l)
            .paymentStatus(PaymentStatus.PAID)
            .paymentId(1l)
            .receiptLink("somelink.com")
            .currency("UAH")
            .imagePath("")
            .order(getOrder())
            .build();
    }

    public static ManualPaymentRequestDto getManualPaymentRequestDto() {
        return ManualPaymentRequestDto.builder()
            .paymentDate("02-08-2021")
            .amount(500l)
            .receiptLink("link")
            .paymentId(1l)
            .build();
    }

    public static Order getOrderTest() {
        return Order.builder()
            .id(1L)
            .orderStatus(OrderStatus.FORMED)
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
            .language(Language.builder().id(1L).code("en").build())
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
                .build()))
            .orderDate(LocalDateTime.of(2021, 5, 15, 10, 20, 5))
            .amountOfBagsOrdered(Collections.singletonMap(1, 2))
            .pointsToUse(100)
            .user(User.builder().id(1L).currentPoints(100).build())
            .build();
    }

    public static OrderDetailStatusRequestDto getTestOrderDetailStatusRequestDto() {
        return OrderDetailStatusRequestDto.builder()
            .orderStatus("FORMED")
            .orderAdminComment("all good")
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
        Map<PositionDto, List<String>> allPositionsEmployees = new HashMap<>();
        Map<PositionDto, String> currentPositionEmployees = new HashMap<>();
        String value = getEmployee().getFirstName() + " " + getEmployee().getLastName();
        List<String> valueList = new ArrayList();
        valueList.add(value);
        allPositionsEmployees.put(getPositionDto(), valueList);
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
            .build();
    }

    private static OrderAddressExportDetailsDtoUpdate createOrderAddressDtoUpdate() {
        return OrderAddressExportDetailsDtoUpdate.builder()
            .addressId(1L)
            .orderId(1L)
            .addressHouseNumber("1")
            .addressEntranceNumber("3")
            .addressDistrict("Syhiv")
            .addressStreet("Stys")
            .addressHouseCorpus("2")
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
                .build(),
            Payment.builder()
                .id(2L)
                .paymentStatus(PaymentStatus.PAID)
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
            .lastLocation(location)
            .uuid("87df9ad5-6393-441f-8423-8b2e770b01a8")
            .recipientName("Taras")
            .build();
    }

    public static Location getLastLocation() {
        return Location.builder()
            .id(1l).locationTranslations(List.of(LocationTranslation.builder()
                .location(getLocation())
                .locationName("Name1")
                .language(Language.builder()
                    .code("ua").build())
                .build()))
            .build();
    }

    public static List<Location> getLocationList() {
        List list = new ArrayList();
        Location location = Location.builder()
            .id(2l)
            .locationTranslations(List.of(LocationTranslation.builder()
                .locationName("Name2").location(getLocation())
                .language(Language.builder().code("ua")
                    .build())
                .build()))
            .build();
        list.add(getLastLocation());
        list.add(location);
        return list;
    }

    public static List<LocationTranslation> getLocationTranslationList() {
        return List.of(LocationTranslation.builder()
            .locationName("Name1")
            .region("Name1")
            .language(getLanguage())
            .region("Name1")
            .location(Location.builder()
                .id(1L)
                .locationStatus(LocationStatus.ACTIVE)
                .build())
            .build(),
            LocationTranslation.builder()
                .locationName("Name2")
                .region("Name2")
                .language(getLanguage())
                .location(Location.builder()
                    .id(2L)
                    .locationStatus(LocationStatus.ACTIVE)
                    .build())
                .build());
    }

    public static List<LocationResponseDto> getLocationResponseDtoList() {
        return List.of(LocationResponseDto.builder()
            .id(1L)
            .name("Name1")
            .region("Name1")
            .languageCode("ua")
            .build(),
            LocationResponseDto.builder()
                .id(2L)
                .name("Name2")
                .region("Name2")
                .languageCode("ua")
                .build());

    }

    private static List<String> createAllLanguageCode() {
        return List.of("ua", "en");
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

    private static UpdateOrderDetailDto createUpdateOrderDetailDto() {
        return UpdateOrderDetailDto.builder()
            .amount(5)
            .orderId(1L)
            .bagId(1)
            .exportedQuantity(10)
            .confirmedQuantity(10)
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

    private static NotificationTemplate createNotificationTemplate() {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setTitle("Test");
        notificationTemplate.setNotificationType(NotificationType.UNPAID_ORDER);
        notificationTemplate.setNotificationReceiverType(SITE);
        notificationTemplate.setBody("Test");
        notificationTemplate.setLanguage(Language.builder().id(1L).code("ua").build());

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

    private static UserNotification createUserNotification3() {
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

    private static Order createTestOrder3() {
        return Order.builder().id(45L).user(User.builder().id(42L).build())
            .confirmedQuantity(new HashMap<>())
            .exportedQuantity(new HashMap<>())
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

    public static List<TariffTranslationDto> getTariffTranslationDto() {
        return List.of(TariffTranslationDto.builder()
            .description("Test")
            .languageId(1L)
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

    public static GetTariffServiceDto getTariffServiceDto() {
        return GetTariffServiceDto.builder()
            .fullPrice(300)
            .languageCode("ua")
            .capacity(120)
            .commission(50)
            .description("description")
            .name("name")
            .price(250)
            .build();
    }

    public static Optional<Bag> getBag() {
        return Optional.of(Bag.builder()
            .id(1)
            .capacity(120)
            .commission(50)
            .price(120)
            .fullPrice(170)
            .location(Location.builder().locationStatus(LocationStatus.ACTIVE).build())
            .createdAt(LocalDate.now())
            .createdBy("User")
            .bagTranslations(List.of(BagTranslation.builder().description("ss").id(1L).build()))
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
            .language(Language.builder().id(1L).code("ua").build())
            .name("Бавовняна сумка")
            .description("Description")
            .build();
    }

    public static Location getLocation() {
        return Location.builder()
            .id(1L)
            .locationStatus(LocationStatus.ACTIVE)
            .build();
    }

    public static Courier getCourier(CourierLimit courierLimit) {
        return Courier.builder()
            .courierTranslationList(getCourierTranslations())
            .build();
    }

    public static Courier getCourier() {
        return Courier.builder()
            .id(1L)
            .courierStatus(CourierStatus.ACTIVE)
            .courierTranslationList(getCourierTranslations())
            .courierLocations(List.of(getCourierLocations()))
            .build();
    }

    public static CourierTranslation getCourierTranslation(CourierLimit courierLimit) {
        return CourierTranslation.builder()
            .id(1L)
            .language(Language.builder().id(1L).code("ua").build())
            .name("name")
            .limitDescription("limitDescription")
            .courier(getCourier(courierLimit))
            .build();
    }

    public static List<BagTranslation> getBagTransaltion() {
        return List.of(BagTranslation.builder()
            .description("Test")
            .name("Test")
            .language(getLanguage())
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

    public static List<EcoNumberDto> getEcoNumberDto() {
        return List.of(EcoNumberDto.builder()
            .newEcoNumber("123456")
            .oldEcoNumber("22222")
            .build(),
            EcoNumberDto.builder()
                .newEcoNumber("123456")
                .oldEcoNumber("22222")
                .build());
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
            .description("test")
            .name("test")
            .languageCode("ua")
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
            .serviceTranslations(getServiceTranslationList())
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
            .name("Test")
            .description("Test")
            .language(getLanguage())
            .build();
    }

    public static CreateCourierDto getCreateCourierDto() {
        return CreateCourierDto.builder()
            .createCourierTranslationDtos(getCreateCourierTranslationDto())
            .createCourierLimitsDto(List.of(getCourierLimitsDto()))
            .build();
    }

    public static Language getLanguage() {
        return Language.builder()
            .id(1L)
            .code("ua")
            .build();
    }

    public static List<CreateCourierTranslationDto> getCreateCourierTranslationDto() {
        return List.of(CreateCourierTranslationDto.builder()
            .limitDescription("Test")
            .languageId(1L)
            .name("Test")
            .build());
    }

    public static List<CourierTranslation> getCourierTranslations() {
        return List.of(CourierTranslation.builder()
            .limitDescription("Test")
            .name("Test")
            .language(ModelUtils.getLanguage())
            .build());
    }

    public static List<OrderInfoDto> getOrderInfoDto() {
        return List.of(OrderInfoDto.builder()
            .id(1L)
            .orderPrice(24.039)
            .build());
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
            .id(1l)
            .exportedQuantity(Collections.singletonMap(1, 1))
            .amountOfBagsOrdered(Map.of(1, 1))
            .confirmedQuantity(Map.of(1, 1))
            .exportedQuantity(Map.of(1, 1))
            .pointsToUse(100)
            .build();
    }

    public static Order getOrderUserSecond() {
        return Order.builder()
            .id(2l)
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
        return OrderStatusTranslation.builder().id(1L).statusId(2L).languageId(1L).name("ds").build();
    }

    public static BagInfoDto getBagInfoDto() {
        return BagInfoDto.builder()
            .id(1)
            .price(100)
            .capacity(10)
            .build();
    }

    public static PaymentTableInfoDto getPaymentTableInfoDto() {
        return PaymentTableInfoDto.builder()
            .paidAmount(100L)
            .unPaidAmount(0L)
            .paymentInfoDtos(List.of(PaymentInfoDto.builder().build()))
            .overpayment(200L)
            .build();
    }

    public static PaymentInfoDto getInfoPayment() {
        return PaymentInfoDto.builder()
            .comment("ddd")
            .build();
    }

    public static OrderPaymentStatusTranslation getOrderPaymentStatusTranslation() {
        return OrderPaymentStatusTranslation.builder()
            .id(1L)
            .orderPaymentStatusId(1L)
            .translationValue("Abc")
            .languageId(1L)
            .build();
    }

    public static OrderFondyClientDto getOrderFondyClientDto() {
        return OrderFondyClientDto.builder()
            .orderId(1L)
            .sum(1)
            .build();
    }

    public static Order getOrderCount() {
        return Order.builder()
            .id(1L)
            .counterOrderPaymentId(2L)
            .payment(Lists.newArrayList(Payment.builder()
                .paymentId(1L)
                .amount(200L)
                .currency("UAH")
                .settlementDate("20.02.1990")
                .comment("avb")
                .paymentStatus(PaymentStatus.PAID)
                .build()))
            .build();
    }

    public static OrderLiqpayClienDto getOrderLiqpayClientDto() {
        return OrderLiqpayClienDto
            .builder()
            .orderId(1l)
            .sum(1)
            .build();
    }

    public static UpdateOrderPageAdminDto updateOrderPageAdminDto() {
        return UpdateOrderPageAdminDto.builder()
            .orderDetailStatusRequestDto(OrderDetailStatusRequestDto
                .builder()
                .orderStatus(String.valueOf(OrderStatus.CONFIRMED))
                .orderPaymentStatus(String.valueOf(PaymentStatus.PAID))
                .orderAdminComment("aaa")
                .build())
            .ubsCustomersDtoUpdate(UbsCustomersDtoUpdate
                .builder()
                .recipientId(2L)
                .recipientName("aaaaa")
                .recipientPhoneNumber("085555")
                .recipientEmail("yura@333gmail.com")
                .build())
            .orderAddressExportDetailsDtoUpdate(OrderAddressExportDetailsDtoUpdate
                .builder()
                .addressId(1L)
                .orderId(1L)
                .addressDistrict("aaaaaaa")
                .addressStreet("aaaaa")
                .addressEntranceNumber("12")
                .addressHouseCorpus("123")
                .addressHouseNumber("121")
                .addressCity("dsfsdf")
                .addressRegion("sdfsdfsd")
                .build())
            .ecoNumberFromShop(List.of(EcoNumberDto
                .builder()
                .newEcoNumber("1")
                .oldEcoNumber("2")
                .build(),
                EcoNumberDto
                    .builder()
                    .newEcoNumber("1")
                    .oldEcoNumber("2")
                    .build()))
            .exportDetailsDtoUpdate(ExportDetailsDtoUpdate
                .builder()
                .dateExport("1997-12-04T15:40:24")
                .timeDeliveryFrom("1997-12-04T15:40:24")
                .timeDeliveryTo("1990-12-11T19:30:30")
                .receivingStation(String.valueOf(ReceivingStation
                    .builder()
                    .id(1L)
                    .build()))
                .build())
            .build();
    }

    public static List<ServiceTranslation> getServiceTranslationList() {
        return List.of(ServiceTranslation.builder()
            .description("Test")
            .language(Language.builder().id(1L).code("ua").build())
            .name("Test")
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
            .locationTranslations(List.of(LocationTranslation.builder().id(1L).build()))
            .build();
    }

    public static LocationTranslation getLocationTranslation() {
        return LocationTranslation
            .builder()
            .id(1l)
            .location(Location.builder().locationStatus(LocationStatus.DEACTIVATED).build())
            .language(Language.builder().code("ua").build())
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
            .description("dd")
            .bag(Bag.builder().id(1).minAmountOfBags(MinAmountOfBag.EXCLUDE)
                .location(Location.builder()
                    .id(1L)
                    .locationStatus(LocationStatus.ACTIVE)
                    .build())
                .build())
            .language(Language.builder().id(1L).build())
            .build();
    }

    public static EditTariffInfoDto editTariffInfoDto() {
        return EditTariffInfoDto.builder()
            .bagId(1)
            .courierId(1L)
            .courierLimitsBy(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .languageId(1L)
            .limitDescription("dd")
            .maxAmountOfBigBag(1L)
            .minAmountOfBigBag(1L)
            .maxAmountOfOrder(1L)
            .minAmountOfOrder(1L)
            .minimalAmountOfBagStatus(MinAmountOfBag.EXCLUDE)
            .locationId(1L)
            .build();
    }

    public static Courier getcourierDto() {
        return Courier.builder()
            .id(1L)
            .courierTranslationList(List.of(CourierTranslation.builder()
                .id(1L)
                .limitDescription("dd")
                .name("mark")
                .build()))
            .courierStatus(CourierStatus.ACTIVE)
            .build();
    }

    public static GetServiceDto getServiceDto() {
        User user = getUser();
        return GetServiceDto.builder()
            .id(1l)
            .name("test")
            .capacity(120)
            .price(100)
            .commission(50)
            .description("test")
            .fullPrice(150)
            .editedAt(LocalDate.now())
            .editedBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .languageCode("ua")
            .courierId(1L)
            .build();
    }

    public static LocationCreateDto getLocationCreateDto() {
        return LocationCreateDto.builder()
            .addLocationDtoList(List.of(getAddLocationTranslationDto()))
            .build();
    }

    public static AddLocationTranslationDto getAddLocationTranslationDto() {
        return AddLocationTranslationDto.builder()
            .locationName("Name1")
            .languageId(1L)
            .region("Name1")
            .build();
    }

    public static ServiceTranslationDto getServiceTranslationDto() {
        return ServiceTranslationDto.builder()
            .description("Test")
            .languageId(1L)
            .name("Test")
            .build();
    }

    public static CourierLocation getCourierLocations() {
        return CourierLocation.builder()
            .maxAmountOfBigBags(99L)
            .minAmountOfBigBags(2L)
            .maxPriceOfOrder(100000L)
            .minPriceOfOrder(500L)
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .location(getLocation())
            .build();
    }

    public static GetCourierLocationDto getCourierLocationsDto() {
        return GetCourierLocationDto.builder()
            .courierLocationId(getLocation().getId())
            .minPriceOfOrder(500L)
            .maxPriceOfOrder(20000L)
            .courierLimit("LIMIT_BY_AMOUNT_OF_BAG")
            .minAmountOfBigBags(2L)
            .maxAmountOfBigBags(20L)
            .locationsDtos(List.of(LocationsDto.builder()
                .locationStatus(getLocation().getLocationStatus().toString())
                .locationId(getLocation().getId())
                .build()))
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

    public static LimitsDto getCourierLimitsDto() {
        return LimitsDto.builder()
            .locationId(1L)
            .maxAmountOfBigBags(99L)
            .minAmountOfBigBags(2L)
            .maxPriceOfOrder(100000L)
            .minPriceOfOrder(500L)
            .build();
    }

    public static CreateCourierDto createCourier() {
        return CreateCourierDto.builder()
            .createCourierLimitsDto(List.of(LimitsDto.builder()
                .minPriceOfOrder(500L)
                .maxPriceOfOrder(500000L)
                .minAmountOfBigBags(2L)
                .maxAmountOfBigBags(50L)
                .build()))
            .createCourierTranslationDtos(List.of(CreateCourierTranslationDto.builder()
                .name("Test")
                .limitDescription("Test")
                .build()))
            .build();
    }

    public static GetServiceDto getAllInfoAboutService() {
        User user = getUser();
        return GetServiceDto.builder()
            .name("Test")
            .capacity(120)
            .price(100)
            .commission(50)
            .description("Test")
            .fullPrice(150)
            .id(1L)
            .createdAt(LocalDate.now())
            .createdBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .languageCode("ua")
            .courierId(1L)
            .build();
    }
}
