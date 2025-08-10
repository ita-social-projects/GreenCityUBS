package greencity.ubstelegrambot.messages;

import greencity.constant.TelegramBotConstants;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MessageProvider {
    private static final Map<String, Map<String, String>> cache = new HashMap<>();

    public static String get(String langCode, String key) {
        Map<String, String> messages = cache.computeIfAbsent(langCode, MessageProvider::loadMessages);
        return messages.getOrDefault(key, "Message not found: " + key);
    }

    private static Map<String, String> loadMessages(String langCode) {
        String fileName = String.format("messages_%s.yaml", langCode);
        try (InputStream inputStream = TelegramBotConstants.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream != null) {
                Yaml yaml = new Yaml();
                Map<String, Object> loaded = yaml.load(inputStream);
                Map<String, String> result = new HashMap<>();
                for (Map.Entry<String, Object> entry : loaded.entrySet()) {
                    result.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                return result;
            } else {
                throw new RuntimeException("Localization file not found: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading localization: " + fileName, e);
        }
    }
}
