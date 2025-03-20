package greencity.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class TariffServiceDto {
    @Min(1)
    @Max(999)
    @NotNull
    private Integer capacity;

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "999999.99")
    @Digits(integer = 6, fraction = 2)
    private Double price;

    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "999999.99")
    @Digits(integer = 6, fraction = 2)
    private Double commission;

    @NotBlank
    @Length(min = 1, max = 255)
    private String name;

    @NotBlank
    @Length(min = 1, max = 255)
    private String nameEng;

    @NotBlank
    @Length(min = 1, max = 255)
    private String description;

    @NotBlank
    @Length(min = 1, max = 255)
    private String descriptionEng;
}
