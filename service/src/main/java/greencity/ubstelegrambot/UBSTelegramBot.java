package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.service.ubs.TelegramPhotoService;
import greencity.service.ubs.TelegramService;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.service.TelegramExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public class UBSTelegramBot extends TelegramLongPollingBot {
    @Value("${greencity.bots.ubs-bot-name}")
    private String botName;
    @Value("${greencity.bots.ubs-bot-token}")
    private String botToken;
    private TelegramService telegramService;
    private TelegramPhotoService telegramPhotoService;
    private TelegramExecutor executor;

    public UBSTelegramBot(String botToken, String botName, TelegramService telegramService,
        TelegramExecutor telegramExecutor, TelegramPhotoService telegramPhotoService) {
        super(botToken);
        this.botToken = botToken;
        this.botName = botName;
        this.executor = telegramExecutor;
        this.telegramService = telegramService;
        this.telegramPhotoService = telegramPhotoService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            var text = message.getText();
            var chatId = message.getChatId().toString();

            if (text.startsWith("/start")) {
                executor.executeCommand(this, telegramService.processStartCommand(message));
                return;
            }
            String command = text.contains(":") ? text.split(":")[0].trim() : text;

            switch (command) {
                case TelegramBotConstants.HELP_COMMAND ->
                    executor.executeCommand(this, MessageFactory.createHelpMessage(message.getChatId().toString()));

                case TelegramBotConstants.SUPPORT_COMMAND ->
                    executor.executeCommand(this, telegramService.processSupportCommand(message));

                case TelegramBotConstants.LOGIN_COMMAND ->
                    executor.executeCommand(this, telegramService.processLoginCommand(message));

                case TelegramBotConstants.CLIENT_END_SUPPORT_MODE ->
                    executor.executeCommand(this, telegramService.stopSupportMode(chatId));

                default -> {
                    if (telegramService.isUserInSupportMode(chatId)) {
                        telegramService.saveManagerMessage(chatId, text);
                        executor.executeCommand(this, MessageFactory.createKeyboardMessage(chatId));
                    } else {
                        executor.executeCommand(this, MessageFactory.createUnknownCommandMessage(chatId));
                    }
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            var message = update.getMessage();
            String caption = message.getCaption();
            var photos = telegramPhotoService.downloadPhotoFromTelegram(message);
            telegramPhotoService.saveToDB(photos, message.getChatId().toString(), caption);
        } else if (update.hasCallbackQuery()) {
            var callBackQuery = update.getCallbackQuery();
            if (callBackQuery.getData().equals(TelegramBotConstants.CLIENT_SUPPORT_CALLBACK)) {
                executor.executeCommand(this, MessageFactory.createClientSupportMessage(
                    callBackQuery.getFrom().getId().toString()));
            }
            if (callBackQuery.getData().equals(TelegramBotConstants.LOGIN_CALLBACK)) {
                executor.executeCommand(this, MessageFactory.createLoginMessage(
                    callBackQuery.getFrom().getId().toString()));
            }
            if (callBackQuery.getData().startsWith(String.format(TelegramBotConstants.SCORE, ""))) {
                executor.executeCommand(this, telegramService.handleUserChatScope(callBackQuery.getData(),
                    callBackQuery.getFrom().getId().toString()));
            }
        }
    }
}
