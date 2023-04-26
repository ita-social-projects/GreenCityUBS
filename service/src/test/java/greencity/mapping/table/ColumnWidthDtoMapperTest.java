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
public class ColumnWidthDtoMapperTest {

    @InjectMocks
    ColumnWidthDtoMapper columnWidthDtoMapper;

    @Test
    void convert() {
        TableColumnWidthForEmployee tableColumnWidthForEmployee = ModelUtils.getTestTableColumnWidth();

        ColumnWidthDto columnWidthDto = columnWidthDtoMapper.convert(tableColumnWidthForEmployee);

        assertEquals(tableColumnWidthForEmployee.getAmountDue(), columnWidthDto.getAmountDue());
    }
}
