package greencity.dto.address;

import greencity.entity.coords.Coordinates;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class AddressDto implements Serializable {
    @NotNull
    @Min(1)
    private Long id;
    @NotBlank
    @Length(max = 30)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-я\\s-'.]")
    private String city;
    @NotBlank
    @Length(max = 30)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-я\\s-'.]")
    private String district;
    @NotBlank
    @Length(max = 30)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-я\\s-'.]")
    private String region;
    @Length(max = 4)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z0-9\\s-'.]")
    private String entranceNumber;
    @Length(max = 5)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z0-9\\s-.]")
    private String houseCorpus;
    @Length(max = 5)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z0-9\\s-.]")
    private String houseNumber;
    @Length(max = 50)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-я\\s-'.]")
    private String street;

    private String addressComment;

    private Coordinates coordinates;

    private Boolean actual;
    @NotBlank
    @Length(max = 30)
    @Pattern(regexp = "[A-Za-z\\s-'.]")
    private String cityEn;
    @NotBlank
    @Length(max = 30)
    @Pattern(regexp = "[A-Za-z\\s-'.]")
    private String regionEn;
    @Length(max = 50)
    @Pattern(regexp = "[A-Za-z\\s-'.]")
    private String streetEn;
    @NotBlank
    @Length(max = 30)
    @Pattern(regexp = "[A-Za-z\\s-'.]")
    private String districtEn;
}