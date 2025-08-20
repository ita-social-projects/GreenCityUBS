package greencity.ubstelegrambot.service;

import greencity.client.config.UserRemoteWebClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.telegram.MessageAssetDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.entity.telegram.MessageAsset;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import greencity.enums.AssetType;
import greencity.enums.ChatState;
import greencity.enums.MessageDeliveryStatus;
import greencity.enums.MessageViewingStatus;
import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.producers.TelegramChatProducer;
import greencity.repository.MessageAssetRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.service.ubs.TelegramNotificationService;
import greencity.service.ubs.TelegramSupportService;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.util.SimpleMultipartFile;
import java.io.IOException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramSupportServiceImpl implements TelegramSupportService {
    private final TelegramChatRepository telegramChatRepository;
    private final UserRemoteWebClient userRemoteWebClient;
    private final TelegramMessageRepository telegramMessageRepository;
    private final MessageAssetRepository messageAssetRepository;
    private final TelegramExecutor telegramExecutor;
    private final TelegramNotificationService telegramNotificationService;
    private final TelegramChatProducer telegramChatProducer;
    private final TelegramUtils telegramUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SendMessage processSupportMessage(Message message) {
        Optional<TelegramChat> optionalChat = telegramChatRepository.findByChatId(message.getFrom().getId().toString());
        if (optionalChat.isEmpty()) {
            log.warn("Telegram chat not found by ID: {}", message.getFrom().getId());
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString());
        }

        TelegramChat chat = optionalChat.get();

        if (message.hasText() && message.getText().contains(TelegramBotConstants.CLIENT_END_SUPPORT_MODE)) {
            SendMessage endSupportSendMessage =
                telegramUtils.updateChatStateAndRespond(chat.getChatId(), ChatState.NORMAL,
                    MessageFactory::createFeedbackMessage);

            telegramExecutor.executeCommand(MessageFactory.deleteEndSupportKeyboardMessage(chat.getChatId()));
            telegramNotificationService.notifyManagerAboutEndSupportModeFromUser(message.getFrom().getUserName());
            return endSupportSendMessage;
        }

        return processMessageContent(chat, message);
    }

    private SendMessage processMessageContent(TelegramChat chat, Message message) {
        String mediaGroupId = message.getMediaGroupId();
        Optional<TelegramMessage> previouslySavedMessage = mediaGroupId == null
            ? Optional.empty()
            : telegramMessageRepository.findByMediaGroupId(mediaGroupId);
        TelegramMessage telegramMessage = getOrSaveTelegramMessage(chat, message, mediaGroupId, previouslySavedMessage);
        SendMessage filesFailMessage = processMessageFiles(chat, message, telegramMessage, previouslySavedMessage);

        return filesFailMessage != null
            ? filesFailMessage
            : notifyUserAboutMessage(chat, message, mediaGroupId, telegramMessage, previouslySavedMessage);
    }

    private TelegramMessage getOrSaveTelegramMessage(TelegramChat chat,
        Message message,
        String mediaGroupId,
        Optional<TelegramMessage> telegramMessageOpt) {
        if (telegramMessageOpt.isPresent()) {
            return telegramMessageOpt.get();
        }

        String messageText = Optional.ofNullable(message.getText())
            .orElse(message.getCaption());

        TelegramMessage telegramMessage = TelegramMessage.builder()
            .chat(chat)
            .fromManager(false)
            .mediaGroupId(mediaGroupId)
            .status(MessageDeliveryStatus.SENT)
            .sendAt(Instant.now())
            .text(messageText)
            .messageViewingStatus(MessageViewingStatus.UNREAD)
            .build();

        telegramMessageRepository.save(telegramMessage);
        chat.setLastMessage(telegramMessage);
        chat.setUnreadMessagesCount(chat.getUnreadMessagesCount() + 1);
        telegramChatRepository.save(chat);

        return telegramMessage;
    }

    private SendMessage processMessageFiles(TelegramChat chat,
        Message message,
        TelegramMessage telegramMessage,
        Optional<TelegramMessage> telegramMessageOpt) {
        SendMessage resultMessage = null;
        FileInfo fileInfo = new FileInfo();

        if (message.hasPhoto()) {
            resultMessage = setPhotoInfo(message, telegramMessage, telegramMessageOpt, fileInfo);
        } else if (message.hasDocument()) {
            resultMessage = setDocumentInfo(message, telegramMessage, telegramMessageOpt, fileInfo);
        }

        if (fileInfo.getFileId() != null) {
            return setFileAsMessageAsset(message, telegramMessage, telegramMessageOpt, fileInfo);
        } else if (!message.hasText() && !message.hasPhoto() && !message.hasDocument()) {
            log.warn("No text or supported file found in message from chat ID: {}", chat.getChatId());
            resultMessage = MessageFactory.buildMessage(message.getChatId().toString(),
                TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN);
        }

        return resultMessage;
    }

    private SendMessage setPhotoInfo(Message message,
        TelegramMessage telegramMessage,
        Optional<TelegramMessage> telegramMessageOpt,
        FileInfo fileInfo) {
        PhotoSize largestPhoto = message.getPhoto().stream()
            .max(Comparator.comparing(PhotoSize::getFileSize))
            .orElse(null);

        if (largestPhoto == null) {
            if (telegramMessageOpt.isEmpty()) {
                telegramMessageRepository.delete(telegramMessage);
            }
            return MessageFactory.buildMessage(message.getChatId().toString(),
                TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN);
        } else {
            fileInfo.setFileId(largestPhoto.getFileId());
            fileInfo.setFileSize(largestPhoto.getFileSize().longValue());
            return null;
        }
    }

    private SendMessage setDocumentInfo(Message message,
        TelegramMessage telegramMessage,
        Optional<TelegramMessage> telegramMessageOpt,
        FileInfo fileInfo) {
        Document document = message.getDocument();
        if (document == null) {
            if (telegramMessageOpt.isEmpty()) {
                telegramMessageRepository.delete(telegramMessage);
            }
            return MessageFactory.buildMessage(message.getChatId().toString(),
                TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_FILE_PLEASE_TRY_AGAIN);
        } else {
            fileInfo.setFileId(document.getFileId());
            fileInfo.setFileSize(document.getFileSize());
            fileInfo.setContentType(document.getMimeType());
            fileInfo.setOriginalFileName(document.getFileName());
            return null;
        }
    }

    private SendMessage setFileAsMessageAsset(Message message,
        TelegramMessage telegramMessage,
        Optional<TelegramMessage> telegramMessageOpt,
        FileInfo fileInfo) {
        try {
            File telegramFile = telegramExecutor.executeGetFile(new GetFile(fileInfo.getFileId()));
            if (telegramFile == null || telegramFile.getFilePath() == null) {
                log.warn("Telegram file not found for fileId: {}", fileInfo.getFileId());
                return MessageFactory.buildMessage(message.getChatId().toString(),
                    TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN);
            }

            if (message.hasPhoto()) {
                fileInfo.setContentType(TelegramUtils.getFileContentType(telegramFile.getFilePath()));
                fileInfo.setOriginalFileName(TelegramUtils.getFileNameFromPath(telegramFile.getFilePath()));
            }
            if (fileInfo.getContentType() == null) {
                fileInfo.setContentType(TelegramUtils.getFileContentType(telegramFile.getFilePath()));
            }

            if (fileInfo.getOriginalFileName() == null) {
                fileInfo.setOriginalFileName(TelegramUtils.getFileNameFromPath(telegramFile.getFilePath()));
            }

            byte[] content = telegramUtils.fileToByteArray(telegramFile);

            MultipartFile multipartFile = new SimpleMultipartFile(content,
                fileInfo.getOriginalFileName(),
                fileInfo.getOriginalFileName(),
                fileInfo.getContentType());

            String azureFileUrl = uploadFile(multipartFile);
            AssetType assetType = TelegramUtils.detectAssetType(fileInfo.getContentType());

            MessageAsset asset = MessageAsset.builder()
                .url(azureFileUrl)
                .fileName(fileInfo.getOriginalFileName())
                .size(fileInfo.getFileSize())
                .contentType(fileInfo.getContentType())
                .type(assetType)
                .message(telegramMessage)
                .build();

            if (telegramMessageOpt.isPresent()) {
                List<MessageAsset> existingAssets = new ArrayList<>(telegramMessage.getAssets());
                existingAssets.add(asset);
                telegramMessage.setAssets(existingAssets);
            } else {
                telegramMessage.setAssets(List.of(asset));
            }

            messageAssetRepository.save(asset);
        } catch (TelegramBotExecutionException | IOException e) {
            log.error("Error loading or saving file from Telegram (Filename: {}): {}", fileInfo.getOriginalFileName(),
                e.getMessage(), e);
            return MessageFactory.buildMessage(message.getChatId().toString(),
                TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN);
        }

        return null;
    }

    private SendMessage notifyUserAboutMessage(TelegramChat chat,
        Message message,
        String mediaGroupId,
        TelegramMessage telegramMessage,
        Optional<TelegramMessage> telegramMessageOpt) {
        List<MessageAssetDto> assetDtos = Optional.ofNullable(telegramMessage.getAssets())
            .orElse(Collections.emptyList())
            .stream()
            .map(asset -> MessageAssetDto.builder()
                .id(asset.getId())
                .url(asset.getUrl())
                .type(asset.getType())
                .fileName(asset.getFileName())
                .size(asset.getSize())
                .contentType(asset.getContentType())
                .build())
            .toList();

        TelegramMessageDto telegramMessageDto = TelegramMessageDto.builder()
            .id(telegramMessage.getId())
            .sendAt(telegramMessage.getSendAt())
            .text(telegramMessage.getText())
            .fromManager(telegramMessage.getFromManager())
            .deliveryStatus(telegramMessage.getStatus())
            .messageViewingStatus(telegramMessage.getMessageViewingStatus())
            .assets(assetDtos)
            .build();

        if (telegramMessageOpt.isEmpty()) {
            telegramChatProducer.notifyNewMessage(telegramMessageDto, chat.getId());

            String contentForNotification = buildContentForNotification(telegramMessage);

            telegramNotificationService.notifyManagerAboutNewMessagesFromUser(
                Optional.ofNullable(message.getFrom().getUserName()).orElse(message.getFrom().getFirstName()),
                contentForNotification,
                chat.getId());
            return MessageFactory.buildMessage(chat.getChatId(),
                TelegramBotConstants.MESSAGE_SENT_TO_MANAGER_WAIT_FOR_RESPONSE);
        } else {
            telegramMessageRepository.save(telegramMessage);
            log.info("Added asset to existing media group message: {}", mediaGroupId);
        }

        return null;
    }

    private String buildContentForNotification(TelegramMessage telegramMessage) {
        String contentForNotification = "Empty message";
        if (telegramMessage.getAssets() != null && !telegramMessage.getAssets().isEmpty()) {
            switch (telegramMessage.getAssets().getFirst().getType()) {
                case IMAGE -> contentForNotification = "Image content ("
                    + telegramMessage.getAssets().size() + " images)";
                // case AUDIO -> contentForNotification = "Audio content ("
                // + telegramMessage.getAssets().size() + " audio)";
                default -> contentForNotification = "File content ("
                    + telegramMessage.getAssets().size() + " files)";
            }
            if (telegramMessage.getText() != null && !telegramMessage.getText().isEmpty()) {
                contentForNotification += " + text";
            }
        } else if (telegramMessage.getText() != null && !telegramMessage.getText().isEmpty()) {
            contentForNotification = telegramMessage.getText();
        }
        return contentForNotification;
    }

    @Getter
    @Setter
    private static class FileInfo {
        private String fileId;
        private String originalFileName;
        private String contentType;
        private Long fileSize;
    }

    private String uploadFile(MultipartFile file) {
        String url = "";
        try {
            url = userRemoteWebClient.uploadFile(file);
        } catch (WebClientRequestException | WebClientResponseException e) {
            log.warn("User service is unavailable: {}", e.getMessage());
        }
        return url;
    }
}
