package greencity.service.ubs;

import greencity.dto.order.BlockedOrderDto;
import greencity.dto.order.ChangeOrderResponseDTO;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.dto.table.TableParamsDto;

import java.util.List;

public interface OrdersAdminsPageService {
    /**
     * Method that return parameters for building table on admin's page.
     *
     * @param userId of {@link String} administrator's uuId;
     * @author Liubomyr Pater
     */
    TableParamsDto getParametersForOrdersTable(String userId);

    /**
     * Method that return.
     *
     * @param userUuid                     of {@link String}
     * @param requestToChangeOrdersDataDTO of {@link RequestToChangeOrdersDataDto}
     * @author Liubomyr Pater
     */
    ChangeOrderResponseDTO chooseOrdersDataSwitcher(String userUuid,
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
     * @param value      of {@link String}
     * @param ordersId   of {@link List}
     * @param employeeId of {@link Long}
     * @author Liubomyr Pater
     */
    List<Long> orderStatusForDevelopStage(List<Long> ordersId, String value, Long employeeId);

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
}
