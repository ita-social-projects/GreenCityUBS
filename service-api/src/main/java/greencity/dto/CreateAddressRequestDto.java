package greencity.dto;

import greencity.dto.location.CoordinatesDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.experimental.SuperBuilder;
import java.util.Objects;
import static greencity.constant.ValidationConstant.STREET_REGEXP;
import static greencity.constant.ValidationConstant.STREET_VALIDATION_MESSAGE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder
@EqualsAndHashCode
public class CreateAddressRequestDto {
    // CHECKSTYLE:OFF
    private static final String VALIDATION_MESSAGE = "Use only English, or Ukrainian letter";
    private static final String NOT_EMPTY_VALIDATION_MESSAGE = "Name must not be empty";
    private static final String HOUSE_NUMBER_NOT_VALID = "House number is invalid";
    private static final String ADDRESS_COMMENT_LENGTH_ERROR_MESSAGE =
        "Address comment must be 255 characters or fewer";
    private static final String ADDRESS_COMMENT_INPUT_ERROR_MESSAGE = "Address comment must not be only whitespace";

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ’]*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_VALIDATION_MESSAGE)
    private String districtEn;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ’]*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_VALIDATION_MESSAGE)
    private String districtUk;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ ʼ'`ʹ’]*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_VALIDATION_MESSAGE)
    private String regionEn;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ ʼ'`ʹ’]*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_VALIDATION_MESSAGE)
    private String regionUk;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9.,ʼ'`ʹ’—/\"\\s]" + "{1,10}", message = HOUSE_NUMBER_NOT_VALID)
    @NotBlank(message = NOT_EMPTY_VALIDATION_MESSAGE)
    private String houseNumber;

    private String entranceNumber;

    private String houseCorpus;

    @Size(max = 255, message = ADDRESS_COMMENT_LENGTH_ERROR_MESSAGE)
    @Pattern(regexp = "^$|\\s*\\S[\\s\\S]*", message = ADDRESS_COMMENT_INPUT_ERROR_MESSAGE)
    private String addressComment;

    private String placeId;

    @NotNull
    private CoordinatesDto coordinates;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ’]*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_VALIDATION_MESSAGE)
    private String cityUk;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ’]*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_VALIDATION_MESSAGE)
    private String cityEn;

    @Pattern(regexp = STREET_REGEXP, message = STREET_VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_VALIDATION_MESSAGE)
    private String streetUk;

    @Pattern(regexp = STREET_REGEXP, message = STREET_VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_VALIDATION_MESSAGE)
    private String streetEn;

    public boolean areAddressesEqual(CreateAddressRequestDto otherAddress) {
        if (otherAddress == null) {
            return false;
        }
        return (Objects.equals(regionUk, otherAddress.getRegionUk())
            || Objects.equals(regionEn, otherAddress.getRegionEn()))
            && (Objects.equals(cityUk, otherAddress.getCityUk()) || Objects.equals(cityEn, otherAddress.getCityEn()))
            && (Objects.equals(districtUk, otherAddress.getDistrictUk())
                || Objects.equals(districtEn, otherAddress.getDistrictEn()))
            && Objects.equals(houseNumber, otherAddress.getHouseNumber())
            && Objects.equals(entranceNumber, otherAddress.getEntranceNumber())
            && Objects.equals(houseCorpus, otherAddress.getHouseCorpus());
    }
}