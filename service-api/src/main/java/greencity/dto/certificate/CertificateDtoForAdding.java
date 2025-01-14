package greencity.dto.certificate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class CertificateDtoForAdding {
    @NotBlank
    @Pattern(regexp = "(\\d{4}-\\d{4})|(^$)", message = "This certificate code is not valid")
    private String code;

    @NotNull
    @Min(0)
    @Max(12)
    private int monthCount;

    @NotNull
    @Min(0)
    @Max(9999)
    private int points;

    @NotNull
    @Min(1)
    @Max(9999)
    private int initialPointsValue;
}
