package greencity.service;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.dto.AddressDto;
import greencity.dto.PersonalDataDto;
import greencity.dto.UserProfileDto;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.order.Certificate;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.exceptions.CertificateNotFoundException;
import greencity.exceptions.NotFoundOrderAddressException;
import greencity.repository.*;
import greencity.service.ubs.UBSClientServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

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
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    UBSClientServiceImpl ubsService;

    @Test
    void getSecondPageData() {
        PersonalDataDto expected = ModelUtils.getOrderResponseDto().getPersonalData();
        User user = new User();
        user.setId(13L);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(ubsUserRepository.getAllByUserId(anyLong()))
            .thenReturn(Collections.singletonList(ModelUtils.getUBSuser()));
        when(modelMapper.map(ModelUtils.getUBSuser(), PersonalDataDto.class)).thenReturn(expected);

        assertEquals(expected, ubsService.getSecondPageData("35467585763t4sfgchjfuyetf").get(0));
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

    @Test
    void checkOrderNotFound() {
        Assertions.assertThrows(NotFoundOrderAddressException.class, () -> {
            ubsService.getAddressByOrderId(10000000l);
        });
    }

    @Test
    void saveProfileData() {
        User user = new User();
        user.setId(13L);
        String uuid = "35467585763t4sfgchjfuyetf";
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        UserProfileDto userProfileDto = new UserProfileDto();
        AddressDto addressDto = ModelUtils.addressDto();
        userProfileDto.setAddressDto(addressDto);
        Address address = ModelUtils.address();
        when(modelMapper.map(addressDto, Address.class)).thenReturn(address);
        when(userRepository.save(user)).thenReturn(user);
        when(addressRepository.save(address)).thenReturn(address);
        when(modelMapper.map(address, AddressDto.class)).thenReturn(addressDto);
        when(modelMapper.map(user, UserProfileDto.class)).thenReturn(userProfileDto);
        ubsService.saveProfileData(uuid, userProfileDto);
        assertNotNull(userProfileDto.getAddressDto());
        assertNotNull(userProfileDto);
        assertNotNull(address);
    }
}
