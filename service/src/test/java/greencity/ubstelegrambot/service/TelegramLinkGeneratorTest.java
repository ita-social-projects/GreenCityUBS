package greencity.ubstelegrambot.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelegramLinkGeneratorTest {
    @Test
    void generateManagerLinkTest() {
        String botName = "botName";
        String userUUID = "userUUId";
        String expectedResult = String.format("https://t.me/%s?start=%s", botName, userUUID);

        String actualResult = TelegramLinkGenerator.generateManagerLink(botName, userUUID);

        assertEquals(expectedResult, actualResult);
    }
}
