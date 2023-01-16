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

    Long id;

    @NotNull
    Integer price;

    @NotNull
    String name;

    @NotNull
    String nameEng;

    String description;

    String descriptionEng;
}
