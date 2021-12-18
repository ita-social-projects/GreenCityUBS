package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateServiceDto {
    Integer capacity;
    @NotNull
    Integer price;
    Integer commission;
    @NotNull
    Long courierId;
    List<ServiceTranslationDto> serviceTranslationDtoList;
}