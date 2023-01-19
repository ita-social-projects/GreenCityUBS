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
public class ServiceDto {
    @NotNull
    Long id;

    @NotNull
    Integer price;

    @NotNull
    String name;

    @NotNull
    String nameEng;

    @NotNull
    String description;

    @NotNull
    String descriptionEng;
}
