package greencity.mapping.order;

import greencity.ModelUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BigOrderTableDtoMapperTest {
    @InjectMocks
    BigOrderTableDtoMapper bigOrderTableDtoMapper;

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
}
