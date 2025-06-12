package greencity.validator;

import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.order.UpdateOrderPageAdminDto;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateOrderPageAdminValidatorTest {
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

    private static Stream<Arguments> provideInvalidUserInfoData() {
        return Stream.of(
            Arguments.of("", "Tester", "Customer Name cannot be blank"),
            Arguments.of("!@#$%^&*()`", "Tester",
                "Only alphabetic characters and '-', ' ', and apostrophe are allowed"),
            Arguments.of("Tester", "", "Customer Surname cannot be blank"),
            Arguments.of("Tester", "!@#$%^&*()",
                "Only alphabetic characters and '-', ' ', and apostrophe are allowed")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUserInfoData")
    void sagsagsagTest(String name, String surname, String expectedMessage) {
        UpdateOrderPageAdminDto invalidDto = UpdateOrderPageAdminDto.builder()
            .userInfoDto(UbsCustomersDtoUpdate.builder()
                .customerId(1L)
                .customerName(name)
                .customerSurname(surname)
                .build())
            .build();

        boolean result = validator.isValid(invalidDto, context);

        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(expectedMessage);
    }

    @Test
    void updateOrderPageAdminValidationForNullDtoTest() {
        boolean result = validator.isValid(new UpdateOrderPageAdminDto(), context);

        assertTrue(result);
    }
}
