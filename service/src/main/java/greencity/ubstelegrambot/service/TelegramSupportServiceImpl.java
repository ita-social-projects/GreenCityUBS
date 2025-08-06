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
import greencity.repository.MessageAssetRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.service.ubs.TelegramNotificationService;
import greencity.service.ubs.TelegramSupportService;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.util.SimpleMultipartFile;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramSupportServiceImpl implements TelegramSupportService {
    private final ApplicationContext applicationContext;
    private final TelegramChatRepository telegramChatRepository;
    private final UserRemoteWebClient userRemoteWebClient;
    private final TelegramMessageRepository telegramMessageRepository;
    private final MessageAssetRepository messageAssetRepository;
    private final TelegramExecutor executor;
    private final TelegramNotificationService telegramNotificationService;
    private final TelegramUtils telegramUtils;

    /**
     * {@inheritDoc}
     */
    public SendMessage processSupportMessage(Message message) {
        var bot = applicationContext.getBean(UBSTelegramBot.class);

        Optional<TelegramChat> optionalChat = telegramChatRepository.findByChatId(message.getFrom().getId().toString());
        if (optionalChat.isEmpty()) {
            log.warn("Telegram chat not found");
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString());
        }

        TelegramChat chat = optionalChat.get();

        if (message.hasText() && message.getText().contains(TelegramBotConstants.CLIENT_END_SUPPORT_MODE)) {
            chat.setChatState(ChatState.NORMAL);
            chat.setChatStateUpdatedAt(LocalDateTime.now());
            telegramChatRepository.save(chat);
            telegramNotificationService.notifyManagerAboutEndSupportModeFromUser(message.getFrom().getUserName());
            return MessageFactory.createEndSupportMessage(chat.getChatId());
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
                .sendAt(LocalDateTime.now())
                .text(messageText)
                .messageViewingStatus(MessageViewingStatus.UNREAD)
                .build();

            telegramMessageRepository.save(telegramMessage);
            chat.setLastMessage(telegramMessage);
            chat.setUnreadMessagesCount(chat.getUnreadMessagesCount() + 1);
            telegramChatRepository.save(chat);
        }

        TelegramMessageDto.TelegramMessageDtoBuilder telegramMessageDtoBuilder = TelegramMessageDto
            .builder()
            .id(telegramMessage.getId())
            .sendAt(telegramMessage.getSendAt())
            .text(telegramMessage.getText())
            .fromManager(telegramMessage.getFromManager())
            .deliveryStatus(telegramMessage.getStatus())
            .messageViewingStatus(telegramMessage.getMessageViewingStatus());

        if (!message.hasPhoto()) {
            telegramNotificationService.notifyNewMessage(telegramMessageDtoBuilder.build(), chat.getId());
            telegramNotificationService.notifyManagerAboutNewMessagesFromUser(
                Optional.ofNullable(message.getFrom().getUserName()).orElse(message.getFrom().getFirstName()),
                Optional.ofNullable(telegramMessage.getText()).orElse(TelegramBotConstants.PHOTO_CONTENT),
                chat.getId());
            return MessageFactory.buildMessage(chat.getChatId(),
                TelegramBotConstants.MESSAGE_SENT_TO_MANAGER_WAIT_FOR_RESPONSE);
        }

        PhotoSize largestPhoto = message.getPhoto().stream()
            .max(Comparator.comparing(PhotoSize::getFileSize))
            .orElse(null);

        if (largestPhoto == null) {
            if (previouslySavedMessage.isEmpty()) {
                telegramNotificationService.notifyNewMessage(telegramMessageDtoBuilder.build(), chat.getId());
                telegramNotificationService.notifyManagerAboutNewMessagesFromUser(
                    Optional.ofNullable(message.getFrom().getUserName()).orElse(message.getFrom().getFirstName()),
                    Optional.ofNullable(telegramMessage.getText()).orElse(TelegramBotConstants.PHOTO_CONTENT),
                    chat.getId());
            }
            log.warn("No photo found in media group message");
            return MessageFactory.buildMessage(message.getChatId().toString(),
                TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN);
        }

        try {
            File telegramFile = executor.executeGetFile(bot, new GetFile(largestPhoto.getFileId()));
            if (telegramFile == null || telegramFile.getFilePath() == null) {
                if (previouslySavedMessage.isEmpty()) {
                    telegramNotificationService.notifyNewMessage(telegramMessageDtoBuilder.build(), chat.getId());
                    telegramNotificationService.notifyManagerAboutNewMessagesFromUser(
                        Optional.ofNullable(message.getFrom().getUserName()).orElse(message.getFrom().getFirstName()),
                        Optional.ofNullable(telegramMessage.getText()).orElse(TelegramBotConstants.PHOTO_CONTENT),
                        chat.getId());
                }
                log.warn("Telegram file not found");
                return MessageFactory.buildMessage(message.getChatId().toString(),
                    TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN);
            }

            byte[] content = telegramUtils.fileToByteArray(telegramFile);

            MultipartFile multipartFile = new SimpleMultipartFile(content,
                TelegramUtils.getFileNameFromPath(telegramFile.getFilePath()),
                TelegramUtils.getFileNameFromPath(telegramFile.getFilePath()),
                TelegramUtils.getFileContentType(telegramFile.getFilePath()));
            String azureFileUrl = uploadFile(multipartFile);
            AssetType assetType =
                TelegramUtils.detectAssetType(TelegramUtils.getFileContentType(telegramFile.getFilePath()));

            MessageAsset asset = MessageAsset.builder()
                .url(azureFileUrl)
                .fileName(TelegramUtils.getFileNameFromPath(telegramFile.getFilePath()))
                .size(largestPhoto.getFileSize().longValue())
                .contentType(TelegramUtils.getFileContentType(telegramFile.getFilePath()))
                .type(assetType)
                .message(telegramMessage)
                .build();

            telegramMessage.setAssets(List.of((asset)));
            messageAssetRepository.save(asset);
        } catch (Exception e) {
            if (previouslySavedMessage.isEmpty()) {
                telegramNotificationService.notifyNewMessage(telegramMessageDtoBuilder.build(), chat.getId());
                telegramNotificationService.notifyManagerAboutNewMessagesFromUser(
                    Optional.ofNullable(message.getFrom().getUserName()).orElse(message.getFrom().getFirstName()),
                    Optional.ofNullable(telegramMessage.getText()).orElse(TelegramBotConstants.PHOTO_CONTENT),
                    chat.getId());
            }
            log.error("Error loading photo: {}", e.getMessage());
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

        telegramMessageDtoBuilder.assets(assetDtos).build();
        if (previouslySavedMessage.isPresent()) {
            return null;
        }

        telegramNotificationService.notifyNewMessage(telegramMessageDtoBuilder.build(), chat.getId());
        telegramNotificationService.notifyManagerAboutNewMessagesFromUser(
            Optional.ofNullable(message.getFrom().getUserName()).orElse(message.getFrom().getFirstName()),
            Optional.ofNullable(telegramMessage.getText()).orElse(TelegramBotConstants.PHOTO_CONTENT), chat.getId());

        return MessageFactory.buildMessage(chat.getChatId(),
            TelegramBotConstants.MESSAGE_SENT_TO_MANAGER_WAIT_FOR_RESPONSE);
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
