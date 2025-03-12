package greencity.ubstelegrambot.service;

import greencity.constant.ErrorMessage;
import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.user.User;
import greencity.exceptions.NotFoundException;
import greencity.repository.AuthorizedUserRepository;
import greencity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@AllArgsConstructor
public class TelegramService {
    private final UserRepository userRepository;
    private final AuthorizedUserRepository telegramBotRepository;

    public void initializeUserWithTelegramBot(Message message) {
        String uuId = message.getText().replace("/start", "").trim();
        User user = userRepository.findUserByUuid(uuId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        user.setTelegramBot(getTelegramBot(user, message.getChatId()));
        userRepository.save(user);
    }

    private AuthorizedUser getTelegramBot(User user, Long chatId) {
        AuthorizedUser authorizedUser = user.getTelegramBot();
        if (authorizedUser == null) {
            authorizedUser = new AuthorizedUser(
                chatId.toString(),
                false,
                true,
                user,
                false);
        } else if (Boolean.FALSE.equals(authorizedUser.getIsNotify())) {
            authorizedUser.setIsNotify(true);
        }
        return telegramBotRepository.save(authorizedUser);
    }
}
