package greencity.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class CreateAddressRequestDto {
    // CHECKSTYLE:OFF
    private static final String validationMessage = "use only English,or Ukrainian letter";
    private static final String notEmptyValidationMessage = "name must not be empty";
    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ 0-9.,ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = notEmptyValidationMessage)
    private String searchAddress;
    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = notEmptyValidationMessage)
    private String districtEn;
    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ .,ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = notEmptyValidationMessage)
    private String district;
    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = "name must not be empty")
    private String regionEn;
    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ ʼ'`ʹ]*", message = validationMessage)
    @NotEmpty(message = notEmptyValidationMessage)
    private String region;
    @Min(1)
    private String houseNumber;
    private String entranceNumber;
    private String houseCorpus;
    @Pattern(regexp = "[-A-Za-zА-Яа-яЇїІіЄєҐґ 0-9.,ʼ'`ʹ!?]*", message = validationMessage)
    private String addressComment;
}
