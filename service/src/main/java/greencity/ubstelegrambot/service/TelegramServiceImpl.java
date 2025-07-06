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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.time.LocalDateTime;
import java.util.*;
import static greencity.constant.ErrorMessage.POSITION_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {
    private final UserRemoteClient userRemoteClient;
    private final TelegramMessageRepository telegramMessageRepository;
    private final TelegramManagerRepository telegramManagerRepository;
    private final TelegramManagerNotificationServiceImpl telegramManagerNotification;
    private final ApplicationContext applicationContext;
    private final TelegramChatRepository telegramChatRepository;
    private final AzureCloudStorageService azureCloudStorageService;
    private final UBSClientService ubsClientService;
    private final TelegramExecutor executor;
    private final Map<String, String> userState = new HashMap<>();
    private final ChatFeedbackRepository chatFeedbackRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationTimestampRepository notificationTimestampRepository;
    private NotificationService notificationService;
    private Integer messageIdForDeleting;
    @Value("${greencity.sing-in.secret-token}")
    private String secretToken;
    private static final String USERNAME = "username";
    private static final String INCORRECT_LOGIN_FORMAT = "Incorrect login format. Please use format: login:password";

    private boolean checkIsEmployeeManager(Employee employee) {
        var employeePositions = employee.getEmployeePosition();

        Position serviceManager = positionRepository.findById(1L)
            .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));

        Position manager = positionRepository.findById(2L)
            .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));

        return employeePositions.contains(manager) || employeePositions.contains(serviceManager);
    }

    public SendMessage processStartBotRequest(Message message) {
        final String uuId = message.getText().replace(TelegramBotConstants.START_COMMAND, "").trim();
        final String chatId = message.getFrom().getId().toString();

        Optional<TelegramChat> telegramChat = telegramChatRepository.findByChatId(chatId);

        if (!uuId.isEmpty() || (telegramChat.isPresent() && telegramChat.get().getUser() != null)) {
            Optional<Employee> employee = employeeRepository.findByUuid(uuId);

            if (employee.isPresent()) {
                boolean isManager = checkIsEmployeeManager(employee.get());
                if (isManager) {
                    telegramChat.ifPresent(telegramChatRepository::delete);

                    Optional<TelegramManager> telegramManager =
                        telegramManagerRepository.findById(employee.get().getId().toString());

                    if (telegramManager.isEmpty()) {
                        return processLoginRequest(chatId);
                    } else {
                        return MessageFactory.createWelcomeManagerMessage(chatId);
                    }
                }
            }
        }

        if (telegramChat.isEmpty()) {
            TelegramChat.TelegramChatBuilder newChatBuilder = TelegramChat
                .builder()
                .chatId(chatId)
                .username(message.getFrom().getUserName())
                .firstName(message.getFrom().getFirstName())
                .lastName(message.getFrom().getLastName())
                .isNotify(true) // need to specify a correct value
                .chatState(ChatState.NORMAL);

            if (!uuId.isEmpty()) {
                Optional<User> user = userRepository.findUserByUuid(uuId);
                user.ifPresent(newChatBuilder::user);
            }
            telegramChatRepository.save(newChatBuilder.build());
        }

        return MessageFactory.createWelcomeMessage(chatId);
    }

    /**
     * Detects the {@link AssetType} of the given file based on its MIME type.
     *
     * <p>
     * This method analyzes the MIME type (Content-Type) of the provided
     * {@link MultipartFile} and returns the corresponding {@link AssetType}:
     * </p>
     * <ul>
     * <li>{@code image/*} → {@link AssetType#IMAGE}</li>
     * <li>{@code video/*} → {@link AssetType#VIDEO}</li>
     * <li>{@code audio/*} → {@link AssetType#AUDIO}</li>
     * <li>{@code application/pdf} → {@link AssetType#FILE}</li>
     * <li>Any other or unknown types → {@link AssetType#FILE}</li>
     * </ul>
     *
     * @param file the uploaded file for which the asset type should be determined
     * @return the detected {@link AssetType}; defaults to {@link AssetType#FILE} if
     *         unknown
     */
    private AssetType detectAssetType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return AssetType.FILE;
        }

        if (contentType.startsWith("image/")) {
            return AssetType.IMAGE;
        }
        if (contentType.startsWith("video/")) {
            return AssetType.VIDEO;
        }
        if (contentType.startsWith("audio/")) {
            return AssetType.AUDIO;
        }

        return AssetType.FILE;
    }

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
                System.out.println(file);
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

    @Override
    public PageableDto<TelegramMessageDto> findUserMessageByChatId(Long chatId, Pageable pageable) {
        Page<TelegramMessage> messages = telegramMessageRepository.findByChatId(chatId, pageable);
        if (messages.isEmpty()) {
            throw new NotFoundException(String.format(TelegramBotConstants.MESSAGES_NOT_FOUND_FOR_CHAT, chatId));
        }

        List<TelegramMessageDto> messageDtoList = messages.stream()
            .map(message -> {
                List<MessageAssetDto> assetDtos = message
                    .getAssets()
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
                    List<MessageAssetDto> assetDtos = message
                        .getAssets()
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

    private SendMessage processSupportRequest(String chatId) {
        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);
        if (chat.isPresent()) {
            chat.get().setChatState(ChatState.IN_SUPPORT);
            telegramChatRepository.save(chat.get());
            return MessageFactory.createSupportMessageCallBackQuery(chatId);
        } else {
            return MessageFactory.createUnknownErrorOccurredMessage(chatId);
        }
    }

    private SendMessage processWorkScheduleRequest(String chatId) {
        return MessageFactory.createWorkScheduleMessage(chatId);
    }

    private SendMessage processAdmissionRulesRequest(String chatId) {
        return MessageFactory.createAdmissionRulesMessage(chatId);
    }

    private SendMessage processGreenOfficeRequest(String chatId) {
        return MessageFactory.createGreenOfficeMessage(chatId);
    }

    private SendMessage processFeedbackRequest(String chatId) {
        return MessageFactory.createFeedbackMessage(chatId);
    }

    private SendMessage processMainMenuRequest(String chatId) {
        return MessageFactory.createAvailableCommandOption(chatId);
    }

    private SendMessage processUnknownRequest(String chatId) {
        return MessageFactory.createUnknownCommandMessage(chatId);
    }

    private SendMessage processRatingFeedbackRequest(String chatId, int rating) {
        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);

        if (chat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(chatId);
        }

        chat.get().setChatState(ChatState.MAKING_FEEDBACK);
        telegramChatRepository.save(chat.get());

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

    private SendMessage processLoginRequest(String chatId) {
        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);

        if (chat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(chatId);
        }

        chat.get().setChatState(ChatState.LOGGING_AS_MANAGER);
        telegramChatRepository.save(chat.get());
        return MessageFactory.createLoginMessage(chatId);
    }

    private SendMessage processInputManagerCredentialsRequest(Message message) {
        String[] parts = message.getText().split(":");

        if (parts.length < 3) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(), INCORRECT_LOGIN_FORMAT);
        }

        String login = parts[1];
        String password = parts[2];

        Optional<Employee> employee = employeeRepository.findByEmail(login);

        if (employee.isEmpty()) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(), "User is not employee");
        }

        boolean isManager = checkIsEmployeeManager(employee.get());

        if (!isManager) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(), "User is not manager");
        }

        var response = userRemoteClient.signIn(new TestersSignInRequest(login, password, secretToken));

        if (!response.getStatusCode().is2xxSuccessful()) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                "Something went wrong please try again later");
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

    private SendMessage processInputCommentRequest(Message message) {
        Optional<ChatFeedback> chatFeedback = chatFeedbackRepository
            .findByChatIdAndFeedbackState(
                message.getChatId(),
                FeedbackState.IN_PROGRESS);

        if (chatFeedback.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString());
        }

        chatFeedback.get().setComment(message.getText());
        return MessageFactory.createFeedbackThanksMessage(message.getChatId().toString());
    }

    @Override
    public void processUpdate(Update update) {
        UBSTelegramBot ubsTelegramBot = applicationContext.getBean(UBSTelegramBot.class);

        if (update.hasCallbackQuery()) {
            CallbackQuery callBackQuery = update.getCallbackQuery();
            String chatId = callBackQuery.getFrom().getId().toString();

            switch (callBackQuery.getData()) {
                case TelegramBotConstants.CLIENT_SUPPORT_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processSupportRequest(chatId));
                case TelegramBotConstants.WORK_SCHEDULE_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processWorkScheduleRequest(chatId));
                case TelegramBotConstants.ADMISSION_RULES_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processAdmissionRulesRequest(chatId));
                case TelegramBotConstants.GREEN_OFFICE_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, processGreenOfficeRequest(chatId));
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
            var message = update.getMessage();
            var text = message.getText();
            var chatId = message.getChatId().toString();

            Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);

            if (chat.isEmpty()) {
                if (text.contains(TelegramBotConstants.START_COMMAND)) {
                    executor.executeCommand(ubsTelegramBot, processStartBotRequest(message));
                }
            } else {
                switch (chat.get().getChatState()) {
                    case IN_SUPPORT -> {
                        // save a message to db, also we need to check for end support mode command
                    }
                    case MAKING_FEEDBACK ->
                        executor.executeCommand(ubsTelegramBot, processInputCommentRequest(message));
                    case LOGGING_AS_MANAGER ->
                        executor.executeCommand(ubsTelegramBot, processInputManagerCredentialsRequest(message));
                    default -> executor.executeCommand(ubsTelegramBot, processNormalMessageRequest(message));
                }
            }
        }
    }

    // private void notifyManagerOfGreenOfficeRequest(Message message, String email)
    // {
    // String chatId = String.valueOf(message.getChatId());
    // Optional<TelegramChat> userOpt = telegramChatRepository.findByChatId(chatId);
    //
    // String username = userOpt
    // .map(u -> u.getUser().getRecipientName() + " " +
    // u.getUser().getRecipientSurname())
    // .orElse(message.getFrom().getUserName());
    //
    // notificationService.notifyManagerWithNewGreenOfficeRequestFromTelegramBot(email,
    // username);
    // }

    // @Lazy
    // @Autowired
    // public void setNotificationService(NotificationService notificationService) {
    // this.notificationService = notificationService;
    // }
}
