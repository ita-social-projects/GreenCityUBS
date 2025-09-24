package greencity.service.ubs.user;

import greencity.enums.UserStatus;
import java.util.List;

public interface UserService {
    /**
     * Method to find user's status by uuid.
     *
     * @param uuid user's uuid.
     * @return user's status.
     */
    UserStatus getUserStatusByUuid(String uuid);

    /**
     * Method to delete a user by uuid, setting their status to DELETED.
     *
     * @param uuid {@link String} user's uuid.
     */
    void deleteUserByUuid(String uuid);

    /**
     * Method that change user status.
     *
     * @param currentUserUuid {@link String} current user uuid.
     * @param targetUserId    {@link Long} user uuid that is deactivated.
     * @param status          {@link UserStatus} user status.
     */
    void updateUserStatusById(String currentUserUuid, Long targetUserId, UserStatus status);

    /**
     * Method for getting a {@link List} of {@link String} - reasons for
     * deactivation of the current user.
     *
     * @param id              {@link Long} - user's id.
     * @param currentUserUuid {@link String} - user's uuid.
     * @return {@link List} of {@link String}.
     */
    List<String> getDeactivationReasons(Long id, String currentUserUuid);

    /**
     * Counts all users by user {@link UserStatus} ACTIVATED.
     *
     * @return amount of users with {@link UserStatus} ACTIVATED.
     */
    long getActivatedUsersAmount();
}
