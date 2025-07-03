package greencity.ubstelegrambot.service;

import greencity.service.ubs.TelegramStreamingService;
import org.springframework.stereotype.Service;

@Service
public class TelegramStreamingServiceImpl implements TelegramStreamingService {
//    private final Map<SseEmitter, String> emitters = new ConcurrentHashMap<>();
//
//    @Override
//    public void streamMessages(String chatId, TelegramTextMessageDto message) {
//        sendMessages(chatId, message);
//    }
//
//    @Override
//    public void streamMessages(String chatId, TelegramImageDto message) {
//        sendMessages(chatId, message);
//    }
//
//    @Override
//    public void addEmitter(SseEmitter emitter, String chatId) {
//        emitters.put(emitter, chatId);
//    }
//
//    @Override
//    public void removeEmitter(SseEmitter emitter) {
//        emitters.remove(emitter);
//    }
//
//    private void sendMessages(String chatId, TelegramMessageDto message) {
//        for (Map.Entry<SseEmitter, String> entry : emitters.entrySet()) {
//            SseEmitter emitter = entry.getKey();
//            String storedChatId = entry.getValue();
//
//            if (storedChatId.equals(chatId)) {
//                try {
//                    emitter.send(SseEmitter.event().data(message));
//                } catch (IOException e) {
//                    emitters.remove(emitter);
//                }
//            }
//        }
//    }
}
