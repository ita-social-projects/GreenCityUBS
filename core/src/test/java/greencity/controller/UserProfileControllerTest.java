package greencity.controller;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserProfileCreateDto;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.configuration.SecurityConfig;
import greencity.constant.AppConstant;
import greencity.converters.UserArgumentResolver;
import greencity.dto.address.AddressDto;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.user.UserProfileDto;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.repository.UserRepository;
import greencity.service.ubs.user.UserService;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class UserProfileControllerTest {
    private static final String deactivateUser = "/user/markUserAsDeactivated";

    private MockMvc mockMvc;

    @InjectMocks
    UserProfileController userProfileController;

    @Mock
    UserRepository userRepository;

    @Mock
    private Validator mockValidator;

    @Mock
    private UserService userService;

    private Principal principal = getPrincipal();
    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userProfileController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userRepository))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes))
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void saveUserDate() throws Exception {
        UserProfileDto userProfileDto = ModelUtils.userProfileDto();
        List<AddressDto> addressDto = ModelUtils.addressDto();
        userProfileDto.setAddressDto(addressDto);

        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(userProfileDto);

        String uuid = "uuid";
        when(userRepository.findUuidByRecipientEmail(principal.getName())).thenReturn(Optional.of(uuid));

        mockMvc.perform(put(AppConstant.UBS_LINK_USERPROFILE + "/user/update")
            .content(responseJSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getProfileData() throws Exception {
        String uuid = "uuid";
        when(userRepository.findUuidByRecipientEmail(principal.getName())).thenReturn(Optional.of(uuid));
        mockMvc.perform(get(AppConstant.UBS_LINK_USERPROFILE + "/user/getUserProfile")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void deactivateUser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeactivateUserRequestDto request = DeactivateUserRequestDto.builder()
            .reason("test")
            .build();
        mockMvc.perform(put(AppConstant.UBS_LINK_USERPROFILE + deactivateUser)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void createUserProfile() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(getUserProfileCreateDto());
        mockMvc.perform(post(AppConstant.UBS_LINK_USERPROFILE + "/user/create")
            .content(content)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
        verify(userService).createUserProfile(getUserProfileCreateDto());
    }
}
