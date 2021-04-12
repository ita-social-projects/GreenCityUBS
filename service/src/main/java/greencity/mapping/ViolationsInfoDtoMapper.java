package greencity.mapping;

import greencity.dto.ViolationsInfoDto;
import greencity.entity.user.User;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ViolationsInfoDtoMapper extends AbstractConverter<User, ViolationsInfoDto> {
    @Override
    protected ViolationsInfoDto convert(User source) {
        return ViolationsInfoDto.builder()
            .violationsAmount(source.getViolations())
            .violationsDescription(source.getViolationsDescription())
            .build();
    }
}
