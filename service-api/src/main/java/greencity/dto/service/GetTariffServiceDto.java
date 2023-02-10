package greencity.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    private Integer price;
    @NotNull
    private Integer commission;
    @NotNull
    private Integer fullPrice;
    @NotNull
    private String name;
    @NotNull
    private String nameEng;
    @NotNull
    private String description;
    @NotNull
    private String descriptionEng;
    @NotNull
    private Boolean limitIncluded;
}
