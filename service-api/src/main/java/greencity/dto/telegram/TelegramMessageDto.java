package greencity.dto.telegram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessageDto {
    private Long id;

    private LocalDateTime sendAt;

    private String text;

    private Boolean fromManager;

    private List<MessageAssetDto> assets;
}