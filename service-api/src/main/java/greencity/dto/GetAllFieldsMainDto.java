package greencity.dto;

import greencity.entity.allfieldsordertable.GetAllValuesFromTable;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class GetAllFieldsMainDto {
    private GetAllValuesFromTable getAllValuesFromTable;
}
