package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.AddEmployeeDto;
import greencity.dto.EmployeeDto;
import greencity.service.ubs.UBSEmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static greencity.ModelUtils.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {
    private final String UBS_LINK = "/admin/ubs-employee";
    private final String SAVE_LINK = "/save-employee";
    private final String UPDATE_LINK = "/update-employee";
    private final String FIND_ALL_LINK = "/getAll-employees";
    private final String DELETE_LINK = "/delete-employee";

    private MockMvc mockMvc;
    @Mock
    private UBSEmployeeService service;

    @Mock
    RestClient restClient;

    @InjectMocks
    EmployeeController controller;

    private Principal principal = getUuid();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(restClient))
            .build();
    }

    @Test
    void saveEmployee() throws Exception {

        AddEmployeeDto dto = getAddEmployeeDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);
        MockMultipartFile jsonFile = new MockMultipartFile("addEmployeeDto",
            "", "application/json", responseJSON.getBytes());

        mockMvc.perform(multipart(UBS_LINK + SAVE_LINK)
            .file(jsonFile)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
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
        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(UBS_LINK + FIND_ALL_LINK + "?page=1"))
            .andExpect(status().isOk());

        verify(service).findAll(pageable);
    }

    @Test
    void update() throws Exception {
        EmployeeDto dto = getEmployeeDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(UBS_LINK + UPDATE_LINK)
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void deleteTest() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete(UBS_LINK + "/" + 1 + DELETE_LINK)
            .principal(principal)).andExpect(status().isOk());
        verify(service, times(1)).delete(1L);
    }
}