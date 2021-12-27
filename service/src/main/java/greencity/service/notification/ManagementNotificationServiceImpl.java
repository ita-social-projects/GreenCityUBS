package greencity.service.notification;

import greencity.constant.ErrorMessage;
import greencity.dto.NotificationTemplateDto;
import greencity.dto.PageableDto;
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
public class ManagementNotificationServiceImpl implements ManagementNotificationService {
    private NotificationTemplateRepository notificationTemplateRepository;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(NotificationTemplateDto notificationTemplateDto) {
        NotificationTemplate template = getById(notificationTemplateDto.getId());
        template.setBody(notificationTemplateDto.getBody());
        template.setTitle(notificationTemplateDto.getTitle());
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
            .map(x -> modelMapper.map(x, NotificationTemplateDto.class)).collect(Collectors.toList());
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

    private NotificationTemplate getById(Long id) {
        return notificationTemplateRepository.findNotificationTemplateById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_TEMPLATE_NOT_FOUND + id));
    }
}
