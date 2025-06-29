package greencity.mapping.telegrammessage;

import greencity.dto.telegram.TelegramMessageDto;
import greencity.entity.telegram.Image;
import greencity.entity.telegram.TelegramMessage;
import greencity.entity.telegram.TextMessage;
import org.springframework.stereotype.Component;

@Component
public class TelegramMessageMapper {
    private final TextMessageMapper textMapper;
    private final ImageConverter imageConverter;

    public TelegramMessageMapper(TextMessageMapper textMapper, ImageConverter imageConverter) {
        this.textMapper = textMapper;
        this.imageConverter = imageConverter;
    }

    public TelegramMessageDto map(TelegramMessage message) {
        return switch (message) {
            case TextMessage text -> textMapper.map(text);
            case Image image -> imageConverter.map(image);
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass().getName());
        };
    }

}
