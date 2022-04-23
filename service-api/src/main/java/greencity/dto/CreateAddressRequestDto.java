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
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ ,0-9']*", message = "use only English,or Ukrainian latter")
    @NotEmpty(message = "name must not be empty")
    private String searchAddress;
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ ']*", message = "use only English,or Ukrainian latter")
    @NotEmpty(message = "name must not be empty")
    private String districtEng;
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ ']*", message = "use only English,or Ukrainian latter")
    @NotEmpty(message = "name must not be empty")
    private String districtUa;
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ ']*", message = "use only English,or Ukrainian latter")
    @NotEmpty(message = "name must not be empty")
    private String regionEng;
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ ']*", message = "use only English,or Ukrainian latter")
    @NotEmpty(message = "name must not be empty")
    private String regionUa;
    @Min(1)
    private String houseNumber;
    private String entranceNumber;
    private String houseCorpus;
    @Pattern(regexp = "[A-Za-zА-Яа-яЇїІіЄєҐґ 0-9']*", message = "use only English,or Ukrainian latter")
    @NotEmpty(message = "name must not be empty")
    private String addressComment;
}
