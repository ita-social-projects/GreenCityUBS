package greencity.mapping.admin;

import greencity.dto.admin.UpdateSectionTextsDto;
import greencity.entity.admin.SettingsText;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UpdateTextDtoMapper extends AbstractConverter<UpdateSectionTextsDto, SettingsText> {
    @Override
    protected SettingsText convert(UpdateSectionTextsDto updateSectionTextsDto) {
        return SettingsText.builder()
            .field(updateSectionTextsDto.getField())
            .valueUK(updateSectionTextsDto.getValueUK())
            .valueEN(updateSectionTextsDto.getValueEN())
            .build();
    }
}
