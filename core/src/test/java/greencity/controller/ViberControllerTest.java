package greencity.controller;

import greencity.client.UserRemoteClient;
import greencity.converters.UserArgumentResolver;
import greencity.service.ubs.ViberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ViberControllerTest {

    private MockMvc mockMvc;

    @Mock
    ViberService viberService;

    @Mock
    UserRemoteClient userRemoteClient;

    @InjectMocks
    ViberController viberController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(viberController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .build();
    }

    @Test
    void setWebHookTest() throws Exception {
        mockMvc.perform(get("/setwebhook"))
            .andExpect(status().isOk());

        verify(viberService).setWebhook();
    }

    @Test
    void removeWebHookTest() throws Exception {
        mockMvc.perform(get("/removewebhook"))
            .andExpect(status().isOk());

        verify(viberService).removeWebHook();
    }

    @Test
    void getAccountInfoTest() throws Exception {
        mockMvc.perform(get("/accountinfo"))
            .andExpect(status().isOk());

        verify(viberService).getAccountInfo();
    }
}
