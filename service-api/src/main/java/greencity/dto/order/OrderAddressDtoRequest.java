package greencity.dto.order;

import greencity.entity.coords.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import static greencity.constant.ValidationConstant.STREET_REGEXP;
import static greencity.constant.ValidationConstant.STREET_VALIDATION_MESSAGE;

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

    @Length(max = 10)
    @Pattern(regexp = "[-A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9.,ʼ'`ʹ—/\"\\s]" + "{1,10}")
    private String houseNumber;

    @Length(max = 50)
    @Pattern(regexp = STREET_REGEXP, message = STREET_VALIDATION_MESSAGE)
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
    @Pattern(regexp = STREET_REGEXP, message = STREET_VALIDATION_MESSAGE)
    private String streetEn;

    @Length(max = 30)
    @Pattern(regexp = "[a-zA-Z-\\s'.]{3,30}")
    private String districtEn;

    private String placeId;
}
