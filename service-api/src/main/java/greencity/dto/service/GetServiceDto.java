package greencity.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class GetServiceDto {
    @NotNull
    private Long id;

    @NotNull
    private Double price;

    @NotBlank
    private String nameUk;

    @NotBlank
    private String nameEn;

    @NotBlank
    private String descriptionUk;

    @NotBlank
    private String descriptionEn;
}
