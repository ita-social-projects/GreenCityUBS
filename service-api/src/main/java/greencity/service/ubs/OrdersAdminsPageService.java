package greencity.service.ubs;

import greencity.dto.order.BlockedOrderDto;
import greencity.dto.order.ChangeOrderResponseDTO;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.dto.table.ColumnWidthDto;
import greencity.dto.table.TableParamsDto;
import greencity.entity.user.employee.Employee;
import java.util.List;

public interface OrdersAdminsPageService {
    /**
     * Retrieves parameters needed to build a table for displaying orders on the
     * admin's page. This method provides a {@link TableParamsDto} that contains the
     * necessary parameters for constructing the orders table. It takes into account
     * the administrator's UUID.
     *
     * @param userId the UUID of the administrator, which identifies the admin user.
     *               This value should be a valid UUID string.
     * @return a {@link TableParamsDto} object containing the parameters required
     *         for building the orders table. The returned object will be populated
     *         based on the provided userId.
     * @author Liubomyr Pater
     */
    TableParamsDto getParametersForOrdersTable(String userId);

    /**
     * Method that return.
     *
     * @param email                        of {@link String}
     * @param requestToChangeOrdersDataDTO of {@link RequestToChangeOrdersDataDto}
     * @author Liubomyr Pater
     */
    ChangeOrderResponseDTO chooseOrdersDataSwitcher(String email,
        RequestToChangeOrdersDataDto requestToChangeOrdersDataDTO);

    /**
     * Method that return a list of orders which are block already.
     *
     * @param userUuid of {@link String}
     * @param orders   of {@link List}
     * @author Liubomyr Pater
     */
    List<BlockedOrderDto> requestToBlockOrder(String userUuid, List<Long> orders);

    /**
     * Method that return list of unblocked orders.
     *
     * @param userUuid of {@link String}
     * @param orders   of {@link List}
     * @author Liubomyr Pater
     */
    List<Long> unblockOrder(String userUuid, List<Long> orders);

    /**
     * Method changing order's status.
     *
     * @param value    of {@link String}
     * @param ordersId of {@link List}
     * @param employee of {@link Employee}
     * @author Liubomyr Pater
     */
    List<Long> orderStatusForDevelopStage(List<Long> ordersId, String value, Employee employee);

    /**
     * Method changing order's date of export.
     *
     * @param value      of {@link String}
     * @param ordersId   of {@link List}
     * @param employeeId of {@link Long}
     * @author Liubomyr Pater
     */
    List<Long> dateOfExportForDevelopStage(List<Long> ordersId, String value, Long employeeId);

    /**
     * Method changing order's time of export.
     *
     * @param value      of {@link String}
     * @param ordersId   of {@link List}
     * @param employeeId of {@link Long}
     * @author Liubomyr Pater
     */
    List<Long> timeOfExportForDevelopStage(List<Long> ordersId, String value, Long employeeId);

    /**
     * Method changing order's receiving station.
     *
     * @param value      of {@link String}
     * @param ordersId   of {@link List}
     * @param employeeId of {@link Long}
     * @author Liubomyr Pater
     */
    List<Long> receivingStationForDevelopStage(List<Long> ordersId, String value, Long employeeId);

    /**
     * Method changing order's responsible employee.
     *
     * @param employee of {@link String}
     * @param ordersId of {@link List}
     * @param position of {@link Long}
     * @param email    of {@link String}
     * @author Liubomyr Pater
     */
    List<Long> responsibleEmployee(List<Long> ordersId, String employee, Long position, String email);

    /**
     * Method to get column width for big order table.
     *
     * @param userUuid of {@link String}
     * @return {@link ColumnWidthDto}
     * @author Oleh Kulbaba
     */
    ColumnWidthDto getColumnWidthForEmployee(String userUuid);

    /**
     * Method to save or update column width for big order table.
     *
     * @param columnWidthDto of {@link ColumnWidthDto}
     * @param userUuid       of {@link String}
     * @author Oleh Kulbaba
     */
    void saveColumnWidthForEmployee(ColumnWidthDto columnWidthDto, String userUuid);
}
