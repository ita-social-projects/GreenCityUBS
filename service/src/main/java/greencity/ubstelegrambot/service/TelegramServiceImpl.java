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
import greencity.exceptions.NotFoundException;
import greencity.repository.*;
import greencity.service.ubs.*;
import greencity.specification.ChatSpecifications;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static greencity.constant.ErrorMessage.POSITION_NOT_FOUND;
import static greencity.constant.ValidationConstant.EMAIL_REGEXP;

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
    private static final String ENTERING_FEEDBACK_COMMENT = "entering_feedback_comment";
    private static final String ENTERING_EMAIL = "entering_email";
    private static final String INCORRECT_LOGIN_FORMAT = "Incorrect login format. Please use format: login:password";

    @Override
    public SendMessage processLoginCommand(Message message) {
        String[] parts = message.getText().split(":");
        if (parts.length < 3) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(), INCORRECT_LOGIN_FORMAT);
        }
        String login = parts[1];
        String password = parts[2];
        managerMode(message.getChatId().toString());
        var response = userRemoteClient.signIn(new TestersSignInRequest(login, password, secretToken));
        var responseBody = response.getBody();
        String username = (responseBody != null && responseBody.name() != null) ? responseBody.name() : USERNAME;

        return MessageFactory.createSuccessLoginMessage(
            message.getChatId().toString(),
            username);
    }

    @Override
    public SendMessage processSupportCommand(Message message) {
        var chatId = message.getChatId().toString();
        startSupportMode(chatId);
        if (isUserInSupportMode(chatId)) {
            saveManagerMessage(chatId, message.getText());
        } else {
            telegramManagerNotification.shouldNotifyManager(chatId);
        }
        return MessageFactory.createSuccessClientSupportMessageSend(chatId);
    }

    private boolean checkIsEmployeeManager(Employee employee) {
        var employeePositions = employee.getEmployeePosition();

        Position serviceManager = positionRepository.findById(1L)
                .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));

        Position manager = positionRepository.findById(2L)
                .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND));

        return employeePositions.contains(manager) || employeePositions.contains(serviceManager);
    }

    @Override
    public void processStartCommand(Message message) {
        UBSTelegramBot ubsTelegramBot = applicationContext.getBean(UBSTelegramBot.class);

        final String uuId = message.getText().replace(TelegramBotConstants.START_COMMAND, "").trim();
        final String chatId = message.getFrom().getId().toString();

        Optional<TelegramChat> telegramChat = telegramChatRepository.findByChatId(chatId);

        if (!uuId.isEmpty()) {
            Optional<Employee> employee = employeeRepository.findByUuid(chatId);

            if (employee.isPresent()) {
                boolean isManager = checkIsEmployeeManager(employee.get());
                if (isManager) {
                    telegramChat.ifPresent(telegramChatRepository::delete);
                    //show login as a manager form
                }
            }
        }

        if (telegramChat.isPresent() && telegramChat.get().getUser() != null) {
            Optional<Employee> employee = employeeRepository.findByUuid(telegramChat.get().getUser().getUuid());

            if (employee.isPresent()) {
                boolean isManager = checkIsEmployeeManager(employee.get());
                if (isManager) {
                    telegramChat.ifPresent(telegramChatRepository::delete);
                    //show login as a manager form
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
                    .isNotify(true) //need to specify a correct value
                    .isSupportStatusActive(false);

            if (!uuId.isEmpty()) {
                Optional<User> user = userRepository.findUserByUuid(uuId);
                user.ifPresent(newChatBuilder::user);
            }

            telegramChatRepository.save(newChatBuilder.build());
        }

        executor.executeCommand(ubsTelegramBot, MessageFactory.createWelcomeMessage(chatId));
        executor.executeCommand(ubsTelegramBot,
                MessageFactory.createAvailableCommandOption(message.getChatId().toString()));
    }

    @Override
    public SendMessage processFailLogin(String errorMessage) {
        return null;
    }

    @Override
    public void saveManagerMessage(String chatId, String message) {
//        var telegramMessage = new TextMessage(
//            chatId,
//            message,
//            false);
//        telegramManagerNotification.shouldNotifyManager(chatId);
//        telegramMessageRepository.save(telegramMessage);
//        telegramStreamingService.streamMessages(chatId, textMessageMapper.map(telegramMessage));
    }

    @Override
    public void saveManagerMessage(String chatId, String message, boolean isManager) {
//        var telegramMessage = new TextMessage(
//            chatId,
//            message,
//            isManager);
//        telegramMessageRepository.save(telegramMessage);
    }

    @Override
    public boolean isUserInSupportMode(String chatId) {
        return telegramChatRepository.findByChatId(chatId)
            .map(TelegramChat::getIsSupportStatusActive).orElse(false);
    }

    @Override
    public void startSupportMode(String chatId) {
        var chat = telegramChatRepository.findByChatId(chatId);
        if (chat.isPresent()) {
            chat.get().setIsSupportStatusActive(true);
            telegramChatRepository.save(chat.get());
        }
    }

    @Override
    public SendMessage stopSupportMode(Message message) {
        var chatId = message.getChatId().toString();
        var chat = telegramChatRepository.findByChatId(message.getChatId().toString());
        if (chat.isPresent()) {
            chat.get().setIsSupportStatusActive(false);
            telegramChatRepository.save(chat.get());
        }
        telegramManagerNotification.notifyManagerAboutEndSupportModeFromUser(chatId);
        notificationTimestampRepository.deleteById(chatId);
        messageIdForDeleting = message.getMessageId();
        return MessageFactory.createEndSupportMessage(chatId);
    }

    @Override
    public void managerMode(String chatId) {
        telegramManagerRepository.save(new TelegramManager(
            chatId,
            null));
    }

    /**
     * Detects the {@link AssetType} of the given file based on its MIME type.
     *
     * <p>This method analyzes the MIME type (Content-Type) of the provided {@link MultipartFile}
     * and returns the corresponding {@link AssetType}:</p>
     * <ul>
     *     <li>{@code image/*} → {@link AssetType#IMAGE}</li>
     *     <li>{@code video/*} → {@link AssetType#VIDEO}</li>
     *     <li>{@code audio/*} → {@link AssetType#AUDIO}</li>
     *     <li>{@code application/pdf} → {@link AssetType#FILE}</li>
     *     <li>Any other or unknown types → {@link AssetType#FILE}</li>
     * </ul>
     *
     * @param file the uploaded file for which the asset type should be determined
     * @return the detected {@link AssetType}; defaults to {@link AssetType#FILE} if unknown
     */
    private AssetType detectAssetType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return AssetType.FILE;

        if (contentType.startsWith("image/")) return AssetType.IMAGE;
        if (contentType.startsWith("video/")) return AssetType.VIDEO;
        if (contentType.startsWith("audio/")) return AssetType.AUDIO;

        return AssetType.FILE;
    }

    @Override
    public void sendMessageToUser(CreateTelegramMessageRequest request, MultipartFile[] files) {
        var bot = applicationContext.getBean(UBSTelegramBot.class);

        TelegramChat chat = telegramChatRepository.findByChatId(request.getChatId().toString())
                .orElseThrow(() -> new NotFoundException("Chat not found"));

        TelegramMessage message = TelegramMessage.builder()
                .chat(chat)
                .text(request.getText())
                .fromManager(true)
                .sendAt(LocalDateTime.now())
                .build();

        List<MessageAsset> assets = new ArrayList<>();

        if (request.getText() != null) {
            var sendTextMessage = MessageFactory.buildMessage(request.getChatId().toString(), message.getText());
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
                    var sendPhotoMessage = MessageFactory.createPhotoSender(request.getChatId().toString(), url, "");
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
                                asset.getContentType()
                        )).toList();

               return new TelegramMessageDto(
                       message.getId(),
                       message.getSendAt(),
                       message.getText(),
                       message.getFromManager(),
                       assetDtos
               );
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
                           .firstName(chat.getUser().getRecipientSurname())
                           .email(chat.getUser().getRecipientEmail())
                           .build();

                   chatDtoBuilder
                           .user(chatUserDto);
               }

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
                feedback.getChatId(),
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
                feedback.getChatId(),
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
    public String generateManagerStartLink(String userUUID) {
        var bot = applicationContext.getBean(UBSTelegramBot.class);
        var botName = bot.getBotUsername();
        return TelegramLinkGenerator.generateManagerLink(botName, userUUID);
    }

    @Override
    public SendMessage handleUserChatScope(String data, String chatId, Integer messageId) {
        int score = Integer.parseInt(data.replace(String.format(TelegramBotConstants.SCORE, ""), ""));
        if ("entering_feedback".equals(userState.get(chatId))) {
            chatFeedbackRepository.save(new ChatFeedback(chatId, score, null));
            userState.put(chatId, ENTERING_FEEDBACK_COMMENT);
            if (score >= 4) {
                return MessageFactory.createEnteringFeedbackMessage(chatId,
                    TelegramBotConstants.GREAT_FEEDBACK_CALLBACK);
            } else {
                return MessageFactory.createEnteringFeedbackMessage(chatId,
                    TelegramBotConstants.BAD_FEEDBACK_CALLBACK);
            }
        } else {
            chatFeedbackRepository.save(new ChatFeedback(chatId, score, ""));
            return MessageFactory.createMessageAfterUserFeedback(chatId);
        }
    }

    @Override
    public void processUpdate(Update update) {

        var message = update.getMessage();
        var text = message.getText();
        var chatId = message.getChatId().toString();

        if (update.hasCallbackQuery()) {
            processCallBackQuery(update);
        } else {
            UBSTelegramBot ubsTelegramBot = applicationContext.getBean(UBSTelegramBot.class);

            if (userState.containsKey(chatId)) {
                processUserState(message);
            } else {
                if (text.startsWith(TelegramBotConstants.START_COMMAND)) {
                    processStartCommand(message);
                    return;
                }
                String command = text.contains(":") ? text.split(":")[0].trim() : text;

                switch (command) {
                    case TelegramBotConstants.HELP_COMMAND -> executor.executeCommand(ubsTelegramBot,
                            MessageFactory.createHelpMessage(message.getChatId().toString()));

                    case TelegramBotConstants.SUPPORT_COMMAND ->
                            executor.executeCommand(ubsTelegramBot, processSupportCommand(message));

                    case TelegramBotConstants.LOGIN_COMMAND ->
                            executor.executeCommand(ubsTelegramBot, processLoginCommand(message));

                    case TelegramBotConstants.CLIENT_END_SUPPORT_MODE ->
                            executor.executeCommand(ubsTelegramBot, stopSupportMode(message));

                    default -> {
                        if (isUserInSupportMode(chatId)) {
                            //Todo implement save message from user here
                        } else {
                            executor.executeCommand(ubsTelegramBot, MessageFactory.createUnknownCommandMessage(chatId));
                        }
                    }
                }
            }
        }
    }


    public void processCallBackQuery(Update update) {
        UBSTelegramBot ubsTelegramBot = applicationContext.getBean(UBSTelegramBot.class);
        var callBackQuery = update.getCallbackQuery();
        var chatId = callBackQuery.getFrom().getId().toString();
        String userId = callBackQuery.getFrom().getId().toString();

        switch (callBackQuery.getData()) {
            case TelegramBotConstants.CLIENT_SUPPORT_CALLBACK ->
                executor.executeCommand(ubsTelegramBot, processSupportCallbackData(userId));

            case TelegramBotConstants.LOGIN_CALLBACK ->
                executor.executeCommand(ubsTelegramBot, MessageFactory.createLoginMessage(userId));

            case TelegramBotConstants.MAIN_MENU_CALLBACK ->
                executor.executeCommand(ubsTelegramBot, MessageFactory.createAvailableCommandOption(userId));

            case TelegramBotConstants.WORK_SCHEDULE_CALLBACK ->
                executor.executeCommand(ubsTelegramBot, MessageFactory.createWorkScheduleMessage(userId));

            case TelegramBotConstants.SORTING_PROCESS_CALLBACK ->
                executor.executeCommand(ubsTelegramBot, MessageFactory.createSortingPricesMessage(userId));

            case TelegramBotConstants.ADMISSION_RULES_CALLBACK ->
                executor.executeCommand(ubsTelegramBot, MessageFactory.createAdmissionRulesMessage(userId));

            case TelegramBotConstants.GREEN_OFFICE_CALLBACK ->
                executor.executeCommand(ubsTelegramBot, MessageFactory.createGreenOfficeMessage(userId));

            case TelegramBotConstants.GREEN_OFFICE_PROCESS_CALLBACK -> {
                userState.put(chatId, ENTERING_EMAIL);
                executor.executeCommand(ubsTelegramBot, MessageFactory.createEnteringEmailMessage(userId));
            }

            case TelegramBotConstants.FEEDBACK_CALLBACK -> {
                userState.put(chatId, "entering_feedback");
                executor.executeCommand(ubsTelegramBot, MessageFactory.createFeedbackMessage(userId));
            }

            case TelegramBotConstants.GREAT_FEEDBACK_CALLBACK -> {
                ChatFeedback chatFeedback = ChatFeedback.builder().chatId(chatId).rating(5).build();
                chatFeedbackRepository.save(chatFeedback);
                userState.put(chatId, ENTERING_FEEDBACK_COMMENT);
                executor.executeCommand(ubsTelegramBot,
                    MessageFactory.createEnteringFeedbackMessage(userId, TelegramBotConstants.GREAT_FEEDBACK_CALLBACK));
            }

            case TelegramBotConstants.BAD_FEEDBACK_CALLBACK -> {
                ChatFeedback chatFeedback = ChatFeedback.builder().chatId(chatId).rating(1).build();
                chatFeedbackRepository.save(chatFeedback);
                userState.put(chatId, ENTERING_FEEDBACK_COMMENT);
                executor.executeCommand(ubsTelegramBot,
                    MessageFactory.createEnteringFeedbackMessage(userId, TelegramBotConstants.BAD_FEEDBACK_CALLBACK));
            }

            default -> {
                if (callBackQuery.getData().startsWith(String.format(TelegramBotConstants.SCORE, ""))) {
                    executor.executeCommand(ubsTelegramBot,
                        handleUserChatScope(callBackQuery.getData(), userId, messageIdForDeleting));

                    Message message = (Message) update.getCallbackQuery().getMessage();
                    InlineKeyboardMarkup inlineKeyboardMarkup = message.getReplyMarkup();

                    List<List<InlineKeyboardButton>> keyboard = inlineKeyboardMarkup.getKeyboard();
                    for (List<InlineKeyboardButton> row : keyboard) {
                        for (InlineKeyboardButton button : row) {
                            if (!TelegramBotConstants.MAIN_MENU_CALLBACK.equals(button.getCallbackData())) {
                                button.setCallbackData("disabled");
                            }
                        }
                    }

                    inlineKeyboardMarkup.setKeyboard(keyboard);

                    EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
                    editMessageReplyMarkup.setChatId(callBackQuery.getMessage().getChatId().toString());
                    editMessageReplyMarkup.setMessageId(message.getMessageId());
                    editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);

                    executor.executeCommand(ubsTelegramBot, editMessageReplyMarkup);
                }
            }
        }
    }

    private SendMessage processSupportCallbackData(String chatId) {
        startSupportMode(chatId);
        return MessageFactory.createSupportMessageCallBackQuery(chatId);
    }

    private void processUserState(Message message) {
        var ubsBot = applicationContext.getBean(UBSTelegramBot.class);
        var currentState = userState.get(message.getChatId().toString());

        if (ENTERING_EMAIL.equals(currentState)) {
            var email = message.getText();
            if (isValidEmail(email)) {
                notifyManagerOfGreenOfficeRequest(message, email);
                userState.remove(message.getChatId().toString());
                executor.executeCommand(ubsBot,
                    MessageFactory.createGreenOfficeThanksMessage(message.getChatId().toString()));
            } else {
                executor.executeCommand(ubsBot,
                    MessageFactory.createInvalidEmailMessage(message.getChatId().toString()));
            }
        } else if (ENTERING_FEEDBACK_COMMENT.equals(currentState)) {
            var chatFeedback = chatFeedbackRepository.findByChatId(message.getChatId().toString());
            chatFeedback.ifPresent(feedback -> chatFeedbackRepository.save(feedback.setComment(message.getText())));
            userState.remove(message.getChatId().toString());
            executor.executeCommand(ubsBot,
                MessageFactory.createFeedbackThanksMessage(message.getChatId().toString()));
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Pattern emailPattern = Pattern.compile(EMAIL_REGEXP);
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    private void notifyManagerOfGreenOfficeRequest(Message message, String email) {
        String chatId = String.valueOf(message.getChatId());
        Optional<TelegramChat> userOpt = telegramChatRepository.findByChatId(chatId);

        String username = userOpt
            .map(u -> u.getUser().getRecipientName() + " " + u.getUser().getRecipientSurname())
            .orElse(message.getFrom().getUserName());

        notificationService.notifyManagerWithNewGreenOfficeRequestFromTelegramBot(email, username);
    }

    @Lazy
    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdersDataForUserDto getLastOrderByChatId(String chatId) {
        TelegramChat telegramChat = telegramChatRepository.findByChatId(chatId).orElseThrow(() -> new NotFoundException("Chat with id " + chatId + " not found"));

        if (telegramChat.getUser() == null ) {
            throw new NotFoundException("Order not found");
        }

        Order order = orderRepository.findFirstByUserIdOrderByOrderDateDesc(telegramChat.getUser().getId()).orElseThrow(() -> new NotFoundException("Order not found"));
        return ubsClientService.getOrdersData(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatDto getChatById(Long chatId) {
        TelegramChat chat =telegramChatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat with id " + chatId + " not found"));
        return ChatDto.builder()
                .id(chat.getId())
                .chatId(chat.getChatId())
                .firstName(chat.getFirstName())
                .lastName(chat.getLastName())
                .username(chat.getUsername())
                .build();

    }
}
