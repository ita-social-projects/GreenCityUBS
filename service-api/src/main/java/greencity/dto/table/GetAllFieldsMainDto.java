package greencity.dto.table;

import greencity.entity.allfieldsordertable.GetAllValuesFromTable;
import lombok.*;

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
