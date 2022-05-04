package greencity.service.notification;

import greencity.constant.ErrorMessage;
import greencity.dto.notification.NotificationScheduleDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.violation.AddingViolationsToUserDto;
import greencity.dto.violation.UserViolationMailDto;
import greencity.entity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.order.Order;
import greencity.entity.schedule.NotificationSchedule;
import greencity.exceptions.NotFoundException;
import greencity.repository.NotificationScheduleRepo;
import greencity.repository.NotificationTemplateRepository;
import greencity.repository.OrderRepository;
import greencity.client.UserRemoteClient;
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
public class NotificationeServiceImpl implements NotificationeService {
    private NotificationTemplateRepository notificationTemplateRepository;
    private final ModelMapper modelMapper;
    private final NotificationScheduleRepo scheduleRepo;
    private final OrderRepository orderRepository;
    private final UserRemoteClient userRemoteClient;

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
                .setSchedule(getScheduleDto(notificationTemplate)))
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

    private NotificationScheduleDto getScheduleDto(NotificationTemplate notificationTemplate) {
        return modelMapper.map(scheduleRepo.getOne(
            notificationTemplate.getNotificationType()), NotificationScheduleDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendNotificationAboutViolation(AddingViolationsToUserDto dto, String language) {
        Order order = orderRepository.findById(dto.getOrderID()).orElse(null);
        UserViolationMailDto mailDto;
        if (order != null) {
            mailDto = UserViolationMailDto.builder()
                .name(order.getUser().getRecipientName())
                .email(order.getUser().getRecipientEmail())
                .violationDescription(dto.getViolationDescription())
                .language(language)
                .build();
            userRemoteClient.sendViolationOnMail(mailDto);
        }
    }
}
