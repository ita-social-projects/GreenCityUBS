package greencity;

import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.AddressStatus;
import greencity.entity.enums.CancellationReason;
import greencity.entity.enums.OrderStatus;
import greencity.entity.enums.PaymentStatus;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.Address;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static greencity.entity.enums.ViolationLevel.MAJOR;

public class ModelUtils {
    public static Principal getPrincipal() {
        return () -> "test@gmail.com";
    }

    public static Principal getUuid() {
        return () -> "35467585763t4sfgchjfuyetf";
    }

    public static List<EventDto> getListEventsDTOS() {
        List<EventDto> eventDTOS = new ArrayList<>();
        eventDTOS.add(EventDto.builder()
            .id(1L)
            .eventDate(LocalDateTime.now())
            .eventName("Holiday")
            .authorName("Oleg").build());

        eventDTOS.add(EventDto.builder()
            .id(1L)
            .eventDate(LocalDateTime.now())
            .eventName("Slovak")
            .authorName("Kola").build());

        eventDTOS.add(EventDto.builder()
            .id(1L)
            .eventDate(LocalDateTime.now())
            .eventName("Weekend")
            .authorName("Martin").build());
        return eventDTOS;
    }

    public static OrderResponseDto getOrderResponseDto() {
        return OrderResponseDto.builder()
            .additionalOrders(new HashSet<>(Arrays.asList("232534634")))
            .bags(Collections.singletonList(new BagDto(3, 999)))
            .orderComment("comment")
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .shouldBePaid(true)
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

    public static NotificationDto getNotificationDto() {
        return NotificationDto.builder()
            .title("Test")
            .body("Test")
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
            .build();
    }

    public static Employee getEmployee() {
        return Employee.builder()
            .id(1L)
            .firstName("Петро")
            .lastName("Петренко")
            .phoneNumber("+380935577455")
            .email("test@gmail.com")
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
            .recipientId(1l)
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

    public static OrderDetailInfoDto getOrderDetailInfoDto() {
        return OrderDetailInfoDto.builder()
            .orderId(1L)
            .capacity(10)
            .price(400)
            .amount(100)
            .exportedQuantity(100)
            .confirmedQuantity(200)
            .name("test")
            .bagId(3)
            .build();
    }

    public static OrderDetailStatusDto getOrderDetailStatusDto() {
        return OrderDetailStatusDto.builder()
            .paymentStatus(PaymentStatus.PAID.name())
            .orderStatus(OrderStatus.CONFIRMED.name())
            .date(LocalDateTime.now().toString())
            .build();
    }

    public static ExportDetailsDto getOrderDetailExportDto() {
        return ExportDetailsDto.builder()
            .dateExport("20-12-2001")
            .timeDeliveryFrom("20:20:20")
            .timeDeliveryTo("20:20:20")
            .receivingStation("Petrivka")
            .allReceivingStations(Arrays.asList("a,b,v"))
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

    public static ViolationDetailInfoDto getViolationDetailInfoDto() {
        LocalDateTime localdatetime = LocalDateTime.of(
            2021, Month.MARCH,
            16, 13, 00, 00);

        return ViolationDetailInfoDto.builder()
            .orderId(1L)
            .userName("Alan Po")
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

    public static ManualPaymentResponseDto getResponseDto() {
        return ManualPaymentResponseDto.builder()
            .amount(500l)
            .paymentDate("09-02-2021")
            .paymentId(10l)
            .currentDate("10-02-2021")
            .receiptLink("somelink.com")
            .imagePath("imagepath")
            .build();
    }

    public static ManualPaymentRequestDto getRequestDto() {
        return ManualPaymentRequestDto.builder()
            .amount(500l)
            .paymentDate("09-02-2021")
            .receiptLink("somelink.com")
            .paymentId(10l)
            .build();
    }

    public static OrderCancellationReasonDto getCancellationDto() {
        return OrderCancellationReasonDto.builder()
            .cancellationReason(CancellationReason.OTHER)
            .cancellationComment("Garbage disappeared")
            .build();
    }

    public static LocationIdDto getLocationIdDto() {
        return LocationIdDto.builder()
            .locationId(1l)
            .build();
    }

    public static List<TariffTranslationDto> getTariffTranslationDto() {
        return List.of(TariffTranslationDto.builder()
            .name("Test")
            .languageId(1L)
            .description("Test")
            .build());
    }

    public static AddServiceDto getAddServiceDto() {
        return AddServiceDto.builder()
            .capacity(120)
            .commission(10)
            .price(100)
            .tariffTranslationDtoList(getTariffTranslationDto())
            .build();
    }

    public static List<ServiceTranslationDto> getServiceTranslationDto() {
        return List.of(ServiceTranslationDto.builder()
            .name("Test")
            .languageId(1L)
            .description("Test")
            .build());
    }

    public static CreateServiceDto createServiceDto() {
        return CreateServiceDto.builder()
            .capacity(120)
            .commission(10)
            .price(100)
            .serviceTranslationDtoList(getServiceTranslationDto())
            .build();
    }

    public static AssignEmployeesForOrderDto assignEmployeeToOrderDto() {
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

    public static AdminCommentDto getAdminComment() {
        return AdminCommentDto.builder()
            .orderId(1L)
            .adminComment("Admin").build();
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

    public static EditServiceDto getEditServiceDto() {
        return EditServiceDto.builder()
            .name("Бавовняна сумка")
            .capacity(120)
            .price(120)
            .commission(50)
            .description("Description")
            .locationId(1L)
            .languageCode("ua")
            .build();
    }

    public static EditAmountOfBagDto getAmountOfSum() {
        return EditAmountOfBagDto.builder()
            .minAmountOfBigBags(1L)
            .maxAmountOfBigBags(2L)
            .build();
    }

    public static EditTariffInfoDto getEditTariffInfoDto() {
        return EditTariffInfoDto.builder()
            .minAmountOfBigBag(2L)
            .maxAmountOfBigBag(3L)
            .minAmountOfOrder(1L)
            .maxAmountOfOrder(2L)
            .courierId(3L)
            .bagId(2)
            .limitDescription("dd")
            .languageId(3L)
            .build();
    }

    public static PaymentResponseDto getPaymentResponseDto() {
        return PaymentResponseDto.builder()
            .order_id("1")
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

    public static OverpaymentInfoRequestDto getOverpaymentInfoRequestDto() {
        return OverpaymentInfoRequestDto.builder()
            .bonuses(1L)
            .overpayment(2L)
            .comment("ss")
            .build();
    }

    public static OrderFondyClientDto getOrderFondyClientDto() {
        return OrderFondyClientDto.builder()
            .orderId(1L)
            .sum(2)
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
            .exportDetailsDto(ExportDetailsDtoUpdate
                .builder()
                .dateExport("20-12-2001")
                .timeDeliveryFrom("20:20:20")
                .timeDeliveryTo("20:20:20")
                .receivingStation(String.valueOf(ReceivingStation
                    .builder()
                    .id(1L)
                    .build()))
                .build())
            .updateOrderDetailDto(List.of(
                UpdateOrderDetailDto.builder()
                    .amount(1)
                    .exportedQuantity(1)
                    .confirmedQuantity(1)
                    .exportedQuantity(1)
                    .bagId(1)
                    .build()))
            .build();
    }

    public static LocationCreateDto getLocationCreateDto() {
        return LocationCreateDto.builder()
            .addLocationDtoList(List.of(getAddLocationTranslationDto())).build();
    }

    public static AddLocationTranslationDto getAddLocationTranslationDto() {
        return AddLocationTranslationDto.builder()
            .region("Test")
            .locationName("Test")
            .languageId(1L)
            .build();
    }
}
