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
     * @author Safarov Renat
     */
    void update(Long id, NotificationTemplateWithPlatformsUpdateDto notificationDto);

    /**
     * Method that returns page with all notification templates.
     *
     * @author Safarov Renat
     */
    PageableDto<NotificationTemplateDto> findAll(Pageable pageable);

    /**
     * Method that finds and returns notification template by id.
     *
     * @author Safarov Renat
     */
    NotificationTemplateWithPlatformsDto findById(Long id);

    /**
     * Method that change status for notification template and all platforms by id.
     *
     * @author Safarov Renat
     */
    void changeNotificationStatusById(Long id, String status);
}
