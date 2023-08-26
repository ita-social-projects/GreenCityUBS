package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import static greencity.constant.ValidationConstant.STREET_REGEXP;
import static greencity.constant.ValidationConstant.STREET_VALIDATION_MESSAGE;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"placeId", "searchAddress", "addressComment"})
@ToString
@Builder
public class CreateAddressRequestDto {
    // CHECKSTYLE:OFF
    private static final String validationMessage = "Use only English, or Ukrainian letter";
    private static final String notEmptyValidationMessage = "Name must not be empty";
    private static final String houseNumberNotValid = "House number is invalid";

    @Pattern(regexp = "[-A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9.,ʼ'`ʹ—/\"\\s]*", message = validationMessage)
    private String searchAddress;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = notEmptyValidationMessage)
    private String districtEn;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = notEmptyValidationMessage)
    private String district;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = notEmptyValidationMessage)
    private String regionEn;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = notEmptyValidationMessage)
    private String region;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9.,ʼ'`ʹ—/\"\\s]" + "{1,10}", message = houseNumberNotValid)
    @NotBlank(message = notEmptyValidationMessage)
    private String houseNumber;

    private String entranceNumber;

    private String houseCorpus;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ 0-9.,ʼ'`ʹ!?]*", message = validationMessage)
    private String addressComment;

    private String placeId;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = notEmptyValidationMessage)
    private String city;

    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = notEmptyValidationMessage)
    private String cityEn;

    @Pattern(regexp = STREET_REGEXP, message = STREET_VALIDATION_MESSAGE)
    @NotEmpty(message = notEmptyValidationMessage)
    private String street;

    @Pattern(regexp = STREET_REGEXP, message = STREET_VALIDATION_MESSAGE)
    @NotEmpty(message = notEmptyValidationMessage)
    private String streetEn;
}
