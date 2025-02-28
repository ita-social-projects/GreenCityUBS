package greencity.service.ubs.manager;

import greencity.dto.order.BigOrderTableDTO;
import greencity.dto.order.OrderCountDto;
import greencity.dto.table.CustomTableViewDto;
import greencity.entity.table.TableColumnWidthForEmployee;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import org.springframework.data.domain.Page;

public interface BigOrderTableServiceView {
    /**
     * Method returns all order's data from big order table .
     *
     * @author Kuzbyt Maksym
     * @param orderPage      used to formed paging
     * @param searchCriteria used to formed filtering and searching
     * @return {@link BigOrderTableDTO}
     */
    Page<BigOrderTableDTO> getOrders(OrderPage orderPage, OrderSearchCriteria searchCriteria, String email);

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

    /**
     * Method sets isTableFreeze field in TableColumnWidthForEmployee.
     *
     * @return {@link TableColumnWidthForEmployee} that has been saved
     *
     * @author Hrenevych Ivan
     */
    TableColumnWidthForEmployee changeIsFreezeStatus(String uuid, Boolean value);

    /**
     * Method returns total number of orders by employee email.
     *
     * @param email employee email.
     * @return {@link OrderCountDto} object for representing count of orders.
     *
     * @author Chernenko Vitaliy.
     */
    OrderCountDto getTotalNumberOfOrdersByEmployee(String email);
}
