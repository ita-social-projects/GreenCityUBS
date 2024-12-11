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
import greencity.dto.order.FondyOrderResponse;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.user.UserInfoDto;
import greencity.enums.OrderStatus;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.repository.OrderRepository;
import greencity.repository.UBSUserRepository;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSManagementService;
import jakarta.servlet.ServletException;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getRedirectionConfig;
import static greencity.ModelUtils.getUbsCustomersDto;
import static greencity.ModelUtils.getUbsCustomersDtoUpdate;
import static greencity.ModelUtils.getUnpaidOrderDetailStatusDto;
import static greencity.ModelUtils.getUserInfoDto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class OrderControllerTest {
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
    private UBSUserRepository ubSuserRepository;

    private static final String UBS_LINK = "/ubs";
    private static final String RANDOM_UUID = UUID.randomUUID().toString();

    private static final Principal PRINCIPAL = getPrincipal();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(orderController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .build();
    }

    @Test
    void getCurrentUserPointsByTariffAndLocationId() throws Exception {
        mockMvc.perform(get(UBS_LINK + "/order-details-for-tariff")
            .principal(PRINCIPAL)
            .param("tariffId", "1")
            .param("locationId", "1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).getFirstPageDataByTariffAndLocationId(1L, 1L);
    }

    @Test
    void getCurrentUserPointsByOrderId() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString())))
            .thenReturn(RANDOM_UUID);

        mockMvc.perform(get(UBS_LINK + "/details-for-existing-order/{orderId}", "1")
            .principal(PRINCIPAL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(userRemoteClient).findUuidByEmail("test@gmail.com");
        verify(ubsClientService).getFirstPageDataByOrderId(RANDOM_UUID, 1L);
    }

    @Test
    void checkIfCertificateAvailable() throws Exception {
        mockMvc.perform(get(UBS_LINK + "/certificate/{code}", "qwefds"))
            .andExpect(status().isOk());
        verify(ubsClientService).checkCertificate("qwefds");
    }

    @Test
    void getUBSUsers() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn(RANDOM_UUID);

        mockMvc.perform(get(UBS_LINK + "/personal-data")
            .principal(PRINCIPAL))
            .andExpect(status().isOk());

        verify(userRemoteClient).findUuidByEmail("test@gmail.com");
        verify(ubsClientService).getSecondPageData(RANDOM_UUID);
    }

    @Test
    void processOrder() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn(RANDOM_UUID);
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();

        String orderResponseDtoJSON = OBJECT_MAPPER.writeValueAsString(dto);

        mockMvc.perform(post(UBS_LINK + "/processOrder")
            .content(orderResponseDtoJSON)
            .principal(PRINCIPAL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).saveFullOrderToDB(any(), eq(RANDOM_UUID), eq(null));
        verify(userRemoteClient).findUuidByEmail("test@gmail.com");
    }

    @Test
    void processOrderId() throws Exception {
        Long orderId = 1L;
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();
        String orderResponseDtoJSON = OBJECT_MAPPER.writeValueAsString(dto);

        OrderDetailStatusDto orderDetailStatusDto = getUnpaidOrderDetailStatusDto();
        orderDetailStatusDto.setOrderStatus(OrderStatus.FORMED.name());

        FondyOrderResponse resultObject = FondyOrderResponse.builder()
            .orderId(orderId)
            .link("Link")
            .build();
        String resultJson = OBJECT_MAPPER.writeValueAsString(resultObject);

        when(userRemoteClient.findUuidByEmail(anyString())).thenReturn(RANDOM_UUID);
        when(ubsManagementService.getOrderDetailStatus(orderId)).thenReturn(orderDetailStatusDto);
        when(ubsClientService.saveFullOrderToDB(any(OrderResponseDto.class), anyString(), anyLong()))
            .thenReturn(resultObject);

        mockMvc.perform(post(UBS_LINK + "/processOrder/{id}", orderId)
            .content(orderResponseDtoJSON)
            .principal(PRINCIPAL)
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

        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn(RANDOM_UUID);
        when(ubsManagementService.getOrderDetailStatus(anyLong())).thenReturn(orderDetailStatusDto);

        String orderResponseDtoJSON = OBJECT_MAPPER.writeValueAsString(dto);

        mockMvc.perform(post(UBS_LINK + "/processOrder/{id}", 1L)
            .content(orderResponseDtoJSON)
            .principal(PRINCIPAL)
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

        when(userRemoteClient.findUuidByEmail(anyString())).thenReturn(RANDOM_UUID);
        when(ubsManagementService.getOrderDetailStatus(anyLong())).thenReturn(orderDetailStatusDto);

        String orderResponseDtoJSON = OBJECT_MAPPER.writeValueAsString(dto);

        mockMvc.perform(post(UBS_LINK + "/processOrder/{id}", orderId)
            .content(orderResponseDtoJSON)
            .principal(PRINCIPAL)
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
        mockMvc.perform(get(UBS_LINK + "/user-info" + "/{orderId}", 1L)
            .principal(PRINCIPAL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).getUserAndUserUbsAndViolationsInfoByOrderId(1L, null);
    }

    @Test
    void updatesRecipientsInfo() throws Exception {
        UbsCustomersDto ubsCustomersDto = getUbsCustomersDto();
        UbsCustomersDtoUpdate ubsCustomersDtoUpdate = getUbsCustomersDtoUpdate();
        when(ubsClientService.updateUbsUserInfoInOrder(ubsCustomersDtoUpdate, null)).thenReturn(ubsCustomersDto);
        mockMvc.perform(put(UBS_LINK + "/update-recipients-data")
            .contentType(MediaType.APPLICATION_JSON)
            .content(OBJECT_MAPPER.writeValueAsString(ubsCustomersDtoUpdate))
            .principal(PRINCIPAL))
            .andExpect(status().isOk());

        verify(ubsClientService).updateUbsUserInfoInOrder(ubsCustomersDtoUpdate, null);
    }

    @Test
    void updatesRecipientsInfoWithOutUser() {
        UbsCustomersDtoUpdate ubsCustomersDtoUpdate = getUbsCustomersDtoUpdate();

        when(ubsClientService.updateUbsUserInfoInOrder(ubsCustomersDtoUpdate, null))
            .thenThrow(UBSuserNotFoundException.class);

        ServletException exception =
            assertThrows(ServletException.class, () -> {
                mockMvc.perform(put(UBS_LINK + "/update-recipients-data")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(OBJECT_MAPPER.writeValueAsString(ubsCustomersDtoUpdate))
                    .principal(PRINCIPAL))
                    .andExpect(status().isBadRequest());
            });

        assertInstanceOf(UBSuserNotFoundException.class, exception.getCause());
        verify(ubsClientService).updateUbsUserInfoInOrder(ubsCustomersDtoUpdate, null);
    }

    @Test
    void getsCancellationReason() throws Exception {
        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn(RANDOM_UUID);
        when(ubsClientService.getOrderCancellationReason(anyLong(), anyString())).thenReturn(dto);

        mockMvc.perform(get(UBS_LINK + "/order/{id}/cancellation", 1L)
            .principal(PRINCIPAL))
            .andExpect(status().isOk());
        verify(ubsClientService).getOrderCancellationReason(1L, RANDOM_UUID);
    }

    @Test
    void testGetOrderHistoryByOrderId() throws Exception {
        mockMvc.perform(get(UBS_LINK + "/order_history" + "/{orderId}", 1L)
            .principal(PRINCIPAL))
            .andExpect(status().isOk());

        verify(ubsClientService, times(1))
            .getAllEventsForOrder(1L, "test@gmail.com", "en");
    }

    @Test
    void getFondyStatusPayment2() throws Exception {
        mockMvc.perform(get(UBS_LINK + "/getFondyStatus/{orderId}", 1)
            .principal(PRINCIPAL))
            .andExpect(status().isOk());
    }

    @Mock
    RedirectionConfigProp redirectionConfigProp;

    @Test
    @SneakyThrows
    void getInfoAboutTariffTest() {
        mockMvc.perform(get(UBS_LINK + "/tariffinfo/{locationId}", 1L)
            .param("courierId", "1"))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getAllActiveLocationsByCourierIdTest() {
        mockMvc.perform(get(UBS_LINK + "/locations/{courierId}", 1L)
            .principal(PRINCIPAL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getAllActiveCouriersTest() throws Exception {
        mockMvc.perform(get(UBS_LINK + "/getAllActiveCouriers")).andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getTariffForOrder() {
        mockMvc.perform(get(UBS_LINK + "/orders/1/tariff")
            .principal(PRINCIPAL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void checkIfTariffExistsByIdTest() throws Exception {
        Long tariffId = 1L;
        when(ubsClientService.checkIfTariffExistsById(tariffId)).thenReturn(true);

        mockMvc.perform(get(UBS_LINK + "/check-if-tariff-exists/{id}", tariffId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));

        verify(ubsClientService).checkIfTariffExistsById(tariffId);
    }

    @Test
    void getAllLocationsTest() throws Exception {
        List<LocationsDto> locationsDtoList = Arrays.asList(new LocationsDto(), new LocationsDto());
        when(ubsClientService.getAllLocations()).thenReturn(locationsDtoList);

        mockMvc.perform(get(UBS_LINK + "/locations")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(locationsDtoList)));

        verify(ubsClientService).getAllLocations();
    }

    @Test
    void getTariffIdByLocationIdTest() throws Exception {
        Long locationId = 1L;
        Long tariffId = 2L;
        when(ubsClientService.getTariffIdByLocationId(locationId)).thenReturn(tariffId);

        mockMvc.perform(get(UBS_LINK + "/tariffs/{locationId}", locationId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(String.valueOf(tariffId)));

        verify(ubsClientService).getTariffIdByLocationId(locationId);
    }

    @Test
    void getAllLocationsByCourierIdTest() throws Exception {
        Long courierId = 1L;
        List<LocationsDto> locationsDtoList = Arrays.asList(new LocationsDto(), new LocationsDto());
        when(ubsClientService.getAllLocationsByCourierId(courierId)).thenReturn(locationsDtoList);

        mockMvc.perform(get(UBS_LINK + "/locationsByCourier/{courierId}", courierId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(locationsDtoList)));

        verify(ubsClientService).getAllLocationsByCourierId(courierId);
    }

    private void setRedirectionConfigProp() {
        RedirectionConfigProp redirectionConfig = getRedirectionConfig();

        Arrays.stream(OrderController.class.getDeclaredFields())
            .filter(field -> field.getName().equals("redirectionConfigProp"))
            .forEach(field -> {
                field.setAccessible(true);
                try {
                    field.set(orderController, redirectionConfig);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
    }
}
