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

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        verify(repository, times(1)).findAllSortedByAsc();
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
        verify(repository, times(1)).findLatestAgreement();
    }

    @Test
    void findLatest_ShouldThrowNotFoundException() {
        when(repository.findLatestAgreement()).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findLatest());
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
        verify(modelMapper, times(1)).map(dto, UserAgreement.class);
        verify(repository, times(1)).save(userAgreement);
        verify(modelMapper, times(1)).map(saved, UserAgreementDetailDto.class);
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
        verify(repository, times(1)).findById(id);
    }

    @Test
    void read_ShouldThrowNotFoundException() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.read(id));
    }

    @Test
    void delete_ShouldDeleteUserAgreement() {
        Long id = 1L;
        UserAgreement userAgreement = getUserAgreement();

        when(repository.findById(id)).thenReturn(Optional.of(userAgreement));

        service.delete(id);

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void delete_ShouldThrowNotFoundException() {
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(id));
    }
}
