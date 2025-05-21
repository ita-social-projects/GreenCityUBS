package greencity.ubstelegrambot.service;

import greencity.dto.telegram.TelegramImageDto;
import greencity.dto.telegram.TelegramTextMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TelegramStreamingServiceImplTest {
    @InjectMocks
    private TelegramStreamingServiceImpl telegramStreamingService;
    private SseEmitter emitter;
    private final String chatId = "12345";

    @BeforeEach
    void setUp() {
        telegramStreamingService = new TelegramStreamingServiceImpl();
        emitter = mock(SseEmitter.class);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddEmitter() throws NoSuchFieldException, IllegalAccessException {
        telegramStreamingService.addEmitter(emitter, chatId);
        assertFalse(getEmittersSize() == 0);
    }

    @Test
    void testRemoveEmitter() throws NoSuchFieldException, IllegalAccessException {
        telegramStreamingService.addEmitter(emitter, chatId);
        telegramStreamingService.removeEmitter(emitter);
        assertTrue(getEmittersSize() == 0);
    }

    @Test
    void testStreamTextMessage() throws IOException {
        telegramStreamingService.addEmitter(emitter, chatId);
        TelegramTextMessageDto message = mock(TelegramTextMessageDto.class);

        doNothing().when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        telegramStreamingService.streamMessages(chatId, message);
        verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void testStreamImageMessage() throws IOException {
        telegramStreamingService.addEmitter(emitter, chatId);
        TelegramImageDto message = mock(TelegramImageDto.class);

        doNothing().when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        telegramStreamingService.streamMessages(chatId, message);
        verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    private int getEmittersSize() throws NoSuchFieldException, IllegalAccessException {
        Field field = TelegramStreamingServiceImpl.class.getDeclaredField("emitters");
        field.setAccessible(true);
        Map<?, ?> emitters = (Map<?, ?>) field.get(telegramStreamingService);
        return emitters.size();
    }
}
