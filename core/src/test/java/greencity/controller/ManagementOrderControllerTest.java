package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import static greencity.ModelUtils.getPrincipal;
import greencity.dto.AddingViolationsToUserDto;
import greencity.dto.CertificateDtoForAdding;
import greencity.service.ubs.UBSManagementService;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.service.ubs.UBSClientService;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

@ExtendWith(MockitoExtension.class)
class ManagementOrderControllerTest {

    private static final String ubsLink = "/ubs/management";

    private MockMvc mockMvc;

    @Mock
    UBSManagementService ubsManagementService;

    @Mock
    RestClient restClient;

    @Mock
    private Validator mockValidator;

    @InjectMocks
    ManagementOrderController managementOrderController;

    public static final String contentForaddingcontroller = "{\n"
        + " \"code\": \"1111-2222\",\n" +
        " \"monthCount\": 8,\n" +
        " \"points\": 100\n"
        + "}";

    public static final String contentForAddViolationToUserControllerTest = "{\n"
            + "\"orderID\": 1,\n" +
            "\"violationDescription\": \"TestTest\" "
            + "}";


    private Principal principal = getPrincipal();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementOrderController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void getAllCertificates() throws Exception {
        int pageNumber = 0;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        mockMvc.perform(MockMvcRequestBuilders.get(ubsLink + "/getAllCertificates"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        verify(ubsManagementService).getAllCertificates(pageable);
    }

    @Test
    void addCertificateTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(ubsLink + "/addCertificate")
            .content(contentForaddingcontroller)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isCreated());
        ObjectMapper mapper = new ObjectMapper();
        CertificateDtoForAdding certificateDtoForAdding = CertificateDtoForAdding.builder()
            .code("1111-2222")
            .points(100)
            .monthCount(8)
            .build();
        verify(ubsManagementService, times(1)).addCertificate(certificateDtoForAdding);
    }

    @Test
    void addUsersViolations() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post(ubsLink+"/addViolationToUser")
        .content(contentForAddViolationToUserControllerTest)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isCreated());
        AddingViolationsToUserDto addingViolationsToUserDto = AddingViolationsToUserDto.builder()
            .orderID(1L)
            .violationDescription("TestTest")
            .build();
        verify(ubsManagementService, times(1)).addUserViolation(addingViolationsToUserDto);
    }
}
