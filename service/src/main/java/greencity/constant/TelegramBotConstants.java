package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TelegramBotConstants {
    public static final String START_COMMAND = "/start";
    public static final String SUPPORT_COMMAND = "/support";
    public static final String LOGIN_COMMAND = "/login";
    public static final String HELP_COMMAND = "/help";
    public static final String ERROR = "Помилка: %s";
    public static final String GREETING_MESSAGE = "Вітаємо!\nВи підписались на UbsBot";
    public static final String CLIENT_SUPPORT_MESSAGE =
        "Будь ласка введіть своє повідомлення в форматі /support:повідомлення";
    public static final String LOGIN_MESSAGE = "Введіть логін та пароль в форматі /login:логін:пароль";
    public static final String ERROR_IN_REQUEST_MESSAGE = "Будь ласка введіть повідомлення в форматі %s";
    public static final String CLIENT_SUPPORT_MESSAGE_SEND = "Ваше повідомлення надіслано менеджеру, очікуй відповіді";
    public static final String SUCCESS_LOGIN = "Вітаємо, %s ! Ви успішно авторизувалися.";
    public static final String SUPPORTED_COMMANDS = "Список доступних команд :";
    public static final String LOGIN_ERROR = "Авторизація не вдалася: %s";
    public static final String UNKNOWN_ERROR = "Сталася невідома помилка%s %s";
    public static final String SYSTEM_ERROR = "Сталася системна помилка:%s";
    public static final String UNKNOWN_COMMAND =
        " Невідома команда, щоб поглянути список доступних команд скористайтеся командою /help";
    public static final String CLIENT_SUPPORT_CALLBACK = "client_support_command";
    public static final String LOGIN_CALLBACK = "login_command";
    public static final String HELP_CALLBACK = "help_command";
    public static final String START_CALLBACK = "start_command";
    public static final String INVALID_COMMAND_FORMAT = "Команда має бути у форматі: /login:логін:пароль";
    public static final String BAD_REQUEST_DEFAULT_MESSAGE = "Запит відхилено. Перевірте ваші дані.";
    public static final String REMOTE_SERVER_UNAVAILABLE = "Сервер недоступний. Спробуйте пізніше.";
    public static final String NEW_SUPPORT_MESSAGE_NOTIFICATION = "%s нових повідомлень від клієнта %s";
    public static final String CLIENT_END_SUPPORT_MODE = "Закінчити розмову з менеджером";
    public static final String CLIENT_STOP_SUPPORT_MODE =
        "Ви закінчили розмову з менеджером, оцініть будь ласка роботу нашої підтримки від 1 до 5";
    public static final String CLIENT_END_SUPPORT_MODE_NOTIFICATION = "Клієнт %s закінчив розмову";
    public static final String MESSAGES_NOT_FOUND_FOR_CHAT = "There are no messages in the chat %s";
    public static final String CLIENT_MESSAGE_AFTER_FEEDBACK = "Дякуємо за ваш відгук";
    public static final String SCORE = "Score%s";
    public static final String CLIENT_SUPPORT_MESSAGE_CALL_BACK_QUERY =
        "Вас вітає підтримка UBS! Будь ласка введіть своє повідомлення";
}
