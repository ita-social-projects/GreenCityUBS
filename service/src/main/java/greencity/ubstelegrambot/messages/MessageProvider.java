package greencity.ubstelegrambot.messages;

import greencity.constant.TelegramBotConstants;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MessageProvider {
    private static Map<String, String> messages = new HashMap<>();
    private static String currentLang = "ua";

    public static void setLanguage(String langCode) {
        currentLang = langCode;
        loadMessages(langCode);
    }

    public static String get(String key) {
        return messages.getOrDefault(key, "Message not found: " + key);
    }

    private static void loadMessages(String langCode) {
        String fileName = String.format("messages_%s.yaml", langCode);
        try (InputStream inputStream = TelegramBotConstants.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream != null) {
                Yaml yaml = new Yaml();
                Map<String, Object> loaded = yaml.load(inputStream);
                messages.clear();
                for (Map.Entry<String, Object> entry : loaded.entrySet()) {
                    messages.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            } else {
                throw new RuntimeException("Localization file not found: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading localization: " + fileName, e);
        }
    }

    static {
        loadMessages(currentLang);
    }
}
