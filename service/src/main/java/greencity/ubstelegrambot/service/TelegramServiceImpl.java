package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.ChatUserDto;
import greencity.dto.telegram.CreateTelegramMessageRequest;
import greencity.dto.telegram.MarkMessagesAsReadRequest;
import greencity.dto.telegram.MessageAssetDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.entity.order.Order;
import greencity.entity.telegram.MessageAsset;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.telegram.TelegramMessage;
import greencity.entity.user.employee.Employee;
import greencity.enums.AssetType;
import greencity.enums.ChatState;
import greencity.enums.MessageDeliveryStatus;
import greencity.enums.MessageViewingStatus;
import greencity.exceptions.NotFoundException;
import greencity.producers.TelegramChatProducer;
import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.repository.EmployeeRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.AzureCloudStorageService;
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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
    private final AzureCloudStorageService azureCloudStorageService;
    private final UBSClientService ubsClientService;
    private final TelegramExecutor telegramExecutor;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TelegramChatProducer telegramChatProducer;
    private final TelegramUtils telegramUtils;
    private final Map<String, TelegramUpdateProcessor> telegramUpdateProcessorMap;
    private final UserRemoteClient userRemoteClient;

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void sendMessageToUser(CreateTelegramMessageRequest request, MultipartFile[] files) {
        TelegramChat chat = telegramChatRepository.findById(request.getChatId())
            .orElseThrow(() -> new NotFoundException("Chat not found"));

        TelegramMessage message = TelegramMessage.builder()
            .chat(chat)
            .text(request.getText())
            .fromManager(true)
            .status(MessageDeliveryStatus.SENT)
            .sendAt(Instant.now())
            .messageViewingStatus(MessageViewingStatus.READ)
            .build();

        if (message.getText() != null && !message.getText().isBlank()) {
            var sendTextMessage = MessageFactory.buildMessage(chat.getChatId(), message.getText());
            telegramExecutor.executeCommand(sendTextMessage);
        }

        List<MessageAsset> assets = handleFiles(chat, message, files);

        message.setAssets(assets);
        telegramMessageRepository.save(message);

        chat.setLastMessage(message);
        telegramChatRepository.save(chat);
    }

    private List<MessageAsset> handleFiles(TelegramChat chat,
        TelegramMessage message, MultipartFile[] files) {
        List<MessageAsset> assets = new ArrayList<>();
        if (files == null) {
            return assets;
        }

        for (MultipartFile file : files) {
            validateFileSize(file);

            String url = azureCloudStorageService.upload(file);
            AssetType assetType = TelegramUtils.detectAssetType(file);

            MessageAsset asset = MessageAsset.builder()
                .url(url)
                .fileName(file.getOriginalFilename())
                .size(file.getSize())
                .contentType(file.getContentType())
                .type(assetType)
                .message(message)
                .build();
            assets.add(asset);

            sendFileByType(chat, file, assetType);
        }
        return assets;
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > 50 * 1024 * 1024) {
            log.warn("File \"{}\" size exceeds 50MB", file.getName());
            throw new IllegalArgumentException("File size exceeds Telegram bot limit (50MB)");
        }
    }

    private void sendFileByType(TelegramChat chat,
        MultipartFile file, AssetType assetType) {
        try {
            if (assetType == AssetType.FILE) {
                sendAsDocument(chat, file);
            } else if (assetType == AssetType.IMAGE) {
                sendImage(chat, file);
            }
        } catch (IOException e) {
            log.error("Failed to send file to Telegram", e);
            throw new TelegramBotExecutionException("Unable to send file to Telegram", e);
        }
    }

    private void sendAsDocument(TelegramChat chat, MultipartFile file) throws IOException {
        log.info("Sending document: {} filename: {} to chat ID: {}",
            file.getContentType(), file.getOriginalFilename(), chat.getChatId());
        var sendFile = MessageFactory.createSendDocument(chat.getChatId(), file);
        telegramExecutor.executeSendFile(sendFile);
    }

    private void sendImage(TelegramChat chat, MultipartFile file) throws IOException {
        boolean canSendAsPhoto = canSendAsPhoto(file);

        if (canSendAsPhoto) {
            log.info("Sending image as photo: {} filename: {} to chat ID: {}",
                file.getContentType(), file.getOriginalFilename(), chat.getChatId());
            var sendPhotoMessage = MessageFactory.createSendPhoto(chat.getChatId(), file);
            telegramExecutor.executeSendPhoto(sendPhotoMessage);
        } else {
            log.info("Sending image as document: {} filename: {} to chat ID: {}",
                file.getContentType(), file.getOriginalFilename(), chat.getChatId());
            var sendDocumentMessage = MessageFactory.createSendDocument(chat.getChatId(), file);
            telegramExecutor.executeSendFile(sendDocumentMessage);
        }
    }

    private boolean canSendAsPhoto(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
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
        Specification<TelegramChat> spec = ChatSpecifications.withSearchAndSort(searchTerm);

        Page<TelegramChat> chats = telegramChatRepository.findAll(spec, pageable);

        List<ChatDto> chatDtos = chats
            .getContent()
            .stream()
            .map(chat -> {
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
    public void markMessagesAsRead(MarkMessagesAsReadRequest request) {
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
            telegramExecutor.executeCommand(sendMessage);
        }
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
        String chatId = update.hasCallbackQuery()
            ? update.getCallbackQuery().getFrom().getId().toString()
            : update.getMessage().getChatId().toString();

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
}
