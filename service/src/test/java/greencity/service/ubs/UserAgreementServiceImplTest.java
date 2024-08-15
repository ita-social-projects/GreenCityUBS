package greencity.service.ubs;

import greencity.dto.pageble.PageableDto;
import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.dto.useragreement.UserAgreementDto;
import greencity.entity.user.UserAgreement;
import greencity.exceptions.NotFoundException;
import greencity.repository.UserAgreementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAgreementServiceImplTest {

    @Mock
    private UserAgreementRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserAgreementServiceImpl service;

    @Test
    void findAll_ShouldReturnPageableDto() {
        UserAgreement userAgreement = getUserAgreement();
        UserAgreementDetailDto dto = getUserAgreementDetailDto();
        Page<UserAgreement> page = new PageImpl<>(List.of(userAgreement));

        when(repository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(userAgreement, UserAgreementDetailDto.class)).thenReturn(dto);

        Pageable pageable = Pageable.ofSize(10);
        PageableDto<UserAgreementDetailDto> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getPage().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(0, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
        verify(repository, times(1)).findAll(pageable);
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

        when(modelMapper.map(dto, UserAgreement.class)).thenReturn(userAgreement);
        when(repository.save(userAgreement)).thenReturn(saved);
        when(modelMapper.map(saved, UserAgreementDetailDto.class)).thenReturn(detailDto);

        UserAgreementDetailDto result = service.create(dto);

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
    void update_ShouldReturnUserAgreementDetailDto() {
        UserAgreementDto dto = new UserAgreementDto("Тест текст", "Test text");
        UserAgreement existingAgreement = getUserAgreement();

        UserAgreement updatedAgreement = getUserAgreement();
        updatedAgreement.setTextUa(dto.getTextUa());
        updatedAgreement.setTextEn(dto.getTextEn());

        UserAgreementDetailDto detailDto = getUserAgreementDetailDto();
        detailDto.setTextUa(dto.getTextUa());
        detailDto.setTextEn(dto.getTextEn());

        when(repository.findById(anyLong())).thenReturn(Optional.of(existingAgreement));
        when(repository.save(any(UserAgreement.class))).thenReturn(updatedAgreement);
        when(modelMapper.map(updatedAgreement, UserAgreementDetailDto.class)).thenReturn(detailDto);
        UserAgreementDetailDto result = service.update(1L, dto);

        assertNotNull(result);
        assertEquals(detailDto.getId(), result.getId());
        assertEquals(detailDto.getTextUa(), result.getTextUa());
        assertEquals(detailDto.getTextEn(), result.getTextEn());

        assertEquals(result.getTextUa(), existingAgreement.getTextUa());
        assertEquals(result.getTextEn(), existingAgreement.getTextEn());
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
