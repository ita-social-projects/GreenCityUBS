package greencity.controller;

import greencity.client.UserRemoteClient;
import greencity.converters.UserArgumentResolver;
import greencity.service.ubs.TelegramPhotoService;
import greencity.service.ubs.TelegramService;
import greencity.service.ubs.TelegramStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;

import static greencity.ModelUtils.getPrincipal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TelegramControllerTest {

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock
    private TelegramService telegramService;

    @Mock
    private TelegramPhotoService telegramPhotoService;

    @Mock
    private TelegramStreamingService telegramStreamingService;

    @InjectMocks
    private TelegramController telegramChatController;

    private MockMvc mockMvc;

    private static final String baseUrl = "/ubs/telegram";
    private String chatId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(telegramChatController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .build();
        chatId = "123";
    }

    @Test
    void getUserMessagesTest() throws Exception {
        mockMvc.perform(get(baseUrl + "/user-messages/" + chatId))
            .andExpect(status().isOk());

        verify(telegramService).findUserMessageByChatId(
            eq(chatId),
            any(PageRequest.class));
    }

    @Test
    void getUserPhotosTest() throws Exception {
        mockMvc.perform(get(baseUrl + "/user-photos/" + chatId))
            .andExpect(status().isOk());

        verify(telegramService).findUserPhotosByChatId(
            eq(chatId),
            any(PageRequest.class));
    }

    @Test
    void getAllAuthoredUsersTest() throws Exception {
        mockMvc.perform(get(baseUrl + "/get-all-authorized-users"))
            .andExpect(status().isOk());

        verify(telegramService).getAllUsers(any(PageRequest.class));
    }

    @Test
    void getAllUnknownUsersTest() throws Exception {
        mockMvc.perform(get(baseUrl + "/get-all-unauthorized-users"))
            .andExpect(status().isOk());

        verify(telegramService).getAllUnauthorizedUsers(any(PageRequest.class));
    }

    @Test
    void sendMessageTest() throws Exception {
        String message = "message";

        mockMvc.perform(post(baseUrl + "/send-message/" + chatId)
            .param("message", message))
            .andExpect(status().isOk());

        verify(telegramService).sendMessageToUser(
            chatId,
            message);
    }

    @Test
    void sendPhotoTest() throws Exception {
        String photoUrl = "photoUrl";
        String caption = "caption";

        mockMvc.perform(post(baseUrl + "/send-photo/" + chatId)
            .param("photoUrl", photoUrl)
            .param("caption", caption))
            .andExpect(status().isOk());

        verify(telegramPhotoService).sendPhotoToUser(
            chatId,
            photoUrl,
            caption);
    }

    @Test
    void uploadPhotoWithValidMultipartFileTest() throws Exception {
        String multipartFileContent = "content";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file",
            "text.txt",
            "text/plain",
            multipartFileContent.getBytes());
        String caption = "caption";
        String savedMultipartFileAzureBlobLink = "link";

        when(telegramPhotoService.savePhotoToAzureBlob(mockMultipartFile))
            .thenReturn(savedMultipartFileAzureBlobLink);

        mockMvc.perform(multipart(baseUrl + "/upload-photo/" + chatId)
            .file(mockMultipartFile)
            .param("caption", caption))
            .andExpect(status().isOk());

        verify(telegramPhotoService).sendPhotoToUser(
            chatId,
            savedMultipartFileAzureBlobLink,
            caption);
        verify(telegramPhotoService).deletePhotoFromAzureBlob(
            savedMultipartFileAzureBlobLink);
    }

    @Test
    void uploadPhotoWithEmptyMultipartFileTest() throws Exception {
        String multipartFileContent = "";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file",
            "text.txt",
            "text/plain",
            multipartFileContent.getBytes());
        String caption = "caption";

        mockMvc.perform(multipart(baseUrl + "/upload-photo/" + chatId)
            .file(mockMultipartFile)
            .param("caption", caption))
            .andExpect(status().isBadRequest());

        verify(telegramPhotoService, never()).savePhotoToAzureBlob(
            any(MultipartFile.class));
        verify(telegramPhotoService, never()).sendPhotoToUser(
            any(),
            any(),
            any());
        verify(telegramPhotoService, never()).deletePhotoFromAzureBlob(
            any());
    }

    @Test
    void generateManagerLinkTest() throws Exception {
        Principal principal = getPrincipal();
        String principalUuid = "uuid";
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn(principalUuid);

        mockMvc.perform(post(baseUrl + "/generate-manager-link")
            .principal(principal))
            .andExpect(status().isOk());

        verify(telegramService).generateManagerStartLink(
            principalUuid);
    }

    @Test
    void streamTest() throws Exception {
        mockMvc.perform(get(baseUrl + "/stream")
            .param("chatId", chatId))
            .andExpect(status().isOk());

        verify(telegramStreamingService).addEmitter(any(), eq(chatId));
    }
}
