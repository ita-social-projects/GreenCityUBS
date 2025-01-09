package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.order.OrderBagDto;
import greencity.dto.order.OrderFondyClientDto;
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
import java.util.Locale;

import static greencity.ModelUtils.getUuid;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class ClientControllerTest {
    private static final String UBS_CLIENT_LINK = "/ubs/client";
    private static final String MAKE_ORDER_AGAIN_LINK = "/make-order-again";
    private static final String ORDER_PAYMENT_DETAIL_LINK = "/order-payment-detail/";
    private static final String USERS_POINTS_TO_USE_LINK = "/users-pointsToUse";
    private MockMvc mockMvc;

    @Mock
    UBSClientService ubsClientService;

    @Mock
    UserRemoteClient userRemoteClient;

    @InjectMocks
    ClientController clientController;

    private static final Principal PRINCIPAL = getUuid();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
            .setCustomArgumentResolvers(new UserArgumentResolver(userRemoteClient))
            .build();
    }

    @Test
    void makeOrderAgain() throws Exception {
        OrderBagDto dto = OrderBagDto.builder()
            .id(1)
            .amount(3)
            .build();
        String responseJSON = OBJECT_MAPPER.writeValueAsString(dto);

        mockMvc.perform(post(UBS_CLIENT_LINK + "/" + 1L + MAKE_ORDER_AGAIN_LINK)
            .principal(PRINCIPAL)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService, times(1)).makeOrderAgain(Locale.ENGLISH, 1L);
    }

    @Test
    void getOrderPaymentDetail() throws Exception {
        mockMvc.perform(get(UBS_CLIENT_LINK + ORDER_PAYMENT_DETAIL_LINK + 1L)
            .principal(PRINCIPAL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService, times(1)).getOrderPaymentDetail(1L);
    }

    @Test
    void getAllPointsForUserTest() throws Exception {
        mockMvc.perform(get(UBS_CLIENT_LINK + USERS_POINTS_TO_USE_LINK)
            .principal(PRINCIPAL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).findAllCurrentPointsForUser(any());
    }

    @Test
    void getAllDataForOrderTest() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

        mockMvc.perform(get(UBS_CLIENT_LINK + "/user-orders", 1)
            .principal(PRINCIPAL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getDataForOrderTest() throws Exception {
        mockMvc.perform(get(UBS_CLIENT_LINK + "/user-order/{id}", 1)
            .principal(PRINCIPAL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void deleteOrderTest() throws Exception {
        mockMvc.perform(delete(UBS_CLIENT_LINK + "/delete-order/{id}", 1)
            .principal(PRINCIPAL)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void processOrderFondy() throws Exception {
        OrderFondyClientDto dto = ModelUtils.getOrderFondyClientDto();
        String dtoJson = OBJECT_MAPPER.writeValueAsString(dto);

        mockMvc.perform(post(UBS_CLIENT_LINK + "/processOrderFondy")
            .contentType(MediaType.APPLICATION_JSON)
            .principal(PRINCIPAL)
            .content(dtoJson))
            .andExpect(status().isOk());
    }

    @Test
    void getUserBonusesTest() throws Exception {
        mockMvc.perform(get(UBS_CLIENT_LINK + "/user-bonuses")
            .principal(PRINCIPAL)).andExpect(status().isOk());
    }
}
