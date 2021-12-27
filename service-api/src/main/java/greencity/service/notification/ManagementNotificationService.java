package greencity.service.notification;

import greencity.dto.NotificationTemplateDto;
import greencity.dto.PageableDto;
import org.springframework.data.domain.Pageable;

public interface ManagementNotificationService {
    /**
     * {@inheritDoc}
     */
    void update(NotificationTemplateDto notificationTemplateDto);

    /**
     * {@inheritDoc}
     */
    PageableDto<NotificationTemplateDto> findAll(Pageable pageable);

    /**
     * {@inheritDoc}
     */
    NotificationTemplateDto findById(Long id);
}
