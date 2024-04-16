package greencity.dto.tariff;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TariffWithChatAccess {
    private Long tariffId;
    private Boolean hasChat;
}
