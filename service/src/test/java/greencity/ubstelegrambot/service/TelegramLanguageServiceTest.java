package greencity.ubstelegrambot.service;

import greencity.entity.telegram.TelegramChat;
import greencity.exceptions.NotFoundException;
import greencity.repository.TelegramChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramLanguageServiceTest {

    @Mock
    private TelegramChatRepository telegramChatRepository;

    @InjectMocks
    private TelegramLanguageServiceImpl service;

    @Test
    void getChatLanguage_ChatExists_ReturnsLanguageCode() {
        TelegramChat chat = new TelegramChat();
        chat.setLanguageCode("en");

        when(telegramChatRepository.findByChatId("123"))
            .thenReturn(Optional.of(chat));

        String result = service.getChatLanguage("123");

        assertEquals("en", result);
        verify(telegramChatRepository).findByChatId("123");
    }

    @Test
    void getChatLanguage_ChatNotFound_ThrowsException() {
        when(telegramChatRepository.findByChatId("123"))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getChatLanguage("123"));

        assertEquals("Chat not found by ID: 123", ex.getMessage());
        verify(telegramChatRepository).findByChatId("123");
    }
}
