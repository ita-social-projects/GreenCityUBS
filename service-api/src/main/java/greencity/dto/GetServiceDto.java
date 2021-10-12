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
public class GetServiceDto {
    @NotNull
    String name;
    Integer capacity;
    @NotNull
    Integer price;
    Integer commission;
    String description;
    Integer fullPrice;
    Long id;
    LocalDate createdAt;
    String createdBy;
    LocalDate editedAt;
    String editedBy;
    Long locationId;
    String languageCode;
}
