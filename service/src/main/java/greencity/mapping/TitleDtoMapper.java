package greencity.mapping;

import greencity.dto.TitleDto;
import greencity.entity.language.Title;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link TitleDto} into
 * {@link Title}.
 */
@Component
public class TitleDtoMapper extends AbstractConverter<TitleDto, Title> {
    /**
     * Method convert {@link TitleDto} to {@link Title}.
     *
     * @return {@link Title}
     */
    @Override
    protected Title convert(TitleDto source) {
        return new Title();
    }
}
