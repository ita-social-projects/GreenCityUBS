package greencity.service.notification;

import greencity.dto.AddingViolationsToUserDto;
import greencity.dto.NotificationTemplateDto;
import greencity.dto.PageableDto;
import org.springframework.data.domain.Pageable;

public interface NotificationeService {
    /**
     * Method that update notification template.
     *
     * @author Dima Sannytski
     */
    void update(NotificationTemplateDto notificationTemplateDto);

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
    NotificationTemplateDto findById(Long id);

    /**
     * Method for send email with description to user.
     *
     * @param dto {@link AddingViolationsToUserDto } order id with description.
     * @author Veremchuk Zakhar.
     */
    void sendNotificationAboutViolation(AddingViolationsToUserDto dto, String language);
}
