package greencity.controller;

import java.security.Principal;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.configuration.RedirectionConfigProp;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.client.UserRemoteClient;
import greencity.repository.OrderRepository;
import greencity.service.ubs.NotificationService;
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

import greencity.dto.OrderAddressDtoRequest;
import greencity.dto.OrderCancellationReasonDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PaymentResponseDto;
import greencity.dto.PaymentResponseDtoLiqPay;
import greencity.dto.UbsCustomersDto;
import greencity.dto.UbsCustomersDtoUpdate;
import greencity.dto.UserInfoDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getRedirectionConfig;
import static greencity.ModelUtils.getUbsCustomersDto;
import static greencity.ModelUtils.getUbsCustomersDtoUpdate;
import static greencity.ModelUtils.getUserInfoDto;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class OrderControllerTest {
    private static final String ubsLink = "/ubs";

    private MockMvc mockMvc;

    @Mock
    UBSClientService ubsClientService;

    @Mock
    UserRemoteClient userRemoteClient;

    @Mock
    OrderRepository orderRepository;
    @Mock
    NotificationService notificationService;
    @InjectMocks
    OrderController orderController;

    private Principal principal = getPrincipal();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(orderController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .build();
    }

    @Test
    void getCurrentUserPoints() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

        mockMvc.perform(get(ubsLink + "/order-details")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userRemoteClient).findUuidByEmail("test@gmail.com");
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
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

        mockMvc.perform(get(ubsLink + "/personal-data")
            .principal(principal))
            .andExpect(status().isOk());

        verify(userRemoteClient).findUuidByEmail("test@gmail.com");
        verify(ubsClientService).getSecondPageData("35467585763t4sfgchjfuyetf");
    }

    @Test
    void processOrder() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String orderResponceDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/processOrder")
            .content(orderResponceDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).saveFullOrderToDB(anyObject(), eq("35467585763t4sfgchjfuyetf"));
        verify(userRemoteClient).findUuidByEmail("test@gmail.com");

    }

    @Test
    void getAllAddressesForCurrentUser() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

        mockMvc.perform(get(ubsLink + "/findAll-order-address")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).findAllAddressesForCurrentOrder(anyString());
    }

    @Test
    void saveAddressForOrder() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

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
        when(ubsClientService.getUserAndUserUbsAndViolationsInfoByOrderId(1L, null)).thenReturn(userInfoDto);
        mockMvc.perform(get(ubsLink + "/user-info" + "/{orderId}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).getUserAndUserUbsAndViolationsInfoByOrderId(1L, null);
    }

    @Test
    void updatesRecipientsInfo() throws Exception {
        UbsCustomersDto ubsCustomersDto = getUbsCustomersDto();
        UbsCustomersDtoUpdate ubsCustomersDtoUpdate = getUbsCustomersDtoUpdate();
        when(ubsClientService.updateUbsUserInfoInOrder(ubsCustomersDtoUpdate, null)).thenReturn(ubsCustomersDto);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(put(ubsLink + "/update-recipients-data")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ubsCustomersDtoUpdate)))
            .andExpect(status().isOk());

        verify(ubsClientService).updateUbsUserInfoInOrder(ubsCustomersDtoUpdate, null);
    }

    @Test
    void getsCancellationReason() throws Exception {
        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        when(ubsClientService.getOrderCancellationReason(anyLong(), anyString())).thenReturn(dto);

        mockMvc.perform(get(ubsLink + "/order/{id}/cancellation", 1L)
            .principal(principal))
            .andExpect(status().isOk());
        verify(ubsClientService).getOrderCancellationReason(1L, "35467585763t4sfgchjfuyetf");
    }

    @Test
    void updatesCancellationReason() throws Exception {
        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
        ObjectMapper objectMapper = new ObjectMapper();
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        when(ubsClientService.updateOrderCancellationReason(anyLong(), any(), anyString())).thenReturn(dto);
        mockMvc.perform(post(ubsLink + "/order/{id}/cancellation/", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());

        verify(ubsClientService).updateOrderCancellationReason(anyLong(), any(), anyString());
    }

    @Test
    void testGetOrderHistoryByOrderId() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        when(ubsClientService.getAllEventsForOrder(1L, "35467585763t4sfgchjfuyetf"))
            .thenReturn(ModelUtils.getListEventsDTOS());
        mockMvc.perform(get(ubsLink + "/order_history" + "/{orderId}", 1L)
            .principal(principal))
            .andExpect(status().isOk());

        verify(ubsClientService, times(1))
            .getAllEventsForOrder(1L, "35467585763t4sfgchjfuyetf");
    }

    @Test
    void processLiqPayOrder() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String orderResponceDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/processLiqPayOrder")
            .content(orderResponceDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).saveFullOrderToDBFromLiqPay(anyObject(), eq("35467585763t4sfgchjfuyetf"));
        verify(userRemoteClient).findUuidByEmail("test@gmail.com");

    }

    @Test
    void receiveLiqPayOrder() throws Exception {
        PaymentResponseDtoLiqPay dto = ModelUtils.getPaymentResponceDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String gotInfo = objectMapper.writeValueAsString(dto);

        setRedirectionConfigProp();

        mockMvc.perform(post(ubsLink + "/receiveLiqPayPayment")
            .content(gotInfo)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void receivePaymentTest() throws Exception {
        PaymentResponseDto dto = ModelUtils.getPaymentResponseDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String paymentResponseJson = objectMapper.writeValueAsString(dto);

        setRedirectionConfigProp();

        mockMvc.perform(post(ubsLink + "/receivePayment")
            .content(paymentResponseJson)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void getLiqPayStatusPaymentTest() throws Exception {
        mockMvc.perform(get(ubsLink + "/getLiqPayStatus/{orderId}", 1)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void deleteOrderAddressTest() throws Exception {
        mockMvc.perform(delete("/ubs" + "/order-addresses" + "/{id}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void getLiqPayStatusPayment() throws Exception {
        mockMvc.perform(get(ubsLink + "/getLiqPayStatus/{orderId}", 1)).andExpect(status().isOk());
    }

    @Test
    void getFondyStatusPayment2() throws Exception {
        mockMvc.perform(get(ubsLink + "/getFondyStatus/{orderId}", 1))
            .andExpect(status().isOk());
    }

    @Mock
    RedirectionConfigProp redirectionConfigProp;

    @Test
    void receivePaymentClientTest() throws Exception {
        PaymentResponseDto dto = ModelUtils.getPaymentResponseDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String paymentResponseJson = objectMapper.writeValueAsString(dto);

        setRedirectionConfigProp();

        mockMvc.perform(post(ubsLink + "/receivePaymentClient")
            .content(paymentResponseJson)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void getCourierLocations() throws Exception {
        mockMvc.perform(get(ubsLink + "/courier/{courierId}", 1))
            .andExpect(status().isOk());
    }

    private void setRedirectionConfigProp() {
        RedirectionConfigProp redirectionConfigProp = getRedirectionConfig();

        Arrays.stream(OrderController.class.getDeclaredFields())
            .filter(field -> field.getName().equals("redirectionConfigProp"))
            .forEach(field -> {
                field.setAccessible(true);
                try {
                    field.set(orderController, redirectionConfigProp);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
    }
}
