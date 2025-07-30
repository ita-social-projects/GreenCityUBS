package greencity.validator;

import greencity.dto.tariff.TariffWithChatAccess;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffsValidatorTest {
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    @Mock
    private ConstraintValidatorContext context;
    @InjectMocks
    private TariffsValidator validator;

    @Test
    void isValid() {
        TariffWithChatAccess tariffWithChatAccess = TariffWithChatAccess.builder()
            .tariffId(1L)
            .hasChat(true)
            .build();

        List<TariffWithChatAccess> singleTariff = List.of(tariffWithChatAccess);

        List<TariffWithChatAccess> multipleTariffs = List.of(tariffWithChatAccess,
            TariffWithChatAccess.builder()
                .tariffId(2L)
                .hasChat(false)
                .build());

        List<TariffWithChatAccess> nullList = null;
        List<TariffWithChatAccess> emptyList = List.of();
        List<TariffWithChatAccess> nullTariffList = new ArrayList<>();
        nullTariffList.add(null);
        List<TariffWithChatAccess> nullTariff = List.of(TariffWithChatAccess.builder().build());

        List<TariffWithChatAccess> duplicateTariffs = List.of(tariffWithChatAccess,
            TariffWithChatAccess.builder()
                .tariffId(1L)
                .hasChat(false)
                .build());

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);

        assertTrue(validator.isValid(singleTariff, context));
        assertTrue(validator.isValid(multipleTariffs, context));

        assertFalse(validator.isValid(nullList, context));
        assertFalse(validator.isValid(emptyList, context));
        assertFalse(validator.isValid(nullTariffList, context));
        assertFalse(validator.isValid(nullTariff, context));
        assertFalse(validator.isValid(duplicateTariffs, context));
    }
}
