package greencity.service.ubs;

import greencity.dto.telegram.TelegramImageDto;
import greencity.dto.telegram.TelegramTextMessageDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface TelegramStreamingService {
    /**
     * Stream messages to client for specified chatId.
     *
     * @param chatId  the telegram chat ID
     * @param message the message to stream
     */
    void streamMessages(String chatId, TelegramTextMessageDto message);

    /**
     * Streams a photo message to client for specified chatId.
     *
     * @param chatId  the telegram chat ID
     * @param message the message to stream
     */
    void streamMessages(String chatId, TelegramImageDto message);

    /**
     * Adds an emitter to the map of emitters for the specified chatId.
     *
     * @param emitter the emitter to add
     * @param chatId  the telegram chat ID
     */
    void addEmitter(SseEmitter emitter, String chatId);

    /**
     * Removes an emitter from the map of emitters for the specified chatId.
     *
     * @param emitter the emitter to remove
     */
    void removeEmitter(SseEmitter emitter);
}
