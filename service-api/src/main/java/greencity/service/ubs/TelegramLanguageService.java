package greencity.service.ubs;

public interface TelegramLanguageService {
    /**
     * Return language code from the telegram chat.
     *
     * @param chatId {@link String} chat ID.
     * @return {@link String} language code.
     */
    String getChatLanguage(String chatId);
}
