package greencity.ubstelegrambot.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramStreamingServiceImplTest {
//    @InjectMocks
//    private TelegramStreamingServiceImpl telegramStreamingService;
//    private SseEmitter emitter;
//    private final String chatId = "12345";
//
//    @BeforeEach
//    void setUp() {
//        telegramStreamingService = new TelegramStreamingServiceImpl();
//        emitter = mock(SseEmitter.class);
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testAddEmitter() throws NoSuchFieldException, IllegalAccessException {
//        telegramStreamingService.addEmitter(emitter, chatId);
//        assertFalse(getEmittersSize() == 0);
//    }
//
//    @Test
//    void testRemoveEmitter() throws NoSuchFieldException, IllegalAccessException {
//        telegramStreamingService.addEmitter(emitter, chatId);
//        telegramStreamingService.removeEmitter(emitter);
//        assertTrue(getEmittersSize() == 0);
//    }
//
//    @Test
//    void testStreamTextMessage() throws IOException {
//        telegramStreamingService.addEmitter(emitter, chatId);
//        TelegramTextMessageDto message = mock(TelegramTextMessageDto.class);
//
//        doNothing().when(emitter).send(any(SseEmitter.SseEventBuilder.class));
//
//        telegramStreamingService.streamMessages(chatId, message);
//        verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
//    }
//
//    @Test
//    void testStreamImageMessage() throws IOException {
//        telegramStreamingService.addEmitter(emitter, chatId);
//        TelegramImageDto message = mock(TelegramImageDto.class);
//
//        doNothing().when(emitter).send(any(SseEmitter.SseEventBuilder.class));
//
//        telegramStreamingService.streamMessages(chatId, message);
//        verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
//    }
//
//    private int getEmittersSize() throws NoSuchFieldException, IllegalAccessException {
//        Field field = TelegramStreamingServiceImpl.class.getDeclaredField("emitters");
//        field.setAccessible(true);
//        Map<?, ?> emitters = (Map<?, ?>) field.get(telegramStreamingService);
//        return emitters.size();
//    }
}
