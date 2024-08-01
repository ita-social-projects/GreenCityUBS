package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.service.ubs.UBSClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import static greencity.ModelUtils.getUuid;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class ClientControllerTest {
    private static final String ubsLink = "/ubs/client";
    private static final String getOrderPaymentDetailLink = "/order-payment-detail/";
    private static final String getAllPointsForUser = "/users-pointsToUse";
    private MockMvc mockMvc;

    @Mock
    UBSClientService ubsClientService;

    @Mock
    UserRemoteClient userRemoteClient;

    @InjectMocks
    ClientController clientController;

    private Principal principal = getUuid();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(clientController)
            .setCustomArgumentResolvers(new UserArgumentResolver(userRemoteClient))
            .build();
    }

    @Test
    void getOrderPaymentDetail() throws Exception {
        mockMvc.perform(get(ubsLink + getOrderPaymentDetailLink + 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService, times(1)).getOrderPaymentDetail(1L);
    }

    @Test
    void getAllPointsForUserTest() throws Exception {
        this.mockMvc.perform(get(ubsLink + getAllPointsForUser)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).findAllCurrentPointsForUser(any());
    }

    @Test
    void getAllDataForOrderTest() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

        this.mockMvc.perform(get(ubsLink + "/user-orders", 1)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getDataForOrderTest() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/user-order/{id}", 1)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void deleteOrderTest() throws Exception {
        this.mockMvc.perform(delete(ubsLink + "/delete-order/{id}", 1)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void processOrder() throws Exception {
        OrderWayForPayClientDto dto = ModelUtils.getOrderWayForPayClientDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String dtoJson = objectMapper.writeValueAsString(dto);

        this.mockMvc.perform(post(ubsLink + "/processOrder")
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal)
            .content(dtoJson))
            .andExpect(status().isOk());
    }

    @Test
    void getUserBonusesTest() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/user-bonuses")
            .principal(principal)).andExpect(status().isOk());
    }
}
