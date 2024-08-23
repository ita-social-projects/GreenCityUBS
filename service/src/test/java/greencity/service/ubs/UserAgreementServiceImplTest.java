package greencity.service.ubs;

import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.dto.useragreement.UserAgreementDto;
import greencity.entity.user.UserAgreement;
import greencity.exceptions.NotFoundException;
import greencity.repository.EmployeeRepository;
import greencity.repository.UserAgreementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getUserAgreement;
import static greencity.ModelUtils.getUserAgreementDetailDto;
import static greencity.ModelUtils.getUserAgreementDto;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAgreementServiceImplTest {

    @Mock
    private UserAgreementRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private UserAgreementServiceImpl service;

    @Test
    void findAll_ShouldReturnListIds() {
        UserAgreement userAgreement = getUserAgreement();
        List<UserAgreement> agreements = List.of(userAgreement);

        when(repository.findAllSortedByAsc()).thenReturn(agreements);

        List<Long> result = service.findAllIdSortedByAsc();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(repository).findAllSortedByAsc();
    }

    @Test
    void findLatest_ShouldReturnUserAgreementDto() {
        UserAgreement userAgreement = getUserAgreement();
        UserAgreementDto dto = getUserAgreementDto();

        when(repository.findLatestAgreement()).thenReturn(Optional.of(userAgreement));
        when(modelMapper.map(userAgreement, UserAgreementDto.class)).thenReturn(dto);

        UserAgreementDto result = service.findLatest();

        assertNotNull(result);
        assertEquals(dto, result);

        verify(repository).findLatestAgreement();
        verify(modelMapper).map(userAgreement, UserAgreementDto.class);
    }

    @Test
    void findLatest_ShouldThrowNotFoundException() {
        when(repository.findLatestAgreement()).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findLatest());
        verify(repository).findLatestAgreement();
    }

    @Test
    void create_ShouldReturnUserAgreementDetailDto() {
        UserAgreementDto dto = getUserAgreementDto();
        UserAgreement userAgreement = getUserAgreement();
        UserAgreement saved = getUserAgreement();
        UserAgreementDetailDto detailDto = getUserAgreementDetailDto();
        String authorEmail = "author@example.com";

        when(modelMapper.map(dto, UserAgreement.class)).thenReturn(userAgreement);
        when(employeeRepository.findByEmail(authorEmail)).thenReturn(Optional.of(getEmployee()));
        when(repository.save(userAgreement)).thenReturn(saved);
        when(modelMapper.map(saved, UserAgreementDetailDto.class)).thenReturn(detailDto);

        UserAgreementDetailDto result = service.create(dto, authorEmail);

        assertNotNull(result);
        assertEquals(detailDto, result);

        verify(modelMapper).map(dto, UserAgreement.class);
        verify(employeeRepository).findByEmail(authorEmail);
        verify(repository).save(userAgreement);
        verify(modelMapper).map(saved, UserAgreementDetailDto.class);
    }

    @Test
    void read_ShouldReturnUserAgreementDetailDto() {
        Long id = 1L;
        UserAgreement userAgreement = getUserAgreement();
        UserAgreementDetailDto detailDto = getUserAgreementDetailDto();

        when(repository.findById(id)).thenReturn(Optional.of(userAgreement));
        when(modelMapper.map(userAgreement, UserAgreementDetailDto.class)).thenReturn(detailDto);

        UserAgreementDetailDto result = service.read(id);

        assertNotNull(result);
        assertEquals(detailDto, result);

        verify(repository).findById(id);
        verify(modelMapper).map(userAgreement, UserAgreementDetailDto.class);
    }

    @Test
    void read_ShouldThrowNotFoundException() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.read(id));
        verify(repository).findById(id);
    }

    @Test
    void delete_ShouldDeleteUserAgreement() {
        Long id = 1L;
        UserAgreement userAgreement = getUserAgreement();

        when(repository.findById(id)).thenReturn(Optional.of(userAgreement));
        doNothing().when(repository).deleteById(id);

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).deleteById(id);
    }

    @Test
    void delete_ShouldThrowNotFoundException() {
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(id));
        verify(repository).findById(id);
    }
}
