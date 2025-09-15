package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.client.config.UserRemoteWebClient;
import greencity.constant.ErrorMessage;
import greencity.constant.TelegramBotConstants;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.ChatUserDto;
import greencity.dto.telegram.CreateTelegramMessageRequest;
import greencity.dto.telegram.EditTelegramMessageRequest;
import greencity.dto.telegram.MarkMessagesAsReadRequestDto;
import greencity.dto.telegram.MessageAssetDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.dto.telegram.ToggleNotificationsRequestDto;
import greencity.entity.order.Order;
import greencity.entity.telegram.MessageAsset;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.telegram.TelegramMessage;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.enums.AssetType;
import greencity.enums.ChatState;
import greencity.enums.MessageDeliveryStatus;
import greencity.enums.MessageViewingStatus;
import greencity.enums.OrderStatus;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.producers.TelegramChatProducer;
import greencity.repository.EmployeeRepository;
import greencity.repository.MessageAssetRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.TelegramService;
import greencity.service.ubs.TelegramUpdateProcessor;
import greencity.service.ubs.UBSClientService;
import greencity.specification.ChatSpecifications;
import greencity.ubstelegrambot.messages.MessageFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {
    private static final String USER_PROCESSOR_NAME = "userUpdateProcessor";
    private static final String MANAGER_PROCESSOR_NAME = "managerUpdateProcessor";

    private final TelegramMessageRepository telegramMessageRepository;
    private final TelegramManagerRepository telegramManagerRepository;
    private final TelegramChatRepository telegramChatRepository;
    private final UserRemoteWebClient userRemoteWebClient;
    private final UserRemoteClient userRemoteClient;
    private final UBSClientService ubsClientService;
    private final TelegramExecutor executor;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TelegramChatProducer telegramChatProducer;
    private final TelegramUtils telegramUtils;
    private final MessageAssetRepository messageAssetRepository;
    private final Map<String, TelegramUpdateProcessor> telegramUpdateProcessorMap;

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void sendMessageToUser(CreateTelegramMessageRequest request, MultipartFile[] files) {
        TelegramChat chat = findChatOrThrow(request.getChatId());

        List<MultipartFile> images = new ArrayList<>();
        List<MultipartFile> others = new ArrayList<>();
        splitFiles(files, images, others);

        if (shouldSendTextOnly(request, files, images, others)) {
            handleTextMessageOnly(chat, request);
            return;
        }

        if (!images.isEmpty()) {
            handleImageMessages(chat, request, images);
        }

        if (!others.isEmpty()) {
            handleOtherFiles(chat, request, images, others);
        }

        telegramChatRepository.save(chat);
    }

    private TelegramChat findChatOrThrow(Long chatId) {
        return telegramChatRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Chat not found"));
    }

    private void splitFiles(MultipartFile[] files, List<MultipartFile> images, List<MultipartFile> others) {
        if (files == null) {
            return;
        }

        for (MultipartFile file : files) {
            AssetType type = TelegramUtils.detectAssetType(file);
            if (type == AssetType.IMAGE && canSendAsPhoto(file)) {
                images.add(file);
            } else {
                others.add(file);
            }
        }
    }

    private boolean shouldSendTextOnly(CreateTelegramMessageRequest request, MultipartFile[] files,
        List<MultipartFile> images, List<MultipartFile> others) {
        return (files == null || (images.isEmpty() && others.isEmpty())) && request.getText() != null;
    }

    private void handleTextMessageOnly(TelegramChat chat, CreateTelegramMessageRequest request) {
        SendMessage sendMessage = MessageFactory.buildMessage(chat.getChatId(), request.getText());
        Message sentMessage = executor.executeSendMessage(sendMessage);

        TelegramMessage textMessage = TelegramMessage.builder()
            .chat(chat)
            .text(request.getText())
            .fromManager(true)
            .status(MessageDeliveryStatus.SENT)
            .sendAt(Instant.now())
            .messageViewingStatus(MessageViewingStatus.READ)
            .telegramMessageId(sentMessage != null ? sentMessage.getMessageId() : null)
            .build();

        telegramMessageRepository.save(textMessage);
        chat.setLastMessage(textMessage);
        telegramChatRepository.save(chat);
    }

    private void handleImageMessages(TelegramChat chat, CreateTelegramMessageRequest request,
        List<MultipartFile> images) {
        TelegramMessage imageMessage = TelegramMessage.builder()
            .chat(chat)
            .text(request.getText())
            .fromManager(true)
            .status(MessageDeliveryStatus.SENT)
            .sendAt(Instant.now())
            .messageViewingStatus(MessageViewingStatus.READ)
            .build();

        List<MessageAsset> imageAssets = createImageAssets(images, imageMessage);

        if (imageAssets.size() > 1) {
            sendAsMediaGroup(chat, request, images, imageMessage, imageAssets);
        } else {
            sendAsSinglePhoto(chat, request, images, imageMessage, imageAssets);
        }

        telegramMessageRepository.save(imageMessage);
        messageAssetRepository.saveAll(imageAssets);
        chat.setLastMessage(imageMessage);
    }

    private List<MessageAsset> createImageAssets(List<MultipartFile> images, TelegramMessage imageMessage) {
        List<MessageAsset> assets = new ArrayList<>();
        for (MultipartFile img : images) {
            validateFileSize(img);
            String url = userRemoteWebClient.uploadFile(img);
            assets.add(MessageAsset.builder()
                .url(url)
                .fileName(img.getOriginalFilename())
                .size(img.getSize())
                .contentType(img.getContentType())
                .type(AssetType.IMAGE)
                .message(imageMessage)
                .build());
        }
        imageMessage.setAssets(assets);
        return assets;
    }

    private void sendAsMediaGroup(TelegramChat chat, CreateTelegramMessageRequest request,
        List<MultipartFile> images, TelegramMessage imageMessage,
        List<MessageAsset> imageAssets) {
        SendMediaGroup sendMediaGroup = MessageFactory.buildSendMediaGroup(chat.getChatId(), images, request.getText());
        List<Message> sentMessages = executor.executeSendMediaGroup(sendMediaGroup);

        if (!sentMessages.isEmpty()) {
            imageMessage.setTelegramMessageId(sentMessages.getFirst().getMessageId());
            imageMessage.setMediaGroupId(sentMessages.getFirst().getMediaGroupId());
            for (int i = 0; i < imageAssets.size() && i < sentMessages.size(); i++) {
                imageAssets.get(i).setTelegramMessageId(sentMessages.get(i).getMessageId());
            }
        }
    }

    private void sendAsSinglePhoto(TelegramChat chat, CreateTelegramMessageRequest request,
        List<MultipartFile> images, TelegramMessage imageMessage,
        List<MessageAsset> imageAssets) {
        try {
            SendPhoto sendPhoto =
                MessageFactory.createSendPhoto(chat.getChatId(), images.getFirst(), request.getText());
            Message sentMessage = executor.executeSendPhoto(sendPhoto);
            if (sentMessage != null) {
                imageMessage.setTelegramMessageId(sentMessage.getMessageId());
                imageMessage.setMediaGroupId(null);
                imageAssets.getFirst().setTelegramMessageId(sentMessage.getMessageId());
            }
        } catch (IOException e) {
            throw new TelegramBotExecutionException("Unable to send file to Telegram", e);
        }
    }

    private void handleOtherFiles(TelegramChat chat, CreateTelegramMessageRequest request,
        List<MultipartFile> images, List<MultipartFile> others) {
        for (MultipartFile file : others) {
            validateFileSize(file);

            String caption = images.isEmpty() ? request.getText() : null;
            TelegramMessage fileMessage = TelegramMessage.builder()
                .chat(chat)
                .fromManager(true)
                .text(caption)
                .status(MessageDeliveryStatus.SENT)
                .sendAt(Instant.now())
                .messageViewingStatus(MessageViewingStatus.READ)
                .build();

            String url = uploadFile(file);
            AssetType type = TelegramUtils.detectAssetType(file);

            MessageAsset asset = MessageAsset.builder()
                .url(url)
                .fileName(file.getOriginalFilename())
                .size(file.getSize())
                .contentType(file.getContentType())
                .type(type)
                .message(fileMessage)
                .build();

            fileMessage.setAssets(List.of(asset));

            Message sentMessage = sendAsDocument(chat, caption, file);
            fileMessage.setTelegramMessageId(sentMessage.getMessageId());
            asset.setTelegramMessageId(sentMessage.getMessageId());

            telegramMessageRepository.save(fileMessage);
            messageAssetRepository.save(asset);
            chat.setLastMessage(fileMessage);
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > 50 * 1024 * 1024) {
            log.warn("File \"{}\" size exceeds 50MB", file.getName());
            throw new IllegalArgumentException("File size exceeds Telegram bot limit (50MB)");
        }
    }

    private Message sendAsDocument(TelegramChat chat, String caption, MultipartFile file) {
        log.info("Sending document: {} filename: {} to chat ID: {}",
            file.getContentType(), file.getOriginalFilename(), chat.getChatId());
        try {
            var sendFile = MessageFactory.createSendDocument(chat.getChatId(), caption, file);
            return executor.executeSendFile(sendFile);
        } catch (IOException e) {
            log.error("Failed to send file to Telegram", e);
            throw new TelegramBotExecutionException("Unable to send file to Telegram", e);
        }
    }

    private boolean canSendAsPhoto(MultipartFile file) {
        BufferedImage image = null;
        try (var stream = file.getInputStream()) {
            image = ImageIO.read(stream);
        } catch (IOException e) {
            throw new TelegramBotExecutionException("Failed to read image data", e);
        }
        if (image == null) {
            return false;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        long fileSize = file.getSize();

        boolean sizeOk = fileSize <= 10 * 1024 * 1024;
        boolean dimensionsOk = (width + height <= 10000);
        boolean aspectOk = ((double) Math.max(width, height) / Math.min(width, height) <= 20.0);

        return sizeOk && dimensionsOk && aspectOk;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public PageableDto<TelegramMessageDto> findUserMessageByChatId(Long chatId, Pageable pageable) {
        Page<TelegramMessage> messages = telegramMessageRepository.findByChatId(chatId, pageable);
        if (messages.isEmpty()) {
            throw new NotFoundException(String.format("There are no messages in chat %s", chatId));
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
                    assetDtos,
                    message.getMessageViewingStatus());
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
        Specification<TelegramChat> spec;
        if (isNumeric(searchTerm)) {
            Long userId = Long.parseLong(searchTerm);
            Optional<TelegramChat> chatOpt = telegramChatRepository.findByUserId(userId);

            if (chatOpt.isPresent()) {
                String chatId = chatOpt.get().getChatId();
                spec = ChatSpecifications.withSearchAndSort(chatId);
            } else {
                return new PageableDto<>(Collections.emptyList(), 0, pageable.getPageNumber(), 0);
            }
        } else {
            spec = ChatSpecifications.withSearchAndSort(searchTerm);
        }

        Page<TelegramChat> chats = telegramChatRepository.findAll(spec, pageable);

        List<ChatDto> chatDtos = chats
            .getContent()
            .stream()
            .map(TelegramServiceImpl::mapTelegramChatToChatDto)
            .toList();

        return new PageableDto<>(
            chatDtos,
            chats.getTotalElements(),
            chats.getNumber(),
            chats.getTotalPages());
    }

    private static ChatDto mapTelegramChatToChatDto(TelegramChat chat) {
        ChatDto.ChatDtoBuilder chatDtoBuilder = ChatDto.builder()
            .id(chat.getId())
            .unreadMessagesCount(chat.getUnreadMessagesCount())
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

        if (chat.getLastMessage() != null) {
            TelegramMessage message = chat.getLastMessage();

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
                .messageViewingStatus(message.getMessageViewingStatus())
                .build();

            chatDtoBuilder.lastMessage(lastMessage);
        }

        return chatDtoBuilder.build();
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
        OrdersDataForUserDto dto = ubsClientService.getOrdersData(order);
        Long completedCount = orderRepository.countByUserIdAndOrderStatus(
            telegramChat.getUser().getId(),
            OrderStatus.DONE);
        dto.setCompletedOrdersCount(completedCount);

        return dto;
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
            .unreadMessagesCount(chat.getUnreadMessagesCount())
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
    @Transactional
    public void markMessagesAsRead(MarkMessagesAsReadRequestDto request) {
        List<TelegramMessage> messages = telegramMessageRepository.findAllById(request.getMessagesIds());

        for (TelegramMessage message : messages) {
            if (message.getMessageViewingStatus() == MessageViewingStatus.UNREAD) {
                message.setMessageViewingStatus(MessageViewingStatus.READ);

                TelegramChat chat = message.getChat();
                int currentUnread = chat.getUnreadMessagesCount();
                if (currentUnread > 0) {
                    chat.setUnreadMessagesCount(currentUnread - 1);
                }
            }
        }

        telegramMessageRepository.saveAll(messages);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void toggleNotifications(String uuid, ToggleNotificationsRequestDto request) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_UUID));

        if (user.getTelegramBot() == null) {
            throw new NotFoundException(ErrorMessage.USER_DOESNT_HAVE_TELEGRAM_CHAT);
        }

        TelegramChat telegramChat = user.getTelegramBot();
        telegramChat.setIsNotify(request.isNotify());
        telegramChatRepository.save(telegramChat);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean getIsNotificationsEnabled(String uuid) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_UUID));

        if (user.getTelegramBot() == null) {
            throw new NotFoundException(ErrorMessage.USER_DOESNT_HAVE_TELEGRAM_CHAT);
        }

        return user.getTelegramBot().getIsNotify();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void processUpdate(Update update) {
        Message message = update.getMessage();
        TelegramUpdateProcessor updateProcessor;

        if (isStartCommand(message)) {
            String uuid = extractUuid(message);
            Long chatId = message.getFrom().getId();
            Optional<TelegramChat> chatOpt = telegramChatRepository.findByChatId(chatId.toString());

            if (chatOpt.isEmpty()) {
                updateProcessor = handleNewChat(uuid, message, chatId);
            } else {
                updateProcessor = handleExistingChat(uuid, chatOpt.get(), chatId);
            }
        } else {
            updateProcessor = handleDefaultUpdate(update);
        }

        SendMessage sendMessage = updateProcessor.process(update);
        if (sendMessage != null) {
            executor.executeCommand(sendMessage);
        }
    }

    @Override
    @Transactional
    public void editManagerMessage(EditTelegramMessageRequest request) {
        telegramChatRepository.findById(request.chatId()).ifPresentOrElse(ch -> {
            TelegramMessage message = telegramMessageRepository.findById(request.messageId())
                .orElseThrow(NotFoundException::new);
            if (Boolean.TRUE.equals(message.getFromManager())) {
                message.setUpdatedAt(Instant.now());
                message.setText(request.newText());
                if (!message.getAssets().isEmpty()) {
                    MessageAsset firstAsset = message.getAssets().getFirst();
                    EditMessageCaption editMessageCaption = MessageFactory
                        .buildEditMessageCaption(ch.getChatId(), firstAsset.getTelegramMessageId(), request.newText());
                    executor.executeCommand(editMessageCaption);
                } else {
                    EditMessageText editMessageText = MessageFactory
                        .buildEditMessageText(ch.getChatId(), message.getTelegramMessageId(), request.newText());
                    executor.executeCommand(editMessageText);
                }

                telegramMessageRepository.save(message);

                if (ch.getLastMessage() != null && ch.getLastMessage().getId().equals(message.getId())) {
                    ch.setLastMessage(message);
                    telegramChatRepository.save(ch);
                }
            }
        }, () -> {
            throw new NotFoundException();
        });
    }

    private boolean isStartCommand(Message message) {
        return message != null
            &&
            message.getText() != null
            &&
            message.getText().contains(TelegramBotConstants.START_COMMAND);
    }

    private String extractUuid(Message message) {
        return message.getText()
            .replace(TelegramBotConstants.START_COMMAND, "")
            .trim();
    }

    private TelegramUpdateProcessor handleNewChat(String uuid, Message message, Long chatId) {
        TelegramChat.TelegramChatBuilder newChatBuilder = TelegramChat.builder()
            .chatId(chatId.toString())
            .username(message.getFrom().getUserName())
            .firstName(message.getFrom().getFirstName())
            .lastName(message.getFrom().getLastName())
            .isNotify(true)
            .chatState(ChatState.NORMAL)
            .chatStateUpdatedAt(Instant.now())
            .languageCode("uk");

        if (!uuid.isEmpty()) {
            userRepository.findUserByUuid(uuid).ifPresent(user -> {
                newChatBuilder.user(user);

                String langCode = "uk";
                try {
                    langCode = userRemoteClient.findUserLanguageByUuid(user.getUuid());
                } catch (Exception e) {
                    log.warn("Failed to get user language from UBS for uuid {}. Using default 'uk'", user.getUuid(), e);
                }
                newChatBuilder.languageCode(langCode);
            });
        }

        TelegramChat createdChat = newChatBuilder.build();

        telegramChatRepository.save(createdChat);

        ChatDto chatDto = ChatDto.builder()
            .id(createdChat.getId())
            .firstName(createdChat.getFirstName())
            .lastName(createdChat.getLastName())
            .unreadMessagesCount(0)
            .username(createdChat.getUsername()).build();

        telegramChatProducer.notifyNewChat(chatDto);

        return resolveProcessorByUuid(uuid, chatId);
    }

    private TelegramUpdateProcessor handleExistingChat(String uuid, TelegramChat chat, Long chatId) {
        if (!uuid.isEmpty()) {
            userRepository.findUserByUuid(uuid).ifPresent(user -> {
                chat.setUser(user);

                String langCode = "uk";
                try {
                    langCode = userRemoteClient.findUserLanguageByUuid(user.getUuid());
                } catch (Exception e) {
                    log.warn("Failed to get user language from UBS for uuid {}. Using default 'uk'", user.getUuid(), e);
                }
                chat.setLanguageCode(langCode);
            });
            telegramChatRepository.save(chat);
        }

        return resolveProcessorByUuid(uuid, chatId);
    }

    private TelegramUpdateProcessor resolveProcessorByUuid(String uuid, Long chatId) {
        if (uuid.isEmpty()) {
            log.info("No user found with uuid: {}", uuid);
            return telegramUpdateProcessorMap.get(USER_PROCESSOR_NAME);
        }

        Optional<Employee> employeeOpt = employeeRepository.findByUuid(uuid);

        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            if (telegramUtils.checkIsEmployeeManager(employee)) {
                telegramManagerRepository.save(
                    TelegramManager.builder()
                        .chatId(chatId.toString())
                        .employee(employee)
                        .build());
                return telegramUpdateProcessorMap.get(MANAGER_PROCESSOR_NAME);
            }
        }

        return telegramUpdateProcessorMap.get(USER_PROCESSOR_NAME);
    }

    private TelegramUpdateProcessor handleDefaultUpdate(Update update) {
        String chatId;
        if(update.hasCallbackQuery()){
            chatId = update.getCallbackQuery().getFrom().getId().toString();
        } else if (update.hasMessage()) {
            chatId = update.getMessage().getChatId().toString();
        } else {
            chatId = update.getEditedMessage().getChatId().toString();
        }

        telegramChatRepository.findByChatId(chatId).ifPresent(chat -> {
            Instant updatedAt = chat.getChatStateUpdatedAt();
            if (Duration.between(updatedAt, Instant.now()).toMinutes() > 10) {
                chat.setChatState(ChatState.NORMAL);
                telegramChatRepository.save(chat);
            }
        });

        if (update.hasCallbackQuery()) {
            String callback = update.getCallbackQuery().getData();
            if (callback.startsWith("set_language_")) {
                return telegramUpdateProcessorMap.get("languageSwitcherProcessor");
            }
        }

        return telegramManagerRepository.findByChatId(chatId)
            .map(m -> telegramUpdateProcessorMap.get(MANAGER_PROCESSOR_NAME))
            .orElseGet(() -> telegramUpdateProcessorMap.get(USER_PROCESSOR_NAME));
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

    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
