package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.TestersSignInRequest;
import greencity.dto.telegram.MessageAssetDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.entity.telegram.*;
import greencity.entity.user.employee.Employee;
import greencity.enums.AssetType;
import greencity.enums.ChatState;
import greencity.enums.FeedbackState;
import greencity.enums.MessageDeliveryStatus;
import greencity.repository.*;
import greencity.service.ubs.AzureCloudStorageService;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.TelegramUpdateProcessor;
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
import org.telegram.telegrambots.meta.api.objects.*;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service("userUpdateProcessor")
@RequiredArgsConstructor
public class UserUpdateProcessor implements TelegramUpdateProcessor {
    private final ApplicationContext applicationContext;
    private final TelegramChatRepository telegramChatRepository;
    private final ChatFeedbackRepository chatFeedbackRepository;
    private final TelegramManagerRepository telegramManagerRepository;
    private final AzureCloudStorageService azureCloudStorageService;
    private final TelegramMessageRepository telegramMessageRepository;
    private final MessageAssetRepository messageAssetRepository;
    private final TelegramExecutor executor;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    @Value("${greencity.bots.ubs-bot-token}")
    private String telegramBotToken;
    private final EmployeeRepository employeeRepository;
    private final TelegramUtils telegramUtils;
    private final UserRemoteClient userRemoteClient;
    @Value("${greencity.sing-in.secret-token}")
    private String secretToken;
    private static final String USERNAME = "username";

    @Override
    public SendMessage process(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callBackQuery = update.getCallbackQuery();
            String chatId = callBackQuery.getMessage().getChatId().toString();
            switch (callBackQuery.getData()) {
                case TelegramBotConstants.CLIENT_SUPPORT_CALLBACK -> {
                    return processSupportRequest(chatId);
                }
                case TelegramBotConstants.SORTING_PRICES_CALLBACK -> {
                    return processSortingPricesRequest(chatId);
                }
                case TelegramBotConstants.WORK_SCHEDULE_CALLBACK -> {
                    return processWorkScheduleRequest(chatId);
                }
                case TelegramBotConstants.ADMISSION_RULES_CALLBACK -> {
                    return processAdmissionRulesRequest(chatId);
                }
                case TelegramBotConstants.GREEN_OFFICE_CALLBACK -> {
                    return processGreenOfficeRequest(chatId);
                }
                case TelegramBotConstants.GREEN_OFFICE_PROCESS_CALLBACK -> {
                    return processGreenOfficeAgreeRequest(chatId);
                }
                case TelegramBotConstants.FEEDBACK_CALLBACK -> {
                    return processFeedbackRequest(chatId);
                }
                case TelegramBotConstants.RATING_TERRIBLY_CALLBACK -> {
                    return processRatingFeedbackRequest(chatId, 1);
                }
                case TelegramBotConstants.RATING_BADLY_CALLBACK -> {
                    return processRatingFeedbackRequest(chatId, 2);
                }
                case TelegramBotConstants.RATING_SATISFACTORILY_CALLBACK -> {
                    return processRatingFeedbackRequest(chatId, 3);
                }
                case TelegramBotConstants.RATING_GOOD_CALLBACK -> {
                    return processRatingFeedbackRequest(chatId, 4);
                }
                case TelegramBotConstants.RATING_PERFECTLY_CALLBACK -> {
                    return processRatingFeedbackRequest(chatId, 5);
                }
                case TelegramBotConstants.LOGIN_CALLBACK -> {
                    return processLoginRequest(chatId);
                }
                default -> {
                    return processMainMenuRequest(chatId);
                }
            }
        } else {
            var message = update.getMessage();

            Optional<TelegramChat> chatOpt = telegramChatRepository.findByChatId(message.getChatId().toString());

            if (chatOpt.isEmpty()) {
                return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString());
            }

            TelegramChat chat = chatOpt.get();
            switch (chat.getChatState()) {
                case IN_SUPPORT -> {
                    return processSupportMessage(message);
                }
                case ENTERING_GREEN_OFFICE_EMAIL -> {
                    return processGreenOfficeEmail(message);
                }
                case MAKING_FEEDBACK -> {
                    return processInputCommentRequest(message);
                }
                case LOGGING_AS_MANAGER -> {
                    return processInputManagerCredentialsRequest(message);
                }
                default -> {
                    return processNormalMessageRequest(message);
                }
            }
        }
    }

    private SendMessage processRatingFeedbackRequest(String chatId, int rating) {
        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);

        if (chat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(chatId);
        }

        chat.get().setChatState(ChatState.MAKING_FEEDBACK);
        chat.get().setChatStateUpdatedAt(LocalDateTime.now());
        telegramChatRepository.save(chat.get());

        Optional<ChatFeedback> inProgressFeedback = chatFeedbackRepository
                .findByChatIdAndFeedbackState(chat.get().getId(), FeedbackState.IN_PROGRESS);

        if (inProgressFeedback.isPresent()) {
            inProgressFeedback.get().setFeedbackState(FeedbackState.CLOSED);
            chatFeedbackRepository.save(inProgressFeedback.get());
        }

        ChatFeedback chatFeedback = ChatFeedback.builder()
                .rating(rating)
                .feedbackState(FeedbackState.IN_PROGRESS)
                .chat(chat.get())
                .build();

        chatFeedbackRepository.save(chatFeedback);

        if (rating >= 4) {
            return MessageFactory.createGreatFeedbackMessage(chatId);
        }

        return MessageFactory.createBadFeedbackMessage(chatId);
    }

    private SendMessage processSupportRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.IN_SUPPORT,
                MessageFactory::createSupportMessageCallBackQuery);
    }

    private SendMessage processSortingPricesRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                MessageFactory::createSortingPricesMessage);
    }

    private SendMessage processWorkScheduleRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                MessageFactory::createWorkScheduleMessage);
    }

    private SendMessage processAdmissionRulesRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                MessageFactory::createAdmissionRulesMessage);
    }

    private SendMessage processGreenOfficeRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                MessageFactory::createGreenOfficeMessage);
    }

    private SendMessage processGreenOfficeAgreeRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.ENTERING_GREEN_OFFICE_EMAIL,
                MessageFactory::createEnteringEmailMessage);
    }

    private SendMessage processFeedbackRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                MessageFactory::createFeedbackMessage);
    }

    private SendMessage processLoginRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.LOGGING_AS_MANAGER,
            MessageFactory::createLoginMessage);
    }

    private SendMessage processMainMenuRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createAvailableCommandsMessage);
    }

    private SendMessage processUnknownRequest(String chatId) {
        return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createUnknownCommandMessage);
    }

    private SendMessage processSupportMessage(Message message) {
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

                    AssetType assetType = TelegramUtils.detectAssetType(TelegramUtils.getFileContentType(telegramFile.getFilePath()));

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

    private SendMessage processGreenOfficeEmail(Message message) {
        String email = message.getText();
        if (!TelegramUtils.isValidEmail(email)) {
            return MessageFactory.createInvalidEmailMessage(message.getChatId().toString());
        }

        Optional<TelegramChat> optChat = telegramChatRepository.findByChatId(message.getFrom().getId().toString());

        if (optChat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString());
        }

        TelegramChat chat = optChat.get();

        String username =
            chat.getUser() != null ? chat.getUser().getRecipientName() + " " + chat.getUser().getRecipientSurname()
                : message.getFrom().getUserName();

        notificationService.notifyManagerWithNewGreenOfficeRequestFromTelegramBot(email, username);
        chat.setChatState(ChatState.NORMAL);
        chat.setChatStateUpdatedAt(LocalDateTime.now());
        telegramChatRepository.save(chat);
        return MessageFactory.createGreenOfficeThanksMessage(message.getChatId().toString());
    }

    private SendMessage processInputCommentRequest(Message message) {
        Optional<TelegramChat> telegramChat = telegramChatRepository.findByChatId(message.getChatId().toString());

        if (telegramChat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString());
        }

        Optional<ChatFeedback> chatFeedback = chatFeedbackRepository
            .findByChatIdAndFeedbackState(
                telegramChat.get().getId(),
                FeedbackState.IN_PROGRESS);

        if (chatFeedback.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString());
        }

        chatFeedback.get().setComment(message.getText());
        chatFeedback.get().setFeedbackState(FeedbackState.CLOSED);
        chatFeedbackRepository.save(chatFeedback.get());
        telegramChat.get().setChatState(ChatState.NORMAL);
        telegramChat.get().setChatStateUpdatedAt(LocalDateTime.now());
        telegramChatRepository.save(telegramChat.get());
        return MessageFactory.createFeedbackThanksMessage(message.getChatId().toString());
    }

    private SendMessage processInputManagerCredentialsRequest(Message message) {
        String[] parts = message.getText().split(":");

        if (parts.length < 2) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                TelegramBotConstants.INCORRECT_LOGIN_FORMAT);
        }

        String login = parts[0];
        String password = parts[1];

        Optional<Employee> employee = employeeRepository.findByEmailWithPositions(login);

        if (employee.isEmpty()) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                TelegramBotConstants.USER_IS_NOT_EMPLOYEE);
        }

        boolean isManager = telegramUtils.checkIsEmployeeManager(employee.get());

        if (!isManager) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                TelegramBotConstants.EMPLOYEE_IS_NOT_MANAGER);
        }

        var response = userRemoteClient.signIn(new TestersSignInRequest(login, password, secretToken));

        if (!response.getStatusCode().is2xxSuccessful()) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                TelegramBotConstants.SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN);
        }

        var responseBody = response.getBody();
        String name = (responseBody != null && responseBody.name() != null) ? responseBody.name() : USERNAME;

        telegramManagerRepository.save(
            TelegramManager
                .builder()
                .chatId(message.getChatId().toString())
                .employee(employee.get())
                .build());

        return MessageFactory.createSuccessLoginMessage(
            message.getChatId().toString(),
            name);
    }

    private SendMessage processNormalMessageRequest(Message message) {
        if (message.getText() == null) {
            return processUnknownRequest(message.getChatId().toString());
        }

        String text = message.getText().split(" ")[0];

        if (text == null) {
            return processUnknownRequest(message.getChatId().toString());
        }

        switch (text) {
            case TelegramBotConstants.START_COMMAND, TelegramBotConstants.HELP_COMMAND -> {
                return processMainMenuRequest(message.getChatId().toString());
            }
            case TelegramBotConstants.SUPPORT_COMMAND -> {
                return processSupportRequest(message.getChatId().toString());
            }
            case TelegramBotConstants.LOGIN_COMMAND -> {
                return processLoginRequest(message.getChatId().toString());
            }
            default -> {
                return processUnknownRequest(message.getChatId().toString());
            }
        }
    }
}
