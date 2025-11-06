package greencity.ubstelegrambot.service;

import greencity.client.config.UserRemoteWebClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.ChatUserDto;
import greencity.dto.telegram.MessageAssetDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.entity.telegram.MessageAsset;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import greencity.enums.*;
import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.exceptions.bots.UnsupportedTelegramAssetException;
import greencity.producers.TelegramChatProducer;
import greencity.repository.MessageAssetRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.service.ubs.TelegramNotificationService;
import greencity.service.ubs.TelegramSupportService;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.messages.MessageProvider;
import greencity.util.SimpleMultipartFile;
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
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import java.io.IOException;
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
    private final TelegramBotResponseServiceImpl telegramBotResponseService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SendMessage processSupportMessage(Message message, String lang) {
        Optional<TelegramChat> optionalChat = findAndValidateChat(message.getFrom().getId().toString());
        if (optionalChat.isEmpty()) {
            log.warn("Telegram chat not found by ID: {}", message.getFrom().getId());
            String text = telegramBotResponseService.getResponseByLangAndMessageType(lang, MessageType.UNKNOWN_ERROR);
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString(),
                TelegramBotConstants.UK, text);
        }

        TelegramChat chat = optionalChat.get();

        if (message.hasText()
            && message.getText().startsWith("/start")
            && chat.getChatState() == ChatState.IN_SUPPORT) {
            log.info("User is already in support chat {}. Filtering system /start message", chat.getChatId());
            String text = telegramBotResponseService.getResponseByLangAndMessageType(lang,
                MessageType.MANAGER_CHAT_ALREADY_OPEN_MESSAGE);
            return MessageFactory.createChatAlreadyOpenMessage(chat.getChatId(), text);
        }

        if (message.hasText() && message.getText().contains(MessageProvider.get(lang, "client.end.support.mode"))) {
            String feedbackText = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.FEEDBACK_MESSAGE);
            SendMessage endSupportSendMessage =
                telegramUtils.updateChatStateAndRespond(chat.getChatId(), ChatState.NORMAL,
                    MessageFactory.createFeedbackMessage(chat.getChatId(), lang, feedbackText));

            String text = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.CLIENT_STOP_SUPPORT_MODE);
            telegramExecutor.executeCommand(MessageFactory.deleteEndSupportKeyboardMessage(
                chat.getChatId(), text));

            telegramNotificationService.notifyManagerAboutEndSupportModeFromUser(message.getFrom().getUserName());
            return endSupportSendMessage;
        }

        return processMessageContent(chat, message);
    }

    @Override
    @Transactional
    public SendMessage processEditedSupportMessage(Message edited, String lang) {
        Optional<TelegramChat> optionalChat = findAndValidateChat(edited.getFrom().getId().toString());
        if (optionalChat.isEmpty()) {
            String text = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.UNKNOWN_ERROR);
            return MessageFactory.createUnknownErrorOccurredMessage(edited.getChatId().toString(),
                TelegramBotConstants.UK, text);
        }
        TelegramChat chat = optionalChat.get();

        if (edited.hasText()
            && edited.getText().startsWith("/start")
            && chat.getChatState() == ChatState.IN_SUPPORT) {
            log.info("User is already in support chat {}. Filtering system /start edited", chat.getChatId());
            return MessageFactory.createChatAlreadyOpenMessage(chat.getChatId(), lang);
        }

        TelegramMessage telegramMessage =
            telegramMessageRepository.findByChatAndTelegramMessageId(chat, edited.getMessageId()).orElse(null);

        if (telegramMessage == null) {
            log.warn("Edited message {} not found in DB", edited.getMessageId());
            return null;
        }
        if (edited.hasText()) {
            telegramMessage.setText(edited.getText());
        } else if (edited.getCaption() != null) {
            telegramMessage.setText(edited.getCaption());
        } else {
            telegramMessage.setText(null);
        }
        telegramMessage.setUpdatedAt(Instant.now());
        telegramMessageRepository.save(telegramMessage);
        return null;
    }

    private Optional<TelegramChat> findAndValidateChat(String chatId) {
        Optional<TelegramChat> optionalChat = telegramChatRepository.findByChatId(chatId);
        if (optionalChat.isEmpty()) {
            log.warn("Telegram chat not found by ID: {}", chatId);
        }
        return optionalChat;
    }

    private SendMessage processMessageContent(TelegramChat chat, Message message) {
        String mediaGroupId = message.getMediaGroupId();
        Optional<TelegramMessage> previouslySavedMessage = mediaGroupId == null
            ? Optional.empty()
            : telegramMessageRepository.findByMediaGroupId(mediaGroupId);
        TelegramMessage telegramMessage = getOrSaveTelegramMessage(chat, message, mediaGroupId, previouslySavedMessage);
        SendMessage filesFailMessage =
            processMessageFiles(chat, message, telegramMessage, previouslySavedMessage, chat.getLanguageCode());

        return filesFailMessage != null
            ? filesFailMessage
            : notifyUserAboutMessage(chat, message, mediaGroupId, telegramMessage, previouslySavedMessage);
    }

    private TelegramMessage getOrSaveTelegramMessage(TelegramChat chat,
        Message message,
        String mediaGroupId,
        Optional<TelegramMessage> previouslySavedMessage) {
        if (previouslySavedMessage.isPresent()) {
            return previouslySavedMessage.get();
        }

        String messageText = Optional.ofNullable(message.getText())
            .orElse(message.getCaption());
        Instant currentTime = Instant.now();

        TelegramMessage telegramMessage = TelegramMessage.builder()
            .chat(chat)
            .fromManager(false)
            .mediaGroupId(mediaGroupId)
            .status(MessageDeliveryStatus.SENT)
            .sendAt(currentTime)
            .updatedAt(currentTime)
            .text(messageText)
            .messageViewingStatus(MessageViewingStatus.UNREAD)
            .telegramMessageId(message.getMessageId())
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
        Optional<TelegramMessage> previouslySavedMessage,
        String lang) {
        SendMessage resultMessage = null;
        FileInfo fileInfo = new FileInfo();

        if (message.hasPhoto()) {
            resultMessage = setPhotoInfo(message, telegramMessage, previouslySavedMessage, fileInfo, lang);
        } else if (message.hasDocument()) {
            resultMessage = setDocumentInfo(message, telegramMessage, previouslySavedMessage, fileInfo, lang);
        } else if (message.hasSticker()) {
            resultMessage = setStickerInfo(message, telegramMessage, previouslySavedMessage, fileInfo, lang);
        } else if (message.hasAnimation()) {
            resultMessage = setAnimationInfo(message, telegramMessage, previouslySavedMessage, fileInfo, lang);
        }

        if (fileInfo.getFileId() != null) {
            return setFileAsMessageAsset(message, telegramMessage, previouslySavedMessage, fileInfo, lang);
        } else if (!message.hasText()
            && !message.hasPhoto()
            && !message.hasDocument()
            && !message.hasSticker()
            && !message.hasAnimation()) {
            log.warn("No text or supported file found in message from chat ID: {}", chat.getChatId());
            resetTelegramChatDataToInternalStatus(chat, telegramMessage);
            String text =
                telegramBotResponseService.getResponseByLangAndMessageType(lang, MessageType.MANAGER_FILE_FAILED);
            resultMessage = MessageFactory.buildMessage(message.getChatId().toString(), text);
        }
        return resultMessage;
    }

    private void resetTelegramChatDataToInternalStatus(TelegramChat chat, TelegramMessage telegramMessage) {
        telegramMessageRepository.delete(telegramMessage);
        var lastMessage = telegramMessageRepository.findFirstByChatOrderBySendAtDesc(chat).orElse(null);
        var messageCount = chat.getUnreadMessagesCount() - 1;
        chat.setLastMessage(lastMessage);
        chat.setUnreadMessagesCount(messageCount);
        telegramChatRepository.save(chat);
    }

    private SendMessage setStickerInfo(Message message, TelegramMessage telegramMessage,
        Optional<TelegramMessage> previouslySavedMessage, FileInfo fileInfo, String lang) {
        Sticker sticker = message.getSticker();
        if (sticker == null) {
            if (previouslySavedMessage.isEmpty()) {
                telegramMessageRepository.delete(telegramMessage);
            }
            String text =
                telegramBotResponseService.getResponseByLangAndMessageType(lang, MessageType.MANAGER_FILE_FAILED);
            return MessageFactory.buildMessage(message.getChatId().toString(), text);
        } else {
            fileInfo.setFileId(sticker.getFileId());
            fileInfo.setFileSize(sticker.getFileSize() != null ? sticker.getFileSize().longValue() : 0L);
            fileInfo.setContentType("image/webp");
            fileInfo.setOriginalFileName("sticker.webp");
            return null;
        }
    }

    private SendMessage setAnimationInfo(Message message,
        TelegramMessage telegramMessage,
        Optional<TelegramMessage> previouslySavedMessage,
        FileInfo fileInfo,
        String lang) {
        Animation animation = message.getAnimation();
        if (animation == null) {
            if (previouslySavedMessage.isEmpty()) {
                telegramMessageRepository.delete(telegramMessage);
            }
            String text =
                telegramBotResponseService.getResponseByLangAndMessageType(lang, MessageType.MANAGER_FILE_FAILED);
            return MessageFactory.buildMessage(message.getChatId().toString(), text);
        } else {
            fileInfo.setFileId(animation.getFileId());
            fileInfo.setFileSize(animation.getFileSize() != null ? animation.getFileSize() : 0L);
            fileInfo.setContentType(animation.getMimetype());
            fileInfo.setOriginalFileName(animation.getFileName() != null
                ? animation.getFileName()
                : "animation.mp4");
            return null;
        }
    }

    private SendMessage setPhotoInfo(Message message,
        TelegramMessage telegramMessage,
        Optional<TelegramMessage> previouslySavedMessage,
        FileInfo fileInfo,
        String lang) {
        PhotoSize largestPhoto = message.getPhoto().stream()
            .max(Comparator.comparing(PhotoSize::getFileSize))
            .orElse(null);

        if (largestPhoto == null) {
            if (previouslySavedMessage.isEmpty()) {
                telegramMessageRepository.delete(telegramMessage);
            }
            String text =
                telegramBotResponseService.getResponseByLangAndMessageType(lang, MessageType.MANAGER_PHOTO_FAILED);
            return MessageFactory.buildMessage(message.getChatId().toString(), text);
        } else {
            fileInfo.setFileId(largestPhoto.getFileId());
            fileInfo.setFileSize(largestPhoto.getFileSize().longValue());
            return null;
        }
    }

    private SendMessage setDocumentInfo(Message message,
        TelegramMessage telegramMessage,
        Optional<TelegramMessage> previouslySavedMessage,
        FileInfo fileInfo,
        String lang) {
        Document document = message.getDocument();
        if (document == null) {
            if (previouslySavedMessage.isEmpty()) {
                telegramMessageRepository.delete(telegramMessage);
            }
            String text =
                telegramBotResponseService.getResponseByLangAndMessageType(lang, MessageType.MANAGER_FILE_FAILED);
            return MessageFactory.buildMessage(message.getChatId().toString(), text);
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
        Optional<TelegramMessage> previouslySavedMessage,
        FileInfo fileInfo,
        String lang) {
        try {
            File telegramFile = telegramExecutor.executeGetFile(new GetFile(fileInfo.getFileId()));
            if (telegramFile == null || telegramFile.getFilePath() == null) {
                log.warn("Telegram file not found for fileId: {}", fileInfo.getFileId());
                String text =
                    telegramBotResponseService.getResponseByLangAndMessageType(lang, MessageType.MANAGER_FILE_FAILED);
                return MessageFactory.buildMessage(message.getChatId().toString(), text);
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

            if (previouslySavedMessage.isPresent()) {
                if (telegramMessage.getAssets() == null) {
                    telegramMessage.setAssets(new ArrayList<>());
                }
                telegramMessage.getAssets().add(asset);
            } else {
                telegramMessage.setAssets((new ArrayList<>(List.of(asset))));
            }

            messageAssetRepository.save(asset);
        } catch (TelegramBotExecutionException | UnsupportedTelegramAssetException | IOException e) {
            log.error("Error loading or saving file from Telegram (Filename: {}): {}", fileInfo.getOriginalFileName(),
                e.getMessage(), e);
            String text =
                telegramBotResponseService.getResponseByLangAndMessageType(lang, MessageType.MANAGER_FILE_FAILED);
            return MessageFactory.buildMessage(message.getChatId().toString(), text);
        }

        return null;
    }

    private SendMessage notifyUserAboutMessage(TelegramChat chat,
        Message message,
        String mediaGroupId,
        TelegramMessage telegramMessage,
        Optional<TelegramMessage> previouslySavedMessage) {
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
            .updatedAt(telegramMessage.getUpdatedAt())
            .text(telegramMessage.getText())
            .fromManager(telegramMessage.getFromManager())
            .deliveryStatus(telegramMessage.getStatus())
            .messageViewingStatus(telegramMessage.getMessageViewingStatus())
            .assets(assetDtos)
            .isUpdated(false)
            .build();

        if (previouslySavedMessage.isEmpty()) {
            telegramChatProducer.notifyNewMessage(telegramMessageDto, chat.getId());

            ChatDto.ChatDtoBuilder chatDtoBuilder = ChatDto.builder()
                    .id(chat.getId())
                    .firstName(chat.getFirstName())
                    .lastName(chat.getLastName())
                    .username(chat.getUsername())
                    .lastMessage(telegramMessageDto)
                    .unreadMessagesCount(chat.getUnreadMessagesCount());

            if (chat.getUser() != null) {
                ChatUserDto chatUserDto = ChatUserDto
                        .builder()
                        .firstName(chat.getUser().getRecipientName())
                        .lastName(chat.getUser().getRecipientSurname())
                        .email(chat.getUser().getRecipientEmail())
                        .build();

                chatDtoBuilder
                        .user(chatUserDto);
            }

            telegramChatProducer.notifyNewChat(chatDtoBuilder.build());

            String contentForNotification = buildContentForNotification(telegramMessage);

            telegramNotificationService.notifyManagerAboutNewMessagesFromUser(
                Optional.ofNullable(message.getFrom().getUserName()).orElse(message.getFrom().getFirstName()),
                contentForNotification,
                chat.getId());
            String text = telegramBotResponseService.getResponseByLangAndMessageType(chat.getLanguageCode(),
                MessageType.MESSAGE_SENT_TO_MANAGER);
            return MessageFactory.buildMessage(chat.getChatId(), text);
        } else {
            telegramMessageRepository.save(telegramMessage);
            log.info("Added asset to existing media group message: {}", mediaGroupId);
        }

        return null;
    }

    private String buildContentForNotification(TelegramMessage telegramMessage) {
        String contentForNotification = "Empty message";
        if (telegramMessage.getAssets() != null && !telegramMessage.getAssets().isEmpty()) {
            contentForNotification = telegramMessage.getAssets().getFirst().getType().equals(AssetType.IMAGE)
                ? "Image content ("
                    + telegramMessage.getAssets().size() + " images)"
                : "File content ("
                    + telegramMessage.getAssets().size() + " files)";

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
