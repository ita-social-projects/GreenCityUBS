package greencity.dto.telegram;

import greencity.enums.MessageDeliveryStatus;
import greencity.enums.MessageViewingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessageDto {
    private Long id;

    private Instant sendAt;

    private String text;

    private Boolean fromManager;

    private MessageDeliveryStatus deliveryStatus;

    private List<MessageAssetDto> assets;

    private MessageViewingStatus messageViewingStatus;
}