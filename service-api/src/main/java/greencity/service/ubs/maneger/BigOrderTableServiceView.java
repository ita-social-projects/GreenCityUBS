package greencity.service.ubs.maneger;

import greencity.dto.BigOrderTableDTO;
import greencity.dto.CustomTableViewDto;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import org.springframework.data.domain.Page;

public interface BigOrderTableServiceView {
    /**
     * Method returns all order's data from big order table.
     *
     * @author Kuzbyt Maksym
     */
    Page<BigOrderTableDTO> getOrders(OrderPage orderPage, OrderSearchCriteria searchCriteria, String uuid);

    /**
     * Method save or update view of Orders table.
     *
     * @author Sikhovskiy Rostyslav
     */
    void changeOrderTableView(String uuid, String titles);

    /**
     * Method return parameters for custom orders table view.
     *
     * @author Sikhovskiy Rostyslav
     */
    CustomTableViewDto getCustomTableParameters(String uuid);
}
