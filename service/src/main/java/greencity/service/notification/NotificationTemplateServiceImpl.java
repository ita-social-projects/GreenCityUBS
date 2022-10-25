package greencity.service.notification;

import greencity.constant.ErrorMessage;
import greencity.dto.notification.BodyDto;
import greencity.dto.notification.NotificationScheduleDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateLocalizedDto;
import greencity.dto.notification.TitleDto;
import greencity.dto.pageble.PageableDto;
import greencity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.schedule.NotificationSchedule;
import greencity.exceptions.NotFoundException;
import greencity.repository.NotificationScheduleRepo;
import greencity.repository.NotificationTemplateRepository;
import java.util.stream.Collector;
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
    public PageableDto<NotificationTemplateLocalizedDto> findAll(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id")
            .descending());
        Page<NotificationTemplate> notificationTemplatesUa =
            notificationTemplateRepository.findAllTemplates(pageRequest, "ua");
        Page<NotificationTemplate> notificationTemplatesEn =
            notificationTemplateRepository.findAllTemplates(pageRequest, "en");
        List<NotificationTemplateLocalizedDto> templateDtoList = notificationTemplatesUa.stream()
            .map(notificationTemplate -> modelMapper.map(notificationTemplate, NotificationTemplateLocalizedDto.class)
                .setSchedule(getScheduleDto(notificationTemplate.getNotificationType()))
                .setTitle(getTitles(notificationTemplatesEn.getContent(), notificationTemplate))
                .setBody(getBodies(notificationTemplatesEn.getContent(), notificationTemplate)))
            .collect(Collectors.toList());
        return new PageableDto<>(
            templateDtoList,
            notificationTemplatesUa.getTotalElements(),
            notificationTemplatesUa.getPageable().getPageNumber(),
            notificationTemplatesUa.getTotalPages());
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

    private TitleDto getTitles(List<NotificationTemplate> notificationTemplatesEn,
        NotificationTemplate notificationTemplate) {
        return TitleDto.builder()
            .uaTitle(notificationTemplate.getTitle())
            .enTitle(notificationTemplatesEn.stream()
                .filter(notification -> (notification.getNotificationType()
                    .equals(notificationTemplate.getNotificationType())
                    &&
                    notification.getNotificationReceiverType()
                        .equals(notificationTemplate.getNotificationReceiverType())))
                .collect(
                    Collectors.toList())
                .get(0).getTitle())
            .build();
    }

    private BodyDto getBodies(List<NotificationTemplate> notificationTemplatesEn,
        NotificationTemplate notificationTemplate) {
        return BodyDto.builder()
            .bodyUa(notificationTemplate.getBody())
            .bodyEn(notificationTemplatesEn.stream()
                .filter(notification -> (notification.getNotificationType()
                    .equals(notificationTemplate.getNotificationType())
                    &&
                    notification.getNotificationReceiverType()
                        .equals(notificationTemplate.getNotificationReceiverType())))
                .collect(
                    Collectors.toList())
                .get(0).getBody())
            .build();
    }
}
