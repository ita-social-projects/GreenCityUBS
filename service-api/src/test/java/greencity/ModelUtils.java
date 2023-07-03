package greencity;

import greencity.dto.AddNewTariffDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.position.PositionDto;
import greencity.dto.table.UbsTableCreationDto;
import greencity.dto.user.UserVO;
import greencity.dto.violation.UserViolationMailDto;
import greencity.entity.user.Location;
import greencity.entity.user.User;

import java.util.List;

public class ModelUtils {

    public static UserVO getUserVO() {
        return UserVO.builder()
            .id(13L)
            .email("email").build();
    }

    public static UbsTableCreationDto getUbsTableCreationDto() {
        return UbsTableCreationDto.builder()
            .uuid("87df9ad5-6393-441f-8423-8b2e770b01a8")
            .build();
    }

    public static UserViolationMailDto getUserViolationMailDto() {
        return UserViolationMailDto.builder()
            .name("String")
            .email("string@gmail.com")
            .violationDescription("Description")
            .build();
    }

    public static Location getLocation() {
        return Location.builder()
            .id(42L)
            .nameEn("Lviv")
            .nameUk("Львів")
            .build();
    }

    public static User getUser() {
        return User.builder()
            .recipientName("Ivan")
            .recipientSurname("Boiko")
            .build();
    }

    public static CreateCourierDto getCreateCourierDto() {
        return CreateCourierDto.builder()
            .nameUk("Тест")
            .nameEn("Test")
            .build();
    }

    public static PaymentResponseDto getPaymentResponseDto() {
        return PaymentResponseDto.builder()
            .actual_amount(10)
            .actual_currency("USD")
            .amount(2)
            .approval_code("123")
            .card_bin(444455)
            .card_type("VISA")
            .currency("USD")
            .eci(6)
            .fee(10)
            .masked_card("1")
            .merchant_data("03/08/22")
            .merchant_id(3)
            .order_id("234")
            .order_status("approved")
            .order_time("13/08/22")
            .payment_id(85233820)
            .payment_system("card")
            .product_id("324")
            .rectoken("Y")
            .rectoken_lifetime("256")
            .response_code(200)
            .response_description("")
            .response_status("APPROVED")
            .reversal_amount(2)
            .rrn("IADG/0000000C00000000")
            .sender_account("sender")
            .sender_cell_phone("+4930901820")
            .sender_email("sender@mail.com")
            .settlement_currency("USD")
            .settlement_date("03/08/22")
            .tran_type("purchase")
            .verification_status("ACTIVE")
            .parent_order_id(1)
            .build();
    }

    public static PaymentRequestDto getPaymentRequestDto() {
        return PaymentRequestDto.builder()
            .orderId("1")
            .merchantId(2)
            .orderDescription("")
            .currency("USD")
            .amount(2L)
            .signature("")
            .responseUrl("responseUrl")
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

    public static AddNewTariffDto getAddNewTariffWithNullFieldsDto() {
        return AddNewTariffDto.builder()
            .regionId(null)
            .courierId(1L)
            .locationIdList(List.of(1L))
            .receivingStationsIdList(null)
            .build();
    }

    public static PositionDto getEmployeePosition() {
        return PositionDto.builder()
            .id(1L)
            .name("Менеджер послуги")
            .name("Service Manager")
            .build();
    }
}
