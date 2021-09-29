package greencity.service.ubs;

import greencity.dto.ChangeOrderResponseDTO;
import greencity.dto.RequestToChangeOrdersDataDTO;
import greencity.dto.TableParamsDTO;

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
}
