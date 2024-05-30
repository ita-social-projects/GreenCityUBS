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
            .receivingStationsIdList(List.of(1L))
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
