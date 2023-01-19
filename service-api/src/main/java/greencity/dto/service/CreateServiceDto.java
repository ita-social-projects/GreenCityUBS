package greencity.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

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

    @NotNull
    String description;

    @NotNull
    String descriptionEng;

    @NotNull
    Integer price;

    @NotNull
    Long tariffId;
}