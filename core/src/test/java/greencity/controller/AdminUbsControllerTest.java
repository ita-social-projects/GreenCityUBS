package greencity.controller;

import greencity.ModelUtils;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.client.UserRemoteClient;
import greencity.dto.order.BlockedOrderDto;
import greencity.dto.order.ChangeOrderResponseDTO;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.service.ubs.OrdersAdminsPageService;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import static greencity.ModelUtils.getUuid;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class AdminUbsControllerTest {
    private MockMvc mockMvc;

    @Mock
    private OrdersAdminsPageService ordersAdminsPageService;

    private static final String management = "/ubs/management";
    @InjectMocks
    AdminUbsController adminUbsController;

    private final Principal principal = getUuid();
    @Mock
    UserRemoteClient userRemoteClient;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(adminUbsController)
            .setCustomArgumentResolvers(new UserArgumentResolver(userRemoteClient))
            .build();
    }

    @Test
    void getTableParameters() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        mockMvc.perform(get(management + "/tableParams")
            .principal(principal))
            .andExpect(status().isOk());
        verify(ordersAdminsPageService).getParametersForOrdersTable("35467585763t4sfgchjfuyetf");
    }

    @Test
    void saveNewValueFromOrdersTableTest() throws Exception {
        RequestToChangeOrdersDataDto dto = ModelUtils.getRequestToChangeOrdersDataDTO();
        ChangeOrderResponseDTO changeOrderResponseDTO = ModelUtils.getChangeOrderResponseDTO();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        when(ordersAdminsPageService.chooseOrdersDataSwitcher(null, dto)).thenReturn(changeOrderResponseDTO);

        mockMvc.perform(put(management + "/changingOrder")
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal)
            .content(json))
            .andExpect(status().isOk());

        verify(ordersAdminsPageService).chooseOrdersDataSwitcher(null, dto);
    }

    @Test
    void getAllOrdersForUserTest() throws Exception {
        List<Long> listOfOrdersId = List.of(1L);
        List<Long> unblockedOrdersId = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(listOfOrdersId);

        when(ordersAdminsPageService.unblockOrder(null, listOfOrdersId)).thenReturn(unblockedOrdersId);

        mockMvc.perform(put(management + "/unblockOrders")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk());

        verify(ordersAdminsPageService).unblockOrder(null, listOfOrdersId);
    }

    @Test
    void blockOrdersTest() throws Exception {
        List<BlockedOrderDto> dto = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        when(ordersAdminsPageService.requestToBlockOrder(null, List.of())).thenReturn(dto);

        mockMvc.perform(put(management + "/blockOrders")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk());

        verify(ordersAdminsPageService).requestToBlockOrder(null, List.of());
    }

}
