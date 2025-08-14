package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.enums.AssetType;
import greencity.enums.ChatState;
import greencity.exceptions.NotFoundException;
import greencity.repository.PositionRepository;
import greencity.repository.TelegramChatRepository;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static greencity.constant.ErrorMessage.POSITION_NOT_FOUND;
import static greencity.constant.ValidationConstant.EMAIL_REGEXP;

@Service
@RequiredArgsConstructor
public class TelegramUtils {
    @Value("${greencity.bots.ubs-bot-token}")
    private String telegramBotToken;
    private final PositionRepository positionRepository;
    private final TelegramChatRepository telegramChatRepository;

    /**
     * Detects the asset type based on the content type of the given multipart file.
     *
     * @param file {@link MultipartFile} the multipart file whose type needs to be detected
     * @return the corresponding {@link AssetType} based on the file's content type
     */
    public static AssetType detectAssetType(MultipartFile file) {
        String contentType = file.getContentType();
        return detectAssetType(contentType);
    }

    /**
     * Detects the asset type based on the given content type string.
     *
     * <ul>
     *   <li>If {@code file} is {@code null}, returns {@link AssetType#FILE}.</li>
     *   <li>If content type starts with {@code image/} (excluding SVG), returns {@link AssetType#IMAGE}.</li>
     *   <li>If content type starts with {@code video/}, returns {@link AssetType#VIDEO}.</li>
     *   <li>If content type starts with {@code audio/}, returns {@link AssetType#AUDIO}.</li>
     *   <li>Otherwise, returns {@link AssetType#FILE}.</li>
     * </ul>
     *
     * @param file the MIME type string
     * @return the corresponding {@link AssetType}
     */
    public static AssetType detectAssetType(String file) {
        if (file == null) {
            return AssetType.FILE;
        }

        if (file.startsWith("image/") && !file.contains("svg")) {
            return AssetType.IMAGE;
        }
        if (file.startsWith("video/")) {
            return AssetType.VIDEO;
        }
        if (file.startsWith("audio/")) {
            return AssetType.AUDIO;
        }

        return AssetType.FILE;
    }

    /**
     * Extracts the file name from the given file path.
     *
     * @param filePath {@link String} the full file path (may contain slashes)
     * @return the file name without the path, or {@code null} if the input is {@code null} or empty
     */
    public static String getFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash != -1) {
            return filePath.substring(lastSlash + 1);
        }
        return filePath;
    }

    /**
     * Determines the MIME content type for a file based on its extension.
     *
     * @param filePath {@link String} the file path or file name
     * @return a MIME type string (e.g., {@code image/jpeg}), or {@code application/octet-stream} if unknown
     */
    public static String getFileContentType(String filePath) {
        if (filePath == null) {
            return "application/octet-stream";
        }
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.endsWith(".png")) {
            return "image/png";
        } else if (filePath.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }

    /**
     * Validates whether the given email address is in a correct format.
     *
     * @param email {@link String} the email string to validate
     * @return {@code true} if the email matches the pattern, {@code false} otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Pattern emailPattern = Pattern.compile(EMAIL_REGEXP);
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Checks if the specified employee has a manager-related position.
     *
     * Positions with IDs {@code 1} (Service Manager) and {@code 2} (Manager) are considered manager roles.
     *
     * @param employee {@link Employee} the employee entity to check
     * @return {@code true} if the employee holds a manager or service manager position, {@code false} otherwise
     * @throws NotFoundException if a required position is not found in the repository
     */
    public boolean checkIsEmployeeManager(Employee employee) {
        var employeePositions = employee.getEmployeePosition();

        Position serviceManager = positionRepository.findById(1L)
            .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));

        Position manager = positionRepository.findById(2L)
            .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));

        return employeePositions.contains(manager) || employeePositions.contains(serviceManager);
    }

    /**
     * Updates the state of a Telegram chat and returns a response message.
     *
     * @param chatId   {@link String} the ID of the chat to update
     * @param newState {@link ChatState} the new state to set for the chat
     * @param message  {@link SendMessage} the message to return as a response
     * @return the provided {@link SendMessage} if the chat exists, or an unknown error message otherwise
     */
    public SendMessage updateChatStateAndRespond(
        String chatId,
        ChatState newState,
        SendMessage message) {
        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);
        if (chat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(chatId, TelegramBotConstants.UA);
        }
        chat.get().setChatState(newState);
        chat.get().setChatStateUpdatedAt(Instant.now());
        telegramChatRepository.save(chat.get());
        return message;
    }

    /**
     * Downloads a file from Telegram servers and returns its contents as a byte array.
     *
     * @param file {@link File} the file metadata containing the URL
     * @return the file's contents as a byte array
     * @throws IOException if an error occurs during download or reading the stream
     */
    public byte[] fileToByteArray(File file) throws IOException {
        URI uri = URI.create(file.getFileUrl(telegramBotToken));
        return IOUtils.toByteArray(uri.toURL().openStream());
    }
}
