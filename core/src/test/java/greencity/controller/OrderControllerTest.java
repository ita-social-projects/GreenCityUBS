package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.RedirectionConfigProp;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.LocationsDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.order.WayForPayOrderResponse;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.user.UserInfoDto;
import greencity.enums.OrderStatus;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.repository.OrderRepository;
import greencity.repository.UBSUserRepository;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSManagementService;
import jakarta.servlet.ServletException;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import java.util.List;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUbsCustomersDto;
import static greencity.ModelUtils.getUbsCustomersDtoUpdate;
import static greencity.ModelUtils.getUserInfoDto;
import static greencity.ModelUtils.getUnpaidOrderDetailStatusDto;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class OrderControllerTest {
    private static final String ubsLink = "/ubs";

    private MockMvc mockMvc;

    @Mock
    UBSClientService ubsClientService;

    @Mock
    UBSManagementService ubsManagementService;

    @Mock
    UserRemoteClient userRemoteClient;

    @Mock
    OrderRepository orderRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    OrderController orderController;

    @Mock
    RedirectionConfigProp prop;

    @Mock
    private UBSUserRepository ubSuserRepository;

    private final Principal principal = getPrincipal();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(orderController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .build();
    }

    @Test
    void getCurrentUserPointsByTariffAndLocationId() throws Exception {
        mockMvc.perform(get(ubsLink + "/order-details-for-tariff")
            .principal(principal)
            .param("tariffId", "1")
            .param("locationId", "1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).getFirstPageDataByTariffAndLocationId(1L, 1L);
    }

    @Test
    void getCurrentUserPointsByOrderId() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString())))
            .thenReturn("35467585763t4sfgchjfuyetf");

        mockMvc.perform(get(ubsLink + "/details-for-existing-order/{orderId}", "1")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(userRemoteClient).findUuidByEmail("test@gmail.com");
        verify(ubsClientService).getFirstPageDataByOrderId("35467585763t4sfgchjfuyetf", 1L);
    }

    @Test
    void checkIfCertificateAvailable() throws Exception {
        String certificateCode = "1111-1111";
        mockMvc.perform(get(ubsLink + "/certificate/{code}", certificateCode)
            .principal(principal))
            .andExpect(status().isOk());
        verify(ubsClientService).checkCertificate(certificateCode, null);
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
        String orderResponseDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/processOrder")
            .content(orderResponseDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).saveFullOrderToDB(any(), eq("35467585763t4sfgchjfuyetf"), eq(null));
        verify(userRemoteClient).findUuidByEmail("test@gmail.com");
    }

    @Test
    void processOrderId() throws Exception {
        Long orderId = 1L;
        String uuid = "35467585763t4sfgchjfuyetf";
        ObjectMapper objectMapper = new ObjectMapper();
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();
        String orderResponseDtoJSON = objectMapper.writeValueAsString(dto);

        OrderDetailStatusDto orderDetailStatusDto = getUnpaidOrderDetailStatusDto();
        orderDetailStatusDto.setOrderStatus(OrderStatus.FORMED.name());

        WayForPayOrderResponse resultObject = WayForPayOrderResponse.builder()
            .orderId(orderId)
            .link("Link")
            .build();
        String resultJson = objectMapper.writeValueAsString(resultObject);

        when(userRemoteClient.findUuidByEmail(anyString())).thenReturn(uuid);
        when(ubsManagementService.getOrderDetailStatus(orderId)).thenReturn(orderDetailStatusDto);
        when(ubsClientService.saveFullOrderToDB(any(OrderResponseDto.class), anyString(), anyLong()))
            .thenReturn(resultObject);

        mockMvc.perform(post(ubsLink + "/processOrder/{id}", orderId)
            .content(orderResponseDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(resultJson));

        verify(userRemoteClient).findUuidByEmail(anyString());
        verify(ubsManagementService).getOrderDetailStatus(orderId);
        verify(ubsClientService).saveFullOrderToDB(any(OrderResponseDto.class), anyString(), anyLong());
    }

    @Test
    void processPaidOrderId() throws Exception {
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();
        OrderDetailStatusDto orderDetailStatusDto = ModelUtils.getPaidOrderDetailStatusDto();

        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        when(ubsManagementService.getOrderDetailStatus(anyLong())).thenReturn(orderDetailStatusDto);

        ObjectMapper objectMapper = new ObjectMapper();
        String orderResponseDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/processOrder/{id}", 1L)
            .content(orderResponseDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class,
        names = "FORMED",
        mode = EnumSource.Mode.EXCLUDE)
    void processPaidOrderIdWithUnacceptableOrderStatusesTest(OrderStatus orderStatus) throws Exception {
        Long orderId = 1L;
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();
        OrderDetailStatusDto orderDetailStatusDto = ModelUtils.getPaidOrderDetailStatusDto();
        orderDetailStatusDto.setOrderStatus(orderStatus.name());

        when(userRemoteClient.findUuidByEmail(anyString())).thenReturn("35467585763t4sfgchjfuyetf");
        when(ubsManagementService.getOrderDetailStatus(anyLong())).thenReturn(orderDetailStatusDto);

        ObjectMapper objectMapper = new ObjectMapper();
        String orderResponseDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/processOrder/{id}", orderId)
            .content(orderResponseDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(userRemoteClient).findUuidByEmail(anyString());
        verify(ubsManagementService).getOrderDetailStatus(orderId);
        verify(ubsClientService, never()).saveFullOrderToDB(any(OrderResponseDto.class), anyString(), anyLong());
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
            .content(objectMapper.writeValueAsString(ubsCustomersDtoUpdate))
            .principal(principal))
            .andExpect(status().isOk());

        verify(ubsClientService).updateUbsUserInfoInOrder(ubsCustomersDtoUpdate, null);
    }

    @Test
    void updatesRecipientsInfoWithOutUser() {
        ObjectMapper objectMapper = new ObjectMapper();
        UbsCustomersDtoUpdate ubsCustomersDtoUpdate = getUbsCustomersDtoUpdate();

        when(ubsClientService.updateUbsUserInfoInOrder(ubsCustomersDtoUpdate, null))
            .thenThrow(UBSuserNotFoundException.class);

        ServletException exception =
            assertThrows(ServletException.class, () -> mockMvc.perform(put(ubsLink + "/update-recipients-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ubsCustomersDtoUpdate))
                .principal(principal))
                .andExpect(status().isBadRequest()));

        assertInstanceOf(UBSuserNotFoundException.class, exception.getCause());
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
    void testGetOrderHistoryByOrderId() throws Exception {
        mockMvc.perform(get(ubsLink + "/order_history" + "/{orderId}", 1L)
            .principal(principal))
            .andExpect(status().isOk());

        verify(ubsClientService, times(1))
            .getAllEventsForOrder(1L, "test@gmail.com", "en");
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
    void getFondyStatusPayment2() throws Exception {
        mockMvc.perform(get(ubsLink + "/getFondyStatus/{orderId}", 1)
            .principal(principal))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getInfoAboutTariffTest() {
        mockMvc.perform(get(ubsLink + "/tariffinfo/{locationId}", 1L)
            .param("courierId", "1"))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getAllActiveLocationsByCourierIdTest() {
        mockMvc.perform(get(ubsLink + "/locations/{courierId}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getAllActiveCouriersTest() throws Exception {
        mockMvc.perform(get(ubsLink + "/getAllActiveCouriers")).andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getTariffForOrder() {
        mockMvc.perform(get(ubsLink + "/orders/1/tariff")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void checkIfTariffExistsByIdTest() throws Exception {
        Long tariffId = 1L;
        when(ubsClientService.checkIfTariffExistsById(tariffId)).thenReturn(true);

        mockMvc.perform(get(ubsLink + "/check-if-tariff-exists/{id}", tariffId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));

        verify(ubsClientService).checkIfTariffExistsById(tariffId);
    }

    @Test
    void getAllLocationsTest() throws Exception {
        List<LocationsDto> locationsDtoList = Arrays.asList(new LocationsDto(), new LocationsDto());
        when(ubsClientService.getAllLocations()).thenReturn(locationsDtoList);

        mockMvc.perform(get(ubsLink + "/locations")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(new ObjectMapper().writeValueAsString(locationsDtoList)));

        verify(ubsClientService).getAllLocations();
    }

    @Test
    void getTariffIdByLocationIdTest() throws Exception {
        Long locationId = 1L;
        Long tariffId = 2L;
        when(ubsClientService.getTariffIdByLocationId(locationId)).thenReturn(tariffId);

        mockMvc.perform(get(ubsLink + "/tariffs/{locationId}", locationId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(String.valueOf(tariffId)));

        verify(ubsClientService).getTariffIdByLocationId(locationId);
    }

    @Test
    void getAllLocationsByCourierIdTest() throws Exception {
        Long id = 1L;
        List<LocationsDto> locationsDtoList = Arrays.asList(new LocationsDto(), new LocationsDto());
        when(ubsClientService.getAllLocationsByCourierId(id)).thenReturn(locationsDtoList);

        mockMvc.perform(get(ubsLink + "/locationsByCourier/" + id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(new ObjectMapper().writeValueAsString(locationsDtoList)));

        verify(ubsClientService).getAllLocationsByCourierId(id);
    }

    private void setRedirectionConfigProp() {
        RedirectionConfigProp redirectionConfigProp = ModelUtils.getRedirectionConfig();

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
