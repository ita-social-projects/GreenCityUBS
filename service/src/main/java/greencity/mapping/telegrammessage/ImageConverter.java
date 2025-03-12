package greencity.mapping.telegrammessage;

import greencity.dto.telegram.TelegramImageDto;
import greencity.entity.telegram.Image;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ImageConverter extends AbstractConverter<Image, TelegramImageDto> {
    @Override
    protected TelegramImageDto convert(Image source) {
        return new TelegramImageDto(
            source.getMessageId(),
            source.getChatId(),
            source.isRead(),
            source.getFileUrl(),
            source.getCaption());
    }

    public TelegramImageDto map(Image source) {
        return convert(source);
    }
}
