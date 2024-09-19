package greencity;

import greencity.dto.AddNewTariffDto;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.position.PositionDto;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import java.util.List;

public class ModelUtils {

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

    public static CreateAddressRequestDto createDefaultAddress() {
        return CreateAddressRequestDto.builder()
            .districtEn("Shevchenkivskyi")
            .district("Шевченківський")
            .regionEn("Kyiv")
            .region("Київ")
            .houseNumber("12A")
            .entranceNumber("1")
            .houseCorpus("B")
            .coordinates(new CoordinatesDto(50.4501, 30.5234))
            .city("Київ")
            .cityEn("Kyiv")
            .street("Khreshchatyk")
            .streetEn("Khreshchatyk")
            .build();
    }

    public static CreateAddressRequestDto createDifferentAddress() {
        return CreateAddressRequestDto.builder()
            .districtEn("Holosiivskyi")
            .district("Голосіївський")
            .regionEn("Kyiv")
            .region("Київ")
            .houseNumber("15")
            .entranceNumber("2")
            .houseCorpus("A")
            .coordinates(new CoordinatesDto(50.4012, 30.5184))
            .city("Київ")
            .cityEn("Kyiv")
            .street("Holosiivskyi")
            .streetEn("Holosiivskyi")
            .build();
    }
}
