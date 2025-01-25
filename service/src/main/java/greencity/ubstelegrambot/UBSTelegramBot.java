package greencity.ubstelegrambot;

import greencity.constant.AppConstant;
import greencity.service.ubs.TelegramService;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class UBSTelegramBot extends TelegramLongPollingBot {
    @Value("${greencity.bots.ubs-bot-name}")
    private String botName;
    @Value("${greencity.bots.ubs-bot-token}")
    private String botToken;
    private final TelegramService telegramService;
    private final TelegramExecutor executor;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message.hasText() && message.getText().startsWith(AppConstant.TELEGRAM_START_COMMAND)) {
            final String uuId = message.getText().replace(AppConstant.TELEGRAM_START_COMMAND, "").trim();
            final Long tgUserId = message.getFrom().getId();

            if (uuId.isEmpty()) {
                telegramService.handleUnknownTelegramUser(update);
                executor.executeCommand(this, MessageFactory.createWelcomeMessage(tgUserId.toString()));
                return;
            }

            telegramService.handleAuthorizedUser(uuId, tgUserId);
            executor.executeCommand(this, MessageFactory.createWelcomeMessage(tgUserId.toString()));
            return;
        }
    }
}
