package greencity.validator;

import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.order.UpdateOrderPageAdminDto;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateOrderPageAdminValidatorTest {
    @Mock
    private ConstraintValidatorContext context;
    @InjectMocks
    private UpdateOrderPageAdminValidator validator;

    @BeforeEach
    void setup() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder =
            Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(
            Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class));
    }

    @Test
    void updateOrderPageAdminValidationForValidNameAndSurnameTest() {
        UpdateOrderPageAdminDto validDto = UpdateOrderPageAdminDto.builder()
            .userInfoDto(UbsCustomersDtoUpdate.builder()
                .customerId(1L)
                .customerName("Tester")
                .customerSurname("Tester")
                .build())
            .build();

        boolean result = validator.isValid(validDto, context);

        assertTrue(result);
    }

    @Test
    void updateOrderPageAdminValidationForNullCustomerNameTest() {
        UpdateOrderPageAdminDto invalidDto = UpdateOrderPageAdminDto.builder()
            .userInfoDto(UbsCustomersDtoUpdate.builder()
                .customerId(1L)
                .customerSurname("T es'- te r")
                .build())
            .build();

        boolean result = validator.isValid(invalidDto, context);

        assertTrue(result);
    }

    @Test
    void updateOrderPageAdminValidationForBlankCustomerNameTest() {
        UpdateOrderPageAdminDto invalidDto = UpdateOrderPageAdminDto.builder()
            .userInfoDto(UbsCustomersDtoUpdate.builder()
                .customerId(1L)
                .customerName("")
                .customerSurname("Tester")
                .build())
            .build();

        boolean result = validator.isValid(invalidDto, context);

        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Customer Name cannot be blank");
    }

    @Test
    void updateOrderPageAdminValidationForInvalidCharactersInCustomerNameTest() {
        UpdateOrderPageAdminDto invalidDto = UpdateOrderPageAdminDto.builder()
            .userInfoDto(UbsCustomersDtoUpdate.builder()
                .customerId(1L)
                .customerName("!@#$%^&*()`")
                .customerSurname("ValidSurname")
                .build())
            .build();

        boolean result = validator.isValid(invalidDto, context);

        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(
            "Only alphabetic characters and '-', ' ', and apostrophe are allowed");
    }

    @Test
    void updateOrderPageAdminValidationForNullCustomerSurnameTest() {
        UpdateOrderPageAdminDto invalidDto = UpdateOrderPageAdminDto.builder()
            .userInfoDto(UbsCustomersDtoUpdate.builder()
                .customerId(1L)
                .customerName("Tester")
                .build())
            .build();

        boolean result = validator.isValid(invalidDto, context);

        assertTrue(result);
    }

    @Test
    void updateOrderPageAdminValidationForBlankCustomerSurnameTest() {
        UpdateOrderPageAdminDto invalidDto = UpdateOrderPageAdminDto.builder()
            .userInfoDto(UbsCustomersDtoUpdate.builder()
                .customerId(1L)
                .customerName("Tester")
                .customerSurname("")
                .build())
            .build();

        boolean result = validator.isValid(invalidDto, context);

        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Customer Surname cannot be blank");
    }

    @Test
    void updateOrderPageAdminValidationForInvalidCharactersInCustomerSurnameTest() {
        UpdateOrderPageAdminDto invalidDto = UpdateOrderPageAdminDto.builder()
            .userInfoDto(UbsCustomersDtoUpdate.builder()
                .customerId(1L)
                .customerName("Tester")
                .customerSurname("!@#$%^&*()")
                .build())
            .build();

        boolean result = validator.isValid(invalidDto, context);

        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(
            "Only alphabetic characters and '-', ' ', and apostrophe are allowed");
    }

    @Test
    void updateOrderPageAdminValidationForNullDtoTest() {
        boolean result = validator.isValid(new UpdateOrderPageAdminDto(), context);

        assertTrue(result);
    }
}
