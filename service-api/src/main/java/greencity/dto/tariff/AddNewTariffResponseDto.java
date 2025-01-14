package greencity.dto.tariff;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddNewTariffResponseDto {
    private List<Long> tariffForLocationAndCourierAlreadyExistIdList;
    private List<Long> nonExistingLocationIdList;
}
