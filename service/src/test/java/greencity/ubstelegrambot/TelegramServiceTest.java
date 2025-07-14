package greencity.ubstelegrambot;

import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.*;
import greencity.entity.order.Order;
import greencity.entity.telegram.ChatFeedback;
import greencity.entity.telegram.MessageAsset;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import greencity.entity.user.User;
import greencity.enums.AssetType;
import greencity.enums.MessageDeliveryStatus;
import greencity.exceptions.NotFoundException;
import greencity.repository.ChatFeedbackRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.service.ubs.AzureCloudStorageService;
import greencity.service.ubs.UBSClientService;
import greencity.ubstelegrambot.service.TelegramExecutor;
import greencity.ubstelegrambot.service.TelegramServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramServiceTest {

    @InjectMocks
    private TelegramServiceImpl telegramService;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private AzureCloudStorageService azureCloudStorageService;

    @Mock
    private TelegramMessageRepository telegramMessageRepository;

    @Mock
    private ChatFeedbackRepository chatFeedbackRepository;

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
    void testSendMessageToUser_OnlyFile_MessageSentAndPhotoUploaded() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/png");
        when(azureCloudStorageService.upload(file)).thenReturn("http://azure.com/image.png");

        telegramService.sendMessageToUser(request, new MultipartFile[] {file});

        verify(azureCloudStorageService).upload(file);
        verify(executor).executeSendPhoto(eq(bot), any(SendPhoto.class));
        verify(telegramMessageRepository).save(any(TelegramMessage.class));
    }

    @Test
    void testSendMessageToUser_TextAndFile_MessageSentAndPhotoUploaded() {
        CreateTelegramMessageRequest request = new CreateTelegramMessageRequest();
        request.setChatId(1L);
        request.setText("Hi");

        TelegramChat chat = new TelegramChat();
        chat.setChatId("123456");

        when(telegramChatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getSize()).thenReturn(2048L);
        when(file.getContentType()).thenReturn("image/png");
        when(azureCloudStorageService.upload(file)).thenReturn("http://image");

        telegramService.sendMessageToUser(request, new MultipartFile[] {file});

        verify(executor).executeCommand(eq(bot), any(SendMessage.class));
        verify(executor).executeSendPhoto(eq(bot), any(SendPhoto.class));
        verify(telegramMessageRepository).save(any(TelegramMessage.class));
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

        Assertions.assertEquals(0, result.getCurrentPage());
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(1, result.getTotalPages());

        TelegramMessageDto dto = result.getPage().getFirst();
        Assertions.assertEquals("Hello", dto.getText());
        Assertions.assertFalse(dto.getFromManager());
        Assertions.assertEquals(MessageDeliveryStatus.SENT, dto.getDeliveryStatus());
        Assertions.assertEquals(1, dto.getAssets().size());

        MessageAssetDto assetDto = dto.getAssets().getFirst();
        Assertions.assertEquals("http://example.com/file.png", assetDto.getUrl());
        Assertions.assertEquals("file.png", assetDto.getFileName());
        Assertions.assertEquals(AssetType.IMAGE, assetDto.getType());
        Assertions.assertEquals(Optional.of(1024L).get(), assetDto.getSize());
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

        Assertions.assertEquals("There are no messages in the chat 1", exception.getMessage());
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

        when(telegramChatRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(chatPage);
        when(telegramMessageRepository.findFirstByChatOrderBySendAtDesc(chat)).thenReturn(Optional.of(message));

        PageableDto<ChatDto> result = telegramService.getChats(searchTerm, pageable);

        Assertions.assertEquals(0, result.getCurrentPage());
        Assertions.assertEquals(1, result.getTotalElements());

        ChatDto chatDto = result.getPage().get(0);
        Assertions.assertEquals("123456789", chatDto.getChatId());
        Assertions.assertEquals("Test", chatDto.getFirstName());
        Assertions.assertNotNull(chatDto.getUser());
        Assertions.assertEquals("ivan@example.com", chatDto.getUser().getEmail());

        TelegramMessageDto lastMessage = chatDto.getLastMessage();
        Assertions.assertNotNull(lastMessage);
        Assertions.assertEquals("Hello", lastMessage.getText());
        Assertions.assertEquals(1, lastMessage.getAssets().size());
        Assertions.assertEquals("http://image.png", lastMessage.getAssets().get(0).getUrl());
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

        when(telegramChatRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(chatPage);
        when(telegramMessageRepository.findFirstByChatOrderBySendAtDesc(chat)).thenReturn(Optional.empty());

        // when
        PageableDto<ChatDto> result = telegramService.getChats(searchTerm, pageable);

        Assertions.assertEquals(0, result.getCurrentPage());
        Assertions.assertEquals(1, result.getTotalElements());

        ChatDto chatDto = result.getPage().getFirst();
        Assertions.assertNull(chatDto.getUser());
        Assertions.assertNull(chatDto.getLastMessage());
    }

    @Test
    void testGetChats_EmptyResult_EmptyPageableDtoReturned() {
        Pageable pageable = PageRequest.of(0, 10);
        when(telegramChatRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty());

        PageableDto<ChatDto> result = telegramService.getChats("nothing", pageable);

        Assertions.assertTrue(result.getPage().isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());
        Assertions.assertEquals(1, result.getTotalPages());
    }

    @Test
    void testGetAllFeedbacksByChatId_FeedbacksFound_FeedbackDtoReturned() {
        String chatId = "123456789";
        Pageable pageable = PageRequest.of(0, 5);

        TelegramChat chat = TelegramChat.builder()
            .chatId(chatId)
            .build();

        ChatFeedback feedback = ChatFeedback.builder()
            .id(1L)
            .chat(chat)
            .rating(5)
            .comment("Great service")
            .build();

        Page<ChatFeedback> feedbackPage = new PageImpl<>(List.of(feedback), pageable, 1);

        when(chatFeedbackRepository.findByChatIdPageable(chatId, pageable)).thenReturn(feedbackPage);

        PageableDto<FeedbackDto> result = telegramService.getAllFeedbacksByChatId(chatId, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(1, result.getPage().size());

        FeedbackDto dto = result.getPage().getFirst();
        Assertions.assertEquals(Optional.of(1L).get(), dto.id());
        Assertions.assertEquals("123456789", dto.chatId());
        Assertions.assertEquals(5, dto.rating());
        Assertions.assertEquals("Great service", dto.comment());
    }

    @Test
    void testGetAllFeedbacksByChatId_ChatNotFound_EmptyPageableDtoReturned() {
        String chatId = "999999999";
        Pageable pageable = PageRequest.of(0, 5);
        when(chatFeedbackRepository.findByChatIdPageable(chatId, pageable)).thenReturn(Page.empty());

        PageableDto<FeedbackDto> result = telegramService.getAllFeedbacksByChatId(chatId, pageable);

        Assertions.assertTrue(result.getPage().isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());
        Assertions.assertEquals(1, result.getTotalPages());
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
        Assertions.assertEquals(Optional.of(100L).get(), result.getId());
    }

    @Test
    void testGetLastOrderByChatId_ChatNotFound_NotFoundExceptionThrown() {
        Long chatId = 2L;
        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> telegramService.getLastOrderByChatId(chatId));

        Assertions.assertEquals("Chat with id 2 not found", exception.getMessage());
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

        Assertions.assertEquals("Order not found", exception.getMessage());
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

        Assertions.assertEquals("Order not found", exception.getMessage());
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
        Assertions.assertEquals(chatId, result.getId());
        Assertions.assertEquals("123456789", result.getChatId());
        Assertions.assertEquals("Іван", result.getFirstName());
        Assertions.assertEquals("Петренко", result.getLastName());
        Assertions.assertEquals("ivan_pet", result.getUsername());
    }

    @Test
    void testGetChatById_ChatNotFound_NotFoundExceptionThrown() {
        Long chatId = 99L;
        when(telegramChatRepository.findById(chatId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> telegramService.getChatById(chatId));

        Assertions.assertEquals("Chat with id 99 not found", exception.getMessage());
    }
}