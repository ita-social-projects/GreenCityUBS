package greencity.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class AddingReceivingStationDto {
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String receivingStation;
}
