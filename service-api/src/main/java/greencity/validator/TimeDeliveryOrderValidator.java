package greencity.validator;

import greencity.annotations.TimeDeliveryOrder;
import greencity.dto.order.ExportDetailsDtoUpdate;
import greencity.exceptions.BadRequestException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeDeliveryOrderValidator implements ConstraintValidator<TimeDeliveryOrder, ExportDetailsDtoUpdate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public void initialize(TimeDeliveryOrder constraintAnnotation) {
    }

    @Override
    public boolean isValid(ExportDetailsDtoUpdate dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getTimeDeliveryFrom() == null || dto.getTimeDeliveryTo() == null) {
            return true;
        }

        try {
            LocalDateTime timeFrom = LocalDateTime.parse(dto.getTimeDeliveryFrom(), FORMATTER);
            LocalDateTime timeTo = LocalDateTime.parse(dto.getTimeDeliveryTo(), FORMATTER);
            return !timeFrom.isAfter(timeTo);
        } catch (Exception e) {
            throw new BadRequestException("timeDeliveryFrom must be before timeDeliveryTo");
        }
    }
}