package greencity.service;

import greencity.dto.CertificateDto;
import greencity.dto.CoordinatesDto;
import greencity.dto.GroupedCoordinatesDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import greencity.dto.UserPointsAndAllBagsDto;
import greencity.entity.user.User;
import java.util.List;
import java.util.Set;

public interface UBSService {
    /**
     * Method to group coordinates into clusters including summary litres and
     * specified coordinates.
     *
     * @param specified          - list of {@link CoordinatesDto}.
     * @param litres             - preferred amount of litres.
     * @param additionalDistance - additional km to radius.
     * @return List of {@link CoordinatesDto} lists.
     * @author Oleh Bilonizhka
     */
    Set<GroupedCoordinatesDto> getClusteredCoordsAlongWithSpecified(Set<CoordinatesDto> specified,
        int litres, double additionalDistance);

    /**
     * Method to group coordinates into clusters including summary litres.
     *
     * @param distance - preferred distance for clusterization.
     * @param litres   - preferred amount of litres.
     * @return List of {@link CoordinatesDto} lists.
     * @author Oleh Bilonizhka
     */
    Set<GroupedCoordinatesDto> getClusteredCoords(double distance, int litres);

    /**
     * Method returns all undelivered orders including litres.
     *
     * @return List of {@link CoordinatesDto} lists.
     * @author Oleh Bilonizhka
     */
    Set<GroupedCoordinatesDto> getAllUndeliveredCoords();

    /**
     * Methods returns all available for order bags and current user's bonus points.
     *
     * @param userId current {@link User}'s id.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Oleh Bilonizhka
     */
    UserPointsAndAllBagsDto getFirstPageData(Long userId);

    /**
     * Methods returns all saved user data.
     *
     * @param userId current {@link User}'s id.
     * @return list of {@link PersonalDataDto}.
     * @author Oleh Bilonizhka
     */
    List<PersonalDataDto> getSecondPageData(Long userId);

    /**
     * Methods return status of entered certificate, empty string if absent.
     *
     * @param code {@link String} code of certificate.
     * @return {@link CertificateDto} which contains status.
     * @author Oleh Bilonizhka
     */
    CertificateDto checkCertificate(String code);

    /**
     * Methods saves all entered by user data to database.
     *
     * @param dto    {@link OrderResponseDto} user entered data;
     * @param userId current {@link User}'s id;
     * @author Oleh Bilonizhka
     */
    void saveFullOrderToDB(OrderResponseDto dto, Long userId);
}
