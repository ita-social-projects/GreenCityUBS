package greencity.mapping.admin;

import greencity.dto.admin.UpdateSectionTextsDto;
import greencity.entity.admin.SettingsText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UpdateTextDtoMapperTest {
    @InjectMocks
    UpdateTextDtoMapper updateTextDtoMapper;

    @Test
    void convertTest() {
        UpdateSectionTextsDto dto = UpdateSectionTextsDto.builder()
            .field("field")
            .valueEN("valueEN")
            .valueUK("valueUK")
            .build();
        SettingsText expected = SettingsText.builder()
            .field("field")
            .valueEN("valueEN")
            .valueUK("valueUK")
            .build();
        SettingsText actual = updateTextDtoMapper.convert(dto);

        assertEquals(expected.getField(), actual.getField());
        assertEquals(expected.getValueEN(), actual.getValueEN());
        assertEquals(expected.getValueUK(), actual.getValueUK());
    }
}
