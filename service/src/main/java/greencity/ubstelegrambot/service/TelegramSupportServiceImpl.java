package greencity.ubstelegrambot.service;

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
import greencity.producers.TelegramChatProducer;
import greencity.repository.MessageAssetRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.service.ubs.FileService;
import greencity.service.ubs.TelegramNotificationService;
import greencity.service.ubs.TelegramSupportService;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.util.SimpleMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
    private final ApplicationContext applicationContext;
    private final TelegramChatRepository telegramChatRepository;
    private final FileService fileService;
    private final TelegramMessageRepository telegramMessageRepository;
    private final MessageAssetRepository messageAssetRepository;
    private final TelegramExecutor executor;
    private final TelegramNotificationService telegramNotificationService;
    private final TelegramChatProducer telegramChatProducer;
    private final TelegramUtils telegramUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SendMessage processSupportMessage(Message message) {
        var bot = applicationContext.getBean(UBSTelegramBot.class);

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

            executor.executeCommand(bot, MessageFactory.deleteEndSupportKeyboardMessage(chat.getChatId()));
            telegramNotificationService.notifyManagerAboutEndSupportModeFromUser(message.getFrom().getUserName());
            return endSupportSendMessage;
        }

        TelegramMessage telegramMessage;

        String mediaGroupId = message.getMediaGroupId();

        Optional<TelegramMessage> previouslySavedMessage = Optional.empty();

        if (mediaGroupId != null) {
            previouslySavedMessage = telegramMessageRepository.findByMediaGroupId(mediaGroupId);
        }

        if (previouslySavedMessage.isPresent()) {
            telegramMessage = previouslySavedMessage.get();
        } else {
            String messageText = Optional.ofNullable(message.getText())
                .orElse(message.getCaption());

            telegramMessage = TelegramMessage.builder()
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
        }

        File telegramFile;
        String fileId = null;
        String originalFileName = null;
        String contentType = null;
        Long fileSize = null;
        AssetType assetType;

        if (message.hasPhoto()) {
            PhotoSize largestPhoto = message.getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElse(null);

            if (largestPhoto == null) {
                if (previouslySavedMessage.isEmpty()) {
                    telegramMessageRepository.delete(telegramMessage);
                }
                return MessageFactory.buildMessage(message.getChatId().toString(),
                    TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN);
            }
            fileId = largestPhoto.getFileId();
            fileSize = largestPhoto.getFileSize().longValue();
        } else if (message.hasDocument()) {
            Document document = message.getDocument();
            if (document == null) {
                if (previouslySavedMessage.isEmpty()) {
                    telegramMessageRepository.delete(telegramMessage);
                }
                return MessageFactory.buildMessage(message.getChatId().toString(),
                    TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_FILE_PLEASE_TRY_AGAIN);
            }
            fileId = document.getFileId();
            fileSize = document.getFileSize();
            contentType = document.getMimeType();
            originalFileName = document.getFileName();
        }

        if (fileId != null) {
            try {
                telegramFile = executor.executeGetFile(bot, new GetFile(fileId));
                if (telegramFile == null || telegramFile.getFilePath() == null) {
                    log.warn("Telegram file not found for fileId: {}", fileId);
                    return MessageFactory.buildMessage(message.getChatId().toString(),
                        TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN);
                }

                if (message.hasPhoto()) {
                    contentType = TelegramUtils.getFileContentType(telegramFile.getFilePath());
                    originalFileName = TelegramUtils.getFileNameFromPath(telegramFile.getFilePath());
                }
                if (contentType == null) {
                    contentType = TelegramUtils.getFileContentType(telegramFile.getFilePath());
                }

                if (originalFileName == null) {
                    originalFileName = TelegramUtils.getFileNameFromPath(telegramFile.getFilePath());
                }

                byte[] content = telegramUtils.fileToByteArray(telegramFile);

                MultipartFile multipartFile = new SimpleMultipartFile(content,
                    originalFileName,
                    originalFileName,
                    contentType);

                String azureFileUrl = fileService.upload(multipartFile);
                assetType = TelegramUtils.detectAssetType(contentType);

                MessageAsset asset = MessageAsset.builder()
                    .url(azureFileUrl)
                    .fileName(originalFileName)
                    .size(fileSize)
                    .contentType(contentType)
                    .type(assetType)
                    .message(telegramMessage)
                    .build();

                if (previouslySavedMessage.isPresent()) {
                    List<MessageAsset> existingAssets = new ArrayList<>(telegramMessage.getAssets());
                    existingAssets.add(asset);
                    telegramMessage.setAssets(existingAssets);
                } else {
                    telegramMessage.setAssets(List.of(asset));
                }
                messageAssetRepository.save(asset);
            } catch (Exception e) {
                log.error("Error loading or saving file from Telegram (Filename: {}): {}", originalFileName,
                    e.getMessage(), e);
                return MessageFactory.buildMessage(message.getChatId().toString(),
                    TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN);
            }
        } else if (!message.hasText() && !message.hasPhoto() && !message.hasDocument()) {
            log.warn("No text or supported file found in message from chat ID: {}", chat.getChatId());
            return MessageFactory.buildMessage(message.getChatId().toString(),
                TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN);
        }

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

        TelegramMessageDto.TelegramMessageDtoBuilder telegramMessageDtoBuilder = TelegramMessageDto
            .builder()
            .id(telegramMessage.getId())
            .sendAt(telegramMessage.getSendAt())
            .text(telegramMessage.getText())
            .fromManager(telegramMessage.getFromManager())
            .deliveryStatus(telegramMessage.getStatus())
            .messageViewingStatus(telegramMessage.getMessageViewingStatus())
            .assets(assetDtos);

        if (previouslySavedMessage.isEmpty()) {
            telegramChatProducer.notifyNewMessage(telegramMessageDtoBuilder.build(), chat.getId());

            String contentForNotification = getString(telegramMessage);

            telegramNotificationService.notifyManagerAboutNewMessagesFromUser(
                Optional.ofNullable(message.getFrom().getUserName()).orElse(message.getFrom().getFirstName()),
                contentForNotification,
                chat.getId());
            return MessageFactory.buildMessage(chat.getChatId(),
                TelegramBotConstants.MESSAGE_SENT_TO_MANAGER_WAIT_FOR_RESPONSE);
        } else {
            telegramMessageRepository.save(telegramMessage);
            log.info("Added asset to existing media group message: {}", mediaGroupId);
            return null;
        }
    }

    private static String getString(TelegramMessage telegramMessage) {
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
}
