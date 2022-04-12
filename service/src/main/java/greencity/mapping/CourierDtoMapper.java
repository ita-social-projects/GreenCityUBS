package greencity.mapping;

import greencity.constant.ErrorMessage;
import greencity.dto.CourierDto;
import greencity.dto.CourierTranslationDto;
import greencity.entity.order.Courier;
import greencity.exceptions.UserNotFoundException;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CourierDtoMapper extends AbstractConverter<Courier, CourierDto> {
    @Override
    protected CourierDto convert(Courier source) {

        return CourierDto.builder()
            .courierId(source.getId())
            .courierStatus(source.getCourierStatus().toString())
            .courierTranslationDtos(
                source.getCourierTranslationList().stream()
                    .map(courierTranslation -> CourierTranslationDto.builder()
                        .languageCode(courierTranslation.getLanguage().getCode())
                        .name(courierTranslation.getName())
                        .build())
                    .collect(Collectors.toList()))
            .createDate(source.getCreateDate())
            .createdBy(Optional.ofNullable(source.getCreatedBy())
                .map(user -> user.getRecipientName() + " " + user.getRecipientSurname())
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.CANNOT_FIND_USER_WHICH_CREATED_COURIER)))
            .build();
    }
}
