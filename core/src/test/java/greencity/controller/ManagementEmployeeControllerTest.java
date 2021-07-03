package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.*;
import greencity.service.ubs.UBSManagementEmployeeService;
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
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.security.Principal;

import static greencity.ModelUtils.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class ManagementEmployeeControllerTest {
    private final String UBS_LINK = "/admin/ubs-employee";
    private final String SAVE_LINK = "/save-employee";
    private final String UPDATE_LINK = "/update-employee";
    private final String FIND_ALL_LINK = "/getAll-employees";
    private final String DELETE_LINK = "/delete-employee";
    private final String SAVE_POSITION_LINK = "/create-position";
    private final String UPDATE_POSITION_LINK = "/update-position";
    private final String GET_ALL_POSITIONS_LINK = "/get-all-positions";
    private final String DELETE_POSITION_LINK = "/delete-position/";
    private final String SAVE_STATION_LINK = "/create-receiving-station";
    private final String UPDATE_STATION_LINK = "/update-receiving-station";
    private final String GET_ALL_STATIONS_LINK = "/get-all-receiving-station";
    private final String DELETE_STATION_LINK = "/delete-receiving-station/";
    private final String DELETE_IMAGE_LINK = "/delete-employee-image/";

    private MockMvc mockMvc;
    @Mock
    private UBSManagementEmployeeService service;

    @Mock
    RestClient restClient;
    @Mock
    private Validator mockValidator;

    @InjectMocks
    ManagementEmployeeController controller;

    private Principal principal = getUuid();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(restClient))
            .setValidator(mockValidator)
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
    void updateEmployeeBadRequest() throws Exception {
        EmployeeDto dto = getEmployeeDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);
        MockMultipartFile jsonFile = new MockMultipartFile("EmployeeDto",
            "", "application/json", responseJSON.getBytes());
        MockMultipartHttpServletRequestBuilder builder =
            MockMvcRequestBuilders.multipart(UBS_LINK + UPDATE_LINK);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(builder.file(jsonFile)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployeeTest() throws Exception {
        doNothing().when(service).deleteEmployee(1L);

        mockMvc.perform(delete(UBS_LINK + DELETE_LINK + "/" + 1)
            .principal(principal)).andExpect(status().isOk());
        verify(service, times(1)).deleteEmployee(1L);
    }

    @Test
    void deleteEmployeeImage() throws Exception {
        mockMvc.perform(delete(UBS_LINK + DELETE_IMAGE_LINK + 1)
            .principal(principal)).andExpect(status().isOk());
        verify(service, atLeastOnce()).deleteEmployeeImage(1L);
    }

    @Test
    void createPosition() throws Exception {
        AddingPositionDto dto = AddingPositionDto.builder().name("Водій").build();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(UBS_LINK + SAVE_POSITION_LINK)
            .principal(principal)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        verify(service, times(1)).create(any(AddingPositionDto.class));
    }

    @Test
    void updatePosition() throws Exception {
        PositionDto dto = getPositionDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(UBS_LINK + UPDATE_POSITION_LINK)
            .principal(principal)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(service, times(1)).update(dto);
    }

    @Test
    void getAllPosition() throws Exception {
        mockMvc.perform(get(UBS_LINK + GET_ALL_POSITIONS_LINK)
            .principal(principal)).andExpect(status().isOk());
        verify(service, times(1)).getAllPositions();
    }

    @Test
    void deletePosition() throws Exception {
        mockMvc.perform(delete(UBS_LINK + DELETE_POSITION_LINK + "/1").principal(principal)).andExpect(status().isOk());
        verify(service, times(1)).deletePosition(1L);
    }

    @Test
    void createReceivingStation() throws Exception {
        AddingReceivingStationDto dto = AddingReceivingStationDto.builder().name("Петрівка").build();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(UBS_LINK + SAVE_STATION_LINK)
            .principal(principal)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        verify(service, times(1)).create(any(AddingReceivingStationDto.class));
    }

    @Test
    void updateReceivingStation() throws Exception {
        ReceivingStationDto dto = getReceivingStationDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(UBS_LINK + UPDATE_STATION_LINK)
            .principal(principal)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(service, times(1)).update(dto);
    }

    @Test
    void getAllReceivingStation() throws Exception {
        mockMvc.perform(get(UBS_LINK + GET_ALL_STATIONS_LINK)
            .principal(principal)).andExpect(status().isOk());
        verify(service, times(1)).getAllReceivingStation();
    }

    @Test
    void deleteReceivingStation() throws Exception {
        mockMvc.perform(delete(UBS_LINK + DELETE_STATION_LINK + "/1").principal(principal)).andExpect(status().isOk());
        verify(service, times(1)).deleteReceivingStation(1L);
    }
}