package greencity.entity.table;

import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class TableColumnWidthForEmployeeTest {
    private static final Employee EMPLOYEE = Employee.builder().build();
    private static final TableColumnWidthForEmployee TABLE_COLUMN_WIDTH = new TableColumnWidthForEmployee(EMPLOYEE);

    @Test
    void checkTableColumnWidthEmployee() {
        assertEquals(EMPLOYEE, TABLE_COLUMN_WIDTH.getEmployee());
    }

    static Stream<Arguments> testArguments() {
        int defaultWidth = 120;
        return Stream.of(
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getAddress()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getAmountDue()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getBagsAmount()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getBlockedBy()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getCity()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getClientEmail()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getClientName()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getClientPhone()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getCommentForOrderByClient()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getCommentToAddressForClient()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getCommentsForOrder()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getDateOfExport()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getDistrict()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getGeneralDiscount()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getOrderId()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getIdOrderFromShop()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getOrderCertificateCode()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getOrderDate()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getOrderPaymentStatus()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getOrderStatus()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getPaymentDate()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getReceivingStatus()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getRegion()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getResponsibleCaller()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getResponsibleDriver()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getResponsibleLogicMan()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getResponsibleNavigator()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getSenderEmail()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getSenderName()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getSenderPhone()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getTimeOfExport()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getTotalOrderSum()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getTotalPayment()),
            Arguments.of(defaultWidth, TABLE_COLUMN_WIDTH.getViolationsAmount()));
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    void tableColumnWidthForEmployeeWithDefaultWidthTest(int expected, int actual) {
        assertEquals(expected, actual);
    }

    @Test
    void testNoArgsConstructor() {
        TableColumnWidthForEmployee tableColumnWidth = new TableColumnWidthForEmployee();
        assertNotNull(tableColumnWidth);
        assertNull(tableColumnWidth.getId());
        assertNull(tableColumnWidth.getEmployee());
    }

    @Test
    void testBuilder() {
        Employee newEmployee = Employee.builder().build();
        TableColumnWidthForEmployee tableColumnWidth = TableColumnWidthForEmployee.builder()
            .employee(newEmployee)
            .address(100)
            .amountDue(200)
            .build();

        assertNotNull(tableColumnWidth);
        assertEquals(EMPLOYEE, tableColumnWidth.getEmployee());
        assertEquals(100, tableColumnWidth.getAddress());
        assertEquals(200, tableColumnWidth.getAmountDue());
    }
}
