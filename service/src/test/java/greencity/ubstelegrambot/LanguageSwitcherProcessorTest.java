package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.enums.MessageType;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.ubstelegrambot.service.LanguageSwitcherProcessor;
import greencity.ubstelegrambot.service.TelegramBotResponseServiceImpl;
import greencity.ubstelegrambot.service.TelegramUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguageSwitcherProcessorTest {

    @Mock
    private TelegramChatRepository chatRepository;

    @Mock
    private TelegramUtils telegramUtils;

    @Mock
    private TelegramManagerRepository telegramManagerRepository;

    @Mock
    private TelegramBotResponseServiceImpl telegramBotResponseService;

    @InjectMocks
    private LanguageSwitcherProcessor processor;

    private Update createUpdate(String chatId, String callbackData) {
        User user = new User();
        user.setId(Long.parseLong(chatId));

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData(callbackData);

        Update update = new Update();
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    @Test
    void testLanguageSwitchToUA_NormalChat() {
        Update update = createUpdate("123", TelegramBotConstants.SET_LANGUAGE_UK_CALLBACK);
        TelegramChat chat = new TelegramChat();
        chat.setChatId("123");
        chat.setLanguageCode(TelegramBotConstants.EN);
        chat.setChatState(ChatState.NORMAL);

        when(chatRepository.findByChatId("123")).thenReturn(java.util.Optional.of(chat));
        SendMessage expectedMessage = new SendMessage("123", "Some commands");
        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);
        when(telegramBotResponseService
            .getResponseByLangAndMessageType(anyString(), eq(MessageType.SUPPORTED_COMMANDS)))
            .thenReturn("Some text");

        SendMessage result = processor.process(update);

        assertNotNull(result);
        assertEquals(expectedMessage, result);
        verify(chatRepository).save(chat);
        assertEquals(TelegramBotConstants.UK, chat.getLanguageCode());
    }

    @Test
    void testLanguageSwitchToEN_InSupportChat() {
        Update update = createUpdate("123", TelegramBotConstants.SET_LANGUAGE_EN_CALLBACK);
        TelegramChat chat = new TelegramChat();
        chat.setChatId("123");
        chat.setLanguageCode(TelegramBotConstants.UK);
        chat.setChatState(ChatState.IN_SUPPORT);

        when(chatRepository.findByChatId("123")).thenReturn(Optional.of(chat));
        when(telegramBotResponseService
            .getResponseByLangAndMessageType(anyString(), eq(MessageType.CLIENT_SUPPORT_MESSAGE_CHANGE_LANGUAGE)))
            .thenReturn("Some text");

        SendMessage result = processor.process(update);

        assertNotNull(result);
        assertEquals("123", result.getChatId());
        assertNotNull(result.getReplyMarkup());
        assertEquals(TelegramBotConstants.EN, chat.getLanguageCode());
        verify(chatRepository).save(chat);
        verifyNoInteractions(telegramUtils);
    }

    @Test
    void testSameLanguage_NoAction() {
        Update update = createUpdate("123", TelegramBotConstants.SET_LANGUAGE_EN_CALLBACK);
        TelegramChat chat = new TelegramChat();
        chat.setChatId("123");
        chat.setLanguageCode(TelegramBotConstants.EN);
        chat.setChatState(ChatState.NORMAL);

        when(chatRepository.findByChatId("123")).thenReturn(java.util.Optional.of(chat));

        SendMessage result = processor.process(update);

        assertNull(result);
        verify(chatRepository, never()).save(any());
        verifyNoInteractions(telegramUtils);
    }

    @Test
    void testInvalidCallback_NoAction() {
        Update update = createUpdate("123", "some_other_callback");
        TelegramChat chat = new TelegramChat();
        chat.setChatId("123");
        chat.setLanguageCode(TelegramBotConstants.EN);
        chat.setChatState(ChatState.NORMAL);

        when(chatRepository.findByChatId("123")).thenReturn(java.util.Optional.of(chat));

        SendMessage result = processor.process(update);

        assertNull(result);
        verify(chatRepository, never()).save(any());
        verifyNoInteractions(telegramUtils);
    }

    @Test
    void process_whenChatStateIsMakingFeedback_shouldReturnNull() {
        Update update = createUpdate("123", TelegramBotConstants.SET_LANGUAGE_EN_CALLBACK);
        TelegramChat chat = new TelegramChat();
        chat.setChatId("123");
        chat.setLanguageCode(TelegramBotConstants.UK);
        chat.setChatState(ChatState.MAKING_FEEDBACK);

        when(chatRepository.findByChatId("123")).thenReturn(Optional.of(chat));

        SendMessage result = processor.process(update);

        assertNull(result);
        assertEquals(TelegramBotConstants.EN, chat.getLanguageCode());
        verify(chatRepository).save(chat);
        verifyNoInteractions(telegramUtils);
    }

    @Test
    void testLanguageSwitchToUA_NormalChat_Manager() {
        Update update = createUpdate("123", TelegramBotConstants.SET_LANGUAGE_UK_CALLBACK);
        TelegramChat chat = new TelegramChat();
        chat.setChatId("123");
        chat.setLanguageCode(TelegramBotConstants.EN);
        chat.setChatState(ChatState.NORMAL);

        when(chatRepository.findByChatId("123")).thenReturn(Optional.of(chat));
        when(telegramManagerRepository.existsByChatId("123")).thenReturn(true);
        SendMessage expectedMessage = new SendMessage("123", "Manager commands");
        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);
        when(telegramBotResponseService
            .getResponseByLangAndMessageType(anyString(), eq(MessageType.SUPPORTED_COMMANDS)))
            .thenReturn("Some text");

        SendMessage result = processor.process(update);

        assertNotNull(result);
        assertEquals(expectedMessage, result);
        verify(chatRepository).save(chat);
        assertEquals(TelegramBotConstants.UK, chat.getLanguageCode());
    }

    @Test
    void testChatNotFound_ThrowsException() {
        Update update = createUpdate("123", TelegramBotConstants.SET_LANGUAGE_UK_CALLBACK);
        when(chatRepository.findByChatId("123")).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> processor.process(update));
        verifyNoInteractions(telegramUtils);
    }
}
