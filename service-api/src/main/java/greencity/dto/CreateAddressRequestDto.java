package greencity.dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class CreateAddressRequestDto {
    // CHECKSTYLE:OFF
    private final String VALIDATION_MESSAGE = "use only English,or Ukrainian latter";
    private final String NOT_EMPTY_MESSAGE = "name must not be empty";
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
