package greencity.ubstelegrambot.messages;

import greencity.constant.TelegramBotConstants;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageProvider {
    private static final Map<String, Map<String, String>> cache = new HashMap<>();

    /**
     * Retrieves a localized message for the specified language code and message
     * key.
     * <p>
     * This method first checks the internal cache for messages corresponding to the
     * provided {@code langCode}. If the messages for the given language are not
     * already cached, they are loaded via
     * {@link MessageProvider#loadMessages(String)} and stored in the cache. Then,
     * it returns the message mapped to the given {@code key}.
     * </p>
     * <p>
     * If the message key is not found in the loaded messages, a default string in
     * the format {@code "Message not found: <key>"} is returned.
     * </p>
     *
     * @param langCode the language code (e.g., {@code "en"}, {@code "ua"}) used to
     *                 determine which language messages to retrieve.
     * @param key      the message key to look up in the localized messages.
     * @return the localized message if found; otherwise, a default "Message not
     *         found" string.
     * @throws NullPointerException if {@code langCode} or {@code key} is
     *                              {@code null}.
     */
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
                Map<String, String> flatMap = new HashMap<>();
                flatten("", loaded, flatMap);
                return flatMap;
            } else {
                throw new RuntimeException("Localization file not found: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading localization: " + fileName, e);
        }
    }

    private static void flatten(String prefix, Object obj, Map<String, String> result) {
        if (obj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();
                String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
                flatten(newPrefix, value, result);
            }
        } else if (obj instanceof List<?> list) {
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(String.valueOf(item));
            }
            result.put(prefix, sb.toString());
        } else {
            result.put(prefix, String.valueOf(obj));
        }
    }
}
