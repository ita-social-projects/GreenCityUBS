package greencity.dto.service;

import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateServiceDto {
    @NotNull
    String name;
    @NotNull
    String nameEng;

    String description;

    String descriptionEng;

    @NotNull
    Integer price;

    @NotNull
    Long tariffsInfoId;
}