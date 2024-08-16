package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.constant.AppConstant;
import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.dto.useragreement.UserAgreementDto;
import greencity.service.ubs.UserAgreementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static greencity.ModelUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserAgreementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserAgreementService service;

    @InjectMocks
    private UserAgreementController controller;

    @Mock
    private final Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllUserAgreementsIds() throws Exception {
        when(service.findAllIdSortedByAsc()).thenReturn(List.of(1L, 2L));

        mockMvc.perform(get(AppConstant.USER_AGREEMENT_LINK)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).findAllIdSortedByAsc();
    }

    @Test
    void getAgreementByIdTest() throws Exception {
        UserAgreementDetailDto agreement = getUserAgreementDetailDto();
        when(service.read(anyLong())).thenReturn(agreement);

        mockMvc.perform(get(AppConstant.USER_AGREEMENT_LINK + "/{id}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(service).read(1L);
    }

    @Test
    void getLatestUserAgreementTest() throws Exception {
        UserAgreementDto latestAgreement = getUserAgreementDto();
        when(service.findLatest()).thenReturn(latestAgreement);

        mockMvc.perform(get("/user-agreement/latest")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(service).findLatest();
    }

    @Test
    void createUserAgreement() throws Exception {
        UserAgreementDto agreementDto = getUserAgreementDto();
        UserAgreementDetailDto result = getUserAgreementDetailDto();
        String authorEmail = "author@email.com";

        when(principal.getName()).thenReturn(authorEmail);
        when(service.create(agreementDto, authorEmail)).thenReturn(result);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(agreementDto);

        mockMvc.perform(post(AppConstant.USER_AGREEMENT_LINK)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isCreated());

        verify(service).create(any(UserAgreementDto.class), anyString());
    }

    @Test
    void deleteUserAgreement() throws Exception {
        mockMvc.perform(delete("/user-agreement/{id}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

}
