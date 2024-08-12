package greencity.controller;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.order.BlockedOrderDto;
import greencity.dto.order.ChangeOrderResponseDTO;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.dto.table.ColumnWidthDto;
import greencity.enums.UkraineRegion;
import greencity.service.ubs.OrdersAdminsPageService;
import java.util.Collections;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        mockMvc.perform(get(management + "/tableParams" + "?region=")
            .principal(principal))
            .andExpect(status().isOk());
        verify(ordersAdminsPageService).getParametersForOrdersTable("35467585763t4sfgchjfuyetf", Collections.emptyList());
    }

    @Test
    void getTableParametersWithRegion() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        mockMvc.perform(get(management + "/tableParams" + "?region=KHARKIV_OBLAST")
                .principal(principal))
            .andExpect(status().isOk());
        verify(ordersAdminsPageService).getParametersForOrdersTable("35467585763t4sfgchjfuyetf", List.of(UkraineRegion.KHARKIV_OBLAST));
    }

    @Test
    void saveNewValueFromOrdersTableTest() throws Exception {
        RequestToChangeOrdersDataDto dto = ModelUtils.getRequestToChangeOrdersDataDTO();
        ChangeOrderResponseDTO changeOrderResponseDTO = ModelUtils.getChangeOrderResponseDTO();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        when(ordersAdminsPageService.chooseOrdersDataSwitcher("35467585763t4sfgchjfuyetf", dto))
            .thenReturn(changeOrderResponseDTO);

        mockMvc.perform(put(management + "/changingOrder")
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal)
            .content(json))
            .andExpect(status().isOk());

        verify(ordersAdminsPageService).chooseOrdersDataSwitcher("35467585763t4sfgchjfuyetf", dto);
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

    @Test
    void getColumnWidthForEmployeeTest() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        when(ordersAdminsPageService.getColumnWidthForEmployee(anyString())).thenReturn(new ColumnWidthDto());

        mockMvc.perform(get(management + "/orderTableColumnsWidth")
            .principal(principal))
            .andExpect(status().isOk());
    }

    @Test
    void saveColumnWidthForEmployeeTest() throws Exception {
        ColumnWidthDto columnWidthDto = new ColumnWidthDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(columnWidthDto);
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        doNothing().when(ordersAdminsPageService).saveColumnWidthForEmployee(any(ColumnWidthDto.class), anyString());
        mockMvc.perform(put(management + "/orderTableColumnsWidth")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk());
    }
}
