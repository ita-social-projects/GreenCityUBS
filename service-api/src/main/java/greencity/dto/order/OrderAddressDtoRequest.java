package greencity.dto.order;

import greencity.entity.coords.Coordinates;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id", "actual"})
@ToString
@Builder
public class OrderAddressDtoRequest {
    @Max(1000000)
    private Long id;
    @NotBlank
    @Length(max = 30)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-\\s'.]{3,30}")
    private String region;
    @NotBlank
    @Length(max = 30)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-\\s'.]{3,30}")
    private String city;
    @NotBlank
    @Length(max = 30)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-\\s'.]{3,30}")
    private String district;
    @Length(max = 4)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Z0-9a-z-.]{0,2}")
    private String entranceNumber;
    @Length(max = 5)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Z0-9a-z-.]{0,5}")
    private String houseCorpus;
    @Length(max = 5)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Z0-9a-z-.]{1,4}")
    private String houseNumber;
    @Length(max = 50)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Z0-9a-z-\\s',]{3,40}")
    private String street;

    @Length(max = 255)
    private String addressComment;

    private String searchAddress;

    private Coordinates coordinates;

    private Boolean actual;
    @Length(max = 30)
    @Pattern(regexp = "[a-zA-Z-\\s'.]{3,30}")
    private String cityEn;
    @Length(max = 30)
    @Pattern(regexp = "[a-zA-Z-\\s'.]{3,30}")
    private String regionEn;
    @Length(max = 50)
    @Pattern(regexp = "[a-zA-Z0-9-\\s'.]{3,40}")
    private String streetEn;
    @Length(max = 30)
    @Pattern(regexp = "[a-zA-Z-\\s'.]{3,30}")
    private String districtEn;
    @NotEmpty
    private String placeId;
}
