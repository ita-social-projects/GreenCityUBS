package greencity.dto.tariff;

import lombok.Data;

@Data
public class TariffWithChatAccess {
    private Long tariffId;
    private Boolean hasChat;
}
