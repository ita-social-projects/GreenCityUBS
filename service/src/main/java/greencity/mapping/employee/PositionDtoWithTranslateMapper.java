package greencity.mapping.employee;

import greencity.dto.position.PositionWithTranslateDto;
import greencity.entity.user.employee.Position;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that used by {@link ModelMapper} to map {@link Position} into
 * {@link PositionDtoWithTranslateMapper}.
 */
@Component
public class PositionDtoWithTranslateMapper extends AbstractConverter<Position, PositionWithTranslateDto> {
    /**
     * Method convert {@link Position} to {@link PositionWithTranslateDto}.
     *
     * @return {@link PositionDtoWithTranslateMapper}
     */
    @Override
    protected PositionWithTranslateDto convert(Position position) {
        Map<String, String> nameTranslations = new HashMap<>();
        nameTranslations.put("ua", position.getName());
        nameTranslations.put("en", position.getNameEn());

        return PositionWithTranslateDto.builder()
            .id(position.getId())
            .name(nameTranslations)
            .build();
    }
}
