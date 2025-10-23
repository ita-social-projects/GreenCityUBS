package greencity.controller;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserProfileCreateDto;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.configuration.SecurityConfig;
import greencity.constant.AppConstant;
import greencity.converters.UserArgumentResolver;
import greencity.dto.address.AddressDto;
import greencity.dto.user.UserDeletionReasonDto;
import greencity.dto.user.UserProfileDto;
import greencity.enums.UserStatus;
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
    private MockMvc mockMvc;

    @InjectMocks
    UserProfileController userProfileController;

    @Mock
    UserRepository userRepository;

    @Mock
    Validator mockValidator;

    @Mock
    UserService userService;

    private Principal principal = getPrincipal();
    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();
    private ObjectMapper objectMapper = new ObjectMapper();

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
    void saveUserDataTest() throws Exception {
        UserProfileDto userProfileDto = ModelUtils.userProfileDto();
        List<AddressDto> addressDto = ModelUtils.addressDto();
        userProfileDto.setAddressDto(addressDto);
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
    void getProfileDataTest() throws Exception {
        String uuid = "uuid";
        when(userRepository.findUuidByRecipientEmail(principal.getName())).thenReturn(Optional.of(uuid));
        mockMvc.perform(get(AppConstant.UBS_LINK_USERPROFILE + "/user/getUserProfile")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void changeUserStatusTest() throws Exception {
        String uuid = "uuid";
        Long id = 1L;
        UserStatus userStatus = UserStatus.DEACTIVATED;

        when(userRepository.findUuidByRecipientEmail(principal.getName()))
            .thenReturn(Optional.of("uuid"));

        mockMvc.perform(put(AppConstant.UBS_LINK_USERPROFILE + "/status/" + id)
            .principal(principal)
            .param("status", String.valueOf(userStatus)))
            .andExpect(status().isOk());

        verify(userService).updateUserStatusById(uuid, id, userStatus);
    }

    @Test
    void createUserProfileTest() throws Exception {
        String content = objectMapper.writeValueAsString(getUserProfileCreateDto());
        mockMvc.perform(post(AppConstant.UBS_LINK_USERPROFILE + "/user/create")
            .content(content)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
        verify(userService).createUserProfile(getUserProfileCreateDto());
    }

    @Test
    void getReasonsOfDeactivationTest() throws Exception {
        String uuid = "uuid";
        Long id = 1L;
        List<String> reasons = List.of("reason1", "reason2");

        when(userRepository.findUuidByRecipientEmail(principal.getName()))
            .thenReturn(Optional.of("uuid"));
        when(userService.getDeactivationReasons(id, uuid))
            .thenReturn(reasons);

        mockMvc.perform(get(AppConstant.UBS_LINK_USERPROFILE + "/reasons")
            .principal(principal)
            .param("id", String.valueOf(id))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(reasons)));

        verify(userService).getDeactivationReasons(id, uuid);
    }

    @Test
    void getUserStatusTest() throws Exception {
        String uuid = "uuid";
        UserStatus userStatus = UserStatus.BLOCKED;

        when(userService.getUserStatusByUuid(uuid))
            .thenReturn(userStatus);

        mockMvc.perform(get(AppConstant.UBS_LINK_USERPROFILE + "/user/status")
            .param("uuid", uuid)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(userStatus)));

        verify(userService).getUserStatusByUuid(uuid);
    }

    @Test
    void deleteUserTest() throws Exception {
        String uuid = "uuid";
        UserDeletionReasonDto dto = UserDeletionReasonDto.builder()
            .reason("reason")
            .build();

        when(userRepository.findUuidByRecipientEmail(principal.getName()))
            .thenReturn(Optional.of("uuid"));

        mockMvc.perform(delete(AppConstant.UBS_LINK_USERPROFILE + "/user/delete")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());

        verify(userService).deleteUserByUuid(uuid, dto);
    }

    @Test
    void getActivatedUsersAmountTest() throws Exception {
        Long amount = 100L;

        when(userService.getActivatedUsersAmount())
            .thenReturn(amount);

        mockMvc.perform(get(AppConstant.UBS_LINK_USERPROFILE + "/activatedUsersAmount")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(String.valueOf(amount)));

        verify(userService).getActivatedUsersAmount();
    }
}
