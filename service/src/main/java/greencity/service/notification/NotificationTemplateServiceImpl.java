package greencity.service.notification;

import greencity.constant.ErrorMessage;
import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.notifications.NotificationTemplate;
import greencity.enums.NotificationStatus;
import greencity.exceptions.BadRequestException;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
    private NotificationTemplateRepository notificationTemplateRepository;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void update(Long id, NotificationTemplateWithPlatformsUpdateDto dto) {
        NotificationTemplate template = getById(id);

        updateNotificationTemplateFromDto(template, dto);
    }

    private void updateNotificationTemplateFromDto(NotificationTemplate template,
        NotificationTemplateWithPlatformsUpdateDto dto) {
        updateNotificationTemplate(template, dto);
        updateNotificationTemplatePlatforms(template.getNotificationPlatforms(), dto.getPlatforms());
    }

    private void updateNotificationTemplate(NotificationTemplate template,
        NotificationTemplateWithPlatformsUpdateDto dto) {
        template.setTitle(dto.getNotificationTemplateMainInfoDto().getTitle());
        template.setTitleEng(dto.getNotificationTemplateMainInfoDto().getTitleEng());
        template.setNotificationType(dto.getNotificationTemplateMainInfoDto().getType());
        template.setTrigger(dto.getNotificationTemplateMainInfoDto().getTrigger());
        template.setTime(dto.getNotificationTemplateMainInfoDto().getTime());
        template.setSchedule(dto.getNotificationTemplateMainInfoDto().getSchedule());
    }

    private void updateNotificationTemplatePlatforms(List<NotificationPlatform> platforms,
        List<NotificationPlatformDto> platformDtos) {
        for (NotificationPlatform platform : platforms) {
            NotificationPlatformDto platformDto = platformDtos.stream()
                .filter(dto -> dto.getId().equals(platform.getId()))
                .findAny()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_PLATFORM_NOT_FOUND));
            platform.setBody(platformDto.getBody());
            platform.setBodyEng(platformDto.getBodyEng());
            platform.setNotificationStatus(platformDto.getStatus());
        }
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

    @Override
    @Transactional
    public void changeNotificationStatusById(Long id, String status) {
        var newStatus = getValidNotificationStatusByNameOrThrow(status);
        var notificationTemplate = getById(id);
        notificationTemplate.setNotificationStatus(newStatus);
        notificationTemplate.getNotificationPlatforms()
            .forEach(platform -> platform.setNotificationStatus(newStatus));
    }

    private NotificationStatus getValidNotificationStatusByNameOrThrow(String status) {
        return Arrays.stream(NotificationStatus.values())
            .filter(s -> s.name().equals(status))
            .findAny()
            .orElseThrow(() -> new BadRequestException(ErrorMessage.NOTIFICATION_STATUS_DOES_NOT_EXIST + status));
    }

    /**
     * {@inheritDoc}
     */
    private NotificationTemplate getById(Long id) {
        return notificationTemplateRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_TEMPLATE_NOT_FOUND_BY_ID + id));
    }
}
