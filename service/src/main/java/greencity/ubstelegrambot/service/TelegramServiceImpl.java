package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.TestersSignInRequest;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.*;
import greencity.entity.order.Order;
import greencity.entity.telegram.*;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.enums.AssetType;
import greencity.enums.ChatState;
import greencity.enums.FeedbackState;
import greencity.enums.MessageDeliveryStatus;
import greencity.exceptions.NotFoundException;
import greencity.repository.*;
import greencity.service.ubs.*;
import greencity.specification.ChatSpecifications;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static greencity.constant.ErrorMessage.POSITION_NOT_FOUND;
import static greencity.constant.ValidationConstant.EMAIL_REGEXP;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {
    private final UserRemoteClient userRemoteClient;
    private final TelegramMessageRepository telegramMessageRepository;
    private final MessageAssetRepository messageAssetRepository;
    private final TelegramManagerRepository telegramManagerRepository;
    private final ApplicationContext applicationContext;
    private final TelegramChatRepository telegramChatRepository;
    private final AzureCloudStorageService azureCloudStorageService;
    private final UBSClientService ubsClientService;
    private final TelegramExecutor executor;
    private final ChatFeedbackRepository chatFeedbackRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    @Value("${greencity.sing-in.secret-token}")
    private String secretToken;
    @Value("${greencity.bots.ubs-bot-token}")
    private String telegramBotToken;
    private static final String USERNAME = "username";

    @Override
    public void sendMessageToUser(CreateTelegramMessageRequest request, MultipartFile[] files) {
        var bot = applicationContext.getBean(UBSTelegramBot.class);

        TelegramChat chat = telegramChatRepository.findById(request.getChatId())
            .orElseThrow(() -> new NotFoundException("Chat not found"));

        TelegramMessage message = TelegramMessage.builder()
            .chat(chat)
            .text(request.getText())
            .fromManager(true)
            .status(MessageDeliveryStatus.SENT)
            .sendAt(LocalDateTime.now())
            .build();

        List<MessageAsset> assets = new ArrayList<>();

        if (request.getText() != null) {
            var sendTextMessage = MessageFactory.buildMessage(chat.getChatId(), message.getText());
            executor.executeCommand(bot, sendTextMessage);
        }

        if (files != null) {
            for (MultipartFile file : files) {
                log.info(file.toString());
                String url = azureCloudStorageService.upload(file);
                AssetType assetType = detectAssetType(file);
                MessageAsset asset = MessageAsset.builder()
                    .url(url)
                    .fileName(file.getOriginalFilename())
                    .size(file.getSize())
                    .contentType(file.getContentType())
                    .type(assetType)
                    .message(message)
                    .build();
                assets.add(asset);

                if (assetType == AssetType.IMAGE) {
                    var sendPhotoMessage = MessageFactory.createPhotoSender(chat.getChatId(), url, "");
                    executor.executeSendPhoto(bot, sendPhotoMessage);
                }
            }
        }

        message.setAssets(assets);
        telegramMessageRepository.save(message);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public PageableDto<TelegramMessageDto> findUserMessageByChatId(Long chatId, Pageable pageable) {
        Page<TelegramMessage> messages = telegramMessageRepository.findByChatId(chatId, pageable);
        if (messages.isEmpty()) {
            throw new NotFoundException(String.format(TelegramBotConstants.MESSAGES_NOT_FOUND_FOR_CHAT, chatId));
        }

        List<TelegramMessageDto> messageDtoList = messages.stream()
            .map(message -> {
                List<MessageAssetDto> assetDtos = Optional.ofNullable(message.getAssets())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(asset -> new MessageAssetDto(
                        asset.getId(),
                        asset.getUrl(),
                        asset.getType(),
                        asset.getFileName(),
                        asset.getSize(),
                        asset.getContentType()))
                    .toList();

                return new TelegramMessageDto(
                    message.getId(),
                    message.getSendAt(),
                    message.getText(),
                    message.getFromManager(),
                    message.getStatus(),
                    assetDtos);
            }).toList();

        return new PageableDto<>(
            messageDtoList,
            messages.getTotalElements(),
            messages.getPageable().getPageNumber(),
            messages.getTotalPages());
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public PageableDto<ChatDto> getChats(String searchTerm, Pageable pageable) {
        Specification<TelegramChat> spec = ChatSpecifications.hasNameLike(searchTerm);

        Page<TelegramChat> chats = telegramChatRepository.findAll(spec, pageable);
        List<ChatDto> chatDtos = chats
            .getContent()
            .stream()
            .map(chat -> {
                ChatDto.ChatDtoBuilder chatDtoBuilder = ChatDto.builder()
                    .id(chat.getId())
                    .chatId(chat.getChatId())
                    .firstName(chat.getFirstName())
                    .lastName(chat.getLastName())
                    .username(chat.getUsername());

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

                telegramMessageRepository.findFirstByChatOrderBySendAtDesc(chat).ifPresent(message -> {
                    List<MessageAssetDto> assetDtos = Optional.ofNullable(message.getAssets())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(asset -> new MessageAssetDto(
                            asset.getId(),
                            asset.getUrl(),
                            asset.getType(),
                            asset.getFileName(),
                            asset.getSize(),
                            asset.getContentType()))
                        .toList();

                    TelegramMessageDto lastMessage = TelegramMessageDto.builder()
                        .id(message.getId())
                        .text(message.getText())
                        .sendAt(message.getSendAt())
                        .fromManager(message.getFromManager())
                        .deliveryStatus(message.getStatus())
                        .assets(assetDtos)
                        .build();

                    chatDtoBuilder.lastMessage(lastMessage);
                });
                return chatDtoBuilder.build();
            })
            .toList();

        return new PageableDto<>(
            chatDtos,
            chats.getTotalElements(),
            chats.getNumber(),
            chats.getTotalPages());
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public PageableDto<FeedbackDto> getAllFeedbacks(Pageable pageable) {
        Page<ChatFeedback> chatFeedbacks = chatFeedbackRepository.findAll(pageable);
        List<FeedbackDto> feedbackDtos = chatFeedbacks
            .getContent()
            .stream()
            .map(feedback -> new FeedbackDto(
                feedback.getId(),
                feedback.getChat().getId().toString(),
                feedback.getRating(),
                feedback.getComment()))
            .toList();

        return new PageableDto<>(
            feedbackDtos,
            chatFeedbacks.getTotalElements(),
            chatFeedbacks.getNumber(),
            chatFeedbacks.getTotalPages());
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public PageableDto<FeedbackDto> getAllFeedbacksByChatId(String chatId, Pageable pageable) {
        Page<ChatFeedback> chatFeedbacks = chatFeedbackRepository.findByChatIdPageable(chatId, pageable);
        List<FeedbackDto> feedbackDtos = chatFeedbacks
            .getContent()
            .stream()
            .map(feedback -> new FeedbackDto(
                feedback.getId(),
                feedback.getChat().getChatId(),
                feedback.getRating(),
                feedback.getComment()))
            .toList();

        return new PageableDto<>(
            feedbackDtos,
            chatFeedbacks.getTotalElements(),
            chatFeedbacks.getNumber(),
            chatFeedbacks.getTotalPages());
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public OrdersDataForUserDto getLastOrderByChatId(Long chatId) {
        TelegramChat telegramChat = telegramChatRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Chat with id " + chatId + " not found"));

        if (telegramChat.getUser() == null) {
            throw new NotFoundException("Order not found");
        }

        Order order = orderRepository.findFirstByUserIdOrderByOrderDateDesc(telegramChat.getUser().getId())
            .orElseThrow(() -> new NotFoundException("Order not found"));
        return ubsClientService.getOrdersData(order);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public ChatDto getChatById(Long chatId) {
        TelegramChat chat = telegramChatRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Chat with id " + chatId + " not found"));
        return ChatDto.builder()
            .id(chat.getId())
            .chatId(chat.getChatId())
            .firstName(chat.getFirstName())
            .lastName(chat.getLastName())
            .username(chat.getUsername())
            .build();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void processUpdate(Update update) {
        UBSTelegramBot ubsTelegramBot = applicationContext.getBean(UBSTelegramBot.class);

        var message = update.getMessage();
        String chatId = null;

        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getFrom().getId().toString();
        }

        if (update.hasMessage()) {
            chatId = update.getMessage().getFrom().getId().toString();
        }

        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);

        if (chat.isPresent()) {
            LocalDateTime updatedAt = chat.get().getChatStateUpdatedAt();
            Instant updatedAtInstant = updatedAt.atZone(ZoneId.systemDefault()).toInstant();

            if (Duration.between(updatedAtInstant, Instant.now()).toMinutes() > 10) {
                if (chat.get().getChatState() != ChatState.NORMAL) {
                    chat.get().setChatState(ChatState.NORMAL);
                    telegramChatRepository.save(chat.get());
                    executor.executeCommand(ubsTelegramBot, MessageFactory.buildMessage(chatId, TelegramBotConstants.PREVIOUS_SESSION_HAS_EXPIRED));
                    return;
                }
            }
        }

        if (update.hasCallbackQuery()) {
            CallbackQuery callBackQuery = update.getCallbackQuery();

            switch (callBackQuery.getData()) {
                case TelegramBotConstants.CLIENT_SUPPORT_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processSupportRequest(chatId));
                case TelegramBotConstants.SORTING_PRICES_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processSortingPricesRequest(chatId));
                case TelegramBotConstants.WORK_SCHEDULE_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processWorkScheduleRequest(chatId));
                case TelegramBotConstants.ADMISSION_RULES_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processAdmissionRulesRequest(chatId));
                case TelegramBotConstants.GREEN_OFFICE_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processGreenOfficeRequest(chatId));
                case TelegramBotConstants.GREEN_OFFICE_PROCESS_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processGreenOfficeAgreeRequest(chatId));
                case TelegramBotConstants.FEEDBACK_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processFeedbackRequest(chatId));
                case TelegramBotConstants.RATING_TERRIBLY_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processRatingFeedbackRequest(chatId, 1));
                case TelegramBotConstants.RATING_BADLY_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processRatingFeedbackRequest(chatId, 2));
                case TelegramBotConstants.RATING_SATISFACTORILY_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processRatingFeedbackRequest(chatId, 3));
                case TelegramBotConstants.RATING_GOOD_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processRatingFeedbackRequest(chatId, 4));
                case TelegramBotConstants.RATING_PERFECTLY_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processRatingFeedbackRequest(chatId, 5));
                case TelegramBotConstants.LOGIN_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processLoginRequest(chatId));
                default ->
                    executor.executeCommand(ubsTelegramBot, processMainMenuRequest(chatId));
            }
        } else {
            var text = message.getText();

            if (chat.isEmpty()) {
                if (text.contains(TelegramBotConstants.START_COMMAND)) {
                    executor.executeCommand(ubsTelegramBot, processStartBotRequest(message));
                }
            } else {
                switch (chat.get().getChatState()) {
                    case IN_SUPPORT ->
                        executor.executeCommand(ubsTelegramBot, processSupportMessage(message));
                    case ENTERING_GREEN_OFFICE_EMAIL ->
                        executor.executeCommand(ubsTelegramBot, processGreenOfficeEmail(message));
                    case MAKING_FEEDBACK ->
                        executor.executeCommand(ubsTelegramBot, processInputCommentRequest(message));
                    case LOGGING_AS_MANAGER ->
                        executor.executeCommand(ubsTelegramBot, processInputManagerCredentialsRequest(message));
                    default -> executor.executeCommand(ubsTelegramBot, processNormalMessageRequest(message));
                }
            }
        }
    }

    private SendMessage updateChatStateAndRespond(
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

    private SendMessage processStartBotRequest(Message message) {
        final String uuId = message.getText().replace(TelegramBotConstants.START_COMMAND, "").trim();
        final String chatId = message.getFrom().getId().toString();

        Optional<TelegramChat> telegramChat = telegramChatRepository.findByChatId(chatId);

        if (telegramChat.isEmpty()) {
            TelegramChat.TelegramChatBuilder newChatBuilder = TelegramChat
                .builder()
                .chatId(chatId)
                .username(message.getFrom().getUserName())
                .firstName(message.getFrom().getFirstName())
                .lastName(message.getFrom().getLastName())
                .isNotify(true)
                .chatState(ChatState.NORMAL)
                .chatStateUpdatedAt(LocalDateTime.now());

            if (!uuId.isEmpty()) {
                Optional<User> user = userRepository.findUserByUuid(uuId);
                user.ifPresent(newChatBuilder::user);
            }

            TelegramChat createdChat = newChatBuilder.build();

            telegramChatRepository.save(createdChat);

            ChatDto chatDto = ChatDto.builder()
                .id(createdChat.getId())
                .chatId(createdChat.getChatId())
                .firstName(createdChat.getFirstName())
                .lastName(createdChat.getLastName())
                .username(createdChat.getUsername()).build();

            notifyNewChat(chatDto);
        } else {
            if (!uuId.isEmpty()) {
                Optional<User> user = userRepository.findUserByUuid(uuId);
                user.ifPresent(value -> telegramChat.get().setUser(value));
                telegramChatRepository.save(telegramChat.get());
            }
        }

        return MessageFactory.createWelcomeMessage(chatId);
    }

    private SendMessage processSupportRequest(String chatId) {
        return updateChatStateAndRespond(chatId, ChatState.IN_SUPPORT,
            MessageFactory::createSupportMessageCallBackQuery);
    }

    private SendMessage processWorkScheduleRequest(String chatId) {
        return updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createWorkScheduleMessage);
    }

    private SendMessage processSortingPricesRequest(String chatId) {
        return updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createSortingPricesMessage);
    }

    private SendMessage processAdmissionRulesRequest(String chatId) {
        return updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createAdmissionRulesMessage);
    }

    private SendMessage processGreenOfficeRequest(String chatId) {
        return updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createGreenOfficeMessage);
    }

    private SendMessage processGreenOfficeAgreeRequest(String chatId) {
        return updateChatStateAndRespond(chatId, ChatState.ENTERING_GREEN_OFFICE_EMAIL,
            MessageFactory::createEnteringEmailMessage);
    }

    private SendMessage processFeedbackRequest(String chatId) {
        return updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createFeedbackMessage);
    }

    private SendMessage processMainMenuRequest(String chatId) {
        return updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createAvailableCommandsMessage);
    }

    private SendMessage processUnknownRequest(String chatId) {
        return updateChatStateAndRespond(chatId, ChatState.NORMAL,
            MessageFactory::createUnknownCommandMessage);
    }

    private SendMessage processLoginRequest(String chatId) {
        Optional<TelegramManager> telegramManager = telegramManagerRepository.findByChatId(chatId);

        if (telegramManager.isPresent()) {
            return MessageFactory.createSuccessLoginMessage(
                chatId,
                telegramManager.get().getEmployee().getFirstName() + " "
                    + telegramManager.get().getEmployee().getLastName());
        }
        return updateChatStateAndRespond(chatId, ChatState.LOGGING_AS_MANAGER,
            MessageFactory::createLoginMessage);
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

        boolean isManager = checkIsEmployeeManager(employee.get());

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
        String text = message.getText();

        if (text == null) {
            return processUnknownRequest(message.getChatId().toString());
        }

        switch (text) {
            case TelegramBotConstants.START_COMMAND -> {
                return processStartBotRequest(message);
            }
            case TelegramBotConstants.SUPPORT_COMMAND -> {
                return processSupportRequest(message.getChatId().toString());
            }
            case TelegramBotConstants.LOGIN_COMMAND -> {
                return processLoginRequest(message.getChatId().toString());
            }
            case TelegramBotConstants.HELP_COMMAND -> {
                return processMainMenuRequest(message.getChatId().toString());
            }
            default -> {
                return processUnknownRequest(message.getChatId().toString());
            }
        }
    }

    private SendMessage processGreenOfficeEmail(Message message) {
        String email = message.getText();
        if (!isValidEmail(email)) {
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
                telegramMessage = telegramMessageRepository.findByMediaGroupId(message.getMediaGroupId()).orElse(telegramMessage);
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

                    AssetType assetType = detectAssetType(getFileContentType(telegramFile.getFilePath()));

                    MessageAsset asset = MessageAsset.builder()
                        .url(azureFileUrl)
                        .fileName(getFileNameFromPath(telegramFile.getFilePath()))
                        .size(largestPhoto.getFileSize().longValue())
                        .contentType(getFileContentType(telegramFile.getFilePath()))
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
        notifyManagerAboutNewMessagesFromUser(message.getFrom().getUserName(), messageText, chat.get().getId());
        return MessageFactory.buildMessage(chat.get().getChatId(),
            TelegramBotConstants.MESSAGE_SENT_TO_MANAGER_WAIT_FOR_RESPONSE);
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

    private void notifyNewChat(ChatDto chatDto) {
        messagingTemplate.convertAndSend("/topic/chats", chatDto);
    }

    private void notifyNewMessage(TelegramMessageDto messageDto, Long chatId) {
        messagingTemplate.convertAndSend("/topic/messages/" + chatId, messageDto);
    }

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Pattern emailPattern = Pattern.compile(EMAIL_REGEXP);
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    private boolean checkIsEmployeeManager(Employee employee) {
        var employeePositions = employee.getEmployeePosition();

        Position serviceManager = positionRepository.findById(1L)
            .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));

        Position manager = positionRepository.findById(2L)
            .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));

        return employeePositions.contains(manager) || employeePositions.contains(serviceManager);
    }

    private String getFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash != -1) {
            return filePath.substring(lastSlash + 1);
        }
        return filePath;
    }

    private String getFileContentType(String filePath) {
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

    private AssetType detectAssetType(MultipartFile file) {
        String contentType = file.getContentType();
        return detectAssetType(contentType);
    }

    private AssetType detectAssetType(String file) {
        if (file == null) {
            return AssetType.FILE;
        }

        if (file.startsWith("image/")) {
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

    private void notifyManagerAboutEndSupportModeFromUser(String username) {
        var telegramBot = applicationContext.getBean(UBSTelegramBot.class);
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            var notification = MessageFactory.createEndSupportModeNotification(manager.getChatId(), username);
            executor.executeCommand(telegramBot, notification);
        }
    }
}
