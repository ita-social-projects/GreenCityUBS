package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.TestersSignInRequest;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.AuthorizedUserDto;
import greencity.dto.telegram.TelegramImageDto;
import greencity.dto.telegram.TelegramTextMessageDto;
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
import greencity.service.ubs.TelegramAuthorizationService;
import greencity.service.ubs.TelegramPhotoService;
import greencity.service.ubs.TelegramService;
import greencity.service.ubs.TelegramStreamingService;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.List;

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
    private final TelegramStreamingService telegramStrimingService;
    private final ChatFeedbackRepository chatFeedbackRepository;
    private final NotificationTimestampRepository notificationTimestampRepository;
    private Integer messageIdForDeleting;
    private static final String SCORE = "Score";
    @Value("${greencity.sing-in.secret-token}")
    private String secretToken;

    @Override
    public SendMessage processLoginCommand(Message message) {
        String[] parts = message.getText().split(":");
        String login = parts[1];
        String password = parts[2];
        managerMode(message.getChatId().toString());
        var response = userRemoteClient.signIn(new TestersSignInRequest(login, password, secretToken));
        return MessageFactory.createSuccessLoginMessage(
            message.getChatId().toString(),
            response.getBody().name());
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
    public SendMessage processStartCommand(Message message) {
        final String uuId = message.getText().replace(TelegramBotConstants.START_COMMAND, "").trim();
        final String tgUserId = String.valueOf(message.getFrom().getId());
        if (uuId.isEmpty()) {
            telegramAuthorizationService.handleUnknownTelegramUser(message);
            return MessageFactory.createWelcomeMessage(tgUserId);
        }
        if (telegramAuthorizationService.handleAuthorizedUser(uuId, tgUserId) == TelegramUser.MANAGER) {
            return MessageFactory.createSuccessLoginMessage(tgUserId, "Manager");
        } else {
            return MessageFactory.createWelcomeMessage(tgUserId);
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
            false,
            message,
            false);
        telegramManagerNotification.shouldNotifyManager(chatId);
        telegramMessageRepository.save(telegramMessage);
        telegramStrimingService.streamMessages(chatId, textMessageMapper.map(telegramMessage));
    }

    @Override
    public void saveManagerMessage(String chatId, String message, boolean isManager) {
        var telegramMessage = new TextMessage(
            chatId,
            false,
            message,
            isManager);
        telegramMessageRepository.save(telegramMessage);
        // TODO: discover if this is needed
        // streamMessages(chatId, modelMapper.map(telegramMessage,
        // TelegramTextMessageDto.class));
    }

    @Override
    public boolean isUserInSupportMode(String chatId) {
        return authorizedUserRepository.findByChatId(chatId).isPresent()
            || unknownTelegramUserRepository.findByChatId(chatId).isPresent();
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

    // TODO: think about adding employee, how to implement this logic
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
                message.isRead(),
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
    public String generateManagerStartLink(String userUUID) {
        var bot = applicationContext.getBean(UBSTelegramBot.class);
        var botName = bot.getBotUsername();
        return TelegramLinkGenerator.generateManagerLink(botName, userUUID);
    }

    @Override
    public SendMessage handleUserChatScope(String data, String chatId, Integer messageId) {
        int score = Integer.parseInt(data.replace(SCORE, ""));
        chatFeedbackRepository.save(new ChatFeedback(chatId, score));
        return MessageFactory.createMessageAfterUserFeedback(chatId);
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

        if (text.startsWith(TelegramBotConstants.START_COMMAND)) {
            executor.executeCommand(ubsTelegramBot, processStartCommand(message));
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
                    executor.executeCommand(ubsTelegramBot, MessageFactory.createKeyboardMessage(chatId));
                } else {
                    executor.executeCommand(ubsTelegramBot, MessageFactory.createUnknownCommandMessage(chatId));
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
        if (callBackQuery.getData().equals(TelegramBotConstants.CLIENT_SUPPORT_CALLBACK)) {
            executor.executeCommand(ubsTelegramBot, MessageFactory.createClientSupportMessage(
                callBackQuery.getFrom().getId().toString()));
        }
        if (callBackQuery.getData().equals(TelegramBotConstants.LOGIN_CALLBACK)) {
            executor.executeCommand(ubsTelegramBot, MessageFactory.createLoginMessage(
                callBackQuery.getFrom().getId().toString()));
        }
        if (callBackQuery.getData().startsWith(String.format(TelegramBotConstants.SCORE, ""))) {
            executor.executeCommand(ubsTelegramBot, handleUserChatScope(callBackQuery.getData(),
                callBackQuery.getFrom().getId().toString(), messageIdForDeleting));
        }
    }
}
