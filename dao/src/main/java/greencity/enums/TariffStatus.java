package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum TariffStatus {
    ACTIVE(1, "Активно"), NEW(2, "Новостворено"), DEACTIVATED(3, "Неактивно");

    private int priority;
    private String status;
}
