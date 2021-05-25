package greencity.dto;

import greencity.entity.coords.Coordinates;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class OrderAddressDtoRequest {
    @Max(1000000)
    private Long id;
    @NotBlank
    @Length(max = 12)
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

    private Coordinates coordinates;

    private Boolean actual;
}
