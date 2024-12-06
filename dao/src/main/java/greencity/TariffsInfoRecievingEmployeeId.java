package greencity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TariffsInfoRecievingEmployeeId implements Serializable {
    private Long employee;
    private Long tariffsInfo;
}
