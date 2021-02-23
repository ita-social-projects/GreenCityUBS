package greencity.dto;

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
    @Length(min = 3, max = 30)
    private String city;
    @NotBlank
    @Length(min = 3)
    private String district;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Length(max = 2)
    private String entranceNumber;
    @NotBlank
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-').]{1,30}")
    private String firstName;
    @Length(max = 5)
    private String houseCorpus;
    @NotBlank
    @Length(min = 1, max = 4)
    private String houseNumber;
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
