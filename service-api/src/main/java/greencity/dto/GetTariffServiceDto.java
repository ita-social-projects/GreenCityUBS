package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class GetTariffServiceDto {
    @NotNull
    private String name;
    @NotNull
    private Integer capacity;
    @NotNull
    private Integer price;
    private Integer commission;
    private String description;
    private String nameEng;
    private Integer fullPrice;
    private Integer id;
    private LocalDate createdAt;
    private String createdBy;
    private LocalDate editedAt;
    private String editedBy;
    private Long locationId;
    private String minAmountOfBag;
}
