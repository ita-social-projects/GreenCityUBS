package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.service.locations.LocationApiService;
import greencity.service.ubs.UBSClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import static greencity.ModelUtils.getPrincipal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class AddressControllerTest {

    private static final String ubsLink = "/ubs";

    private MockMvc mockMvc;

    @Mock
    private UBSClientService ubsClientService;

    @Mock
    private LocationApiService locationApiService;

    @Mock
    private UserRemoteClient userRemoteClient;

    @InjectMocks
    private AddressController addressController;

    private final Principal principal = getPrincipal();

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(addressController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .build();
    }

    @Test
    void getAllAddressesForCurrentUser() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

        mockMvc.perform(get(ubsLink + "/findAll-order-address")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).findAllAddressesForCurrentOrder(anyString());
    }

    @Test
    void saveAddressForOrder() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

        CreateAddressRequestDto dto = ModelUtils.getAddressRequestDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String createAddressRequestDto = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/save-order-address")
            .content(createAddressRequestDto)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        verify(ubsClientService).saveCurrentAddressForOrder(any(), eq("35467585763t4sfgchjfuyetf"));
    }

    @Test
    void updateAddressForOrder() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");

        OrderAddressDtoRequest dto = ModelUtils.getOrderAddressDtoRequest();

        ObjectMapper objectMapper = new ObjectMapper();
        String orderAddressDtoRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(ubsLink + "/update-order-address")
            .content(orderAddressDtoRequest)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        verify(ubsClientService).updateCurrentAddressForOrder(any(), eq("35467585763t4sfgchjfuyetf"));
    }

    @Test
    void deleteOrderAddress() throws Exception {
        mockMvc.perform(delete(ubsLink + "/order-addresses/{id}", 1L)
            .principal(principal))
            .andExpect(status().isOk());
    }

    @Test
    void makeAddressActual() throws Exception {
        Long addressId = 1L;
        String uuid = "35467585763t4sfgchjfuyetf";

        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn(uuid);

        mockMvc.perform(patch(ubsLink + "/makeAddressActual/{addressId}", addressId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(ubsClientService).makeAddressActual(addressId, uuid);
    }

    @Test
    void getAllDistrictsForRegionAndCity() throws Exception {
        String region = "Львівська";
        String city = "Львів";
        List<DistrictDto> mockLocationDtoList = new ArrayList<>();
        when(ubsClientService.getAllDistricts(region, city)).thenReturn(mockLocationDtoList);
        mockMvc.perform(get(ubsLink + "/get-all-districts")
            .param("region", region)
            .param("city", city)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).getAllDistricts(region, city);
    }
}
