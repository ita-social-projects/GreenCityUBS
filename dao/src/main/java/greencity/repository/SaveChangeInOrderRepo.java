package greencity.repository;

import greencity.entity.parameters.MultiValue;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class SaveChangeInOrderRepo {
    /**
     * Method for save changes imputed from orders table.
     */
    public boolean saveChange(String query, Long orderId, MultiValue multiValue) {
        Class clazz = multiValue.getClazz();
        return true;
    }
}
