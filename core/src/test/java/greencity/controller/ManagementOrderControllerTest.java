package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.dto.order.*;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.user.AddBonusesToUserDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.violation.ViolationDetailInfoDto;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import greencity.service.ubs.*;
import greencity.service.ubs.manager.BigOrderTableServiceView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.security.Principal;
import java.util.Optional;

import static greencity.ModelUtils.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ManagementOrderControllerTest {

    private static final String ubsLink = "/ubs/management";

    private MockMvc mockMvc;

    @Mock
    UBSManagementService ubsManagementService;

    @Mock
    private ViolationService violationService;

    @Mock
    CoordinateService coordinateService;

    @Mock
    CertificateService certificateService;

    @Mock
    LiqPayService liqPayService;

    @Mock
    private Validator mockValidator;

    @InjectMocks
    ManagementOrderController managementOrderController;

    @Mock
    BigOrderTableServiceView bigOrderTableServiceView;

    private final Principal principal = getUuid();

    public static final String contentForaddingcontroller = "{\n"
        + " \"code\": \"1111-2222\",\n" +
        " \"monthCount\": 8,\n" +
        " \"points\": 100\n"
        + "}";

    public static final String contentForUpdatingController = "{\n"
        + " \"district\": \"test\",\n"
        + " \"street\": \"test\",\n"
        + " \"houseCorpus\": \"4\",\n"
        + " \"entranceNumber\": \"2\",\n"
        + " \"houseNumber\": \"1\"\n"
        + "}";

    public static final String contentForUpdatingOrderDetailController = "[\n"
        + "{\n"
        + "\"amount\": 0,\n"
        + "\"bagId\": 0,\n"
        + "\"confirmedQuantity\": 0,\n"
        + "\"exportedQuantity\": 0,\n"
        + "\"orderId\": 0\n"
        + "}\n"
        + "]";

    public static final String contentForUpdatingEmployeeByOrderController = "{\n"
        + "\"employeeOrderPositionDTOS\": [\n"
        + "{\n"
        + "\"name\": \"Alisson Becker\",\n"
        + "\"positionId\": 1\n"
        + "}\n"
        + "],\n"
        + "\"orderId\": 8\n"
        + "}";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementOrderController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void getAllCertificates() throws Exception {
        CertificateFilterCriteria certificateFilterCriteria = new CertificateFilterCriteria();
        CertificatePage certificatePage = new CertificatePage();
        mockMvc
            .perform(MockMvcRequestBuilders.get(ubsLink + "/getAllCertificates"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        verify(certificateService).getCertificatesWithFilter(certificatePage, certificateFilterCriteria);
    }

    @Test
    void addCertificateTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(ubsLink + "/addCertificate")
            .content(contentForaddingcontroller)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isCreated());
        CertificateDtoForAdding certificateDtoForAdding = CertificateDtoForAdding.builder()
            .code("1111-2222")
            .points(100)
            .monthCount(8)
            .build();
        verify(certificateService, times(1)).addCertificate(certificateDtoForAdding);
    }

    @Test
    void deleteCertificateTest() throws Exception {
        doNothing().when(certificateService).deleteCertificate("1111-1234");

        mockMvc.perform(delete(ubsLink + "/deleteCertificate" + "/" + "{code}", "1111-1234")
            .principal(principal)).andExpect(status().isOk());
        verify(certificateService, times(1)).deleteCertificate("1111-1234");
    }

    @Test
    void getAddressByOrder() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/read-address-order" + "/{id}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void getOrderDetail() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/read-order-info" + "/{id}", 1L)
            .param("language", "ua"))
            .andExpect(status().isOk());
    }

    @Test
    void getSumOrderDetail() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/get-order-sum-detail" + "/{id}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void answersNotFoundWhenNoViolationWithGivenOrderId() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/violation-details" + "/{orderId}", 1L))
            .andExpect(status().isNotFound());

        verify(violationService).getViolationDetailsByOrderId(1L);
    }

    @Test
    void returnsDetailsAboutViolationWithGivenOrderId() throws Exception {
        ViolationDetailInfoDto violationDetailInfoDto = getViolationDetailInfoDto();
        when(violationService.getViolationDetailsByOrderId(1L)).thenReturn(Optional.of(violationDetailInfoDto));

        this.mockMvc.perform(get(ubsLink + "/violation-details" + "/{orderId}", 1L))
            .andExpect(status().isOk());

        verify(violationService).getViolationDetailsByOrderId(1L);
    }

    @Test
    void updateOrderStatusesDetail() throws Exception {
        OrderDetailStatusDto dto = ModelUtils.getOrderDetailStatusDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String orderResponceDtoJSON = objectMapper.writeValueAsString(dto);
        this.mockMvc.perform(put(ubsLink + "/update-order-detail-status" + "/{id}", 1L)
            .content(orderResponceDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void geOrderStatusesDetail() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/read-order-detail-status" + "/{id}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void getOrderExportDetail() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/get-order-export-details" + "/{id}", 1L))
            .andExpect(status().isOk());

        verify(ubsManagementService).getOrderExportDetails(1L);
    }

    @Test
    void getAllDataForOrderTest() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/get-all-orders" + "/{uuid}", "uuid7"));
        verify(ubsManagementService).getOrdersForUser("uuid7");
    }

    @Test
    void getDataForOrderStatusPageTest() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/get-data-for-order/{id}", 1L)
            .principal(principal));
        verify(ubsManagementService).getOrderStatusData(1L, "35467585763t4sfgchjfuyetf");
    }

    @Test
    void checkEmployeeForOrderPageTest() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/check-employee-for-order/{id}", 1L)
            .principal(principal));
        verify(ubsManagementService).checkEmployeeForOrder(1L, "35467585763t4sfgchjfuyetf");
    }

    @Test
    void updateOrderExportedDetail() throws Exception {
        ExportDetailsDto dto = ModelUtils.getOrderDetailExportDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String orderResponceDtoJSON = objectMapper.writeValueAsString(dto);
        this.mockMvc.perform(put(ubsLink + "/update-order-export-details" + "/{id}", 1L)
            .content(orderResponceDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void deletesViolationFromOrder() throws Exception {
        mockMvc.perform(delete(ubsLink + "/delete-violation-from-order" + "/{orderId}", 1L))
            .andExpect(status().isOk());

        verify(violationService).deleteViolation(1L, null);
    }

    @Test
    void addManualPayment() throws Exception {
        ManualPaymentRequestDto dto = getRequestDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);
        MockMultipartFile jsonFile = new MockMultipartFile("manualPaymentDto",
            "", "application/json", responseJSON.getBytes());

        mockMvc.perform(multipart(ubsLink + "/add-manual-payment/{id}", 1)
            .file(jsonFile)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void deleteManualPayment() throws Exception {
        mockMvc.perform(delete(ubsLink + "/delete-manual-payment/{id}", 1l))
            .andExpect(status().isOk()).andDo(print());
    }

    @Test
    void updateManualPayment() throws Exception {
        ManualPaymentRequestDto dto = getRequestDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);
        MockMultipartFile jsonFile = new MockMultipartFile("manualPaymentDto",
            "", "application/json", responseJSON.getBytes());

        MockMultipartHttpServletRequestBuilder builder =
            MockMvcRequestBuilders.multipart(ubsLink + "/update-manual-payment/{id}", 1l);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(builder.file(jsonFile)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getAllEmployeeByPositionTest() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/get-all-employee-by-position" + "/{id}", 1L)
            .principal(principal))
            .andExpect(status().isOk());
    }

    @Test
    void groupCoordsWithSpecifiedOnes() throws Exception {
        this.mockMvc.perform(
            post(ubsLink + "/group-undelivered-with-specified")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"latitude\":84.525254,\"longitude\":12.436964}]"))
            .andExpect(status().isOk());
    }

    @Test
    void saveAdminCommentToOrder() throws Exception {
        AdminCommentDto adminCommentDto = ModelUtils.getAdminComment();
        ObjectMapper objectMapper = new ObjectMapper();
        String writeValueAsString = objectMapper.writeValueAsString(adminCommentDto);

        mockMvc.perform(MockMvcRequestBuilders.post(ubsLink + "/save-admin-comment", 1L)
            .content(writeValueAsString)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void updateEcoNumberForOrder() throws Exception {
        EcoNumberDto ecoNumberDto = getEcoNumberDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String writeValueAsString = objectMapper.writeValueAsString(ecoNumberDto);

        mockMvc.perform(MockMvcRequestBuilders.put(ubsLink + "/update-eco-store{id}", 1L)
            .content(writeValueAsString)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void getCustomTableParametersTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ubsLink + "/getOrdersViewParameters"))
            .andExpect(status().isOk());
    }

    @Test
    void setCustomTableTest() throws Exception {
        mockMvc.perform(put(ubsLink + "/changeOrdersTableView"))
            .andExpect(status().isOk());
    }

    @Test
    void allUndeliveredCoordsTest() throws Exception {
        mockMvc.perform(get(ubsLink + "/all-undelivered"))
            .andExpect(status().isOk());

        verify(coordinateService).getAllUndeliveredOrdersWithLiters();
    }

    @Test
    void addPointsToUserTest() throws Exception {
        AddingPointsToUserDto dto = ModelUtils.getAddingPointsToUserDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String dtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch(ubsLink + "/addPointsToUser")
            .content(dtoJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void paymentInfoTest() throws Exception {
        mockMvc.perform(get(ubsLink + "/getPaymentInfo")
            .principal(principal)
            .param("orderId", "1")
            .param("sumToPay", "1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void groupCoordsTest() throws Exception {
        mockMvc.perform(get(ubsLink + "/group-undelivered")
            .param("radius", "2.04")
            .param("litres", "2")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getUserViolationsTest() throws Exception {
        mockMvc.perform(get(ubsLink + "/getUsersViolations")
            .param("email", "max@email.com")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void returnOverpaymentAsMoneyInfoTest() throws Exception {
        mockMvc.perform(get(ubsLink + "/return-overpayment-as-money-info")
            .param("orderId", "2")
            .param("sumToPay", "1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void setCustomTable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(ubsLink + "/changeOrdersTableView")
            .content("titles1,titles2,titles3")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getCustomTableParameters() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/getOrdersViewParameters", "uuid1"))
            .andExpect(status().isOk());
    }

    @Test
    void getOrders() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/bigOrderTable", "uuid1")
            .principal(principal))
            .andExpect(status().isOk());
    }

    @Test
    void getOrderBagsInfo() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/getOrderBagsInfo" + "/{id}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void getUpdateAllOrderPageAdminInfoTest() throws Exception {
        UpdateAllOrderPageDto dto = ModelUtils.getUpdateAllOrderPageDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String JsonDto = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(ubsLink + "/all-order-page-admin-info")
            .content(JsonDto)
            .principal(principal)
            .param("lang", "ua")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void addPaymentDiscountTest() throws Exception {
        AddBonusesToUserDto addBonusesToUserDto = getAddBonusesToUserDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonDto = objectMapper.writeValueAsString(addBonusesToUserDto);

        mockMvc.perform(post(ubsLink + "/add-bonuses-user/{id}", 1L)
            .content(jsonDto)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

    }

    @Test
    void getOrderCancellationReason() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/get-order-cancellation-reason" + "/{id}", 1L))
            .andExpect(status().isOk());
        verify(ubsManagementService).getOrderCancellationReason(1L);
    }

    @Test
    void getNotTakenOrderReason() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/get-not-taken-order-reason/{id}", 1L))
            .andExpect(status().isOk());
        verify(ubsManagementService).getNotTakenOrderReason(1L);
    }
}
