package greencity.dto.admin;

import greencity.enums.MainPageTextSection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingsTextDto {
    private Map<String, Map<String, String>> uk;
    private Map<String, Map<String, String>> en;
    private List<MainPageTextSection> section;
}
