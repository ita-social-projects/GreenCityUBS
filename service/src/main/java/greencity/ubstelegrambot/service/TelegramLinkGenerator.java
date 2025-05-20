package greencity.ubstelegrambot.service;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TelegramLinkGenerator {
    public static final String BASE_URL = "https://t.me/%s?start=%s";

    /**
     * Generates a link for the manager to communicate with the user.
     *
     * @param botName  the name of the telegram bot
     * @param userUUID the uuid of the user to communicate with
     * @return a link to communicate with the user
     */
    public static String generateManagerLink(String botName, String userUUID) {
        return String.format(BASE_URL, botName, userUUID);
    }
}
