package greencity;

import java.io.Serializable;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TariffsInfoRecievingEmployeeId implements Serializable {
    private Long employee;
    private Long tariffsInfo;
}
