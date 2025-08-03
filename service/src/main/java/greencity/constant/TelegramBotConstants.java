package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TelegramBotConstants {
    public static final String START_COMMAND = "/start";
    public static final String SUPPORT_COMMAND = "/support";
    public static final String LOGIN_COMMAND = "/login";
    public static final String HELP_COMMAND = "/help";
    public static final String INCORRECT_LOGIN_FORMAT =
        "Некоректний формат. Будь ласка, введіть дані в форматі login:password";
    public static final String LOGOUT_MANAGER = "Вийти з ролі менеджера";
    public static final String SUCCESSFUL_LOGOUT_MANAGER = "Успішно вийшли з ролі менеджера!";
    public static final String FORBIDDEN_COMMANDS_MANAGER = "Менеджеру заборонено викликати команди";
    public static final String USER_IS_NOT_EMPLOYEE = "Користувач не є співробітником";
    public static final String EMPLOYEE_IS_NOT_MANAGER = "Співробітник не є менеджером";
    public static final String SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN = "Щось пішло не так спробуйте, будь ласка, знову";
    public static final String MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN =
        "Менеджер не зміг отримати ваше фото, спробуйте будь ласка, знову";
    public static final String MANAGER_DIDNT_RECEIVED_YOUR_FILE_PLEASE_TRY_AGAIN =
        "Менеджер не зміг отримати ваш файл, спробуйте будь ласка, знову";
    public static final String GREETING_MESSAGE =
        """
            Привіт! Я бот і допомагаю своїм колегам-менеджеркам обробляти запити.
            Будь ласка, поставте своє запитання, обравши чат з людиною або виберіть варіант інший варіант.
            """;
    public static final String GREETING_MANAGER_MESSAGE =
        """
            Привіт менеджер!
            """;
    public static final String LOGIN_MESSAGE = "Введіть логін та пароль в форматі логін:пароль";
    public static final String SUCCESS_LOGIN = "Вітаємо, %s ! Ви успішно авторизувалися.";
    public static final String SUPPORTED_COMMANDS = "Список доступних команд :";
    public static final String LOGIN_ERROR = "Авторизація не вдалася: %s";
    public static final String UNKNOWN_COMMAND =
        " Невідома команда. Оберіть команду зі списку доступних.";
    public static final String WORK_SCHEDULE_MESSAGE =
        """
            <b>Станція No Waste Recycling Station</b>
            Київ, вул. Саперно-Слобідська, 25/4 (Україна без сміття)

            <b>Графік роботи станції</b>
            вт–пт — 13:00-18:00
            сб-нд — 10:00-15:30

            <b>Платна послуга «УБС Uklon»</b>
            пн-пт: 9:00-18:00
            сб-нд: 10:00-15:30

            <b>Платна послуга «Досортування»</b>
            Доступна 24/7
            """;

    public static final String SORTING_RULES_PRICING_MESSAGE =
        """
            У 120-ти літровий пакет потрібно скласти змішану вторсировину й неліквіди (окрім небезпечних, забруднених, будівельних відходів, а також відходів, які контактували з рідинами тіла - перелік тут — <a href="https://nowaste.com.ua/sort-station">Посилання</a>). Текстильні відходи (зношений одяг, взуття, сумки, м’які іграшки, ковдри) потрібно складати в окремі пакети.

            Максимальна вага пакету на 120 л для міксу паковання до 5 кг
            Максимальна вага пакету на 120 л для текстилю до 10 кг.

            <b>Вартість послуги</b>👇
            Мікс сировини:

            120 грн/35 л\s

            180 грн/60 л

            270 грн/120 л\s

            350 грн/160 л

            520 грн/240 л

            Текстильні відходи:

            130 грн/20 л

            295 грн/60 л
            """;

    public static final String LOGOUT_MANAGER_CALLBACK = "logout_manager_command";
    public static final String CLIENT_SUPPORT_CALLBACK = "client_support_command";
    public static final String MAIN_MENU_CALLBACK = "main_menu_command";
    public static final String LOGIN_CALLBACK = "login_command";
    public static final String WORK_SCHEDULE_CALLBACK = "work_schedule_command";
    public static final String ADMISSION_RULES_CALLBACK = "admission_rules_command";
    public static final String FEEDBACK_CALLBACK = "feedback_command";
    public static final String GREEN_OFFICE_CALLBACK = "green_office_command";
    public static final String GREEN_OFFICE_PROCESS_CALLBACK = "green_office_process_command";
    public static final String RATING_TERRIBLY_CALLBACK = "rating_terribly_callback";
    public static final String RATING_BADLY_CALLBACK = "rating_badly_callback";
    public static final String RATING_SATISFACTORILY_CALLBACK = "rating_satisfactorily_callback";
    public static final String RATING_GOOD_CALLBACK = "rating_good_callback";
    public static final String RATING_PERFECTLY_CALLBACK = "rating_perfectly_callback";
    public static final String SORTING_PRICES_CALLBACK = "sorting_prices_command";
    public static final String ADMISSION_RULES_TEXT =
        """
            Все, що ми приймаємо на станції на переробку або безпечне спалення (перелік тут — <a href="https://nowaste.com.ua/sort-station">Посилання</a>)
            """;
    public static final String GREEN_OFFICE_TEXT =
        """
            Бажаєте замовити послугу, як юридична особа або ФОП?
            """;
    public static final String ENTERING_EMAIL_MESSAGE =
        """
            Для юридичних осіб і ФОП у нас діє послуга «Зелений Офіс» 🏢

            Якщо хочете дізнатися більше про цю послугу, залиште свій
            e-mail — і ми надішлемо вам лист із детальним описом.

            Напишіть ваш e-mail ✍️👇
            """;
    public static final String INVALID_EMAIL_MESSAGE =
        """
            Це не схоже на e-mail. Спробуймо ще раз ⬅️

            Напишіть ваш e-mail ✍️👇
            """;
    public static final String GREEN_OFFICE_THANK_YOU_MESSAGE =
        """
            Дякую! Упродовж доби вам прийде лист із описом послуги «Зелений Офіс» 👌
            """;
    public static final String FEEDBACK_MESSAGE =
        """
            Ми хочемо ставати кращою версією себе 🤗

            Тому нам дуже потрібні ваші чесні відгуки про нашу роботу ☝️

            Нам важливо знати вашу думку! Поділіться, будь ласка, своїм враженням про нашу роботу.
            """;
    public static final String GREAT_FEEDBACK_MESSAGE =
        """
            Дуже рада це чути! ❤️

            Ваші відгуки допомагають нам розвиватися! Будемо й надалі вдосконалювати нашу роботу 💪
            Залиште, будь ляска, кілька слів, що саме вам вподобалося в нашому сервісі 👇
            """;
    public static final String BAD_FEEDBACK_MESSAGE =
        """
            Дуже прикро, що так сталося 😔

            Напишіть, будь ласка, що саме було не так, щоб ми це виправили ✍️👇
            """;
    public static final String FEEDBACK_THANK_YOU_MESSAGE =
        """
            Щиро дякую за ваш відгук! 🙏

            Список доступних команд :
            """;
    public static final String BACK_TO_MAIN_MENU = "⏪ В головне меню\n";
    public static final String CLIENT_END_SUPPORT_MODE = "Закінчити розмову з менеджером";
    public static final String CLIENT_STOP_SUPPORT_MODE =
        "Ви закінчили розмову з менеджером, оцініть будь ласка роботу нашої підтримки від 1 до 5";
    public static final String CLIENT_END_SUPPORT_MODE_NOTIFICATION = "Клієнт %s закінчив розмову";
    public static final String MESSAGES_NOT_FOUND_FOR_CHAT = "There are no messages in the chat %s";
    public static final String CLIENT_SUPPORT_MESSAGE_CALL_BACK_QUERY =
        """
            Поставте, будь ласка, своє запитання — і я покличу когось із моїх колег-людей ✍️👇
            Якщо після спілкування з людиною ви захочете повернутися до моїх функцій,
            просто натисніть кнопку "Завершити розмову з менеджером"
            """;
    public static final String GREEN_OFFICE_SUBJECT = "Цікавить Зелений офіс";
    public static final String UNKNOWN_ERROR_OCCURRED_PLEASE_TRY_AGAIN =
        "Сталася невідома помилка, спробуйте, будь ласка, знову";
    public static final String MESSAGE_SENT_TO_MANAGER_WAIT_FOR_RESPONSE =
        "Ваше повідомлення надіслано менеджеру, очікуйте на відповідь";

    public static final String CLIENT_WANT_TO_SPEAK =
        """
            👨@%s хоче поговорити з людиною

            👉 %s

            💬 http://localhost:4200/chat/%s
            """;

    public static final String PREVIOUS_SESSION_HAS_EXPIRED =
        "Час попередньої сесії минув. Оберіть, будь ласка, команду зі списку щоб продовжити";

    public static final String PHOTO_CONTENT =
        "Фото контент";
}
