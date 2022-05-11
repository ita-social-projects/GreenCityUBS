package greencity.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum LocationStatus {
    ACTIVE("Активно"), DEACTIVATED("Неактивно"), NEW("Новостворено");

    private String status;
}
