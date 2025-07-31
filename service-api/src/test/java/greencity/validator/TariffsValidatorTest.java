package greencity.validator;

import greencity.dto.tariff.TariffWithChatAccess;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static greencity.ModelUtils.getTariffWithChatAccess;
import static greencity.constant.ErrorMessage.TARIFF_LIST_IS_NULL;
import static greencity.constant.ErrorMessage.TARIFF_LIST_IS_EMPTY;
import static greencity.constant.ErrorMessage.TARIFF_IS_NULL;
import static greencity.constant.ErrorMessage.TARIFFID_IS_NULL;
import static greencity.constant.ErrorMessage.TARIFFID_IS_NOT_POSITIVE;
import static greencity.constant.ErrorMessage.TARIFF_LIST_CONTAINS_DUPLICATES;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffsValidatorTest {
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    @Mock
    private ConstraintValidatorContext context;
    @InjectMocks
    private TariffsValidator validator;
    private static final TariffWithChatAccess tariffWithChatAccess = getTariffWithChatAccess();

    @ParameterizedTest
    @MethodSource("provideValidTariffs")
    void isValidWithValidTariffsTest(List<TariffWithChatAccess> tariffs) {
        assertTrue(validator.isValid(tariffs, context));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTariffs")
    void isValidWithInvalidTariffsTest(List<TariffWithChatAccess> tariffs, String expectedMessage) {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        assertFalse(validator.isValid(tariffs, context));
        verify(context).buildConstraintViolationWithTemplate(expectedMessage);
    }

    private static Stream<Arguments> provideValidTariffs() {
        List<TariffWithChatAccess> validTariffs = List.of(tariffWithChatAccess);
        List<TariffWithChatAccess> multipleTariffs = List.of(tariffWithChatAccess,
            TariffWithChatAccess.builder()
                .tariffId(2L)
                .hasChat(false)
                .build());

        return Stream.of(
            Arguments.of(validTariffs),
            Arguments.of(multipleTariffs));
    }

    private static Stream<Arguments> provideInvalidTariffs() {
        List<TariffWithChatAccess> nullList = null;
        List<TariffWithChatAccess> emptyList = List.of();
        List<TariffWithChatAccess> nullTariff = new ArrayList<>();
        nullTariff.add(null);
        List<TariffWithChatAccess> nullTariffId = List.of(TariffWithChatAccess.builder()
            .build());
        List<TariffWithChatAccess> zeroTariffId = List.of(TariffWithChatAccess.builder()
            .tariffId(0L)
            .build());
        List<TariffWithChatAccess> duplicateTariffs = List.of(tariffWithChatAccess,
            TariffWithChatAccess.builder()
                .tariffId(1L)
                .hasChat(false)
                .build());

        return Stream.of(
            Arguments.of(nullList, TARIFF_LIST_IS_NULL),
            Arguments.of(emptyList, TARIFF_LIST_IS_EMPTY),
            Arguments.of(nullTariff, TARIFF_IS_NULL),
            Arguments.of(nullTariffId, TARIFFID_IS_NULL),
            Arguments.of(zeroTariffId, TARIFFID_IS_NOT_POSITIVE),
            Arguments.of(duplicateTariffs, TARIFF_LIST_CONTAINS_DUPLICATES));
    }
}