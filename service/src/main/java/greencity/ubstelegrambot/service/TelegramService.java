package greencity.ubstelegrambot.service;

import greencity.constant.ErrorMessage;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import greencity.exceptions.NotFoundException;
import greencity.repository.TelegramBotRepository;
import greencity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@AllArgsConstructor
public class TelegramService {
    private final UserRepository userRepository;
    private final TelegramBotRepository telegramBotRepository;

    public void initializeUserWithTelegramBot(Message message) {
        String uuId = message.getText().replace("/start", "").trim();
        User user = userRepository.findUserByUuid(uuId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        user.setTelegramBot(getTelegramBot(user, message.getChatId()));
        userRepository.save(user);
    }

    private TelegramBot getTelegramBot(User user, Long chatId) {
        TelegramBot telegramBot = user.getTelegramBot();
        if (telegramBot == null) {
            telegramBot = TelegramBot.builder()
                .chatId(chatId)
                .user(user)
                .isNotify(true)
                .build();
        } else if (!telegramBot.getIsNotify()) {
            telegramBot.setIsNotify(true);
        }
        return telegramBotRepository.save(telegramBot);
    }
}
