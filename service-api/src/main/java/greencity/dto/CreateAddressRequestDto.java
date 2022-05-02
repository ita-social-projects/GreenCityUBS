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
    private final static String VALIDATION_MESSAGE = "use only English,or Ukrainian latter";
    private final static String NOT_EMPTY_MESSAGE = "name must not be empty";
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ ,0-9']*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_MESSAGE)
    private String searchAddress;
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ ']*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_MESSAGE)
    private String districtEn;
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ ']*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_MESSAGE)
    private String district;
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ ']*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = "name must not be empty")
    private String regionEn;
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ ']*", message = VALIDATION_MESSAGE)
    @NotEmpty(message = NOT_EMPTY_MESSAGE)
    private String region;
    @Min(1)
    private String houseNumber;
    private String entranceNumber;
    private String houseCorpus;
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ 0-9']*", message = VALIDATION_MESSAGE)
    private String addressComment;
}
