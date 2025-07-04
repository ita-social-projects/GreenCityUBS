package greencity.dto.telegram;

import greencity.enums.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageAssetDto {
    private Long id;

    private String url;

    private AssetType type;

    private String fileName;

    private Long size;

    private String contentType;
}