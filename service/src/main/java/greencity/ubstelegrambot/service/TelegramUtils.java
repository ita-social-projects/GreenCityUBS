package greencity.ubstelegrambot.service;

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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;
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

    public static AssetType detectAssetType(MultipartFile file) {
        String contentType = file.getContentType();
        return detectAssetType(contentType);
    }

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

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Pattern emailPattern = Pattern.compile(EMAIL_REGEXP);
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    public boolean checkIsEmployeeManager(Employee employee) {
        var employeePositions = employee.getEmployeePosition();

        Position serviceManager = positionRepository.findById(1L)
            .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));

        Position manager = positionRepository.findById(2L)
            .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));

        return employeePositions.contains(manager) || employeePositions.contains(serviceManager);
    }

    public SendMessage updateChatStateAndRespond(
        String chatId,
        ChatState newState,
        Function<String, SendMessage> messageSupplier) {
        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);
        if (chat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(chatId);
        }
        chat.get().setChatState(newState);
        chat.get().setChatStateUpdatedAt(LocalDateTime.now());
        telegramChatRepository.save(chat.get());
        return messageSupplier.apply(chatId);
    }

    public byte[] fileToByteArray(File file) throws IOException {
        URI uri = URI.create(file.getFileUrl(telegramBotToken));
        return IOUtils.toByteArray(uri.toURL().openStream());
    }
}
