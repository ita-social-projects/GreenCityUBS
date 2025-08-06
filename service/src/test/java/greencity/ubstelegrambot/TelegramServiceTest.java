package greencity.ubstelegrambot;

import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.CreateTelegramMessageRequest;
import greencity.dto.telegram.MessageAssetDto;
import greencity.dto.telegram.TelegramMessageDto;
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
import greencity.repository.EmployeeRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.AzureCloudStorageService;
import greencity.service.ubs.TelegramNotificationService;
import greencity.service.ubs.TelegramUpdateProcessor;
import greencity.service.ubs.UBSClientService;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.service.TelegramExecutor;
import greencity.ubstelegrambot.service.TelegramServiceImpl;
import greencity.ubstelegrambot.service.TelegramUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramServiceTest {
    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private AzureCloudStorageService azureCloudStorageService;

    @Mock
    private TelegramMessageRepository telegramMessageRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UBSClientService ubsClientService;

    @Mock
    private TelegramExecutor executor;

    @Mock
    private UBSTelegramBot bot;

    @Mock
    private MultipartFile file;

    @Mock
    private TelegramNotificationService telegramNotificationService;

    @Mock
    private TelegramManagerRepository telegramManagerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TelegramUtils telegramUtils;

    private TelegramServiceImpl telegramService;

    public Map<String, TelegramUpdateProcessor> telegramUpdateProcessorMap;

    @BeforeEach
    void setUp() {
        telegramUpdateProcessorMap = new HashMap<>();

        telegramService = new TelegramServiceImpl(
            telegramMessageRepository,
            telegramManagerRepository,
            applicationContext,
            telegramChatRepository,
            azureCloudStorageService,
            ubsClientService,
            executor,
            employeeRepository,
            orderRepository,
            userRepository,
            telegramNotificationService,
            telegramUtils,
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
        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);

        telegramService.sendMessageToUser(request, null);

        verify(executor).executeCommand(eq(bot), any(SendMessage.class));
        verify(telegramMessageRepository).save(any(TelegramMessage.class));
    }

    @Test
    void testSendMessageToUser_FileUnknownContentType_MessageSentAndPhotoUploaded() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);
        request.setText("Hi");

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);
        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(file.getOriginalFilename()).thenReturn("image.svg");
        when(file.getSize()).thenReturn(2048L);
        when(file.getContentType()).thenReturn(null);
        when(azureCloudStorageService.upload(file)).thenReturn("http://image");

        telegramService.sendMessageToUser(request, new MultipartFile[] {file});

        verify(azureCloudStorageService).upload(file);
        verify(executor).executeSendFile(eq(bot), any(SendDocument.class));
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

        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);
        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getSize()).thenReturn(2048L);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getInputStream()).thenReturn(bais);
        when(azureCloudStorageService.upload(file)).thenReturn("http://image");

        telegramService.sendMessageToUser(request, new MultipartFile[] {file});

        verify(azureCloudStorageService).upload(file);
        verify(executor).executeSendFile(eq(bot), any(SendDocument.class));
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

        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);
        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getSize()).thenReturn(2048L);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getInputStream()).thenReturn(bais);
        when(azureCloudStorageService.upload(file)).thenReturn("http://image");

        telegramService.sendMessageToUser(request, new MultipartFile[] {file});

        verify(azureCloudStorageService).upload(file);
        verify(executor).executeSendPhoto(eq(bot), any(SendPhoto.class));
    }

    @Test
    void testSendMessageToUser_WrongChatId_NotFoundExceptionThrown() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(99L);

        when(telegramChatRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> telegramService.sendMessageToUser(request, null));

        verify(telegramMessageRepository, never()).save(any());
    }

    @Test
    void testFindUserMessageByChatId_MessagesFound_PageableDtoReturned() {

        Long chatId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        TelegramMessage message = TelegramMessage.builder()
            .id(100L)
            .text("Hello")
            .sendAt(LocalDateTime.now())
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

        assertEquals("There are no messages in the chat 1", exception.getMessage());
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

        TelegramChat chat = TelegramChat.builder()
            .id(1L)
            .chatId("123456789")
            .firstName("Test")
            .lastName("User")
            .username("testuser")
            .user(user)
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
            .sendAt(LocalDateTime.now())
            .fromManager(true)
            .status(MessageDeliveryStatus.SENT)
            .assets(List.of(asset))
            .build();

        Page<TelegramChat> chatPage = new PageImpl<>(List.of(chat), pageable, 1);

        when(telegramChatRepository.findAll((ArgumentMatchers.<Specification<TelegramChat>>any()), eq(pageable)))
            .thenReturn(chatPage);
        when(telegramMessageRepository.findFirstByChatOrderBySendAtDesc(chat)).thenReturn(Optional.of(message));

        PageableDto<ChatDto> result = telegramService.getChats(searchTerm, pageable);

        assertEquals(0, result.getCurrentPage());
        assertEquals(1, result.getTotalElements());

        ChatDto chatDto = result.getPage().getFirst();
        assertEquals("Test", chatDto.getFirstName());
        Assertions.assertNotNull(chatDto.getUser());
        assertEquals("ivan@example.com", chatDto.getUser().getEmail());

        TelegramMessageDto lastMessage = chatDto.getLastMessage();
        Assertions.assertNotNull(lastMessage);
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
        when(telegramMessageRepository.findFirstByChatOrderBySendAtDesc(chat)).thenReturn(Optional.empty());

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

        Assertions.assertNotNull(result);
        assertEquals(Optional.of(100L).get(), result.getId());
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

        Assertions.assertNotNull(result);
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
    void processUpdate_shouldReturnUserProcessor_whenStartWithoutUuid() {
        Long chatId = 12345L;
        String startCommand = "/start";

        var from = getTelegramAPIUser(chatId);

        Message message = new Message();
        message.setFrom(from);
        message.setText(startCommand);

        Update update = new Update();
        update.setMessage(message);

        doNothing().when(telegramNotificationService).notifyNewChat(any(ChatDto.class));

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
        SendMessage result = telegramService.processUpdate(update).process(update);

        // assert
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());
    }

    @Test
    void processUpdate_shouldReturnManagerProcessor_whenStartWithManagerUuidAndChatExists() {
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
        SendMessage result = telegramService.processUpdate(update).process(update);

        // assert
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());
    }

    @Test
    void processUpdate_shouldReturnUserProcessor_whenNoStartCommand() {
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
            .chatStateUpdatedAt(LocalDateTime.now().minusMinutes(15))
            .build();

        when(telegramChatRepository.findByChatId(chatId.toString())).thenReturn(Optional.of(telegramChat));
        when(telegramManagerRepository.findByChatId(chatId.toString())).thenReturn(Optional.empty());

        TelegramUpdateProcessor userProcessor = mock(TelegramUpdateProcessor.class);
        telegramUpdateProcessorMap.put("userUpdateProcessor", userProcessor);

        SendMessage expected = new SendMessage(chatId.toString(), "default user reply");
        when(userProcessor.process(update)).thenReturn(expected);

        // act
        SendMessage result = telegramService.processUpdate(update).process(update);

        // assert
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());

        verify(telegramChatRepository).save(telegramChat);
    }

    @Test
    void processUpdate_shouldReturnManagerProcessor_whenCallbackFromManager() {
        // arrange
        Long chatId = 12345L;

        var telegramUser = getTelegramAPIUser(chatId);

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(telegramUser);

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
        SendMessage result = telegramService.processUpdate(update).process(update);

        // assert
        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());
    }

    @Test
    void processUpdate_shouldReturnUserProcessor_whenStartWithUserUuidAndChatExists() {
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

        SendMessage result = telegramService.processUpdate(update).process(update);

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

        SendMessage result = telegramService.processUpdate(update).process(update);

        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());

        TelegramChat savedChat = chatCaptor.getValue();
        assertEquals(chatId.toString(), savedChat.getChatId());
        assertEquals("TestFirst", savedChat.getFirstName());
        assertEquals("TestLast", savedChat.getLastName());
        Assertions.assertNull(savedChat.getUser());
    }

    @Test
    void processUpdate_shouldReturnUserProcessor_whenMessageTextIsNull() {
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
            .chatStateUpdatedAt(LocalDateTime.now().minusMinutes(15))
            .build();

        when(telegramChatRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.of(telegramChat));

        when(telegramManagerRepository.findByChatId(chatId.toString()))
            .thenReturn(Optional.empty());

        TelegramUpdateProcessor userProcessor = mock(TelegramUpdateProcessor.class);
        telegramUpdateProcessorMap.put("userUpdateProcessor", userProcessor);

        SendMessage expected = new SendMessage(chatId.toString(), "default user reply");
        when(userProcessor.process(update)).thenReturn(expected);

        SendMessage result = telegramService.processUpdate(update).process(update);

        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());

        verify(telegramChatRepository).save(telegramChat);
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

        SendMessage result = telegramService.processUpdate(update).process(update);

        assertEquals(expected.getText(), result.getText());
        assertEquals(expected.getChatId(), result.getChatId());

        verify(userRepository, never()).findUserByUuid(any());
        verify(telegramChatRepository, never()).save(any());
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
        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);
        when(file.getName()).thenReturn("large_file.pdf");
        when(file.getSize()).thenReturn(51L * 1024 * 1024);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> telegramService.sendMessageToUser(request, new MultipartFile[] {file}));

        assertEquals("File size exceeds Telegram bot limit (50MB)", exception.getMessage());

        verifyNoInteractions(azureCloudStorageService);
        verifyNoInteractions(executor);
        verify(telegramMessageRepository, never()).save(any());
    }

    @Test
    void testSendMessageToUser_FileAssetType_ShouldSendDocument() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);
        when(file.getOriginalFilename()).thenReturn("doc.pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");

        telegramService.sendMessageToUser(request, new MultipartFile[] {file});

        verify(executor).executeSendFile(eq(bot), any(SendDocument.class));
        verify(telegramMessageRepository).save(any());
    }

    @Test
    void testSendMessageToUser_FileAssetType_WhenSendFails_ShouldThrowRuntimeException() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);
        when(file.getOriginalFilename()).thenReturn("doc.pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");

        try (MockedStatic<MessageFactory> messageFactoryMock = mockStatic(MessageFactory.class)) {
            messageFactoryMock
                .when(() -> MessageFactory.createSendDocument(anyString(), any(MultipartFile.class)))
                .thenThrow(new IOException("Simulated IO error"));

            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> telegramService.sendMessageToUser(request, new MultipartFile[] {file}));

            assertTrue(exception.getMessage().contains("Unable to send file to Telegram"));
            verify(executor, never()).executeSendFile(any(), any());
            verify(telegramMessageRepository, never()).save(any());
        }
    }

    @Test
    void testProcessUpdate_ChatNotExistsUuidIsEmpty_ChatCreatedUserUpdateProcessorReturned() {

    }

    @Test
    void testProcessUpdate_ChatExistsUuidIsEmpty_UserUpdateProcessorReturned() {

    }

    @Test
    void testProcessUpdate_ChatNotExistsUuidIsPresentIsManagerUuid_ManagerUpdateProcessorReturned() {

    }

    @Test
    void testProcessUpdate_ChatNotExistsUuidIsPresentIsUserUuid_UserUpdateProcessorReturned() {

    }

    @Test
    void testMarkMessagesAsRead() {
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
            MarkMessagesAsReadRequest.builder().messagesIds(messageIds).build());

        verify(telegramMessageRepository).findAllById(messageIds);
        verify(telegramMessageRepository).saveAll(List.of(message1, message2));

        assertEquals(MessageViewingStatus.READ, message1.getMessageViewingStatus());
        assertEquals(MessageViewingStatus.READ, message2.getMessageViewingStatus());

        assertEquals(0, chat1.getUnreadMessagesCount());
        assertEquals(0, chat2.getUnreadMessagesCount());
    }

}