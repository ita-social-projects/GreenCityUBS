package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.ChatUserDto;
import greencity.dto.telegram.CreateTelegramMessageRequest;
import greencity.dto.telegram.DeleteTelegramMessageRequest;
import greencity.dto.telegram.EditTelegramMessageRequest;
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
import greencity.repository.EmployeeRepository;
import greencity.repository.MessageAssetRepository;
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
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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
    private final TelegramMessageRepository telegramMessageRepository;
    private final TelegramManagerRepository telegramManagerRepository;
    private final ApplicationContext applicationContext;
    private final TelegramChatRepository telegramChatRepository;
    private final AzureCloudStorageService azureCloudStorageService;
    private final UBSClientService ubsClientService;
    private final TelegramExecutor executor;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TelegramChatProducer telegramChatProducer;
    private final TelegramUtils telegramUtils;
    private final MessageAssetRepository messageAssetRepository;
    private final Map<String, TelegramUpdateProcessor> telegramUpdateProcessorMap;

    @Override
    public void sendMessageToUser(CreateTelegramMessageRequest request, MultipartFile[] files) {
        TelegramChat chat = telegramChatRepository.findById(request.getChatId())
            .orElseThrow(() -> new NotFoundException("Chat not found"));

        var bot = applicationContext.getBean(UBSTelegramBot.class);

        List<MultipartFile> images = new ArrayList<>();
        List<MultipartFile> others = new ArrayList<>();

        if (files != null) {
            for (MultipartFile file : files) {
                AssetType type = TelegramUtils.detectAssetType(file);
                if (type == AssetType.IMAGE && canSendAsPhoto(file)) images.add(file);
                else others.add(file);
            }
        }

        if (!images.isEmpty()) {
            TelegramMessage imageMessage = TelegramMessage.builder()
                    .chat(chat)
                    .text(request.getText())
                    .fromManager(true)
                    .status(MessageDeliveryStatus.SENT)
                    .sendAt(Instant.now())
                    .messageViewingStatus(MessageViewingStatus.READ)
                    .build();

            List<MessageAsset> imageAssets = new ArrayList<>();
            for (MultipartFile img : images) {
                validateFileSize(img);
                String url = azureCloudStorageService.upload(img);
                imageAssets.add(MessageAsset.builder()
                        .url(url)
                        .fileName(img.getOriginalFilename())
                        .size(img.getSize())
                        .contentType(img.getContentType())
                        .type(AssetType.IMAGE)
                        .message(imageMessage)
                        .build());
            }
            imageMessage.setAssets(imageAssets);
            if(imageAssets.size() > 1) {
                SendMediaGroup sendMediaGroup = MessageFactory.buildSendMediaGroup(chat.getChatId(), images, request.getText());
                List<Message> sentMessages = executor.executeSendMediaGroup(bot, sendMediaGroup);
                if (!sentMessages.isEmpty()) {
                    imageMessage.setTelegramMessageId(sentMessages.getFirst().getMessageId());
                    imageMessage.setMediaGroupId(sentMessages.getFirst().getMediaGroupId());
                    for (int i = 0; i < imageAssets.size() && i < sentMessages.size(); i++) {
                        imageAssets.get(i).setTelegramMessageId(sentMessages.get(i).getMessageId());
                    }
                }
            } else {
                SendPhoto sendPhoto;
                try {
                    sendPhoto = MessageFactory.createSendPhoto(chat.getChatId(), images.getFirst(), request.getText());
                } catch (IOException e) {
                    throw new RuntimeException("Unable to send file to Telegram", e);
                }
                Message sentMessage = executor.executeSendPhoto(bot, sendPhoto);
                if(sentMessage != null) {
                    imageMessage.setTelegramMessageId(sentMessage.getMessageId());
                    imageMessage.setMediaGroupId(null);
                    imageAssets.getFirst().setTelegramMessageId(sentMessage.getMessageId());
                }
            }

            telegramMessageRepository.save(imageMessage);
            chat.setLastMessage(imageMessage);
        }

        for (MultipartFile file : others) {
            validateFileSize(file);
            TelegramMessage fileMessage = TelegramMessage.builder()
                    .chat(chat)
                    .fromManager(true)
                    .status(MessageDeliveryStatus.SENT)
                    .sendAt(Instant.now())
                    .messageViewingStatus(MessageViewingStatus.READ)
                    .build();

            String url = azureCloudStorageService.upload(file);
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

            Message sentMessage = sendAsDocument(bot, chat, file);
            fileMessage.setTelegramMessageId(sentMessage.getMessageId());
            asset.setTelegramMessageId(sentMessage.getMessageId());

            messageAssetRepository.save(asset);
            telegramMessageRepository.save(fileMessage);
            chat.setLastMessage(fileMessage);
        }

        telegramChatRepository.save(chat);
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > 50 * 1024 * 1024) {
            log.warn("File \"{}\" size exceeds 50MB", file.getName());
            throw new IllegalArgumentException("File size exceeds Telegram bot limit (50MB)");
        }
    }

    private Message sendAsDocument(UBSTelegramBot bot, TelegramChat chat, MultipartFile file) {
        log.info("Sending document: {} filename: {} to chat ID: {}",
            file.getContentType(), file.getOriginalFilename(), chat.getChatId());
        try {
            var sendFile = MessageFactory.createSendDocument(chat.getChatId(), file);
            return executor.executeSendFile(bot, sendFile);
        } catch (IOException e) {
            log.error("Failed to send file to Telegram", e);
            throw new RuntimeException("Unable to send file to Telegram", e);
        }
    }

    private boolean canSendAsPhoto(MultipartFile file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    public TelegramUpdateProcessor processUpdate(Update update) {
        Message message = update.getMessage();

        if (isStartCommand(message)) {
            String uuid = extractUuid(message);
            Long chatId = message.getFrom().getId();
            Optional<TelegramChat> chatOpt = telegramChatRepository.findByChatId(chatId.toString());

            if (chatOpt.isEmpty()) {
                return handleNewChat(uuid, message, chatId);
            } else {
                return handleExistingChat(uuid, chatOpt.get(), chatId);
            }
        }

        return handleDefaultUpdate(update);
    }

    @Override
    public void editManagerMessage(EditTelegramMessageRequest request) {
        TelegramChat chat = telegramChatRepository.findById(request.chatId()).orElseThrow(NotFoundException::new);
        if(chat != null) {
            TelegramMessage message = telegramMessageRepository.findByTelegramMessageId(request.messageId())
                    .orElseThrow(EntityNotFoundException::new);
            if(message.getFromManager()) {
                var bot = applicationContext.getBean(UBSTelegramBot.class);
                message.setUpdatedAt(Instant.now());
                message.setText(request.newText());
                if (!message.getAssets().isEmpty()){
                    EditMessageCaption editMessageCaption = MessageFactory
                            .buildEditMessageCaption(chat.getChatId(), message.getTelegramMessageId(), request.newText());
                    executor.executeCommand(bot,  editMessageCaption);
                } else {
                    EditMessageText editMessageText = MessageFactory
                            .buildEditMessageText(chat.getChatId(), message.getTelegramMessageId(), request.newText());
                    executor.executeCommand(bot, editMessageText);
                }

                telegramMessageRepository.save(message);
            }
        }
    }

    @Override
    @Transactional
    public void deleteManagerMessage(DeleteTelegramMessageRequest request) {
        if(request.messageId() != null){
            deleteMessage(request);
        } else if (request.assetId() != null){
            deleteAsset(request);
        }
    }


    private void deleteMessage(DeleteTelegramMessageRequest request) {
        telegramChatRepository.findById(request.chatId()).ifPresent(chat -> {
            TelegramMessage message = telegramMessageRepository.findByTelegramMessageId(request.messageId())
                    .orElseThrow(EntityNotFoundException::new);
            if(message.getFromManager()) {
                var bot = applicationContext.getBean(UBSTelegramBot.class);
                DeleteMessage deleteMessage = MessageFactory.buildDeleteMessage(chat.getChatId(), request.messageId());
                executor.executeCommand(bot, deleteMessage);
                telegramMessageRepository.findByTelegramMessageId(request.messageId())
                        .ifPresent(telegramMessageRepository::delete);

            }
        });
    }

    private void deleteAsset(DeleteTelegramMessageRequest request) {
        messageAssetRepository.findById(request.assetId()).ifPresent(asset -> {
            TelegramMessage parent = asset.getMessage();
            if(parent.getFromManager()){
            var bot = applicationContext.getBean(UBSTelegramBot.class);
            DeleteMessage deleteMessage = MessageFactory.buildDeleteMessage(parent.getChat().getChatId(),
                    asset.getTelegramMessageId());
            executor.executeCommand(bot, deleteMessage);

            parent.getAssets().remove(asset);
            messageAssetRepository.delete(asset);

            if (parent.getAssets().isEmpty()) {
                telegramMessageRepository.delete(parent);
            }
            }
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
            .chatStateUpdatedAt(Instant.now());

        if (!uuid.isEmpty()) {
            userRepository.findUserByUuid(uuid).ifPresent(newChatBuilder::user);
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
            userRepository.findUserByUuid(uuid).ifPresent(chat::setUser);
            telegramChatRepository.save(chat);
        }

        return resolveProcessorByUuid(uuid, chatId);
    }

    private TelegramUpdateProcessor resolveProcessorByUuid(String uuid, Long chatId) {
        if (uuid.isEmpty()) {
            return telegramUpdateProcessorMap.get("userUpdateProcessor");
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
                return telegramUpdateProcessorMap.get("managerUpdateProcessor");
            }
        }

        return telegramUpdateProcessorMap.get("userUpdateProcessor");
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

        return telegramManagerRepository.findByChatId(chatId)
            .map(m -> telegramUpdateProcessorMap.get("managerUpdateProcessor"))
            .orElseGet(() -> telegramUpdateProcessorMap.get("userUpdateProcessor"));
    }
}
