package greencity.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceDto {
    @NotBlank
    @Length(max = 255)
    private String name;

    @NotBlank
    @Length(max = 255)
    private String nameEng;

    @NotBlank
    private String description;

    @NotBlank
    private String descriptionEng;

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "999999.99")
    @Digits(integer = 6, fraction = 2)
    private Double price;
}