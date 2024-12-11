package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.service.ubs.UBSClientService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static greencity.ModelUtils.getPrincipal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
class AddressControllerTest {
    private static final String UBS_LINK = "/ubs";
    private static final String RANDOM_UUID = UUID.randomUUID().toString();

    private static final MockMvc mockMvc;
    private static final UBSClientService ubsClientService;
    private static final UserRemoteClient userRemoteClient;
    private static final AddressController addressController;

    private final Principal principal = getPrincipal();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        ubsClientService = mock(UBSClientService.class);
        userRemoteClient = mock(UserRemoteClient.class);
        addressController = new AddressController(ubsClientService);

        mockMvc = MockMvcBuilders.standaloneSetup(addressController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .build();
    }

    @Test
    void getAllAddressesForCurrentUser() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn(RANDOM_UUID);

        mockMvc.perform(get(UBS_LINK + "/findAll-order-address")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).findAllAddressesForCurrentOrder(anyString());
    }

    @Test
    void saveAddressForOrder() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn(RANDOM_UUID);

        CreateAddressRequestDto dto = ModelUtils.getAddressRequestDto();

        String createAddressRequestDto = OBJECT_MAPPER.writeValueAsString(dto);

        mockMvc.perform(post(UBS_LINK + "/save-order-address")
            .content(createAddressRequestDto)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        verify(ubsClientService).saveCurrentAddressForOrder(any(), eq(RANDOM_UUID));
    }

    @Test
    void updateAddressForOrder() throws Exception {
        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn(RANDOM_UUID);

        OrderAddressDtoRequest dto = ModelUtils.getOrderAddressDtoRequest();

        String orderAddressDtoRequest = OBJECT_MAPPER.writeValueAsString(dto);

        mockMvc.perform(put(UBS_LINK + "/update-order-address")
            .content(orderAddressDtoRequest)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).updateCurrentAddressForOrder(any(), eq(RANDOM_UUID));
    }

    @Test
    void deleteOrderAddress() throws Exception {
        mockMvc.perform(delete(UBS_LINK + "/order-addresses/{id}", 1L)
            .principal(principal))
            .andExpect(status().isOk());
    }

    @Test
    void makeAddressActual() throws Exception {
        Long addressId = 1L;
        String uuid = RANDOM_UUID;

        when(userRemoteClient.findUuidByEmail((anyString()))).thenReturn(uuid);

        mockMvc.perform(patch(UBS_LINK + "/makeAddressActual/{addressId}", addressId)
            .principal(principal))
            .andExpect(status().isOk());

        verify(ubsClientService).makeAddressActual(addressId, uuid);
    }

    @Test
    void getAllDistrictsForRegionAndCity() throws Exception {
        String region = "Львівська";
        String city = "Львів";
        List<DistrictDto> mockLocationDtoList = List.of(DistrictDto.builder()
            .nameUa("Львів")
            .nameEn("Lviv")
            .build());
        when(ubsClientService.getAllDistricts(region, city)).thenReturn(mockLocationDtoList);
        mockMvc.perform(get(UBS_LINK + "/get-all-districts")
            .param("region", region)
            .param("city", city)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ubsClientService).getAllDistricts(region, city);
    }

}
