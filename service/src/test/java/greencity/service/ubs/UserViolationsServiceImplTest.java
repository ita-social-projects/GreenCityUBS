package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.entity.enums.SortingOrder;
import greencity.repository.UserRepository;
import greencity.repository.UserViolationsTableRepo;
import greencity.repository.ViolationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UserViolationsServiceImplTest {
    @Mock
    UserViolationsTableRepo userViolationsTableRepo;
    @Mock
    ViolationRepository violationRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserViolationsServiceImpl userViolationsService;

    @Test
    void getAllViolations() {

        when(violationRepository.getNumberOfViolationsByUser(anyLong())).thenReturn(5L);
        when(userRepository.getOne(any())).thenReturn(ModelUtils.getUser());
        when(userViolationsTableRepo.findAll(anyLong(), anyString(), any(), any())).thenReturn(
            new PageImpl<>(List.of(ModelUtils.getViolation()),
                PageRequest.of(0, 5, Sort.by("id").descending()), 5));

        userViolationsService.getAllViolations(Pageable.unpaged(), 1L, "violationDate", SortingOrder.ASC);
        assertTrue(userViolationsService.getAllViolations(Pageable.unpaged(), 1L, "violationDate", SortingOrder.ASC)
                .getUserViolationsDto().getPage().get(0).getViolationDate().equals(ModelUtils.getViolation().getViolationDate()));
    }
}
