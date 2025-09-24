package greencity.ubstelegrambot;

import greencity.client.UserRemoteClient;
import greencity.client.config.UserRemoteWebClient;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.ChatDto;
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
import greencity.enums.MessageDeliveryStatus;
import greencity.enums.MessageViewingStatus;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.exceptions.bots.UnsupportedTelegramAssetException;
import greencity.producers.TelegramChatProducer;
import greencity.repository.EmployeeRepository;
import greencity.repository.MessageAssetRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.TelegramUpdateProcessor;
import greencity.service.ubs.UBSClientService;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.service.TelegramExecutor;
import greencity.ubstelegrambot.service.TelegramServiceImpl;
import greencity.ubstelegrambot.service.TelegramUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class TelegramServiceTest {
    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private UserRemoteWebClient userRemoteWebClient;

    @Mock
    private TelegramMessageRepository telegramMessageRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UBSClientService ubsClientService;

    @Mock
    private TelegramExecutor executor;

    @Mock
    private MultipartFile file;

    @Mock
    private TelegramChatProducer telegramChatProducer;

    @Mock
    private TelegramManagerRepository telegramManagerRepository;

    @Mock
    private MessageAssetRepository messageAssetRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TelegramUtils telegramUtils;

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock
    private TelegramUpdateProcessor updateProcessor;

    private TelegramServiceImpl telegramService;

    public Map<String, TelegramUpdateProcessor> telegramUpdateProcessorMap;

    @BeforeEach
    void setUp() {
        telegramUpdateProcessorMap = new HashMap<>();
        telegramUpdateProcessorMap.put("userUpdateProcessor", updateProcessor);

        telegramService = new TelegramServiceImpl(
            telegramMessageRepository,
            telegramManagerRepository,
            telegramChatRepository,
            userRemoteWebClient,
            userRemoteClient,
            ubsClientService,
            executor,
            employeeRepository,
            orderRepository,
            userRepository,
            telegramChatProducer,
            telegramUtils,
            messageAssetRepository,
            telegramUpdateProcessorMap);
    }

    @Test
    void testSendMessageToUser_OnlyText_MessageSent() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);
        request.setText("Hello");

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));

        telegramService.sendMessageToUser(request, null);

        verify(executor).executeSendMessage(any(SendMessage.class));
        verify(telegramMessageRepository).save(any(TelegramMessage.class));
        verify(telegramChatRepository).save(any(TelegramChat.class));
    }

    @Test
    void testSendMessageToUser_FileUnknownContentType_MessageSentAndPhotoUploaded() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);
        request.setText("Hi");

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(file.getContentType()).thenReturn(null);

        assertThrows(UnsupportedTelegramAssetException.class,
            () -> telegramService.sendMessageToUser(request, new MultipartFile[] {file}));

        verify(telegramChatRepository).findById(1L);
        verifyNoInteractions(userRemoteWebClient);
        verifyNoInteractions(executor);
    }

    @Test
    void testSendMessageToUser_FileImageContentTypeLargeDimensions_MessageSentAndPhotoUploaded() throws IOException {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        BufferedImage img = new BufferedImage(10000, 10000, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        when(executor.executeSendFile(any(SendDocument.class))).thenReturn(mockTelegramResponse(10));
        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getSize()).thenReturn(2048L);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getInputStream()).thenReturn(bais);
        when(userRemoteWebClient.uploadFile(file)).thenReturn("http://image");

        telegramService.sendMessageToUser(request, new MultipartFile[] {file});

        verify(userRemoteWebClient).uploadFile(file);
        verify(executor).executeSendFile(any(SendDocument.class));
        verify(telegramMessageRepository).save(any(TelegramMessage.class));
        verify(telegramChatRepository).save(any(TelegramChat.class));
    }

    @Test
    void testSendMessageToUser_FileImageContentTypeNormalDimensions_MessageSentAndPhotoUploaded() throws IOException {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        when(executor.executeSendPhoto(any(SendPhoto.class))).thenReturn(mockTelegramResponse(20));
        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getSize()).thenReturn(2048L);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getInputStream()).thenReturn(bais);
        when(userRemoteWebClient.uploadFile(file)).thenReturn("http://image");

        telegramService.sendMessageToUser(request, new MultipartFile[] {file});

        verify(userRemoteWebClient).uploadFile(file);
        verify(executor).executeSendPhoto(any(SendPhoto.class));
        verify(telegramMessageRepository).save(any(TelegramMessage.class));
        verify(telegramChatRepository).save(any(TelegramChat.class));
    }

    @Test
    void testSendMessageToUser_WrongChatId_NotFoundExceptionThrown() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(99L);

        when(telegramChatRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> telegramService.sendMessageToUser(request, null));

        verify(telegramMessageRepository, never()).save(any());
        verify(telegramChatRepository, never()).save(any());
    }

    @Test
    void testFindUserMessageByChatId_MessagesFound_PageableDtoReturned() {

        Long chatId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        TelegramMessage message = TelegramMessage.builder()
            .id(100L)
            .text("Hello")
            .sendAt(Instant.now())
            .fromManager(false)
            .status(MessageDeliveryStatus.SENT)
            .assets(List.of(MessageAsset.builder()
                .id(1L)
                .url("http://example.com/file.png")
                .type(AssetType.IMAGE)
                .fileName("file.png")
                .size(1024L)
                .contentType("image/png")
                .build()))
            .build();

        Page<TelegramMessage> messagePage = new PageImpl<>(List.of(message), pageable, 1);

        when(telegramMessageRepository.findByChatId(chatId, pageable)).thenReturn(messagePage);

        PageableDto<TelegramMessageDto> result = telegramService.findUserMessageByChatId(chatId, pageable);

        assertEquals(0, result.getCurrentPage());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        TelegramMessageDto dto = result.getPage().getFirst();
        assertEquals("Hello", dto.getText());
        Assertions.assertFalse(dto.getFromManager());
        assertEquals(MessageDeliveryStatus.SENT, dto.getDeliveryStatus());
        assertEquals(1, dto.getAssets().size());

        MessageAssetDto assetDto = dto.getAssets().getFirst();
        assertEquals("http://example.com/file.png", assetDto.getUrl());
        assertEquals("file.png", assetDto.getFileName());
        assertEquals(AssetType.IMAGE, assetDto.getType());
        assertEquals(Optional.of(1024L).get(), assetDto.getSize());
    }

    @Test
    void testFindUserMessageByChatId_ChatNotFound_NotFoundExceptionThrown() {
        Long chatId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<TelegramMessage> emptyPage = new PageImpl<>(Collections.emptyList());

        when(telegramMessageRepository.findByChatId(chatId, pageable)).thenReturn(emptyPage);

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> telegramService.findUserMessageByChatId(chatId, pageable));

        assertEquals("There are no messages in chat 1", exception.getMessage());
    }

    @Test
    void testGetChats_WithUserAndLastMessage_ChatsReturned() {
        String searchTerm = "test";
        Pageable pageable = PageRequest.of(0, 10);

        User user = User.builder()
            .recipientName("Іван")
            .recipientSurname("Петренко")
            .recipientEmail("ivan@example.com")
            .build();

        MessageAsset asset = MessageAsset.builder()
            .id(10L)
            .url("http://image.png")
            .type(AssetType.IMAGE)
            .fileName("image.png")
            .size(1234L)
            .contentType("image/png")
            .build();

        TelegramMessage message = TelegramMessage.builder()
            .id(100L)
            .text("Hello")
            .sendAt(Instant.now())
            .fromManager(true)
            .status(MessageDeliveryStatus.SENT)
            .assets(List.of(asset))
            .build();

        TelegramChat chat = TelegramChat.builder()
            .id(1L)
            .chatId("123456789")
            .firstName("Test")
            .lastName("User")
            .username("testuser")
            .user(user)
            .lastMessage(message)
            .build();

        Page<TelegramChat> chatPage = new PageImpl<>(List.of(chat), pageable, 1);

        when(telegramChatRepository.findAll((ArgumentMatchers.<Specification<TelegramChat>>any()), eq(pageable)))
            .thenReturn(chatPage);

        PageableDto<ChatDto> result = telegramService.getChats(searchTerm, pageable);

        assertEquals(0, result.getCurrentPage());
        assertEquals(1, result.getTotalElements());

        ChatDto chatDto = result.getPage().getFirst();
        assertEquals("Test", chatDto.getFirstName());
        assertNotNull(chatDto.getUser());
        assertEquals("ivan@example.com", chatDto.getUser().getEmail());

        TelegramMessageDto lastMessage = chatDto.getLastMessage();
        assertNotNull(lastMessage);
        assertEquals("Hello", lastMessage.getText());
        assertEquals(1, lastMessage.getAssets().size());
        assertEquals("http://image.png", lastMessage.getAssets().getFirst().getUrl());
    }

    @Test
    void testGetChats_WithoutUser_ChatsReturned() {
        String searchTerm = "test";
        Pageable pageable = PageRequest.of(0, 10);

        TelegramChat chat = TelegramChat.builder()
            .id(1L)
            .chatId("123456789")
            .firstName("No")
            .lastName("User")
            .username("nouser")
            .user(null)
            .build();

        Page<TelegramChat> chatPage = new PageImpl<>(List.of(chat), pageable, 1);

        when(telegramChatRepository.findAll((ArgumentMatchers.<Specification<TelegramChat>>any()), eq(pageable)))
            .thenReturn(chatPage);

        // when
        PageableDto<ChatDto> result = telegramService.getChats(searchTerm, pageable);

        assertEquals(0, result.getCurrentPage());
        assertEquals(1, result.getTotalElements());

        ChatDto chatDto = result.getPage().getFirst();
        Assertions.assertNull(chatDto.getUser());
        Assertions.assertNull(chatDto.getLastMessage());
    }

    @Test
    void testGetChats_EmptyResult_EmptyPageableDtoReturned() {
        Pageable pageable = PageRequest.of(0, 10);
        when(telegramChatRepository.findAll((ArgumentMatchers.<Specification<TelegramChat>>any()), eq(pageable)))
            .thenReturn(Page.empty());

        PageableDto<ChatDto> result = telegramService.getChats("nothing", pageable);

        assertTrue(result.getPage().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void testGetLastOrderByChatId_ChatFoundUserAndOrderExists_LastOrderReturned() {
        Long chatId = 1L;

        User user = User.builder()
            .id(42L)
            .build();

        TelegramChat chat = TelegramChat.builder()
            .id(chatId)
            .user(user)
            .build();

        Order order = Order.builder()
            .id(100L)
            .orderDate(LocalDateTime.now())
            .user(user)
            .build();

        OrdersDataForUserDto expectedDto = OrdersDataForUserDto.builder()
            .id(100L)
            .build();

        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(orderRepository.findFirstByUserIdOrderByOrderDateDesc(user.getId())).thenReturn(Optional.of(order));
        when(ubsClientService.getOrdersData(order)).thenReturn(expectedDto);

        OrdersDataForUserDto result = telegramService.getLastOrderByChatId(chatId);

        assertNotNull(result);
        assertEquals(Optional.of(100L).get(), result.getId());
    }

    @Test
    void testGetChats_WithLastMessageAndNullAssets_ShouldReturnEmptyAssets() {
        Pageable pageable = PageRequest.of(0, 10);
        TelegramMessage message = TelegramMessage.builder()
            .id(100L)
            .text("Message with null assets")
            .sendAt(Instant.now())
            .fromManager(true)
            .status(MessageDeliveryStatus.SENT)
            .assets(null)
            .build();

        TelegramChat chat = TelegramChat.builder()
            .id(1L)
            .chatId("123")
            .firstName("Test")
            .lastName("User")
            .lastMessage(message)
            .build();

        Page<TelegramChat> chatPage = new PageImpl<>(List.of(chat), pageable, 1);

        when(telegramChatRepository.findAll(ArgumentMatchers.<Specification<TelegramChat>>any(), eq(pageable)))
            .thenReturn(chatPage);

        PageableDto<ChatDto> result = telegramService.getChats("", pageable);

        assertEquals(1, result.getTotalElements());
        ChatDto dto = result.getPage().getFirst();

        assertNotNull(dto.getLastMessage());
        assertEquals("Message with null assets", dto.getLastMessage().getText());
        assertNotNull(dto.getLastMessage().getAssets());
        assertTrue(dto.getLastMessage().getAssets().isEmpty());
    }

    @Test
    void getChats_shouldReturnChatDtos_whenUserIdExists() {
        Long userId = 123L;
        String chatId = "456";

        TelegramChat chat = new TelegramChat();
        chat.setId(1L);
        chat.setChatId(chatId);
        chat.setFirstName("John");
        chat.setLastName("Doe");

        when(telegramChatRepository.findByUserId(userId))
            .thenReturn(Optional.of(chat));
        when(telegramChatRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(chat)));

        Pageable pageable = PageRequest.of(0, 10);

        PageableDto<ChatDto> result = telegramService.getChats(userId.toString(), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getPage().get(0).getFirstName());
        verify(telegramChatRepository).findByUserId(userId);
        verify(telegramChatRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getChats_shouldReturnEmpty_whenUserIdNotFound() {
        Long userId = 999L;
        when(telegramChatRepository.findByUserId(userId))
            .thenReturn(Optional.empty());

        Pageable pageable = PageRequest.of(0, 10);

        PageableDto<ChatDto> result = telegramService.getChats(userId.toString(), pageable);

        assertTrue(result.getPage().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(telegramChatRepository).findByUserId(userId);
        verify(telegramChatRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getChats_shouldWorkWithNullSearchTerm() {
        Pageable pageable = PageRequest.of(0, 10);

        TelegramChat chat = new TelegramChat();
        chat.setId(1L);
        chat.setChatId("123");
        chat.setFirstName("Jane");

        when(telegramChatRepository.findAll(any(Specification.class), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(chat)));

        PageableDto<ChatDto> result = telegramService.getChats(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Jane", result.getPage().get(0).getFirstName());
        verify(telegramChatRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetLastOrderByChatId_ChatNotFound_NotFoundExceptionThrown() {
        Long chatId = 2L;
        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> telegramService.getLastOrderByChatId(chatId));

        assertEquals("Chat with id 2 not found", exception.getMessage());
    }

    @Test
    void testGetLastOrderByChatId_UserIsNull_NotFoundExceptionThrown() {
        Long chatId = 3L;
        TelegramChat chat = TelegramChat.builder()
            .id(chatId)
            .user(null)
            .build();

        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> telegramService.getLastOrderByChatId(chatId));

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void testGetLastOrderByChatId_OrderNotFound_NotFoundExceptionThrown() {
        Long chatId = 4L;
        User user = User.builder().id(55L).build();
        TelegramChat chat = TelegramChat.builder()
            .id(chatId)
            .user(user)
            .build();

        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(orderRepository.findFirstByUserIdOrderByOrderDateDesc(user.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> telegramService.getLastOrderByChatId(chatId));

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void testGetChatById_ChatFound_ChatDtoReturned() {
        Long chatId = 1L;
        TelegramChat chat = TelegramChat.builder()
            .id(chatId)
            .chatId("123456789")
            .firstName("Іван")
            .lastName("Петренко")
            .username("ivan_pet")
            .build();

        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        ChatDto result = telegramService.getChatById(chatId);

        assertNotNull(result);
        assertEquals(chatId, result.getId());
        assertEquals("Іван", result.getFirstName());
        assertEquals("Петренко", result.getLastName());
        assertEquals("ivan_pet", result.getUsername());
    }

    @Test
    void testGetChatById_ChatNotFound_NotFoundExceptionThrown() {
        Long chatId = 99L;
        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> telegramService.getChatById(chatId));

        assertEquals("Chat with id 99 not found", exception.getMessage());
    }

    @Test
    void processUpdate_whenStartWithoutUuid() {
        Long chatId = 12345L;
        String startCommand = "/start";

        var from = getTelegramAPIUser(chatId);

        Message message = new Message();
        message.setFrom(from);
        message.setText(startCommand);

        Update update = new Update();
        update.setMessage(message);

        doNothing().when(telegramChatProducer).notifyNewChat(any(ChatDto.class));

        TelegramChat savedChat = TelegramChat.builder()
            .id(1L)
            .chatId(chatId.toString())
            .build();

        when(telegramChatRepository.findByChatId(chatId.toString())).thenReturn(Optional.empty());
        when(telegramChatRepository.save(any())).thenReturn(savedChat);

        TelegramUpdateProcessor userProcessor = mock(TelegramUpdateProcessor.class);
        telegramUpdateProcessorMap.put("userUpdateProcessor", userProcessor);

        SendMessage expected = new SendMessage(chatId.toString(), "Hello user!");
        when(userProcessor.process(update)).thenReturn(expected);

        // act
        telegramService.processUpdate(update);

        // assert
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(executor).executeCommand(captor.capture());

        SendMessage result = captor.getValue();
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());
    }

    @Test
    void processUpdate_whenStartWithManagerUuidAndChatExists() {
        // arrange
        Long chatId = 12345L;
        String uuid = "some-uuid";
        String startCommand = "/start " + uuid;

        var apiUser = getTelegramAPIUser(chatId);

        Message message = new Message();
        message.setFrom(apiUser);
        message.setText(startCommand);

        Update update = new Update();
        update.setMessage(message);

        TelegramChat existingChat = TelegramChat.builder()
            .id(1L)
            .chatId(chatId.toString())
            .build();

        when(telegramChatRepository.findByChatId(chatId.toString())).thenReturn(Optional.of(existingChat));

        Employee employee = new Employee(); // або просто mock(Employee.class)
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));
        when(telegramUtils.checkIsEmployeeManager(employee)).thenReturn(true);

        TelegramUpdateProcessor managerProcessor = mock(TelegramUpdateProcessor.class);
        telegramUpdateProcessorMap.put("managerUpdateProcessor", managerProcessor);

        SendMessage expected = new SendMessage(chatId.toString(), "Hello manager!");
        when(managerProcessor.process(update)).thenReturn(expected);

        // act
        telegramService.processUpdate(update);

        // assert
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(executor).executeCommand(captor.capture());

        SendMessage result = captor.getValue();
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());
    }

    @Test
    void processUpdate_whenNoStartCommand() {
        // arrange
        Long chatId = 12345L;
        String text = "Some regular text";

        var apiUser = getTelegramAPIUser(chatId);

        Chat chat = new Chat();
        chat.setId(chatId);

        Message message = new Message();
        message.setFrom(apiUser);
        message.setText(text);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        TelegramChat telegramChat = TelegramChat.builder()
            .chatId(chatId.toString())
            .chatStateUpdatedAt(Instant.now().minus(15, ChronoUnit.MINUTES))
            .build();

        when(telegramChatRepository.findByChatId(chatId.toString())).thenReturn(Optional.of(telegramChat));
        when(telegramManagerRepository.findByChatId(chatId.toString())).thenReturn(Optional.empty());

        TelegramUpdateProcessor userProcessor = mock(TelegramUpdateProcessor.class);
        telegramUpdateProcessorMap.put("userUpdateProcessor", userProcessor);

        SendMessage expected = new SendMessage(chatId.toString(), "default user reply");
        when(userProcessor.process(update)).thenReturn(expected);

        // act
        telegramService.processUpdate(update);

        // assert
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(executor).executeCommand(captor.capture());
        verify(telegramChatRepository).save(telegramChat);

        SendMessage result = captor.getValue();
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());
    }

    @Test
    void processUpdate_whenCallbackFromManager() {
        // arrange
        Long chatId = 12345L;

        var telegramUser = getTelegramAPIUser(chatId);

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(telegramUser);
        callbackQuery.setData("manager_callback_example");

        Chat chat = new Chat();
        chat.setId(chatId);

        Message callbackMessage = new Message();
        callbackMessage.setChat(chat);

        callbackQuery.setMessage(callbackMessage);

        var update = new Update();
        update.setCallbackQuery(callbackQuery);

        when(telegramManagerRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.of(mock(TelegramManager.class)));

        TelegramUpdateProcessor managerProcessor = mock(TelegramUpdateProcessor.class);
        telegramUpdateProcessorMap.put("managerUpdateProcessor", managerProcessor);

        SendMessage expected = new SendMessage(chatId.toString(), "manager callback response");
        when(managerProcessor.process(update)).thenReturn(expected);

        // act
        telegramService.processUpdate(update);

        // assert
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(executor).executeCommand(captor.capture());

        SendMessage result = captor.getValue();
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());
    }

    @Test
    void processUpdate_whenStartWithUserUuidAndChatExists() {
        Long chatId = 12345L;
        String uuid = "user-uuid";
        String startCommand = "/start " + uuid;

        var apiUser = getTelegramAPIUser(chatId);

        Message message = new Message();
        message.setFrom(apiUser);
        message.setText(startCommand);

        Update update = new Update();
        update.setMessage(message);

        TelegramChat existingChat = TelegramChat.builder()
            .id(1L)
            .chatId(chatId.toString())
            .build();

        when(telegramChatRepository.findByChatId(chatId.toString())).thenReturn(Optional.of(existingChat));

        Employee employee = new Employee();
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));

        when(telegramUtils.checkIsEmployeeManager(employee)).thenReturn(false);

        TelegramUpdateProcessor userProcessor = mock(TelegramUpdateProcessor.class);
        telegramUpdateProcessorMap.put("userUpdateProcessor", userProcessor);

        SendMessage expected = new SendMessage(chatId.toString(), "Hello user!");
        when(userProcessor.process(update)).thenReturn(expected);

        telegramService.processUpdate(update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(executor).executeCommand(captor.capture());

        SendMessage result = captor.getValue();
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());
    }

    @Test
    void processUpdate_shouldCreateNewChatWithoutUser_whenStartWithUuidAndUserNotFound() {
        Long chatId = 12345L;
        String uuid = "some-uuid";
        String startCommand = "/start " + uuid;

        var apiUser = getTelegramAPIUser(chatId);
        apiUser.setFirstName("TestFirst");
        apiUser.setLastName("TestLast");

        Message message = new Message();
        message.setFrom(apiUser);
        message.setText(startCommand);

        Update update = new Update();
        update.setMessage(message);

        when(telegramChatRepository.findByChatId(chatId.toString())).thenReturn(Optional.empty());
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        ArgumentCaptor<TelegramChat> chatCaptor = ArgumentCaptor.forClass(TelegramChat.class);
        when(telegramChatRepository.save(chatCaptor.capture())).thenAnswer(invocation -> {
            TelegramChat chat = invocation.getArgument(0);
            chat.setId(1L);
            return chat;
        });

        TelegramUpdateProcessor userProcessor = mock(TelegramUpdateProcessor.class);
        telegramUpdateProcessorMap.put("userUpdateProcessor", userProcessor);

        SendMessage expected = new SendMessage(chatId.toString(), "response after creating chat without user");
        when(userProcessor.process(update)).thenReturn(expected);

        telegramService.processUpdate(update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(executor).executeCommand(captor.capture());

        SendMessage result = captor.getValue();
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());

        TelegramChat savedChat = chatCaptor.getValue();
        assertEquals(chatId.toString(), savedChat.getChatId());
        assertEquals("TestFirst", savedChat.getFirstName());
        assertEquals("TestLast", savedChat.getLastName());
        Assertions.assertNull(savedChat.getUser());
    }

    @Test
    void processUpdate_whenMessageTextIsNull() {
        Long chatId = 12345L;

        var apiUser = getTelegramAPIUser(chatId);

        Message message = new Message();
        message.setFrom(apiUser);
        message.setText(null);

        Chat chat = new Chat();
        chat.setId(chatId);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        TelegramChat telegramChat = TelegramChat.builder()
            .chatId(chatId.toString())
            .chatStateUpdatedAt(Instant.now().minus(15, ChronoUnit.MINUTES))
            .build();

        when(telegramChatRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.of(telegramChat));

        when(telegramManagerRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.empty());

        TelegramUpdateProcessor userProcessor = mock(TelegramUpdateProcessor.class);
        telegramUpdateProcessorMap.put("userUpdateProcessor", userProcessor);

        SendMessage expected = new SendMessage(chatId.toString(), "default user reply");
        when(userProcessor.process(update)).thenReturn(expected);

        telegramService.processUpdate(update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(executor).executeCommand(captor.capture());
        verify(telegramChatRepository).save(telegramChat);

        SendMessage result = captor.getValue();
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());

    }

    @Test
    void processUpdate_shouldNotSaveUser_whenUuidIsEmptyInExistingChat() {
        Long chatId = 12345L;
        String startCommand = "/start";

        var apiUser = getTelegramAPIUser(chatId);

        Message message = new Message();
        message.setFrom(apiUser);
        message.setText(startCommand);

        Chat chat = new Chat();
        chat.setId(chatId);

        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        TelegramChat existingChat = TelegramChat.builder()
            .id(1L)
            .chatId(chatId.toString())
            .build();

        when(telegramChatRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.of(existingChat));

        TelegramUpdateProcessor userProcessor = mock(TelegramUpdateProcessor.class);
        telegramUpdateProcessorMap.put("userUpdateProcessor", userProcessor);

        SendMessage expected = new SendMessage(chatId.toString(), "Hello user!");
        when(userProcessor.process(update)).thenReturn(expected);

        telegramService.processUpdate(update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(executor).executeCommand(captor.capture());
        verify(userRepository, never()).findUserByUuid(any());
        verify(telegramChatRepository, never()).save(any());

        SendMessage result = captor.getValue();
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());
    }

    @Test
    void processUpdate_shouldSetUserAndLanguage_whenUuidProvidedAndUserFound() {
        Long chatId = 12345L;
        String uuid = "user-uuid";
        String startCommand = "/start " + uuid;

        var apiUser = getTelegramAPIUser(chatId);
        apiUser.setUserName("testUser");
        apiUser.setFirstName("Test");
        apiUser.setLastName("User");

        Message message = new Message();
        message.setFrom(apiUser);
        message.setText(startCommand);

        Update update = new Update();
        update.setMessage(message);

        when(telegramChatRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.empty());

        User mockUser = new User();
        mockUser.setUuid(uuid);
        when(userRepository.findUserByUuid(uuid))
            .thenReturn(Optional.of(mockUser));

        String expectedLang = "en";
        when(userRemoteClient.findUserLanguageByUuid(uuid))
            .thenReturn(expectedLang);

        telegramService.processUpdate(update);

        ArgumentCaptor<TelegramChat> chatCaptor = ArgumentCaptor.forClass(TelegramChat.class);
        verify(telegramChatRepository).save(chatCaptor.capture());

        TelegramChat savedChat = chatCaptor.getValue();
        assertEquals(mockUser, savedChat.getUser());
        assertEquals(expectedLang, savedChat.getLanguageCode());
    }

    @Test
    void processUpdate_shouldSetDefaultLanguage_whenUserLanguageServiceFails() {
        Long chatId = 12345L;
        String uuid = "user-uuid";
        String startCommand = "/start " + uuid;

        var apiUser = getTelegramAPIUser(chatId);
        apiUser.setUserName("testUser");
        apiUser.setFirstName("Test");
        apiUser.setLastName("User");

        Message message = new Message();
        message.setFrom(apiUser);
        message.setText(startCommand);

        Update update = new Update();
        update.setMessage(message);

        when(telegramChatRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.empty());

        User mockUser = new User();
        mockUser.setUuid(uuid);
        when(userRepository.findUserByUuid(uuid))
            .thenReturn(Optional.of(mockUser));

        when(userRemoteClient.findUserLanguageByUuid(uuid))
            .thenThrow(new RuntimeException("Service unavailable"));

        telegramService.processUpdate(update);

        ArgumentCaptor<TelegramChat> chatCaptor = ArgumentCaptor.forClass(TelegramChat.class);
        verify(telegramChatRepository).save(chatCaptor.capture());

        TelegramChat savedChat = chatCaptor.getValue();
        assertEquals(mockUser, savedChat.getUser());
        assertEquals("uk", savedChat.getLanguageCode());
    }

    @Test
    void processUpdate_shouldUpdateUserAndLanguage_whenExistingChatAndUserLanguageFound() {
        Long chatId = 12345L;
        String uuid = "user-uuid";
        String startCommand = "/start " + uuid;

        var apiUser = getTelegramAPIUser(chatId);
        apiUser.setUserName("testUser");
        apiUser.setFirstName("Test");
        apiUser.setLastName("User");

        Message message = new Message();
        message.setFrom(apiUser);
        message.setText(startCommand);

        Update update = new Update();
        update.setMessage(message);

        TelegramChat existingChat = TelegramChat.builder()
            .id(1L)
            .chatId(chatId.toString())
            .build();
        when(telegramChatRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.of(existingChat));

        User mockUser = new User();
        mockUser.setUuid(uuid);
        when(userRepository.findUserByUuid(uuid))
            .thenReturn(Optional.of(mockUser));

        String expectedLang = "en";
        when(userRemoteClient.findUserLanguageByUuid(uuid))
            .thenReturn(expectedLang);

        telegramService.processUpdate(update);

        ArgumentCaptor<TelegramChat> chatCaptor = ArgumentCaptor.forClass(TelegramChat.class);
        verify(telegramChatRepository).save(chatCaptor.capture());

        TelegramChat savedChat = chatCaptor.getValue();
        assertEquals(mockUser, savedChat.getUser());
        assertEquals(expectedLang, savedChat.getLanguageCode());
    }

    @Test
    void processUpdate_shouldUpdateUserAndDefaultLanguage_whenExistingChatAndLanguageServiceFails() {
        Long chatId = 12345L;
        String uuid = "user-uuid";
        String startCommand = "/start " + uuid;

        var apiUser = getTelegramAPIUser(chatId);
        apiUser.setUserName("testUser");
        apiUser.setFirstName("Test");
        apiUser.setLastName("User");

        Message message = new Message();
        message.setFrom(apiUser);
        message.setText(startCommand);

        Update update = new Update();
        update.setMessage(message);

        TelegramChat existingChat = TelegramChat.builder()
            .id(1L)
            .chatId(chatId.toString())
            .build();
        when(telegramChatRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.of(existingChat));

        User mockUser = new User();
        mockUser.setUuid(uuid);
        when(userRepository.findUserByUuid(uuid))
            .thenReturn(Optional.of(mockUser));

        when(userRemoteClient.findUserLanguageByUuid(uuid))
            .thenThrow(new RuntimeException("Service unavailable"));

        telegramService.processUpdate(update);

        ArgumentCaptor<TelegramChat> chatCaptor = ArgumentCaptor.forClass(TelegramChat.class);
        verify(telegramChatRepository).save(chatCaptor.capture());

        TelegramChat savedChat = chatCaptor.getValue();
        assertEquals(mockUser, savedChat.getUser());
        assertEquals("uk", savedChat.getLanguageCode());
    }

    private static org.telegram.telegrambots.meta.api.objects.User getTelegramAPIUser(Long chatId) {
        org.telegram.telegrambots.meta.api.objects.User apiUser = new org.telegram.telegrambots.meta.api.objects.User();
        apiUser.setId(chatId);
        apiUser.setUserName("testUser");
        return apiUser;
    }

    @Test
    void testSendMessageToUser_FileTooLarge_ShouldThrowException() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getName()).thenReturn("large_file.pdf");
        when(file.getSize()).thenReturn(51L * 1024 * 1024);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> telegramService.sendMessageToUser(request, new MultipartFile[] {file}));

        assertEquals("File size exceeds Telegram bot limit (50MB)", exception.getMessage());

        verify(telegramChatRepository).findById(1L);
        verifyNoInteractions(userRemoteWebClient);
        verifyNoInteractions(executor);
        verify(telegramMessageRepository, never()).save(any());
    }

    @Test
    void testSendMessageToUser_FileAssetType_ShouldSendDocument() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        when(executor.executeSendFile(any(SendDocument.class))).thenReturn(mockTelegramResponse(10));
        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(file.getOriginalFilename()).thenReturn("doc.pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");

        telegramService.sendMessageToUser(request, new MultipartFile[] {file});

        verify(executor).executeSendFile(any(SendDocument.class));
        verify(telegramMessageRepository).save(any());
    }

    @Test
    void testSendMessageToUser_WhenFileSendingFails_ShouldThrowTelegramBotExecutionException() throws IOException {
        Long chatId = 123L;
        CreateTelegramMessageRequest request =
            new CreateTelegramMessageRequest(chatId, "test caption");

        TelegramChat chat = new TelegramChat();
        chat.setChatId(chatId.toString());

        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.getContentType()).thenReturn("application/pdf");
        when(badFile.getInputStream()).thenThrow(new IOException("fake IO fail"));

        MultipartFile[] files = new MultipartFile[] {badFile};

        assertThrows(TelegramBotExecutionException.class,
            () -> telegramService.sendMessageToUser(request, files));
    }

    @Test
    void testMarkMessagesAsRead_IdsSpecified_MessagesMarkedAsRead() {
        List<Long> messageIds = List.of(1L, 2L);

        TelegramChat chat1 = TelegramChat.builder()
            .id(10L)
            .unreadMessagesCount(1)
            .build();

        TelegramChat chat2 = TelegramChat.builder()
            .id(20L)
            .unreadMessagesCount(0)
            .build();

        TelegramMessage message1 = TelegramMessage.builder()
            .id(1L)
            .messageViewingStatus(MessageViewingStatus.UNREAD)
            .chat(chat1)
            .build();

        TelegramMessage message2 = TelegramMessage.builder()
            .id(2L)
            .messageViewingStatus(MessageViewingStatus.READ)
            .chat(chat2)
            .build();

        when(telegramMessageRepository.findAllById(messageIds))
            .thenReturn(List.of(message1, message2));

        telegramService.markMessagesAsRead(
            MarkMessagesAsReadRequestDto.builder().messagesIds(messageIds).build());

        verify(telegramMessageRepository).findAllById(messageIds);
        verify(telegramMessageRepository).saveAll(List.of(message1, message2));

        assertEquals(MessageViewingStatus.READ, message1.getMessageViewingStatus());
        assertEquals(MessageViewingStatus.READ, message2.getMessageViewingStatus());

        assertEquals(0, chat1.getUnreadMessagesCount());
        assertEquals(0, chat2.getUnreadMessagesCount());
    }

    @Test
    void editManagerMessage_shouldThrowNotFoundException_whenChatNotFound() {
        EditTelegramMessageRequest request = new EditTelegramMessageRequest(1L, 100L, "new text");

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> telegramService.editManagerMessage(request));
    }

    @Test
    void editManagerMessage_shouldThrowEntityNotFound_whenMessageNotFound() {
        EditTelegramMessageRequest request = new EditTelegramMessageRequest(1L, 100L, "new text");
        TelegramChat chat = new TelegramChat();
        chat.setId(1L);

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(telegramMessageRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> telegramService.editManagerMessage(request));
    }

    @Test
    void editManagerMessage_shouldDoNothing_whenMessageNotFromManager() {
        EditTelegramMessageRequest request = new EditTelegramMessageRequest(1L, 100L, "new text");
        TelegramChat chat = new TelegramChat();
        chat.setId(1L);

        TelegramMessage message = new TelegramMessage();
        message.setFromManager(false);

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(telegramMessageRepository.findById(100L)).thenReturn(Optional.of(message));

        telegramService.editManagerMessage(request);

        verify(telegramMessageRepository, never()).save(any());
    }

    @Test
    void editManagerMessage_shouldUpdateCaption_whenMessageHasAssets() {
        EditTelegramMessageRequest request = new EditTelegramMessageRequest(1L, 100L, "new text");
        TelegramChat chat = new TelegramChat();
        chat.setId(1L);
        chat.setChatId("12345");

        TelegramMessage message = new TelegramMessage();
        message.setId(100L);
        message.setFromManager(true);
        message.setTelegramMessageId(777);
        message.setAssets(List.of(new MessageAsset()));

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(telegramMessageRepository.findById(100L)).thenReturn(Optional.of(message));

        telegramService.editManagerMessage(request);

        verify(executor).executeCommand(any(EditMessageCaption.class));
        verify(telegramMessageRepository).save(message);
    }

    @Test
    void editManagerMessage_shouldUpdateText_whenMessageWithoutAssets() {
        EditTelegramMessageRequest request = new EditTelegramMessageRequest(1L, 100L, "new text");
        TelegramChat chat = new TelegramChat();
        chat.setId(1L);
        chat.setChatId("12345");

        TelegramMessage message = new TelegramMessage();
        message.setId(100L);
        message.setFromManager(true);
        message.setTelegramMessageId(888);
        message.setAssets(Collections.emptyList());

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(telegramMessageRepository.findById(100L)).thenReturn(Optional.of(message));

        telegramService.editManagerMessage(request);

        verify(executor).executeCommand(any(EditMessageText.class));
        verify(telegramMessageRepository).save(message);
    }

    @Test
    void editManagerMessage_shouldUpdateChatLastMessage_whenEditedMessageIsLastMessage() {
        EditTelegramMessageRequest request = new EditTelegramMessageRequest(1L, 100L, "new text");
        TelegramChat chat = new TelegramChat();
        chat.setId(1L);
        chat.setChatId("12345");

        TelegramMessage message = new TelegramMessage();
        message.setId(100L);
        message.setFromManager(true);
        message.setTelegramMessageId(999);
        message.setAssets(Collections.emptyList());

        chat.setLastMessage(message);

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(telegramMessageRepository.findById(100L)).thenReturn(Optional.of(message));

        telegramService.editManagerMessage(request);

        verify(telegramChatRepository).save(chat);
    }

    @Test
    void editManagerMessage_shouldNotUpdateChat_whenEditedMessageIsNotLastMessage() {
        EditTelegramMessageRequest request = new EditTelegramMessageRequest(1L, 200L, "new text");
        TelegramChat chat = new TelegramChat();
        chat.setId(1L);
        chat.setChatId("12345");

        TelegramMessage lastMessage = new TelegramMessage();
        lastMessage.setId(999L);
        chat.setLastMessage(lastMessage);

        TelegramMessage message = new TelegramMessage();
        message.setId(200L);
        message.setFromManager(true);
        message.setTelegramMessageId(777);
        message.setAssets(Collections.emptyList());

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(telegramMessageRepository.findById(200L)).thenReturn(Optional.of(message));

        telegramService.editManagerMessage(request);

        verify(telegramChatRepository, never()).save(chat);
    }

    @Test
    void sendMessageToUser_shouldThrowIOException() {
        TelegramChat chat = new TelegramChat();
        chat.setChatId("123");
        when(telegramChatRepository.findById(any())).thenReturn(Optional.of(chat));

        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);

        try (MockedStatic<MessageFactory> mf = mockStatic(MessageFactory.class)) {
            mf.when(() -> MessageFactory.createSendPhoto(anyString(), any(), any())).thenThrow(IOException.class);

            assertThrows(RuntimeException.class,
                () -> telegramService.sendMessageToUser(request, new MultipartFile[] {file}));
        }
    }

    @Test
    void sendMessageToUser_WithMultipleImages_ShouldUseSendAsMediaGroup() throws IOException {
        Long chatId = 123L;

        TelegramChat chat = new TelegramChat();
        chat.setChatId(chatId.toString());
        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        CreateTelegramMessageRequest request =
            new CreateTelegramMessageRequest(chatId, "Test caption for group");

        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        MultipartFile image1 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("img1.png");
        when(image1.getSize()).thenReturn((long) imageBytes.length);
        when(image1.getContentType()).thenReturn("image/png");
        when(image1.getInputStream()).thenReturn(new ByteArrayInputStream(imageBytes));

        MultipartFile image2 = mock(MultipartFile.class);
        when(image2.getOriginalFilename()).thenReturn("img2.png");
        when(image2.getSize()).thenReturn((long) imageBytes.length);
        when(image2.getContentType()).thenReturn("image/png");
        when(image2.getInputStream()).thenReturn(new ByteArrayInputStream(imageBytes));

        MultipartFile[] files = new MultipartFile[] {image1, image2};

        try (MockedStatic<MessageFactory> mf = mockStatic(MessageFactory.class)) {
            SendMediaGroup fakeGroup = new SendMediaGroup();
            mf.when(() -> MessageFactory.buildSendMediaGroup(anyString(), anyList(), any()))
                .thenReturn(fakeGroup);

            Message sentMsg1 = new Message();
            sentMsg1.setMessageId(101);
            sentMsg1.setMediaGroupId("mg1");

            Message sentMsg2 = new Message();
            sentMsg2.setMessageId(102);
            sentMsg2.setMediaGroupId("mg1");

            when(executor.executeSendMediaGroup(fakeGroup))
                .thenReturn(List.of(sentMsg1, sentMsg2));

            telegramService.sendMessageToUser(request, files);
        }

        verify(executor).executeSendMediaGroup(any(SendMediaGroup.class));

        ArgumentCaptor<TelegramMessage> messageCaptor = ArgumentCaptor.forClass(TelegramMessage.class);
        verify(telegramMessageRepository).save(messageCaptor.capture());
        TelegramMessage savedMessage = messageCaptor.getValue();
        assertThat(savedMessage.getTelegramMessageId()).isEqualTo(101);
        assertThat(savedMessage.getMediaGroupId()).isEqualTo("mg1");

        ArgumentCaptor<List<MessageAsset>> assetCaptor = ArgumentCaptor.forClass(List.class);
        verify(messageAssetRepository, atLeastOnce()).saveAll(assetCaptor.capture());
        List<MessageAsset> savedAssets = assetCaptor.getValue();

        assertThat(savedAssets).hasSize(2);
        assertThat(savedAssets.get(0).getTelegramMessageId()).isEqualTo(101);
        assertThat(savedAssets.get(1).getTelegramMessageId()).isEqualTo(102);
    }

    @Test
    void sendMessageToUser_TextOnly_ShouldCallSendMessage() {
        Long chatId = 1L;

        TelegramChat chat = new TelegramChat();
        chat.setChatId(chatId.toString());
        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(chatId);
        request.setText("Just text");

        telegramService.sendMessageToUser(request, null);

        verify(executor).executeSendMessage(any(SendMessage.class));
        verify(telegramMessageRepository).save(any(TelegramMessage.class));
        verify(telegramChatRepository).save(any(TelegramChat.class));
    }

    @Test
    void sendMessageToUser_FilesNotImagesOrOthers_ShouldSendTextOnly() {
        Long chatId = 1L;

        TelegramChat chat = new TelegramChat();
        chat.setChatId(chatId.toString());
        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(chatId);
        request.setText("Text with non-image files");

        MultipartFile nonImageFile = mock(MultipartFile.class);
        when(nonImageFile.getOriginalFilename()).thenReturn("file.bin");
        when(nonImageFile.getSize()).thenReturn(1024L);
        when(nonImageFile.getContentType()).thenReturn("application/octet-stream");

        Message sentDoc = new Message();
        sentDoc.setMessageId(999);
        when(executor.executeSendFile(any(SendDocument.class))).thenReturn(sentDoc);

        when(userRemoteWebClient.uploadFile(nonImageFile)).thenReturn("http://fakeurl");

        telegramService.sendMessageToUser(request, new MultipartFile[] {nonImageFile});

        verify(userRemoteWebClient).uploadFile(nonImageFile);
        verify(executor).executeSendFile(any(SendDocument.class));

        ArgumentCaptor<TelegramMessage> messageCaptor = ArgumentCaptor.forClass(TelegramMessage.class);
        verify(telegramMessageRepository).save(messageCaptor.capture());
        TelegramMessage savedMessage = messageCaptor.getValue();
        assertThat(savedMessage.getTelegramMessageId()).isEqualTo(999);

        ArgumentCaptor<MessageAsset> assetCaptor = ArgumentCaptor.forClass(MessageAsset.class);
        verify(messageAssetRepository).save(assetCaptor.capture());
        MessageAsset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getTelegramMessageId()).isEqualTo(999);

        verify(telegramChatRepository).save(chat);
    }

    @Test
    void sendMessageToUser_WhenFileSendingThrowsIOException_ShouldWrapInTelegramBotExecutionException()
        throws IOException {
        Long chatId = 1L;

        TelegramChat chat = new TelegramChat();
        chat.setChatId(chatId.toString());
        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(chatId);
        request.setText("Text");

        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.getContentType()).thenReturn("image/png");

        when(badFile.getInputStream()).thenThrow(new IOException("fake IO fail"));

        assertThrows(TelegramBotExecutionException.class,
            () -> telegramService.sendMessageToUser(request, new MultipartFile[] {badFile}));

        verify(telegramMessageRepository, never()).save(any());
        verify(messageAssetRepository, never()).save(any());
    }

    @Test
    void sendMessageToUser_WhenSendDocumentReturnsNull_ShouldNotThrowNPE() {
        Long chatId = 1L;

        TelegramChat chat = new TelegramChat();
        chat.setChatId(chatId.toString());
        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(chatId);
        request.setText("Text");

        when(file.getOriginalFilename()).thenReturn("file.bin");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/octet-stream");

        when(userRemoteWebClient.uploadFile(file)).thenReturn("http://fakeurl");

        Message fakeMessage = new Message();
        fakeMessage.setMessageId(123);
        when(executor.executeSendFile(any(SendDocument.class))).thenReturn(fakeMessage);

        telegramService.sendMessageToUser(request, new MultipartFile[] {file});

        verify(userRemoteWebClient).uploadFile(file);
        verify(executor).executeSendFile(any(SendDocument.class));
        verify(telegramMessageRepository).save(any());
        verify(messageAssetRepository).save(any());
        verify(telegramChatRepository).save(chat);
    }

    @Test
    void testToggleNotifications_UserNotFoundByUuid_ShouldThrowException() {
        String uuid = "some-uuid";
        ToggleNotificationsRequestDto requestDto = ToggleNotificationsRequestDto
            .builder()
            .isNotify(true)
            .build();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> telegramService.toggleNotifications(uuid, requestDto));

        verify(userRepository).findUserByUuid(uuid);
        verifyNoInteractions(telegramChatRepository);
    }

    @Test
    void testToggleNotifications_UserWithoutChat_ShouldThrowException() {
        String uuid = "some-uuid";
        ToggleNotificationsRequestDto requestDto = ToggleNotificationsRequestDto
            .builder()
            .isNotify(true)
            .build();

        User user = User.builder()
            .uuid(uuid)
            .build();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class,
            () -> telegramService.toggleNotifications(uuid, requestDto));

        verify(userRepository).findUserByUuid(uuid);
        verifyNoInteractions(telegramChatRepository);
    }

    @Test
    void testToggleNotifications_UserWithChat_NotificationsToggled() {
        String uuid = "some-uuid";
        ToggleNotificationsRequestDto requestDto = ToggleNotificationsRequestDto
            .builder()
            .isNotify(true)
            .build();

        TelegramChat telegramChat = TelegramChat
            .builder()
            .id(1L)
            .build();

        User user = User.builder()
            .uuid(uuid)
            .telegramBot(telegramChat)
            .build();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));

        telegramService.toggleNotifications(uuid, requestDto);

        verify(userRepository).findUserByUuid(uuid);
        verify(telegramChatRepository).save(telegramChat);
    }

    @Test
    void testGetIsNotificationsEnabled_UserNotFoundByUuid_ShouldThrowException() {
        String uuid = "some-uuid";

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> telegramService.getIsNotificationsEnabled(uuid));

        verify(userRepository).findUserByUuid(uuid);
        verifyNoInteractions(telegramChatRepository);
    }

    @Test
    void testGetIsNotificationsEnabled_UserWithoutChat_ShouldThrowException() {
        String uuid = "some-uuid";

        User user = User.builder()
            .uuid(uuid)
            .build();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class,
            () -> telegramService.getIsNotificationsEnabled(uuid));

        verify(userRepository).findUserByUuid(uuid);
        verifyNoInteractions(telegramChatRepository);
    }

    @Test
    void testGetIsNotificationsEnabled_UserWithChat_NotificationsToggled() {
        String uuid = "some-uuid";

        TelegramChat telegramChat = TelegramChat
            .builder()
            .id(1L)
            .isNotify(true)
            .build();

        User user = User.builder()
            .uuid(uuid)
            .telegramBot(telegramChat)
            .build();

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));

        boolean result = telegramService.getIsNotificationsEnabled(uuid);

        verify(userRepository).findUserByUuid(uuid);
        assertEquals(user.getTelegramBot().getIsNotify(), result);
    }

    private Message mockTelegramResponse(int id) {
        Message m = new Message();
        m.setMessageId(id);
        return m;
    }
}