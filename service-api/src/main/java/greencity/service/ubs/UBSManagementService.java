package greencity.service.ubs;

import greencity.dto.*;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;

public interface UBSManagementService {
    /**
     * Method to group orders into clusters including summary litres and specified
     * coordinates.
     *
     * @param specified          - list of {@link CoordinatesDto}.
     * @param litres             - preferred amount of litres.
     * @param additionalDistance - additional km to radius.
     * @return List of {@link GroupedOrderDto} lists.
     * @author Oleh Bilonizhka
     */
    List<GroupedOrderDto> getClusteredCoordsAlongWithSpecified(Set<CoordinatesDto> specified,
        int litres, double additionalDistance);

    /**
     * Method to group orders into clusters including summary litres.
     *
     * @param distance - preferred distance for clusterization.
     * @param litres   - preferred amount of litres.
     * @return List of {@link GroupedOrderDto} lists.
     * @author Oleh Bilonizhka
     */
    List<GroupedOrderDto> getClusteredCoords(double distance, int litres);

    /**
     * Method returns all undelivered orders including litres.
     *
     * @return List of {@link GroupedOrderDto} lists.
     * @author Oleh Bilonizhka
     */
    List<GroupedOrderDto> getAllUndeliveredOrdersWithLiters();

    /**
     * Method returns all certificates.
     *
     * @return List of {@link greencity.entity.order.Certificate} lists.
     * @author Nazar Struk
     */
    PageableDto<CertificateDtoForSearching> getAllCertificates(Pageable page);

    /**
     * Method add a certificates.
     *
     * @author Nazar Struk
     */
    void addCertificate(CertificateDtoForAdding add);

    /**
     * Method add some points to UserUBS by email.
     *
     * @author Nazar Struk
     */
    void addPointsToUser(AddingPointsToUserDto addingPointsToUserDto);

    /**
     * Method returns all users violations.
     *
     * @return {@link ViolationsInfoDto} count of Users violations with order id
     *         descriptions.
     * @author Nazar Struk
     */
    ViolationsInfoDto getAllUserViolations(String email);

    /**
     * Method for adding violation for user.
     *
     * @param add {@link AddingViolationsToUserDto} transfer order_id + violation
     *            description.
     * @author Nazar Struk
     */
    void addUserViolation(AddingViolationsToUserDto add);

    /**
     * Method for send email with description to user.
     *
     * @param dto {@link AddingViolationsToUserDto } order id with description.
     * @author Veremchuk Zakhar.
     */
    void sendNotificationAboutViolation(AddingViolationsToUserDto dto, String language);

    /**
     * Method for getting all values from table .
     *
     * @author Nazar Struk
     */
    List<AllFieldsFromTableDto> getAllValuesFromTable();

    /**
     * Method for getting all sorted values from table .
     *
     * @author Nazar Struk
     */
    List<AllFieldsFromTableDto> getAllSortedValuesFromTable(String column, String sortingType);

    /**
     * Method that read user address by order id.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link ReadAddressByOrderDto} that contains one address;
     * @author Mahdziak Orest
     */
    ReadAddressByOrderDto getAddressByOrderId(Long orderId);

    /**
     * Method that update address.
     *
     * @param dtoUpdate of {@link OrderAddressDtoUpdate} order id;
     * @return {@link OrderAddressDtoResponse} that contains address;
     * @author Mahdziak Orest
     */
    OrderAddressDtoResponse updateAddress(OrderAddressDtoUpdate dtoUpdate);
}
