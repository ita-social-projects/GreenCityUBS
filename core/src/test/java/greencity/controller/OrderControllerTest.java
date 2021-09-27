package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.*;
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

import static greencity.ModelUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        when(restClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

        mockMvc.perform(get(ubsLink + "/order-details")
            .principal(principal))
            .andExpect(status().isOk());

        verify(restClient).findUuidByEmail("test@gmail.com");
        verify(ubsClientService).getFirstPageData("35467585763t4sfgchjfuyetf");
    }

    @Test
    void checkIfCertificateAvailable() throws Exception {
        mockMvc.perform(get(ubsLink + "/certificate/{code}", "qwefds"))
            .andExpect(status().isOk());
        verify(ubsClientService).checkCertificate("qwefds");
    }

    @Test
    void getUBSusers() throws Exception {
        when(restClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

        mockMvc.perform(get(ubsLink + "/personal-data")
            .principal(principal))
            .andExpect(status().isOk());

        verify(restClient).findUuidByEmail("test@gmail.com");
        verify(ubsClientService).getSecondPageData("35467585763t4sfgchjfuyetf");
    }

    @Test
    void processOrder() throws Exception {
        when(restClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String orderResponceDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/processOrder")
            .content(orderResponceDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).saveFullOrderToDB(anyObject(), eq("35467585763t4sfgchjfuyetf"));
        verify(restClient).findUuidByEmail("test@gmail.com");

    }

    @Test
    void getAllAddressesForCurrentUser() throws Exception {
        when(restClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

        mockMvc.perform(get(ubsLink + "/findAll-order-address")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).findAllAddressesForCurrentOrder(anyString());
    }

    @Test
    void saveAddressForOrder() throws Exception {
        when(restClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

        OrderAddressDtoRequest dto = ModelUtils.getOrderAddressDtoRequest();

        ObjectMapper objectMapper = new ObjectMapper();
        String orderAddressDtoRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/save-order-address")
            .content(orderAddressDtoRequest)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        verify(ubsClientService).saveCurrentAddressForOrder(anyObject(), eq("35467585763t4sfgchjfuyetf"));
    }

    @Test
    void deleteOrderAddress() throws Exception {

        this.mockMvc.perform(delete(ubsLink + "/{id}", 1L))
            .andExpect(status().isNotFound());
    }

    @Test
    void getOrderDetailsByOrderId() throws Exception {
        UserInfoDto userInfoDto = getUserInfoDto();
        when(ubsClientService.getUserAndUserUbsAndViolationsInfoByOrderId(1L)).thenReturn(userInfoDto);
        mockMvc.perform(get(ubsLink + "/user-info" + "/{orderId}", 1L))
            .andExpect(status().isOk());

        verify(ubsClientService).getUserAndUserUbsAndViolationsInfoByOrderId(1L);
    }

    @Test
    void updatesRecipientsInfo() throws Exception {
        UbsCustomersDto ubsCustomersDto = getUbsCustomersDto();
        UbsCustomersDtoUpdate ubsCustomersDtoUpdate = getUbsCustomersDtoUpdate();
        when(ubsClientService.updateUbsUserInfoInOrder(ubsCustomersDtoUpdate)).thenReturn(ubsCustomersDto);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(put(ubsLink + "/update-recipients-data")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ubsCustomersDtoUpdate)))
            .andExpect(status().isOk());

        verify(ubsClientService).updateUbsUserInfoInOrder(ubsCustomersDtoUpdate);
    }

    @Test
    void getsCancellationReason() throws Exception {
        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
        when(ubsClientService.getOrderCancellationReason(anyLong())).thenReturn(dto);
        mockMvc.perform(get(ubsLink + "/order/{id}/cancellation", 1L))
            .andExpect(status().isOk());
        verify(ubsClientService).getOrderCancellationReason(1L);
    }

    @Test
    void updatesCancellationReason() throws Exception {
        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
        ObjectMapper objectMapper = new ObjectMapper();
        when(ubsClientService.updateOrderCancellationReason(anyLong(), any())).thenReturn(dto);
        mockMvc.perform(post(ubsLink + "/order/{id}/cancellation/", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());

        verify(ubsClientService).updateOrderCancellationReason(anyLong(), anyObject());
    }

    @Test
    void testGetAllLocationsForPopUp() throws Exception {
        when(restClient.findUuidByEmail((anyString()))).thenReturn("uuid");
        mockMvc.perform(get(ubsLink + "/order/get-locations")
            .principal(principal))
            .andExpect(status().isOk());

        verify(ubsClientService).getAllLocations("uuid");
    }

    @Test
    void testSetNewLastOrderLocationForUser() throws Exception {
        LocationIdDto locationIdDto = getLocationIdDto();
        ObjectMapper objectMapper = new ObjectMapper();
        when(restClient.findUuidByEmail((anyString()))).thenReturn("uuid");

        mockMvc.perform(post(ubsLink + "/order/get-locations")
            .content(objectMapper.writeValueAsString(locationIdDto))
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).setNewLastOrderLocation("uuid", locationIdDto);
    }

    @Test
    void testGetOrderHistoryByOrderId() throws Exception {
        when(ubsClientService.getAllEventsForOrderById(1L))
            .thenReturn(ModelUtils.getListEventsDTOS());
        mockMvc.perform(get(ubsLink + "/order_history" + "/{orderId}", 1L))
            .andExpect(status().isOk());

        verify(ubsClientService, times(1))
            .getAllEventsForOrderById(1L);
    }

    @Test
    void processLiqPayOrder() throws Exception {
        when(restClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String orderResponceDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/processLiqPayOrder")
            .content(orderResponceDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).saveFullOrderToDBFromLiqPay(anyObject(), eq("35467585763t4sfgchjfuyetf"));
        verify(restClient).findUuidByEmail("test@gmail.com");

    }

    @Test
    void reciveLiqPayOrder() throws Exception {
        PaymentResponseDtoLiqPay dto = new PaymentResponseDtoLiqPay();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponce = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/receiveLiqPayPayment")
            .content(jsonResponce)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).validateLiqPayPayment(anyObject(), any(), eq(null));
    }

}
