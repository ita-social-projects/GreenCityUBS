package greencity.service.ubs;

import greencity.dto.BlockedOrderDTO;
import greencity.dto.ChangeOrderResponseDTO;
import greencity.dto.RequestToChangeOrdersDataDTO;
import greencity.dto.TableParamsDTO;

import java.util.List;

public interface OrdersAdminsPageService {
    /**
     * Method that return parameters for building table on admin's page.
     *
     * @param userId of {@link Long} administrator's user id;
     * @author Liubomyr Pater
     */
    TableParamsDTO getParametersForOrdersTable(Long userId);

    /**
     * Method that return.
     *
     * @param userUuid                     of {@link String}
     * @param requestToChangeOrdersDataDTO of {@link RequestToChangeOrdersDataDTO}
     * @author Liubomyr Pater
     */
    ChangeOrderResponseDTO chooseOrdersDataSwitcher(String userUuid,
        RequestToChangeOrdersDataDTO requestToChangeOrdersDataDTO);

    /**
     * Method that return a list of orders which are block already.
     *
     * @param userUuid of {@link String}
     * @param orders   of {@link List}
     * @author Liubomyr Pater
     */
    List<BlockedOrderDTO> requestToBlockOrder(String userUuid, List<Long> orders);

    /**
     * Method that return list of unblocked orders.
     *
     * @param userUuid of {@link String}
     * @param orders   of {@link List}
     * @author Liubomyr Pater
     */
    List<Long> unblockOrder(String userUuid, List<Long> orders);
}
