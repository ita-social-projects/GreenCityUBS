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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.location.LocationsForTariffDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.dto.tariff.GetActiveTariffInfoDto;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.repository.UserRepository;
import greencity.service.ubs.AddressService;
import greencity.service.ubs.CertificateService;
import greencity.service.ubs.EventService;
import greencity.service.ubs.order.OrderCheckoutService;
import greencity.service.ubs.order.OrderService;
import greencity.service.ubs.payment.ProcessPaymentService;
import greencity.service.ubs.tariff.TariffService;
import greencity.service.ubs.user.CourierService;
import greencity.service.ubs.user.UserService;
import greencity.service.ubs.wayforpay.WayForPayRedirectService;
import greencity.service.ubs.wayforpay.WayForPayResultService;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    UserRepository userRepository;

    @Mock
    private WayForPayRedirectService wayForPayRedirectService;

    @Mock
    private WayForPayResultService wayForPayResultService;

    @Mock
    private ProcessPaymentService processPaymentService;

    @Mock
    private OrderCheckoutService orderCheckoutService;

    @Mock
    private OrderService orderService;

    @Mock
    private TariffService tariffService;

    @Mock
    private AddressService addressService;

    @Mock
    private EventService eventService;

    @Mock
    private CertificateService certificateService;

    @Mock
    private UserService userService;

    @Mock
    private CourierService courierService;

    @InjectMocks
    OrderController orderController;

    private MockMvc mockMvc;

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

        verify(orderCheckoutService).getFirstPageDataByTariff(1L);
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
        verify(orderCheckoutService).getFirstPageDataByOrderId("35467585763t4sfgchjfuyetf", 1L);
    }

    @Test
    void checkIfCertificateAvailable() throws Exception {
        when(userRepository.findUuidByRecipientEmail(principal.getName())).thenReturn(
            Optional.of("35467585763t4sfgchjfuyetf"));

        String certificateCode = "1111-1111";
        mockMvc.perform(get(ubsLink + "/certificate/{code}", certificateCode)
                .principal(principal))
            .andExpect(status().isOk());
        verify(certificateService).checkCertificate(certificateCode, "35467585763t4sfgchjfuyetf");
    }

    @Test
    void getUBSusers() throws Exception {
        when(userRepository.findUuidByRecipientEmail((anyString())))
            .thenReturn(Optional.of("35467585763t4sfgchjfuyetf"));

        mockMvc.perform(get(ubsLink + "/personal-data")
                .principal(principal))
            .andExpect(status().isOk());

        verify(userRepository).findUuidByRecipientEmail("test@gmail.com");
        verify(orderCheckoutService).getSecondPageData("35467585763t4sfgchjfuyetf");
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
        verify(processPaymentService).processNewOrder(any(), eq("35467585763t4sfgchjfuyetf"));

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
        when(processPaymentService.processExistingOrder(any(OrderResponseDto.class), anyString(), anyLong()))
            .thenReturn(resultObject);

        mockMvc.perform(post(ubsLink + "/processOrder/{id}", orderId)
            .content(orderResponseDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(resultJson));

        verify(userRepository).findUuidByRecipientEmail(anyString());
        verify(processPaymentService).processExistingOrder(any(OrderResponseDto.class), anyString(), anyLong());
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
        when(orderService.getOrderCancellationReason(anyLong(), anyString())).thenReturn(dto);

        mockMvc.perform(get(ubsLink + "/order/{id}/cancellation", 1L)
            .principal(principal))
            .andExpect(status().isOk());
        verify(orderService).getOrderCancellationReason(1L, "35467585763t4sfgchjfuyetf");
    }

    @Test
    void testGetOrderHistoryByOrderId() throws Exception {
        mockMvc.perform(get(ubsLink + "/order_history" + "/{orderId}", 1L)
            .principal(principal))
            .andExpect(status().isOk());

        verify(eventService, times(1))
            .getAllEventsForOrder(1L, "test@gmail.com", "en");
    }

    @Test
    void receivePaymentTest() throws Exception {
        PaymentResponseDto dto = ModelUtils.getPaymentResponseDto();
        PaymentResponseWayForPay mockResponse = new PaymentResponseWayForPay();
        mockResponse.setStatus("approved");
        mockResponse.setOrderReference(dto.getOrderReference());

        when(wayForPayResultService.convertMapIntoPaymentResponseDto(anyMap()))
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
    void handleWayForPayReturn_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post(ubsLink + "/payment/return")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("orderReference", "ORDER123")
            .param("amount", "100.00"))
            .andExpect(status().isFound());

        verify(wayForPayRedirectService).redirectUser(
            Mockito.any(Map.class));
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
        when(tariffService.checkIfTariffExistsById(tariffId)).thenReturn(true);

        mockMvc.perform(get(ubsLink + "/check-if-tariff-exists/{id}", tariffId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(tariffService).checkIfTariffExistsById(tariffId);
    }

    @Test
    void checkIfTariffExistsById_Returns404_WhenTariffDoesNotExist() throws Exception {
        Long tariffId = 999999L;
        when(tariffService.checkIfTariffExistsById(tariffId)).thenReturn(false);

        mockMvc.perform(get(ubsLink + "/check-if-tariff-exists/{id}", tariffId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));

        verify(tariffService).checkIfTariffExistsById(tariffId);
    }

    @Test
    void getAllLocationsTest() throws Exception {
        List<LocationsForTariffDto> locationsForTariffDtoList =
            Arrays.asList(new LocationsForTariffDto(), new LocationsForTariffDto());
        when(addressService.getAllLocations()).thenReturn(locationsForTariffDtoList);

        mockMvc.perform(get(ubsLink + "/locations")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(new ObjectMapper().writeValueAsString(locationsForTariffDtoList)));

        verify(addressService).getAllLocations();
    }

    @Test
    void getTariffIdByLocationIdTest() throws Exception {
        Long locationId = 1L;
        List<Long> tariffId = List.of(2L);
        when(tariffService.getTariffIdByLocationId(locationId)).thenReturn(tariffId);

        mockMvc.perform(get(ubsLink + "/tariffs/{locationId}", locationId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(String.valueOf(tariffId)));

        verify(tariffService).getTariffIdByLocationId(locationId);
    }

    @Test
    void getAllLocationsByCourierIdTest() throws Exception {
        Long id = 1L;
        List<LocationsForTariffDto> locationsForTariffDtoList =
            Arrays.asList(new LocationsForTariffDto(), new LocationsForTariffDto());
        when(addressService.getAllLocationsByCourierId(id)).thenReturn(locationsForTariffDtoList);

        mockMvc.perform(get(ubsLink + "/locationsByCourier/" + id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(new ObjectMapper().writeValueAsString(locationsForTariffDtoList)));

        verify(addressService).getAllLocationsByCourierId(id);
    }

    @Test
    void cancelPaymentAttemptTest() throws Exception {
        Long orderId = 1L;
        String uuid = "35467585763t4sfgchjfuyetf";

        when(userRepository.findUuidByRecipientEmail(principal.getName())).thenReturn(
            Optional.of("35467585763t4sfgchjfuyetf"));

        mockMvc.perform(post(ubsLink + "/cancelPaymentAttempt/{id}", orderId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(processPaymentService).cancelPaymentAttempt(uuid, orderId);
    }

    @Test
    void getAllActiveTariffs() throws Exception {
        List<GetActiveTariffInfoDto> listOfTariffs =
            Arrays.asList(new GetActiveTariffInfoDto(), new GetActiveTariffInfoDto());
        when(tariffService.getTariffsInfo(1L)).thenReturn(listOfTariffs);

        mockMvc.perform(get(ubsLink + "/activeTariffsInfo/" + 1L))
            .andExpect(status().isOk())
            .andExpect(content().json(new ObjectMapper().writeValueAsString(listOfTariffs)));

        verify(tariffService).getTariffsInfo(1L);
    }

    @Test
    void redirectToWayForPay_ShouldReturnFoundAndLocationHeader() throws Exception {
        when(processPaymentService.formedLink(1L)).thenReturn("test-link");

        mockMvc.perform(get(ubsLink + "/redirect/" + 1L))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "test-link"));

        verify(processPaymentService).formedLink(1L);
    }
}
