package greencity.service.notification;

import greencity.constant.ErrorMessage;
import greencity.dto.notification.AddNotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.notifications.NotificationTemplate;
import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationStatus;
import greencity.enums.NotificationType;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.ForbiddenException;
import greencity.exceptions.notification.IncorrectTemplateException;
import greencity.exceptions.notification.TemplateDeleteException;
import greencity.notificator.listener.NotificationPlanner;
import greencity.repository.NotificationTemplateRepository;
import greencity.repository.UserNotificationRepository;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationPlanner notificationPlanner;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void update(Long id, NotificationTemplateWithPlatformsUpdateDto dto) {
        NotificationTemplate template = getById(id);

        updateNotificationTemplateFromDto(template, dto);
        restartNotificationSchedule(template.getNotificationType());
    }

    private void updateNotificationTemplateFromDto(NotificationTemplate template,
        NotificationTemplateWithPlatformsUpdateDto dto) {
        updateNotificationTemplate(template, dto);
        updateNotificationTemplatePlatforms(template.getNotificationPlatforms(), dto.getPlatforms());
    }

    private void updateNotificationTemplate(NotificationTemplate template,
        NotificationTemplateWithPlatformsUpdateDto dto) {
        template.setTitleUk(dto.getNotificationTemplateUpdateInfo().getTitleUk());
        template.setTitleEn(dto.getNotificationTemplateUpdateInfo().getTitleEn());
        template.setTrigger(dto.getNotificationTemplateUpdateInfo().getTrigger());
        template.setTime(dto.getNotificationTemplateUpdateInfo().getTime());

        updateScheduleIfUpdateIsNotForbidden(template, dto);
        updateUserCategoryIfTemplateIsCustom(template, dto);
    }

    private void updateScheduleIfUpdateIsNotForbidden(
        NotificationTemplate template, NotificationTemplateWithPlatformsUpdateDto dto) {
        if (!template.isScheduleUpdateForbidden()) {
            template.setSchedule(dto.getNotificationTemplateUpdateInfo().getSchedule());
        }
    }

    private void updateUserCategoryIfTemplateIsCustom(
        NotificationTemplate template, NotificationTemplateWithPlatformsUpdateDto dto) {
        if (template.getNotificationType().equals(NotificationType.CUSTOM)) {
            template.setUserCategory(dto.getNotificationTemplateUpdateInfo().getUserCategory());
        }
    }

    private void updateNotificationTemplatePlatforms(List<NotificationPlatform> platforms,
        List<NotificationPlatformDto> platformDTOs) {
        for (NotificationPlatform platform : platforms) {
            NotificationPlatformDto platformDto = platformDTOs.stream()
                .filter(dto -> dto.getId().equals(platform.getId()))
                .findAny()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_PLATFORM_NOT_FOUND));
            platform.setBodyUk(platformDto.getBodyUk());
            platform.setBodyEn(platformDto.getBodyEn());
            platform.setNotificationStatus(platformDto.getStatus());
        }
    }

    private void restartNotificationSchedule(NotificationType notificationType) {
        notificationPlanner.restartNotificator(notificationType);
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
            .toList();
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

    @Override
    @Transactional
    public void changeNotificationStatusById(Long id, String status) {
        NotificationStatus newStatus = getValidNotificationStatusByNameOrThrow(status);
        NotificationTemplate notificationTemplate = getById(id);
        notificationTemplate.setNotificationStatus(newStatus);
        notificationTemplate.getNotificationPlatforms()
            .forEach(platform -> platform.setNotificationStatus(newStatus));

        restartNotificationSchedule(NotificationType.CUSTOM);
    }

    private NotificationStatus getValidNotificationStatusByNameOrThrow(String status) {
        return Arrays.stream(NotificationStatus.values())
            .filter(s -> s.name().equals(status))
            .findAny()
            .orElseThrow(() -> new BadRequestException(ErrorMessage.NOTIFICATION_STATUS_DOES_NOT_EXIST + status));
    }

    @Override
    @Transactional
    public void createNotificationTemplate(AddNotificationTemplateWithPlatformsDto notificationTemplateDto) {
        NotificationTemplate notificationTemplate =
            modelMapper.map(notificationTemplateDto, NotificationTemplate.class);
        checkTemplateContainsMessagesForAllPlatforms(notificationTemplate.getNotificationPlatforms());
        notificationTemplateRepository.save(notificationTemplate);
        restartNotificationSchedule(NotificationType.CUSTOM);
    }

    private void checkTemplateContainsMessagesForAllPlatforms(List<NotificationPlatform> notificationPlatforms) {
        var notificationReceiverTypes = notificationPlatforms.stream()
            .map(NotificationPlatform::getNotificationReceiverType)
            .toList();

        if (!isPlatformsSizesIdentical(notificationReceiverTypes)
            || !isAllPlatformTypesPresent(notificationReceiverTypes)) {
            throw new IncorrectTemplateException(
                ErrorMessage.TEMPLATE_DOES_NOT_CONTAIN_ALL_PLATFORMS + getPlatformNames());
        }
    }

    private String getPlatformNames() {
        return Arrays.stream(NotificationReceiverType.values())
            .map(NotificationReceiverType::getName)
            .reduce("", (s1, s2) -> s1 + "," + s2);
    }

    private boolean isPlatformsSizesIdentical(List<NotificationReceiverType> notificationReceiverTypes) {
        return notificationReceiverTypes.size() == NotificationReceiverType.values().length;
    }

    private boolean isAllPlatformTypesPresent(List<NotificationReceiverType> notificationReceiverTypes) {
        return (new HashSet<>(notificationReceiverTypes).containsAll(List.of(NotificationReceiverType.values())));
    }

    @Override
    @Transactional
    public void removeNotificationTemplate(Long id) {
        checkTemplateIsCustom(id);

        if (userNotificationRepository.existsByTemplateId(id)) {
            throw new ForbiddenException(ErrorMessage.TEMPLATE_IN_USE_CANNOT_BE_DELETED);
        }

        removeTemplate(id);
        restartNotificationSchedule(NotificationType.CUSTOM);
    }

    private void checkTemplateIsCustom(Long id) {
        NotificationTemplate template = getById(id);
        if (!template.getNotificationType().equals(NotificationType.CUSTOM)) {
            throw new TemplateDeleteException(ErrorMessage.PREDEFINED_NOTIFICATION_CANNOT_BE_DELETED);
        }
    }

    private void removeTemplate(Long id) {
        notificationTemplateRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    private NotificationTemplate getById(Long id) {
        return notificationTemplateRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_TEMPLATE_NOT_FOUND_BY_ID + id));
    }
}
