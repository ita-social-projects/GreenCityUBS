package greencity.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class GetServiceDto {
    @NotNull
    @Min(1)
    private Long id;

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "999999.99")
    @Digits(integer = 6, fraction = 2)
    private Double price;

    @NotBlank
    @Length(min = 1, max = 30)
    private String name;

    @NotBlank
    @Length(min = 1, max = 30)
    private String nameEng;

    @NotBlank
    @Length(min = 1, max = 255)
    private String description;

    @NotBlank
    @Length(min = 1, max = 255)
    private String descriptionEng;
}
