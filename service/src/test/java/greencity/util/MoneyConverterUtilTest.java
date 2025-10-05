package greencity.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MoneyConverterUtilTest {
    @InjectMocks
    private MoneyConverterUtil converter;

    private static final Long COINS = 126L;
    private static final Double BILLS = 100.45;

    @Test
    void convertCoinsIntoBills() {
        Double bills = converter.convertCoinsIntoBills(COINS);
        Double expectedBills = 1.26;
        Assertions.assertEquals(expectedBills, bills);
    }

    @Test
    void convertBillsIntoCoins() {
        Long coins = converter.convertBillsIntoCoins(BILLS);
        Long expectedCoins = 10045L;
        Assertions.assertEquals(expectedCoins, coins);
    }
}