package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.dto.order.AdminCommentDto;
import greencity.dto.order.EcoNumberDto;
import greencity.dto.order.ExportDetailsDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.UpdateAllOrderPageDto;
import greencity.dto.order.UpdateOrderPageAdminDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.violation.ViolationDetailInfoDto;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import greencity.service.ubs.CertificateService;
import greencity.service.ubs.CoordinateService;
import greencity.service.ubs.PaymentService;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSManagementService;
import greencity.service.ubs.ViolationService;
import greencity.service.ubs.manager.BigOrderTableServiceView;
import java.security.Principal;
import java.util.Optional;
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

import static greencity.ModelUtils.getEcoNumberDto;
import static greencity.ModelUtils.getRequestDto;
import static greencity.ModelUtils.getUpdateOrderPageAdminDto;
import static greencity.ModelUtils.getUuid;
import static greencity.ModelUtils.getViolationDetailInfoDto;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    UBSClientService ubsClientService;

    @Mock
    private Validator mockValidator;

    @InjectMocks
    ManagementOrderController managementOrderController;

    @Mock
    BigOrderTableServiceView bigOrderTableServiceView;

    @Mock
    PaymentService paymentService;

    private final Principal principal = getUuid();

    public static final String contentForaddingcontroller = """
        {
         "code": "1111-2222",
         "monthCount": 8,
         "points": 100
        }""";

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
        OrderDetailStatusDto dto = ModelUtils.getPaidOrderDetailStatusDto();
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
        mockMvc.perform(delete(ubsLink + "/delete-manual-payment/{id}", 1L))
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
            multipart(ubsLink + "/update-manual-payment/{id}", 1L);
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
        String jsonDto = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(ubsLink + "/all-order-page-admin-info")
            .content(jsonDto)
            .principal(principal)
            .param("lang", "ua")
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

    @Test
    void updatePageAdminInfoTest() throws Exception {
        UpdateOrderPageAdminDto dto = getUpdateOrderPageAdminDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);

        MockMultipartFile jsonFile = new MockMultipartFile(
            "updateOrderPageAdminDto",
            "updateOrderPageAdminDto.json",
            "application/json",
            responseJSON.getBytes());

        MockMultipartHttpServletRequestBuilder builder =
            MockMvcRequestBuilders.multipart(ubsLink + "/update-order-page-admin-info/{id}", 1L);
        builder.with(request -> {
            request.setMethod("PATCH");
            return request;
        });

        mockMvc.perform(
            builder.file(jsonFile)
                .param("language", "en")
                .principal(principal)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isCreated());
    }

    @Test
    void checkIfOrderStatusIsFormedToCanceledTest() throws Exception {
        Long orderId = 1L;
        when(ubsManagementService.checkIfOrderStatusIsFormedToCanceled(orderId)).thenReturn(true);
        mockMvc.perform(get(ubsLink + "/check-status-transition/formed-to-canceled/{id}", orderId)
            .contentType(MediaType.APPLICATION_XML))
            .andExpect(status().isOk())
            .andExpect(content().string("<Boolean>true</Boolean>"));
        verify(ubsManagementService).checkIfOrderStatusIsFormedToCanceled(orderId);
    }
}
