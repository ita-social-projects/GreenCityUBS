package greencity.mapping.table;

import greencity.ModelUtils;
import greencity.dto.table.ColumnWidthDto;
import greencity.entity.table.TableColumnWidthForEmployee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TableColumnWidthConverterTest {
    @InjectMocks
    private TableColumnWidthConverter tableColumnWidthConverter;

    @Test
    void convert() {
        ColumnWidthDto columnWidthDto = ModelUtils.getTestColumnWidthDto();

        TableColumnWidthForEmployee tableColumnWidthForEmployee = tableColumnWidthConverter.convert(columnWidthDto);

        assertEquals(columnWidthDto.getAmountDue(), tableColumnWidthForEmployee.getAmountDue());
    }
}
