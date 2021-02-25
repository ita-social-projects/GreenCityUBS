package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import static greencity.ModelUtils.getPrincipal;
import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.OrderResponseDto;
import greencity.service.ubs.UBSClientService;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class OrderControllerTest {
    private static final String ubsLink = "/ubs";

    private MockMvc mockMvc;

    @Mock
    UBSClientService ubsClientService;

    @Mock
    RestClient restClient;

    @InjectMocks
    OrderController orderController;

    private Principal principal = getPrincipal();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(orderController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(restClient))
            .build();
    }

    @Test
    void getCurrentUserPoints() throws Exception {
        when(restClient.findIdByEmail(anyString())).thenReturn(13L);

        mockMvc.perform(get(ubsLink + "/order-details")
            .principal(principal))
            .andExpect(status().isOk());

        verify(restClient).findIdByEmail("test@gmail.com");
        verify(ubsClientService).getFirstPageData(eq(13L));
    }

    @Test
    void checkIfCertificateAvailable() throws Exception {
        mockMvc.perform(get(ubsLink + "/certificate/{code}", "qwefds"))
            .andExpect(status().isOk());

        verify(ubsClientService).checkCertificate(eq("qwefds"));
    }

    @Test
    void getUBSusers() throws Exception {
        when(restClient.findIdByEmail(anyString())).thenReturn(13L);

        mockMvc.perform(get(ubsLink + "/personal-data")
            .principal(principal))
            .andExpect(status().isOk());

        verify(restClient).findIdByEmail("test@gmail.com");
        verify(ubsClientService).getSecondPageData(eq(13L));
    }

    @Test
    void processOrder() throws Exception {
        when(restClient.findIdByEmail(anyString())).thenReturn(13L);
        OrderResponseDto dto = ModelUtils.getOrderResponceDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String orderResponceDtoJSON = objectMapper.writeValueAsString(dto);

        System.out.println(orderResponceDtoJSON);

        mockMvc.perform(post(ubsLink + "/processOrder")
            .content(orderResponceDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        verify(ubsClientService).saveFullOrderToDB(anyObject(), eq(13L));
        verify(restClient).findIdByEmail("test@gmail.com");

    }
}
