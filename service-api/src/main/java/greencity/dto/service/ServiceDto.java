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
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceDto {
    @NotNull
    @Length(min = 1, max = 30)
    private String name;

    @NotNull
    @Length(min = 1, max = 30)
    private String nameEng;

    @NotNull
    @Length(min = 1, max = 255)
    private String description;

    @NotNull
    @Length(min = 1, max = 255)
    private String descriptionEng;

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "999999.99")
    @Digits(integer = 6, fraction = 2)
    private Double price;
}