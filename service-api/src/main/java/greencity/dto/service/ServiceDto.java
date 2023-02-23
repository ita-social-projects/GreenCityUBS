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
public class ServiceDto {
    @NotNull
    private String name;

    @NotNull
    private String nameEng;

    @NotNull
    private String description;

    @NotNull
    private String descriptionEng;

    @NotNull
    private Integer price;
}