package greencity.controller;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUbsCustomersDtoUpdate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.configuration.RedirectionConfigProp;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.LocationsDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.repository.OrderRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSManagementService;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class OrderControllerTest {
    private static final String ubsLink = "/ubs";
    private final Principal principal = getPrincipal();
    @Mock
    UBSClientService ubsClientService;

    @Mock
    UBSManagementService ubsManagementService;

    @Mock
    UserRepository userRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    OrderController orderController;

    @Mock
    RedirectionConfigProp prop;
    private MockMvc mockMvc;
    @Mock
    private UBSUserRepository ubSuserRepository;
    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(orderController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRepository))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes))
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
        when(userRepository.findUuidByRecipientEmail((anyString())))
            .thenReturn(Optional.of("35467585763t4sfgchjfuyetf"));

        mockMvc.perform(get(ubsLink + "/details-for-existing-order/{orderId}", "1")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(userRepository).findUuidByRecipientEmail("test@gmail.com");
        verify(ubsClientService).getFirstPageDataByOrderId("35467585763t4sfgchjfuyetf", 1L);
    }

    @Test
    void checkIfCertificateAvailable() throws Exception {
        when(userRepository.findUuidByRecipientEmail(principal.getName())).thenReturn(
            Optional.of("35467585763t4sfgchjfuyetf"));

        String certificateCode = "1111-1111";
        mockMvc.perform(get(ubsLink + "/certificate/{code}", certificateCode)
                .principal(principal))
            .andExpect(status().isOk());
        verify(ubsClientService).checkCertificate(certificateCode, "35467585763t4sfgchjfuyetf");
    }

    @Test
    void getUBSusers() throws Exception {
        when(userRepository.findUuidByRecipientEmail((anyString())))
            .thenReturn(Optional.of("35467585763t4sfgchjfuyetf"));

        mockMvc.perform(get(ubsLink + "/personal-data")
                .principal(principal))
            .andExpect(status().isOk());

        verify(userRepository).findUuidByRecipientEmail("test@gmail.com");
        verify(ubsClientService).getSecondPageData("35467585763t4sfgchjfuyetf");
    }

    @Test
    void processOrder() throws Exception {
        when(userRepository.findUuidByRecipientEmail((anyString())))
            .thenReturn(Optional.of("35467585763t4sfgchjfuyetf"));
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String orderResponseDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/processOrder")
                .content(orderResponseDtoJSON)
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(userRepository).findUuidByRecipientEmail("test@gmail.com");
        verify(ubsClientService).processNewOrder(any(), eq("35467585763t4sfgchjfuyetf"));

    }

    @Test
    void processOrderId() throws Exception {
        Long orderId = 1L;
        String uuid = "35467585763t4sfgchjfuyetf";
        ObjectMapper objectMapper = new ObjectMapper();
        OrderResponseDto dto = ModelUtils.getOrderResponseDto();
        String orderResponseDtoJSON = objectMapper.writeValueAsString(dto);

        PaymentSystemResponse resultObject = PaymentSystemResponse.builder()
            .orderId(orderId)
            .link("Link")
            .build();
        String resultJson = objectMapper.writeValueAsString(resultObject);

        when(userRepository.findUuidByRecipientEmail(anyString())).thenReturn(Optional.of(uuid));
        when(ubsClientService.processExistingOrder(any(OrderResponseDto.class), anyString(), anyLong()))
            .thenReturn(resultObject);

        mockMvc.perform(post(ubsLink + "/processOrder/{id}", orderId)
            .content(orderResponseDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(resultJson));

        verify(userRepository).findUuidByRecipientEmail(anyString());
        verify(ubsClientService).processExistingOrder(any(OrderResponseDto.class), anyString(), anyLong());
    }

    @Test
    void getOrderDetailsByOrderId() throws Exception {
        when(userRepository.findUuidByRecipientEmail(principal.getName())).thenReturn(
            Optional.of("35467585763t4sfgchjfuyetf"));
        mockMvc.perform(get(ubsLink + "/user-info" + "/{orderId}", 1L)
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void updatesRecipientsInfo() throws Exception {
        UbsCustomersDtoUpdate ubsCustomersDtoUpdate = getUbsCustomersDtoUpdate();
        when(userRepository.findUuidByRecipientEmail(principal.getName())).thenReturn(Optional.empty());
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(put(ubsLink + "/update-recipients-data")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ubsCustomersDtoUpdate))
            .principal(principal))
            .andExpect(status().isNotFound());
    }

    @Test
    void updatesRecipientsInfoWithOutUser() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        UbsCustomersDtoUpdate ubsCustomersDtoUpdate = getUbsCustomersDtoUpdate();

        when(userRepository.findUuidByRecipientEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(put(ubsLink + "/update-recipients-data")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ubsCustomersDtoUpdate))
            .principal(principal))
            .andExpect(status().isNotFound());
    }

    @Test
    void getsCancellationReason() throws Exception {
        OrderCancellationReasonDto dto = ModelUtils.getCancellationDto();
        when(userRepository.findUuidByRecipientEmail((anyString())))
            .thenReturn(Optional.of("35467585763t4sfgchjfuyetf"));
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
        PaymentResponseWayForPay mockResponse = new PaymentResponseWayForPay();
        mockResponse.setStatus("approved");
        mockResponse.setOrderReference(dto.getOrderReference());

        when(ubsClientService.convertMapIntoPaymentResponseDto(anyMap()))
            .thenReturn(mockResponse);

        mockMvc.perform(post(ubsLink + "/receivePayment")
            .param("merchantAccount", dto.getMerchantAccount())
            .param("orderReference", dto.getOrderReference())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.orderReference").value(dto.getOrderReference()))
            .andExpect(jsonPath("$.status").value("approved"));
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
        when(userRepository.findUuidByRecipientEmail(principal.getName())).thenReturn(
            Optional.of("35467585763t4sfgchjfuyetf"));
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
    void checkIfTariffExistsById_Returns200_WhenTariffExists() throws Exception {
        Long tariffId = 1L;
        when(ubsClientService.checkIfTariffExistsById(tariffId)).thenReturn(true);

        mockMvc.perform(get(ubsLink + "/check-if-tariff-exists/{id}", tariffId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(ubsClientService).checkIfTariffExistsById(tariffId);
    }

    @Test
    void checkIfTariffExistsById_Returns404_WhenTariffDoesNotExist() throws Exception {
        Long tariffId = 999999L;
        when(ubsClientService.checkIfTariffExistsById(tariffId)).thenReturn(false);

        mockMvc.perform(get(ubsLink + "/check-if-tariff-exists/{id}", tariffId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));

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
        List<Long> tariffId = List.of(2L);
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
}
