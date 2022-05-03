package greencity.dto.certificate;

import lombok.*;

import javax.validation.constraints.*;

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
    @Max(1000)
    private int points;
}
