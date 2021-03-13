package greencity.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class PersonalDataDto implements Serializable {
    @Length(max = 170)
    private String addressComment;
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-').]{3,30}")
    private String city;
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-').]{3,30}")
    private String district;
    @NotBlank
    @Email
    private String email;
    @Length(max = 2)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Z0-9a-z-.]{0,2}")
    private String entranceNumber;
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-').]{1,30}")
    private String firstName;
    @Length(max = 5)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Z0-9a-z-.]{0,5}")
    private String houseCorpus;
    @NotBlank
    @Length(min = 1, max = 4)
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Z0-9a-z-.]{1,4}")
    private String houseNumber;
    @Min(1)
    @Max(1000000)
    private Long id;
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-').]{1,30}")
    private String lastName;
    private double latitude;
    private double longitude;
    @NotBlank
    @Pattern(regexp = "[0-9]{9}")
    private String phoneNumber;
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-',]{3,40}")
    private String street;
}
