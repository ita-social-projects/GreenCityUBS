package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.TestersSignInRequest;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.AuthorizedUserDto;
import greencity.dto.telegram.FeedbackDto;
import greencity.dto.telegram.TelegramImageDto;
import greencity.dto.telegram.TelegramTextMessageDto;
import greencity.dto.telegram.UnknownTelegramUserDto;
import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.telegram.ChatFeedback;
import greencity.entity.telegram.Image;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.telegram.TextMessage;
import greencity.entity.telegram.UnknownTelegramUser;
import greencity.enums.TelegramUser;
import greencity.exceptions.BadRequestException;
import greencity.mapping.telegrammessage.TextMessageMapper;
import greencity.repository.AuthorizedUserRepository;
import greencity.repository.ChatFeedbackRepository;
import greencity.repository.NotificationTimestampRepository;
import greencity.repository.TelegramImageRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.repository.UnknownTelegramUserRepository;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.TelegramAuthorizationService;
import greencity.service.ubs.TelegramPhotoService;
import greencity.service.ubs.TelegramService;
import greencity.service.ubs.TelegramStreamingService;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static greencity.constant.ValidationConstant.EMAIL_REGEXP;

@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {
    private final UserRemoteClient userRemoteClient;
    private final TelegramMessageRepository telegramMessageRepository;
    private final TelegramManagerRepository telegramManagerRepository;
    private final TelegramExecutor telegramExecutor;
    private final TelegramManagerNotificationServiceImpl telegramManagerNotification;
    private final ApplicationContext applicationContext;
    private final TelegramAuthorizationService telegramAuthorizationService;
    private final ModelMapper modelMapper;
    private final TelegramImageRepository telegramImageRepository;
    private final AuthorizedUserRepository authorizedUserRepository;
    private final UnknownTelegramUserRepository unknownTelegramUserRepository;
    private final TextMessageMapper textMessageMapper;
    private final TelegramPhotoService telegramPhotoService;
    private final TelegramExecutor executor;
    private final Map<String, String> userState = new HashMap<>();
    private final TelegramStreamingService telegramStreamingService;
    private final ChatFeedbackRepository chatFeedbackRepository;
    private final NotificationTimestampRepository notificationTimestampRepository;
    private NotificationService notificationService;
    private Integer messageIdForDeleting;
    @Value("${greencity.sing-in.secret-token}")
    private String secretToken;
    private static final String USERNAME = "username";
    private static final String ENTERING_FEEDBACK_COMMENT = "entering_feedback_comment";
    private static final String ENTERING_EMAIL = "entering_email";

    @Override
    public SendMessage processLoginCommand(Message message) {
        String[] parts = message.getText().split(":");
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

    @Override
    public void processStartCommand(Message message) {
        UBSTelegramBot ubsTelegramBot = applicationContext.getBean(UBSTelegramBot.class);
        final String uuId = message.getText().replace(TelegramBotConstants.START_COMMAND, "").trim();
        final String tgUserId = String.valueOf(message.getFrom().getId());
        if (uuId.isEmpty()) {
            telegramAuthorizationService.handleUnknownTelegramUser(message);
            executor.executeCommand(ubsTelegramBot, MessageFactory.createWelcomeMessage(tgUserId));
            executor.executeCommand(ubsTelegramBot,
                MessageFactory.createAvailableCommandOption(message.getChatId().toString()));
        } else if (telegramAuthorizationService.handleAuthorizedUser(uuId, tgUserId) == TelegramUser.MANAGER) {
            executor.executeCommand(ubsTelegramBot,
                MessageFactory.createSuccessLoginMessage(tgUserId, String.valueOf(TelegramUser.MANAGER)));
        } else {
            executor.executeCommand(ubsTelegramBot, MessageFactory.createWelcomeMessage(tgUserId));
            executor.executeCommand(ubsTelegramBot,
                MessageFactory.createAvailableCommandOption(message.getChatId().toString()));
        }
    }

    @Override
    public SendMessage processFailLogin(String errorMessage) {
        return null;
    }

    @Override
    public void saveManagerMessage(String chatId, String message) {
        var telegramMessage = new TextMessage(
            chatId,
            message,
            false);
        telegramManagerNotification.shouldNotifyManager(chatId);
        telegramMessageRepository.save(telegramMessage);
        telegramStreamingService.streamMessages(chatId, textMessageMapper.map(telegramMessage));
    }

    @Override
    public void saveManagerMessage(String chatId, String message, boolean isManager) {
        var telegramMessage = new TextMessage(
            chatId,
            message,
            isManager);
        telegramMessageRepository.save(telegramMessage);
    }

    @Override
    public boolean isUserInSupportMode(String chatId) {
        return authorizedUserRepository.findByChatId(chatId)
                .map(AuthorizedUser::getIsSupportStatusActive)
                .orElseGet(() -> unknownTelegramUserRepository.findByChatId(chatId)
                        .map(UnknownTelegramUser::getIsSupportStatusActive)
                        .orElse(false));
    }



    @Override
    public void startSupportMode(String chatId) {
        var authorizedUser = authorizedUserRepository.findByChatId(chatId);
        if (authorizedUser.isPresent()) {
            authorizedUser.get().setIsSupportStatusActive(true);
            authorizedUserRepository.save(authorizedUser.get());
        } else {
            var unknownTelegramUser = unknownTelegramUserRepository.findByChatId(chatId);
            if (unknownTelegramUser.isPresent()) {
                UnknownTelegramUser user = unknownTelegramUser.get();
                user.setIsSupportStatusActive(true);
                unknownTelegramUserRepository.save(user);
            }
        }
    }

    @Override
    public SendMessage stopSupportMode(Message message) {
        var chatId = message.getChatId().toString();
        var supportModeUser = authorizedUserRepository.findByChatId(message.getChatId().toString());
        if (supportModeUser.isPresent()) {
            supportModeUser.get().setIsSupportStatusActive(false);
            authorizedUserRepository.save(supportModeUser.get());
        } else {
            var unknownTelegramUser = unknownTelegramUserRepository.findByChatId(chatId);
            if (unknownTelegramUser.isPresent()) {
                UnknownTelegramUser user = unknownTelegramUser.get();
                user.setIsSupportStatusActive(false);
                unknownTelegramUserRepository.save(user);
            }
        }
        telegramManagerNotification.notifyManagerAboutEndSupportModeFromUser(chatId);
        notificationTimestampRepository.deleteById(chatId);
        messageIdForDeleting = message.getMessageId();
        return MessageFactory.createEndSupportMessage(chatId);
    }

    @Override
    public boolean isManager(String chatId) {
        return telegramMessageRepository.existsByChatId(chatId);
    }
    @Override
    public void managerMode(String chatId) {
        telegramManagerRepository.save(new TelegramManager(
            chatId,
            null));
    }

    @Override
    public void sendMessageToUser(String chatId, String message) {
        var bot = applicationContext.getBean(UBSTelegramBot.class);
        var sendMessage = MessageFactory.buildMessage(chatId, message);
        saveManagerMessage(chatId, message, true);
        telegramExecutor.executeCommand(bot, sendMessage);
    }

    @Override
    public PageableDto<TelegramTextMessageDto> findUserMessageByChatId(String chatId, Pageable pageable) {
        Page<TextMessage> textMessages = telegramMessageRepository.findByChatId(chatId, pageable);
        if (textMessages.isEmpty()) {
            throw new BadRequestException(String.format(TelegramBotConstants.MESSAGES_NOT_FOUND_FOR_CHAT, chatId));
        }
        List<TelegramTextMessageDto> messageDtoList = textMessages.stream()
            .map(message -> new TelegramTextMessageDto(
                message.getMessageId(),
                message.getChatId(),
                message.getSendAt(),
                message.getText(),
                message.isManagerMessage()))
            .toList();

        return new PageableDto<>(
            messageDtoList,
            textMessages.getTotalElements(),
            textMessages.getPageable().getPageNumber(),
            textMessages.getTotalPages());
    }

    @Override
    public PageableDto<AuthorizedUserDto> getAllUsers(Pageable pageable) {
        Page<AuthorizedUser> authorizedUsers = authorizedUserRepository.findAllUsers(pageable);
        List<AuthorizedUserDto> authorizedUserDtos = authorizedUsers
            .getContent()
            .stream()
            .map(user -> new AuthorizedUserDto(
                user.getId(),
                user.getChatId(),
                user.getIsSupportStatusActive(),
                user.getIsNotify(),
                user.getUser().getId()))
            .toList();

        return new PageableDto<>(
            authorizedUserDtos,
            authorizedUsers.getTotalElements(),
            authorizedUsers.getNumber(),
            authorizedUsers.getTotalPages());
    }

    @Override
    public PageableDto<UnknownTelegramUserDto> getAllUnauthorizedUsers(Pageable pageable) {
        Page<UnknownTelegramUser> unknownTelegramUsers = unknownTelegramUserRepository.findAllUsers(pageable);
        List<UnknownTelegramUserDto> unknownTelegramUserDtos = unknownTelegramUsers
            .getContent()
            .stream()
            .map(user -> new UnknownTelegramUserDto(
                user.getId(),
                user.getChatId(),
                user.getIsSupportStatusActive(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserName(),
                user.getMobileNumber()))
            .toList();

        return new PageableDto<>(
            unknownTelegramUserDtos,
            unknownTelegramUsers.getTotalElements(),
            unknownTelegramUsers.getNumber(),
            unknownTelegramUsers.getTotalPages());
    }

    @Override
    public PageableDto<FeedbackDto> getAllFeedbacks(Pageable pageable){
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
    public PageableDto<FeedbackDto> getAlFeedbacksByChatId(String chatId, Pageable pageable){
        Page<ChatFeedback> chatFeedbacks = chatFeedbackRepository.findByChatIdPageable(chatId,pageable);
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
        if (userState.get(chatId).equals("entering_feedback")) {
            chatFeedbackRepository.save(new ChatFeedback(chatId, score,null));
            userState.put(chatId, ENTERING_FEEDBACK_COMMENT);
            if (score >= 4){
                return MessageFactory.createEnteringFeedbackMessage(chatId,
                        TelegramBotConstants.GREAT_FEEDBACK_CALLBACK);
            } else {
                return MessageFactory.createEnteringFeedbackMessage(chatId,
                        TelegramBotConstants.BAD_FEEDBACK_CALLBACK);
            }
        } else {
            chatFeedbackRepository.save(new ChatFeedback(chatId, score,""));
            return MessageFactory.createMessageAfterUserFeedback(chatId);
        }
    }

    @Override
    public PageableDto<TelegramImageDto> findUserPhotosByChatId(String chatId, Pageable page) {
        Page<Image> telegramImages = telegramImageRepository.findByChatId(chatId, page);
        if (telegramImages.isEmpty()) {
            throw new BadRequestException(String.format(TelegramBotConstants.MESSAGES_NOT_FOUND_FOR_CHAT, chatId));
        }
        List<TelegramImageDto> messages = telegramImages.stream()
            .map(message -> modelMapper.map(message, TelegramImageDto.class))
            .toList();
        return new PageableDto<>(
            messages,
            telegramImages.getTotalElements(),
            telegramImages.getPageable().getPageNumber(),
            telegramImages.getTotalPages());
    }

    @Override
    public void processTextCommand(Update update) {
        var message = update.getMessage();
        var text = message.getText();
        var chatId = message.getChatId().toString();
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
                        saveManagerMessage(chatId, text);
                    } else {
                        executor.executeCommand(ubsTelegramBot, MessageFactory.createUnknownCommandMessage(chatId));
                    }
                }
            }
        }
    }

    @Override
    public void processImageCommand(Update update) {
        var message = update.getMessage();
        var photos = telegramPhotoService.downloadPhotoFromTelegram(message);
        telegramPhotoService.saveToDB(photos, message.getChatId().toString(), message.getCaption());
    }

    @Override
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

            case TelegramBotConstants.SORTING_CALLBACK ->
                    executor.executeCommand(ubsTelegramBot, MessageFactory.createSortingMessage(userId));

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
                telegramExecutor.executeCommand(ubsTelegramBot,
                        MessageFactory.createEnteringFeedbackMessage(userId, TelegramBotConstants.GREAT_FEEDBACK_CALLBACK));
            }

            case TelegramBotConstants.BAD_FEEDBACK_CALLBACK -> {
                ChatFeedback chatFeedback = ChatFeedback.builder().chatId(chatId).rating(1).build();
                chatFeedbackRepository.save(chatFeedback);
                userState.put(chatId, ENTERING_FEEDBACK_COMMENT);
                telegramExecutor.executeCommand(ubsTelegramBot,
                        MessageFactory.createEnteringFeedbackMessage(userId, TelegramBotConstants.BAD_FEEDBACK_CALLBACK));
            }

            default -> {
                if (callBackQuery.getData().startsWith(String.format(TelegramBotConstants.SCORE, ""))) {
                    executor.executeCommand(ubsTelegramBot, handleUserChatScope(callBackQuery.getData(), userId, messageIdForDeleting));

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
                telegramExecutor.executeCommand(ubsBot,
                    MessageFactory.createGreenOfficeThanksMessage(message.getChatId().toString()));
            } else {
                telegramExecutor.executeCommand(ubsBot,
                    MessageFactory.createInvalidEmailMessage(message.getChatId().toString()));
            }
        } else if (ENTERING_FEEDBACK_COMMENT.equals(currentState)) {
            var chatFeedback = chatFeedbackRepository.findByChatId(message.getChatId().toString());
            chatFeedback.ifPresent(feedback ->
                chatFeedbackRepository.save(feedback.setComment(message.getText())));
            userState.remove(message.getChatId().toString());
            telegramExecutor.executeCommand(ubsBot, MessageFactory.createFeedbackThanksMessage(message.getChatId().toString()));
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
        Optional<AuthorizedUser> userOpt = authorizedUserRepository.findByChatId(chatId);

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

}
