package greencity.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class GetTariffServiceDto {
    @NotNull
    private Integer id;

    @NotNull
    private Integer capacity;

    @NotNull
    private Double price;

    @NotNull
    private Double commission;

    @NotNull
    private Double fullPrice;

    @NotBlank
    private String name;

    @NotBlank
    private String nameEng;

    @NotBlank
    private String description;

    @NotBlank
    private String descriptionEng;

    @NotNull
    private Boolean limitIncluded;
}
