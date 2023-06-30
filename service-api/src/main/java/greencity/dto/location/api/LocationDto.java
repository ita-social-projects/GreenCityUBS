package greencity.dto.location.api;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class LocationDto {
    private String parentId;
    private String id;
    private Map<String, String> name;
}