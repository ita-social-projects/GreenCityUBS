package greencity.service.notification;

import greencity.constant.ErrorMessage;
import greencity.dto.notification.NotificationScheduleDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.pageble.PageableDto;
import greencity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.schedule.NotificationSchedule;
import greencity.exceptions.NotFoundException;
import greencity.repository.NotificationScheduleRepo;
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
    private final NotificationScheduleRepo scheduleRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(NotificationTemplateDto notificationTemplateDto) {
        NotificationTemplate template = getById(notificationTemplateDto.getId());
        NotificationSchedule notificationSchedule = scheduleRepo.getOne(NotificationType.valueOf(
            notificationTemplateDto.getNotificationType()));
        modelMapper.map(notificationTemplateDto.getSchedule(), notificationSchedule);
        template.setBody(notificationTemplateDto.getBody());
        template.setTitle(notificationTemplateDto.getTitle());
        notificationTemplateRepository.save(template);
        scheduleRepo.save(notificationSchedule);
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
            .map(notificationTemplate -> modelMapper.map(notificationTemplate, NotificationTemplateDto.class)
                .setSchedule(getScheduleDto(notificationTemplate.getNotificationType())))
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
    public NotificationTemplateDto findById(Long id) {
        return modelMapper.map(getById(id), NotificationTemplateDto.class);
    }

    /**
     * {@inheritDoc}
     */
    private NotificationTemplate getById(Long id) {
        return notificationTemplateRepository.findNotificationTemplateById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_TEMPLATE_NOT_FOUND));
    }

    private NotificationScheduleDto getScheduleDto(NotificationType notificationType) {
        NotificationSchedule notificationSchedule =
            scheduleRepo.findNotificationScheduleByNotificationType(notificationType);

        if (notificationSchedule != null) {
            return modelMapper.map(notificationSchedule,
                NotificationScheduleDto.class);
        } else {
            return null;
        }
    }
}
