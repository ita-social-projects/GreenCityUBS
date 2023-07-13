package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.SecurityConfig;
import greencity.constant.AppConstant;
import greencity.converters.UserArgumentResolver;
import greencity.dto.address.AddressDto;
import greencity.dto.address.AddressWithDistrictsDto;
import greencity.dto.user.UserProfileDto;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.ubs.UBSClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.security.Principal;
import java.util.List;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserProfileCreateDto;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    UserRemoteClient userRemoteClient;

    @Mock
    private Validator mockValidator;

    private Principal principal = getPrincipal();
    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userProfileController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRemoteClient))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes))
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void saveUserDate() throws Exception {
        UserProfileDto userProfileDto = ModelUtils.userProfileDto();
        List<AddressWithDistrictsDto> addressDto = ModelUtils.userProfileDto().getAddressDto();
        userProfileDto.setAddressDto(addressDto);

        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(userProfileDto);

        mockMvc.perform(put(AppConstant.ubsLink + "/user/update")
            .content(responseJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
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

    @Test
    void createUserProfile() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(getUserProfileCreateDto());
        mockMvc.perform(post(AppConstant.ubsLink + "/user/create")
            .content(content)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
        verify(ubsClientService).createUserProfile(getUserProfileCreateDto());
    }
}
