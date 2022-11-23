package greencity.dto.service;

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
public class GetServiceDto {
    @NotNull
    String name;
    @NotNull
    String nameEng;
    Integer capacity;
    @NotNull
    Integer price;
    Integer commission;
    String description;
    String descriptionEng;
    Integer fullPrice;
    Long id;
    LocalDate createdAt;
    String createdBy;
    LocalDate editedAt;
    String editedBy;
    Long courierId;
}
