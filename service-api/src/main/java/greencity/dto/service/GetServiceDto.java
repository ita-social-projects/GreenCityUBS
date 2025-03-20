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
    private String name;

    @NotBlank
    private String nameEng;

    @NotBlank
    private String description;

    @NotBlank
    private String descriptionEng;
}
