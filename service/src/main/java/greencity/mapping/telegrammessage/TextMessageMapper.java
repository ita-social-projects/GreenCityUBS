package greencity.mapping.telegrammessage;

import greencity.dto.telegram.TelegramTextMessageDto;
import greencity.entity.telegram.TextMessage;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class TextMessageMapper extends AbstractConverter<TextMessage, TelegramTextMessageDto> {
    @Override
    protected TelegramTextMessageDto convert(TextMessage source) {
        return new TelegramTextMessageDto(
            source.getMessageId(),
            source.getChatId(),
            source.isRead(),
            source.getSendAt(),
            source.getText(),
            source.isManagerMessage());
    }

    public TelegramTextMessageDto map(TextMessage source) {
        return convert(source);
    }
}
