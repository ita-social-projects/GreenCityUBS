package greencity.ubstelegrambot.service;

import greencity.exceptions.NotFoundException;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.TelegramLanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramLanguageServiceImpl implements TelegramLanguageService {
    private final TelegramChatRepository telegramChatRepository;

    @Override
    public String getChatLanguage(String chatId) {
        return telegramChatRepository.findByChatId(chatId)
            .orElseThrow(() -> new NotFoundException("Chat not found by ID: " + chatId)).getLanguageCode();
    }
}
