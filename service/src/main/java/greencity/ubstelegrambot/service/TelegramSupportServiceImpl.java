package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.dto.telegram.MessageAssetDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.entity.telegram.MessageAsset;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.telegram.TelegramMessage;
import greencity.enums.AssetType;
import greencity.enums.ChatState;
import greencity.enums.MessageDeliveryStatus;
import greencity.repository.*;
import greencity.service.ubs.AzureCloudStorageService;
import greencity.service.ubs.TelegramSupportService;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramSupportServiceImpl implements TelegramSupportService {
    @Value("${greencity.bots.ubs-bot-token}")
    private String telegramBotToken;
    private final ApplicationContext applicationContext;
    private final TelegramChatRepository telegramChatRepository;
    private final TelegramManagerRepository telegramManagerRepository;
    private final AzureCloudStorageService azureCloudStorageService;
    private final TelegramMessageRepository telegramMessageRepository;
    private final MessageAssetRepository messageAssetRepository;
    private final TelegramExecutor executor;
    private final SimpMessagingTemplate messagingTemplate;

    public SendMessage processSupportMessage(Message message) {
        var bot = applicationContext.getBean(UBSTelegramBot.class);
        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(message.getFrom().getId().toString());
        if (chat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString());
        }

        if (message.hasText() && message.getText().contains(TelegramBotConstants.CLIENT_END_SUPPORT_MODE)) {
            chat.get().setChatState(ChatState.NORMAL);
            chat.get().setChatStateUpdatedAt(LocalDateTime.now());
            telegramChatRepository.save(chat.get());
            notifyManagerAboutEndSupportModeFromUser(message.getFrom().getUserName());
            return MessageFactory.createEndSupportMessage(chat.get().getChatId());
        }

        TelegramMessage telegramMessage = TelegramMessage.builder()
                .chat(chat.get())
                .fromManager(false)
                .mediaGroupId(message.getMediaGroupId())
                .status(MessageDeliveryStatus.SENT)
                .sendAt(LocalDateTime.now())
                .build();

        if (message.hasPhoto()) {
            if (message.getMediaGroupId() != null) {
                telegramMessage =
                        telegramMessageRepository.findByMediaGroupId(message.getMediaGroupId()).orElse(telegramMessage);
            }

            telegramMessageRepository.save(telegramMessage);

            PhotoSize largestPhoto = message.getPhoto().stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null);

            if (largestPhoto == null) {
                log.warn("No photo found in media group message");
                return MessageFactory.buildMessage(message.getChatId().toString(),
                        TelegramBotConstants.SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN);
            }

            try {
                File telegramFile = executor.executeGetFile(bot, new GetFile(largestPhoto.getFileId()));
                if (telegramFile == null || telegramFile.getFilePath() == null) {
                    return MessageFactory.buildMessage(message.getChatId().toString(),
                            TelegramBotConstants.SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN);
                }

                URI uri = URI.create(telegramFile.getFileUrl(telegramBotToken));

                try (InputStream inputStream = uri.toURL().openStream()) {
                    String azureFileUrl = azureCloudStorageService.upload(
                            inputStream, telegramFile.getFilePath(), telegramFile.getFileSize());

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

                    messageAssetRepository.save(asset);
                }
            } catch (Exception e) {
                log.error("Error loading photo: {}", e.getMessage());
                return MessageFactory.buildMessage(message.getChatId().toString(),
                        TelegramBotConstants.SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN);
            }
        }

        String messageText = message.hasText() ? message.getText() : message.getCaption();
        telegramMessage.setText(messageText);
        telegramMessageRepository.save(telegramMessage);

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

        TelegramMessageDto telegramMessageDto = TelegramMessageDto
                .builder()
                .id(telegramMessage.getId())
                .sendAt(telegramMessage.getSendAt())
                .text(telegramMessage.getText())
                .fromManager(telegramMessage.getFromManager())
                .deliveryStatus(telegramMessage.getStatus())
                .assets(assetDtos)
                .build();

        notifyNewMessage(telegramMessageDto, chat.get().getId());
        notifyManagerAboutNewMessagesFromUser(
                message.getFrom().getUserName() == null ? message.getFrom().getFirstName()
                        : message.getFrom().getUserName(),
                messageText, chat.get().getId());
        return MessageFactory.buildMessage(chat.get().getChatId(),
                TelegramBotConstants.MESSAGE_SENT_TO_MANAGER_WAIT_FOR_RESPONSE);
    }

    private void notifyManagerAboutEndSupportModeFromUser(String username) {
        var telegramBot = applicationContext.getBean(UBSTelegramBot.class);
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            var notification = MessageFactory.createEndSupportModeNotification(manager.getChatId(), username);
            executor.executeCommand(telegramBot, notification);
        }
    }

    private void notifyManagerAboutNewMessagesFromUser(String username, String messageText, Long innerChatId) {
        var telegramBot = applicationContext.getBean(UBSTelegramBot.class);
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            SendMessage notification =
                    MessageFactory.createNotificationMessageForManager(manager.getChatId(), username, messageText,
                            innerChatId);
            executor.executeCommand(telegramBot, notification);
        }
    }

    private void notifyNewMessage(TelegramMessageDto messageDto, Long chatId) {
        messagingTemplate.convertAndSend("/topic/messages/" + chatId, messageDto);
    }
}
