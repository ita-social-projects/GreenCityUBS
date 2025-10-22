package greencity.service.ubs;

import greencity.dto.position.PositionDto;
import greencity.repository.PositionRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {
    private final PositionRepository positionRepository;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PositionDto> findAllByIds(Set<Long> ids) {
        return positionRepository.findAllById(ids).stream()
            .map(e -> modelMapper.map(e, PositionDto.class))
            .toList();
    }
}
