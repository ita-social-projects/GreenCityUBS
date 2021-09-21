package greencity.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class SaveChangeInOrderRepo {
    /**
     * Method for save changes imputed from orders table.
     */
    public boolean saveChange(String query, Long orderId) {
        System.out.println(query + orderId);
        return true;
    }
}
