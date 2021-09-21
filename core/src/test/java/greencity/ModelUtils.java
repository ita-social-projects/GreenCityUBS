package greencity;

import greencity.dto.*;
import greencity.entity.coords.Coordinates;
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
            .personalData(PersonalDataDto.builder()
                .firstName("Anton")
                .lastName("Antonov")
                .id(13L)
                .email("mail@mail.ua")
                .phoneNumber("0678945221")
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
            .exportedDate("2020")
            .exportedTime("10:20:30")
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

    public static AssignEmployeeForOrderDto assignEmployeeToOrderDto() {
        return AssignEmployeeForOrderDto.builder()
            .positionId(1L)
            .employeeId(1L)
            .build();
    }
}
