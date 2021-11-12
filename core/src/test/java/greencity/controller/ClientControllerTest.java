package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.OrderBagDto;
import greencity.dto.OrderClientDto;
import greencity.service.ubs.UBSClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

import static greencity.ModelUtils.getOrderClientDto;
import static greencity.ModelUtils.getUuid;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class ClientControllerTest {
    private static final String ubsLink = "/ubs/client";
    private static final String getAllUserOrderLink = "/getAll-users-orders";
    private static final String cancelFormedOrderLink = "/cancel-formed-order";
    private static final String makeOrderAgainLink = "/make-order-again";
    private static final String getOrderPaymentDetailLink = "/order-payment-detail/";
    private static final String getAllPointsForUser = "/users-pointsToUse";
    private MockMvc mockMvc;

    @Mock
    UBSClientService ubsClientService;

    @Mock
    RestClient restClient;

    @InjectMocks
    ClientController clientController;

    private Principal principal = getUuid();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(clientController)
            .setCustomArgumentResolvers(new UserArgumentResolver(restClient))
            .build();
    }

    @Test
    void getAllOrdersDoneByUser() throws Exception {
        OrderClientDto dto = getOrderClientDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(List.of(dto));

        mockMvc.perform(get(ubsLink + getAllUserOrderLink)
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService, times(1)).getAllOrdersDoneByUser(any());
    }

    @Test
    void cancelFormedOrder() throws Exception {
        OrderClientDto dto = getOrderClientDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch(ubsLink + "/" + 1L + cancelFormedOrderLink)
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService, times(1)).cancelFormedOrder(1L);
    }

    @Test
    void makeOrderAgain() throws Exception {
        OrderBagDto dto = OrderBagDto.builder()
            .id(1)
            .amount(3)
            .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/" + 1L + makeOrderAgainLink)
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService, times(1)).makeOrderAgain(new Locale("en"), 1L);
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
    void getDataForOrderStatusPageTest() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/get-data-for-order-surcharge/{id}/{langId}", 1L, 2L));
        verify(ubsClientService).getOrderInfoForSurcharge(1L, 2L);
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
        this.mockMvc.perform(get(ubsLink + "/get-all-orders-data/{lang}", 1)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).getOrdersForUser(null, 1L);
    }
}