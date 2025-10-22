package greencity.service.ubs;

import greencity.dto.position.PositionDto;
import java.util.List;
import java.util.Set;

public interface PositionService {
    /**
     * Finds all positions by their ids.
     *
     * @param ids set of position ids
     * @return list of {@link PositionDto}
     */
    List<PositionDto> findAllByIds(Set<Long> ids);
}
