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
import greencity.service.ubs.UBSManagementService;
import greencity.service.ubs.ViolationService;
import greencity.service.ubs.manager.BigOrderTableServiceView;
import java.security.Principal;
import java.util.Optional;

import greencity.validators.payment.ManualPaymentRequestValidator;
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
import static greencity.ModelUtils.getEcoNumberDto;
import static greencity.ModelUtils.getManualPaymentRequestDto;
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
    private static final String ubsLink = "/ubs";
    private static final String ubsManagementLink = ubsLink + "/management";

    private MockMvc mockMvc;

    @Mock
    UBSManagementService ubsManagementService;

    @Mock
    private ViolationService violationService;

    @Mock
    CoordinateService coordinateService;

    @Mock
    CertificateService certificateService;

    @InjectMocks
    ManagementOrderController managementOrderController;

    @Mock
    BigOrderTableServiceView bigOrderTableServiceView;

    @Mock
    private ManualPaymentRequestValidator manualPaymentRequestValidator;

    @Mock
    PaymentService paymentService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Principal principal = getUuid();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementOrderController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getAllCertificates() throws Exception {
        CertificateFilterCriteria certificateFilterCriteria = new CertificateFilterCriteria();
        CertificatePage certificatePage = new CertificatePage();
        mockMvc
            .perform(MockMvcRequestBuilders.get(ubsManagementLink + "/getAllCertificates"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        verify(certificateService).getCertificatesWithFilter(certificatePage, certificateFilterCriteria);
    }

    @Test
    void addCertificateTest() throws Exception {
        CertificateDtoForAdding certificateDtoForAdding = ModelUtils.getCertificateDtoForAdding();
        String json = objectMapper.writeValueAsString(certificateDtoForAdding);
        mockMvc.perform(MockMvcRequestBuilders.post(ubsManagementLink + "/addCertificate")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isCreated());
        verify(certificateService, times(1)).addCertificate(certificateDtoForAdding);
    }

    @Test
    void deleteCertificateTest() throws Exception {
        doNothing().when(certificateService).deleteCertificate("1111-1234");

        mockMvc.perform(delete(ubsManagementLink + "/deleteCertificate" + "/" + "{code}", "1111-1234")
            .principal(principal)).andExpect(status().isOk());
        verify(certificateService, times(1)).deleteCertificate("1111-1234");
    }

    @Test
    void getOrderDetail() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/read-order-info" + "/{id}", 1L)
            .param("language", "ua"))
            .andExpect(status().isOk());
    }

    @Test
    void getSumOrderDetail() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/get-order-sum-detail" + "/{id}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void answersNotFoundWhenNoViolationWithGivenOrderId() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/violation-details" + "/{orderId}", 1L))
            .andExpect(status().isNotFound());

        verify(violationService).getViolationDetailsByOrderId(1L);
    }

    @Test
    void returnsDetailsAboutViolationWithGivenOrderId() throws Exception {
        ViolationDetailInfoDto violationDetailInfoDto = getViolationDetailInfoDto();
        when(violationService.getViolationDetailsByOrderId(1L)).thenReturn(Optional.of(violationDetailInfoDto));

        this.mockMvc.perform(get(ubsManagementLink + "/violation-details" + "/{orderId}", 1L))
            .andExpect(status().isOk());

        verify(violationService).getViolationDetailsByOrderId(1L);
    }

    @Test
    void updateOrderStatusesDetail() throws Exception {
        OrderDetailStatusDto dto = ModelUtils.getPaidOrderDetailStatusDto();

        String orderResponceDtoJSON = objectMapper.writeValueAsString(dto);
        this.mockMvc.perform(put(ubsManagementLink + "/update-order-detail-status" + "/{id}", 1L)
            .content(orderResponceDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void geOrderStatusesDetail() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/read-order-detail-status" + "/{id}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void getOrderExportDetail() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/get-order-export-details" + "/{id}", 1L))
            .andExpect(status().isOk());

        verify(ubsManagementService).getOrderExportDetails(1L);
    }

    @Test
    void getAllDataForOrderTest() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/get-all-orders" + "/{uuid}", "uuid7"));
        verify(ubsManagementService).getOrdersForUser("uuid7");
    }

    @Test
    void getOrdersTotalAmountTest() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/orders/count")
            .principal(principal))
            .andExpect(status().isOk());

        verify(bigOrderTableServiceView, times(1)).getTotalNumberOfOrdersByEmployee(principal.getName());
    }

    @Test
    void getDataForOrderStatusPageTest() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/get-data-for-order/{id}", 1L)
            .principal(principal));
        verify(ubsManagementService).getOrderStatusData(1L, "35467585763t4sfgchjfuyetf");
    }

    @Test
    void checkEmployeeForOrderPageTest() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/check-employee-for-order/{id}", 1L)
            .principal(principal));
        verify(ubsManagementService).checkEmployeeForOrder(1L, "35467585763t4sfgchjfuyetf");
    }

    @Test
    void updateOrderExportedDetail() throws Exception {
        ExportDetailsDto dto = ModelUtils.getOrderDetailExportDto();

        String orderResponceDtoJSON = objectMapper.writeValueAsString(dto);
        this.mockMvc.perform(put(ubsManagementLink + "/update-order-export-details" + "/{id}", 1L)
            .content(orderResponceDtoJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void deletesViolationFromOrder() throws Exception {
        mockMvc.perform(delete(ubsManagementLink + "/delete-violation-from-order" + "/{orderId}", 1L))
            .andExpect(status().isOk());

        verify(violationService).deleteViolation(1L, null);
    }

    @Test
    void addManualPayment() throws Exception {
        ManualPaymentRequestDto dto = getManualPaymentRequestDto();

        String responseJSON = objectMapper.writeValueAsString(dto);
        MockMultipartFile jsonFile = new MockMultipartFile("manualPaymentDto",
            "", "application/json", responseJSON.getBytes());

        mockMvc.perform(multipart(ubsManagementLink + "/add-manual-payment/{id}", 1)
            .file(jsonFile)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void deleteManualPayment() throws Exception {
        mockMvc.perform(delete(ubsManagementLink + "/delete-manual-payment/{id}", 1L))
            .andExpect(status().isOk()).andDo(print());
    }

    @Test
    void updateManualPayment() throws Exception {
        ManualPaymentRequestDto dto = getManualPaymentRequestDto();

        String responseJSON = objectMapper.writeValueAsString(dto);
        MockMultipartFile jsonFile = new MockMultipartFile("manualPaymentDto",
            "", "application/json", responseJSON.getBytes());

        MockMultipartHttpServletRequestBuilder builder =
            multipart(ubsManagementLink + "/update-manual-payment/{id}", 1L);
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
        this.mockMvc.perform(get(ubsManagementLink + "/get-all-employee-by-position" + "/{id}", 1L)
            .principal(principal))
            .andExpect(status().isOk());
    }

    @Test
    void groupCoordsWithSpecifiedOnes() throws Exception {
        this.mockMvc.perform(
            post(ubsManagementLink + "/group-undelivered-with-specified")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"latitude\":84.525254,\"longitude\":12.436964}]"))
            .andExpect(status().isOk());
    }

    @Test
    void saveAdminCommentToOrder() throws Exception {
        AdminCommentDto adminCommentDto = ModelUtils.getAdminComment();

        String writeValueAsString = objectMapper.writeValueAsString(adminCommentDto);

        mockMvc.perform(MockMvcRequestBuilders.post(ubsManagementLink + "/save-admin-comment", 1L)
            .content(writeValueAsString)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void updateEcoNumberForOrder() throws Exception {
        EcoNumberDto ecoNumberDto = getEcoNumberDto();

        String writeValueAsString = objectMapper.writeValueAsString(ecoNumberDto);

        mockMvc.perform(MockMvcRequestBuilders.put(ubsManagementLink + "/update-eco-store{id}", 1L)
            .content(writeValueAsString)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void getCustomTableParametersTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ubsManagementLink + "/getOrdersViewParameters"))
            .andExpect(status().isOk());
    }

    @Test
    void setCustomTableTest() throws Exception {
        mockMvc.perform(put(ubsManagementLink + "/changeOrdersTableView"))
            .andExpect(status().isOk());
    }

    @Test
    void allUndeliveredCoordsTest() throws Exception {
        mockMvc.perform(get(ubsManagementLink + "/all-undelivered"))
            .andExpect(status().isOk());

        verify(coordinateService).getAllUndeliveredOrdersWithLiters();
    }

    @Test
    void addPointsToUserTest() throws Exception {
        AddingPointsToUserDto dto = ModelUtils.getAddingPointsToUserDto();

        String dtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch(ubsManagementLink + "/addPointsToUser")
            .content(dtoJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void paymentInfoTest() throws Exception {
        mockMvc.perform(get(ubsManagementLink + "/getPaymentInfo")
            .principal(principal)
            .param("orderId", "1")
            .param("sumToPay", "1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void groupCoordsTest() throws Exception {
        mockMvc.perform(get(ubsManagementLink + "/group-undelivered")
            .param("radius", "2.04")
            .param("litres", "2")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getUserViolationsTest() throws Exception {
        mockMvc.perform(get(ubsManagementLink + "/getUsersViolations")
            .param("email", "max@email.com")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void setCustomTable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(ubsManagementLink + "/changeOrdersTableView")
            .content("titles1,titles2,titles3")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getCustomTableParameters() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/getOrdersViewParameters", "uuid1"))
            .andExpect(status().isOk());
    }

    @Test
    void getOrders() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/bigOrderTable", "uuid1")
            .principal(principal))
            .andExpect(status().isOk());
    }

    @Test
    void getOrderBagsInfo() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/getOrderBagsInfo" + "/{id}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void getUpdateAllOrderPageAdminInfoTest() throws Exception {
        UpdateAllOrderPageDto dto = ModelUtils.getUpdateAllOrderPageDto();

        String jsonDto = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(ubsManagementLink + "/all-order-page-admin-info")
            .content(jsonDto)
            .principal(principal)
            .param("lang", "ua")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void getOrderCancellationReason() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/get-order-cancellation-reason" + "/{id}", 1L))
            .andExpect(status().isOk());
        verify(ubsManagementService).getOrderCancellationReason(1L);
    }

    @Test
    void getNotTakenOrderReason() throws Exception {
        this.mockMvc.perform(get(ubsManagementLink + "/get-not-taken-order-reason/{id}", 1L))
            .andExpect(status().isOk());
        verify(ubsManagementService).getNotTakenOrderReason(1L);
    }

    @Test
    void updatePageAdminInfoTest() throws Exception {
        UpdateOrderPageAdminDto dto = getUpdateOrderPageAdminDto();

        String responseJSON = objectMapper.writeValueAsString(dto);

        MockMultipartFile jsonFile = new MockMultipartFile(
            "updateOrderPageAdminDto",
            "updateOrderPageAdminDto.json",
            "application/json",
            responseJSON.getBytes());

        MockMultipartHttpServletRequestBuilder builder =
            MockMvcRequestBuilders.multipart(ubsManagementLink + "/update-order-page-admin-info/{id}", 1L);
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
        mockMvc.perform(get(ubsManagementLink + "/check-status-transition/formed-to-canceled/{id}", orderId)
            .contentType(MediaType.APPLICATION_XML))
            .andExpect(status().isOk())
            .andExpect(content().string("<Boolean>true</Boolean>"));
        verify(ubsManagementService).checkIfOrderStatusIsFormedToCanceled(orderId);
    }
}
