package greencity.ubstelegrambot.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TelegramConstants {
    public static final String FEEDBACK_THANK_YOU_MESSAGE =
        """
            Щиро дякую за ваш відгук! 🙏

            Список доступних команд :
            """;
    public static final String UNKNOWN_ERROR_OCCURRED_PLEASE_TRY_AGAIN =
        "Сталася невідома помилка, спробуйте, будь ласка, знову";
    public static final String BAD_FEEDBACK_MESSAGE =
        """
            Дуже прикро, що так сталося 😔

            Напишіть, будь ласка, що саме було не так, щоб ми це виправили ✍️👇
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
    public static final String INVALID_EMAIL_MESSAGE =
        """
            Це не схоже на e-mail. Спробуймо ще раз ⬅️

            Напишіть ваш e-mail ✍️👇
            """;
    public static final String GREEN_OFFICE_THANK_YOU_MESSAGE =
        """
            Дякую! Упродовж доби вам прийде лист із описом послуги «Зелений Офіс» 👌
            """;
}
