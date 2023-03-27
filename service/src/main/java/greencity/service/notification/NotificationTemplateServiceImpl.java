package greencity.service.notification;

import greencity.constant.ErrorMessage;
import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.notifications.NotificationTemplate;
import greencity.exceptions.NotFoundException;
import greencity.repository.NotificationTemplateRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
    private NotificationTemplateRepository notificationTemplateRepository;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Long id, NotificationTemplateWithPlatformsDto notificationDto) {
        NotificationTemplate template = getById(id);
        template.setTitle(notificationDto.getTitle());
        template.setTitleEng(notificationDto.getTitleEng());
        template.setNotificationType(notificationDto.getType());
        template.setTrigger(notificationDto.getTrigger());
        template.setTrigger(notificationDto.getTrigger());
        template.setTime(notificationDto.getTime());
        template.setSchedule(notificationDto.getSchedule());
        List<NotificationPlatform> notificationPlatforms = template.getNotificationPlatforms();
        List<NotificationPlatformDto> notificationPlatformDtos = notificationDto.getPlatforms();

        for (int i = 0; i < notificationPlatforms.size(); i++) {
            var notificationPlatform = notificationPlatforms.get(i);
            var notificationPlatformDto = notificationPlatformDtos.get(i);
            notificationPlatform.setNotificationReceiverType(notificationPlatformDto.getReceiverType());
            notificationPlatform.setBody(notificationPlatformDto.getBody());
            notificationPlatform.setBodyEng(notificationPlatformDto.getBodyEng());
        }
        notificationTemplateRepository.save(template);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<NotificationTemplateDto> findAll(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id")
            .descending());
        Page<NotificationTemplate> notificationTemplates = notificationTemplateRepository.findAll(pageRequest);
        List<NotificationTemplateDto> templateDtoList = notificationTemplates.stream()
            .map(notificationTemplate -> modelMapper.map(notificationTemplate, NotificationTemplateDto.class))
            .collect(Collectors.toList());
        return new PageableDto<>(
            templateDtoList,
                notificationTemplates.getTotalElements(),
                notificationTemplates.getPageable().getPageNumber(),
                notificationTemplates.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationTemplateWithPlatformsDto findById(Long id) {
        return modelMapper.map(getById(id), NotificationTemplateWithPlatformsDto.class);
    }

    /**
     * {@inheritDoc}
     */
    private NotificationTemplate getById(Long id) {
        return notificationTemplateRepository.findNotificationTemplateById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_TEMPLATE_NOT_FOUND));
    }
}
