package greencity.service;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import greencity.dto.UbsTableCreationDto;
import greencity.dto.UserPointsAndAllBagsDto;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.CertificateNotFoundException;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.UBSClientServiceImpl;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class UBSClientServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private BagRepository bagRepository;
    @Mock
    private UBSuserRepository ubsUserRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private CertificateRepository certificateRepository;
    @Mock
    private RestClient restClient;
    @InjectMocks
    UBSClientServiceImpl ubsService;

    @Test
    void getFirstPageData() {
        User user = new User();
        user.setCurrentPoints(254);
        List<Bag> bags = Collections.singletonList(new Bag(120, 1, "name", 250));

        when(userRepository.findByUuid(anyString())).thenReturn(user);
        when(bagRepository.findAll()).thenReturn(bags);

        UserPointsAndAllBagsDto expected = ubsService.getFirstPageData("35467585763t4sfgchjfuyetf");

        assertTrue(254 == expected.getPoints() && bags == expected.getAllBags());
    }

    @Test
    void getSecondPageData() {
        PersonalDataDto expected = ModelUtils.getOrderResponceDto().getPersonalData();
        User user = new User();
        user.setId(13L);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(ubsUserRepository.getAllByUserId(anyLong()))
            .thenReturn(Collections.singletonList(ModelUtils.getUBSuser()));
        when(modelMapper.map(ModelUtils.getUBSuser(), PersonalDataDto.class)).thenReturn(expected);

        assertEquals(expected, ubsService.getSecondPageData("35467585763t4sfgchjfuyetf").get(0));
    }

    @Test
    void getSecondPageDataForNewBuyer() {
        User user = new User();
        user.setId(13L);

        when(restClient.getDataForUbsTableRecordCreation()).thenReturn(UbsTableCreationDto.builder()
                    .uuid("35467585763t4sfgchjfuyetf").build());
        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(ubsUserRepository.getAllByUserId(13L)).thenReturn(Collections.emptyList());

        List<PersonalDataDto> expected = List.of(PersonalDataDto.builder().build());
        List<PersonalDataDto> actual = ubsService.getSecondPageData("null");

        assertTrue(expected.get(0).equals(actual.get(0)));
    }

    @Test
    void checkCertificate() {
        when(certificateRepository.findById("certificate")).thenReturn(Optional.of(Certificate.builder()
            .code("certificate")
            .certificateStatus(CertificateStatus.ACTIVE)
            .build()));

        assertEquals("ACTIVE", ubsService.checkCertificate("certificate").getCertificateStatus());
    }

    @Test
    void checkCertificateWithNoAvailable() {
        Assertions.assertThrows(CertificateNotFoundException.class, () -> {
            ubsService.checkCertificate("randomstring").getCertificateStatus();
        });
    }

}