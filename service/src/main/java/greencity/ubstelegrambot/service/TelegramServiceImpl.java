package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.ChatUserDto;
import greencity.dto.telegram.CreateTelegramMessageRequest;
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
import greencity.exceptions.NotFoundException;
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
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final TelegramUtils telegramUtils;
    private final Map<String, TelegramUpdateProcessor> telegramUpdateProcessorMap;

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
                if (file.getSize() > 50 * 1024 * 1024) {
                    log.warn("File \"{}\" size has over than 50MB", file.getName());
                    throw new IllegalArgumentException("File size exceeds Telegram bot limit (50MB)");
                }
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

                if (assetType == AssetType.IMAGE) {
                    try {
                        var sendPhotoMessage = MessageFactory.createMultipartFileSender(chat.getChatId(), file);
                        executor.executeSendPhoto(bot, sendPhotoMessage);
                    } catch (IOException e) {
                        log.error("Failed to send image to Telegram", e);
                        throw new RuntimeException("Unable to send image to Telegram", e);
                    }
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
            .chatStateUpdatedAt(LocalDateTime.now());

        if (!uuid.isEmpty()) {
            userRepository.findUserByUuid(uuid).ifPresent(newChatBuilder::user);
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
            Instant updatedAt = chat.getChatStateUpdatedAt().atZone(ZoneId.systemDefault()).toInstant();
            if (Duration.between(updatedAt, Instant.now()).toMinutes() > 10) {
                chat.setChatState(ChatState.NORMAL);
                telegramChatRepository.save(chat);
            }
        });

        return telegramManagerRepository.findByChatId(chatId)
            .map(m -> telegramUpdateProcessorMap.get("managerUpdateProcessor"))
            .orElseGet(() -> telegramUpdateProcessorMap.get("userUpdateProcessor"));
    }

    private void notifyNewChat(ChatDto chatDto) {
        messagingTemplate.convertAndSend("/topic/chats", chatDto);
    }
}
