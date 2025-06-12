package greencity.validator;

import greencity.annotations.ValidUpdateOrderPageAdmin;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.order.UpdateOrderPageAdminDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UpdateOrderPageAdminValidator
    implements ConstraintValidator<ValidUpdateOrderPageAdmin, UpdateOrderPageAdminDto> {
    @Override
    public void initialize(ValidUpdateOrderPageAdmin constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(UpdateOrderPageAdminDto updateOrderPageAdminDto,
        ConstraintValidatorContext context) {
        UbsCustomersDtoUpdate userInfo = updateOrderPageAdminDto.getUserInfoDto();

        if (userInfo == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        String name = userInfo.getCustomerName();
        if (name != null) {
            if (name.isBlank()) {
                context.buildConstraintViolationWithTemplate("Customer Name cannot be blank")
                    .addPropertyNode("customerName")
                    .addConstraintViolation();
                return false;
            } else if (!name.matches("^[A-Za-zА-Яа-я\\- '\\s]+$")) {
                context.buildConstraintViolationWithTemplate(
                    "Only alphabetic characters and '-', ' ', and apostrophe are allowed")
                    .addPropertyNode("customerName")
                    .addConstraintViolation();
                return false;
            }
        }

        String surname = userInfo.getCustomerSurname();
        if (surname != null) {
            if (surname.isBlank()) {
                context.buildConstraintViolationWithTemplate("Customer Surname cannot be blank")
                    .addPropertyNode("customerSurname")
                    .addConstraintViolation();
                return false;
            } else if (!surname.matches("^[A-Za-zА-Яа-я\\- '\\s]+$")) {
                context.buildConstraintViolationWithTemplate(
                    "Only alphabetic characters and '-', ' ', and apostrophe are allowed")
                    .addPropertyNode("customerSurname")
                    .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
