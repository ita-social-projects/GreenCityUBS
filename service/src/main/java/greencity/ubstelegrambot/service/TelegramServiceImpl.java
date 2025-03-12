package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.TestersSignInRequest;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.AuthorizedUserDto;
import greencity.dto.telegram.TelegramImageDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.dto.telegram.TelegramTextMessageDto;
import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.telegram.Image;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.telegram.TextMessage;
import greencity.entity.telegram.UnknownTelegramUser;
import greencity.enums.TelegramUser;
import greencity.exceptions.BadRequestException;
import greencity.mapping.telegrammessage.TextMessageMapper;
import greencity.repository.AuthorizedUserRepository;
import greencity.repository.TelegramImageRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.repository.UnknownTelegramUserRepository;
import greencity.service.ubs.TelegramAuthorizationService;
import greencity.service.ubs.TelegramService;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final AuthorizedUserRepository telegramBotRepository;
    private final Map<SseEmitter, String> emitters = new ConcurrentHashMap<>();
    private final TelegramImageRepository telegramImageRepository;
    private final AuthorizedUserRepository authorizedUserRepository;
    private final UnknownTelegramUserRepository unknownTelegramUserRepository;
    private final TextMessageMapper textMessageMapper;
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
        streamMessages(chatId, textMessageMapper.map(telegramMessage));
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
    public SendMessage stopSupportMode(String chatId) {
        var supportModeUser = authorizedUserRepository.findByChatId(chatId);
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
        return MessageFactory.createStopSupportModeMessage(chatId);
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
    public SendMessage handleUserChatScope(String data, String chatId) {
        // TODO: customers business logic
        return MessageFactory.createMessageAfterUserFeedback(chatId);
    }

    @Override
    public void streamMessages(String chatId, TelegramTextMessageDto message) {
        sendMessages(chatId, message);
    }

    @Override
    public void streamMessages(String chatId, TelegramImageDto message) {
        sendMessages(chatId, message);
    }

    @Override
    public void addEmitter(SseEmitter emitter, String chatId) {
        emitters.put(emitter, chatId);
    }

    @Override
    public void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
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

    private void sendMessages(String chatId, TelegramMessageDto message) {
        for (Map.Entry<SseEmitter, String> entry : emitters.entrySet()) {
            SseEmitter emitter = entry.getKey();
            String storedChatId = entry.getValue();

            if (storedChatId.equals(chatId)) {
                try {
                    emitter.send(SseEmitter.event().data(message));
                } catch (IOException e) {
                    emitters.remove(emitter);
                }
            }
        }
    }
}
