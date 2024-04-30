package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.tariff.TariffWithChatAccess;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSManagementEmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static greencity.ModelUtils.getUuid;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class ManagementEmployeeControllerTest {
    private final String UBS_LINK = "/admin/ubs-employee";
    private final String SAVE_LINK = "/save-employee";
    private final String UPDATE_LINK = "/update-employee";
    private final String FIND_ALL_LINK = "/getAll-employees";
    private final String DELETE_LINK = "/deactivate-employee";
    private final String ACTIVATE_LINK = "/activate-employee";
    private final String GET_ALL_POSITIONS_LINK = "/get-all-positions";
    private final String DELETE_IMAGE_LINK = "/delete-employee-image/";
    private final String GET_ALL_TARIFFS = "/getTariffs";

    private MockMvc mockMvc;
    @Mock
    private UBSManagementEmployeeService service;
    @Mock
    private UBSClientService ubsClientService;
    @Mock
    UserRemoteClient userRemoteClient;
    @Mock
    private Validator mockValidator;

    @InjectMocks
    ManagementEmployeeController controller;

    private final Principal principal = getUuid();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void saveEmployeeTest() throws Exception {
        EmployeeDto employeeDto = new EmployeeDto();
        List<TariffWithChatAccess> tariffs = new ArrayList<>();
        EmployeeWithTariffsIdDto dto = new EmployeeWithTariffsIdDto();
        dto.setEmployeeDto(employeeDto);
        dto.setTariffs(tariffs);
        ObjectMapper objectMapper = new ObjectMapper();
        String dtoJson = objectMapper.writeValueAsString(dto);
        MockMultipartFile jsonFile = new MockMultipartFile(
            "employee",
            "employee.json",
            MediaType.APPLICATION_JSON_VALUE,
            dtoJson.getBytes());
        mockMvc.perform(
            multipart(UBS_LINK + SAVE_LINK)
                .file(jsonFile)
                .principal(principal)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isCreated());
        verify(service).save(dto, null);
    }

    @Test
    void saveBadRequestTest() throws Exception {
        mockMvc.perform(post(UBS_LINK + SAVE_LINK)
            .content("{}")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getAllEmployees() throws Exception {
        EmployeePage employeePage = new EmployeePage();
        EmployeeFilterCriteria employeeFilterCriteria = new EmployeeFilterCriteria();

        mockMvc.perform(get(UBS_LINK + FIND_ALL_LINK))
            .andExpect(status().isOk());

        verify(service).findAll(employeePage, employeeFilterCriteria);
    }

    @Test
    void getAllActiveEmployees() throws Exception {
        EmployeePage employeePage = new EmployeePage();
        EmployeeFilterCriteria employeeFilterCriteria = new EmployeeFilterCriteria();

        mockMvc.perform(get(UBS_LINK + FIND_ALL_LINK))
            .andExpect(status().isOk());

        verify(service).findAll(employeePage, employeeFilterCriteria);
    }

    @Test
    void updateEmployeeTest() throws Exception {
        EmployeeDto employeeDto = new EmployeeDto();
        List<TariffWithChatAccess> tariffs = new ArrayList<>();
        EmployeeWithTariffsIdDto dto = new EmployeeWithTariffsIdDto();
        dto.setEmployeeDto(employeeDto);
        dto.setTariffs(tariffs);
        ObjectMapper objectMapper = new ObjectMapper();
        String dtoJson = objectMapper.writeValueAsString(dto);

        MockMultipartFile jsonFile = new MockMultipartFile(
            "employee",
            "employee.json",
            MediaType.APPLICATION_JSON_VALUE,
            dtoJson.getBytes());
        MockMultipartHttpServletRequestBuilder builder =
            MockMvcRequestBuilders.multipart(UBS_LINK + UPDATE_LINK);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(builder.file(jsonFile)
            .principal(principal)
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk());
        verify(service, times(1)).update(dto, null);
    }

    @Test
    void deleteEmployeeTest() throws Exception {
        doNothing().when(service).deactivateEmployee(1L);

        mockMvc.perform(put(UBS_LINK + DELETE_LINK + "/" + 1)
            .principal(principal)).andExpect(status().isOk());
        verify(service, times(1)).deactivateEmployee(1L);
    }

    @Test
    void activateEmployeeTest() throws Exception {
        doNothing().when(service).activateEmployee(1L);

        mockMvc.perform(put(UBS_LINK + ACTIVATE_LINK + "/" + 1)
            .principal(principal)).andExpect(status().isOk());
        verify(service, times(1)).activateEmployee(1L);
    }

    @Test
    void deleteEmployeeImage() throws Exception {
        mockMvc.perform(delete(UBS_LINK + DELETE_IMAGE_LINK + 1)
            .principal(principal)).andExpect(status().isOk());
        verify(service, atLeastOnce()).deleteEmployeeImage(1L);
    }

    @Test
    void getAllPosition() throws Exception {
        mockMvc.perform(get(UBS_LINK + GET_ALL_POSITIONS_LINK)
            .principal(principal)).andExpect(status().isOk());
        verify(service, times(1)).getAllPositions();
    }

    @Test
    void getAllAuthorities() throws Exception {
        Set<String> authorities = new HashSet<>();
        authorities.add("ADMIN");
        when(ubsClientService.getAllAuthorities(anyString())).thenReturn(authorities);
        mockMvc.perform(get(UBS_LINK + "/get-all-authorities" + "?email=test@mail.com"))
            .andExpect(status().isOk());
        verify(ubsClientService).getAllAuthorities("test@mail.com");
    }

    @Test
    void getPositionsAndRelatedAuthoritiesTest() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testmail@gmail.com");

        mockMvc.perform(get(UBS_LINK + "/get-positions-authorities" + "?email=" + principal.getName())
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).getPositionsAndRelatedAuthorities(principal.getName());
    }

    @Test
    void getEmployeeLoginPositionNamesTest() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testmail@gmail.com");

        mockMvc.perform(get(UBS_LINK + "/get-employee-login-positions" + "?email=" + principal.getName())
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).getEmployeeLoginPositionNames(principal.getName());
    }

    @Test
    void editAuthorities() throws Exception {
        UserEmployeeAuthorityDto dto = ModelUtils.getUserEmployeeAuthorityDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(UBS_LINK + "/edit-authorities")
            .principal(principal)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).updateEmployeesAuthorities(dto);
    }

    @Test
    void getTariffInfoForEmployeeTest() throws Exception {
        mockMvc.perform(get(UBS_LINK + GET_ALL_TARIFFS)
            .principal(principal)).andExpect(status().isOk());
        verify(service, times(1)).getTariffsForEmployee();
    }

    @Test
    void getEmployeesByTariffIdTest() throws Exception {
        Long tariffId = 1L;
        List<EmployeeWithTariffsDto> employees = new ArrayList<>();
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setFirstName("John");

        EmployeeWithTariffsDto employee1 = new EmployeeWithTariffsDto();
        employee1.setEmployeeDto(employeeDto);

        employees.add(employee1);
        when(service.getEmployeesByTariffId(tariffId)).thenReturn(employees);

        mockMvc.perform(get(UBS_LINK + "/get-employees/{tariffId}", tariffId))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].employeeDto.firstName")
                .value(employee1.getEmployeeDto().getFirstName()))
            .andReturn();

        verify(service, times(1)).getEmployeesByTariffId(tariffId);
    }
}
