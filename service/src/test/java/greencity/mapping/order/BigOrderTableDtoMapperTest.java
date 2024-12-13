package greencity.mapping.order;

import greencity.ModelUtils;
import greencity.dto.order.BigOrderTableDTO;
import greencity.entity.order.BigOrderTableViews;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BigOrderTableDtoMapperTest {
    @InjectMocks
    BigOrderTableDtoMapper bigOrderTableDtoMapper;
    private BigOrderTableViews bigViews;

    @BeforeEach
    void setUp() {
        bigViews = mock(BigOrderTableViews.class);
    }

    @Test
    void convert() {
        var bigOrderTableDto = ModelUtils.getBigOrderTableDto();
        var bigOrderTableView = ModelUtils.getBigOrderTableViews();
        assertEquals(bigOrderTableDto, bigOrderTableDtoMapper.convert(bigOrderTableView));
    }

    @Test
    void convertNullDateValue() {
        var bigOrderTableDto = ModelUtils.getBigOrderTableDtoByDateNullTest();
        var bigOrderTableView = ModelUtils.getBigOrderTableViewsByDateNullTest();
        assertEquals(bigOrderTableDto, bigOrderTableDtoMapper.convert(bigOrderTableView));
    }

    @ParameterizedTest
    @CsvSource({
        "0, -, setMixedWaste120L",
        "100, '100', setMixedWaste120L",
        "0, '-', setTextileWaste60L",
        "50, '50', setTextileWaste60L",
        "0, '-', setTextileWaste20L",
        "20, '20', setTextileWaste20L"
    })
    void testWasteFields(Long input, String expected, String method) {
        switch (method) {
            case "setMixedWaste120L" -> {
                when(bigViews.getMixedWaste120()).thenReturn(input);
                BigOrderTableDTO result = bigOrderTableDtoMapper.convert(bigViews);
                assertEquals(expected, result.getMixedWaste120L());
            }
            case "setTextileWaste60L" -> {
                when(bigViews.getTextileWaste60()).thenReturn(input);
                BigOrderTableDTO result = bigOrderTableDtoMapper.convert(bigViews);
                assertEquals(expected, result.getTextileWaste60L());
            }
            case "setTextileWaste20L" -> {
                when(bigViews.getTextileWaste20()).thenReturn(input);
                BigOrderTableDTO result = bigOrderTableDtoMapper.convert(bigViews);
                assertEquals(expected, result.getTextileWaste20L());
            }
        }
    }
}
