package greencity.service.notification;

import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.dto.pageble.PageableDto;
import org.springframework.data.domain.Pageable;

public interface NotificationTemplateService {
    /**
     * Method that update notification template.
     *
     * @author Dima Sannytski
     */
    void update(Long id, NotificationTemplateWithPlatformsUpdateDto notificationDto);

    /**
     * Method that returns page with all notification templates.
     *
     * @author Dima Sannytski
     */
    PageableDto<NotificationTemplateDto> findAll(Pageable pageable);

    /**
     * Method that finds and returns notification template by id.
     *
     * @author Dima Sannytski
     */
    NotificationTemplateWithPlatformsDto findById(Long id);

    /**
     * Method that deactivate notification template and all platforms by id.
     *
     * @author Dima Sannytski
     */
    void deactivateNotificationById(Long id);
}
