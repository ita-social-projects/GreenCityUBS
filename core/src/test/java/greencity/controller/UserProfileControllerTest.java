package greencity.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.constant.AppConstant;
import greencity.converters.UserArgumentResolver;
import greencity.dto.AddressDto;
import greencity.dto.UserProfileDto;
import greencity.service.ubs.UBSClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static greencity.ModelUtils.getPrincipal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class UserProfileControllerTest {
    private static final String deactivateUser = "/user/markUserAsDeactivated";

    private MockMvc mockMvc;

    @Mock
    UBSClientService ubsClientService;

    @InjectMocks
    UserProfileController userProfileController;

    @Mock
    RestClient restClient;

    private Principal principal = getPrincipal();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userProfileController)
            .setCustomArgumentResolvers(new UserArgumentResolver(restClient))
            .build();
    }

    @Test
    void saveUserDate() throws Exception {
        UserProfileDto userProfileDto = ModelUtils.userProfileDto();
        AddressDto addressDto = ModelUtils.addressDto();
        userProfileDto.setAddressDto(addressDto);

        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(userProfileDto);

        mockMvc.perform(post(AppConstant.ubsLink + "/user/save")
            .content(responseJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void getProfileData() throws Exception {
        mockMvc.perform(get(AppConstant.ubsLink + "/user/getUserProfile")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void deactivateUser() throws Exception {
        mockMvc.perform(put(AppConstant.ubsLink + deactivateUser + "?id=5"))
            .andExpect(status().isOk());
        verify(ubsClientService).markUserAsDeactivated(5L);
    }
}