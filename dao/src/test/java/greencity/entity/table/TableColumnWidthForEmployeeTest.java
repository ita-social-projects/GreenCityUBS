package greencity.entity.table;

import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TableColumnWidthForEmployeeTest {
    private static final Employee employee = Employee.builder().build();
    private static final TableColumnWidthForEmployee tableColumnWidth = new TableColumnWidthForEmployee(employee);

    @Test
    void checkTableColumnWidthEmployee() {
        assertEquals(employee, tableColumnWidth.getEmployee());
    }

    static Stream<Arguments> testArguments() {
        int defaultWidth = 120;
        return Stream.of(
            Arguments.of(defaultWidth, tableColumnWidth.getAddress()),
            Arguments.of(defaultWidth, tableColumnWidth.getAmountDue()),
            Arguments.of(defaultWidth, tableColumnWidth.getBagsAmount()),
            Arguments.of(defaultWidth, tableColumnWidth.getBlockedBy()),
            Arguments.of(defaultWidth, tableColumnWidth.getCity()),
            Arguments.of(defaultWidth, tableColumnWidth.getClientEmail()),
            Arguments.of(defaultWidth, tableColumnWidth.getClientName()),
            Arguments.of(defaultWidth, tableColumnWidth.getClientPhone()),
            Arguments.of(defaultWidth, tableColumnWidth.getCommentForOrderByClient()),
            Arguments.of(defaultWidth, tableColumnWidth.getCommentToAddressForClient()),
            Arguments.of(defaultWidth, tableColumnWidth.getCommentsForOrder()),
            Arguments.of(defaultWidth, tableColumnWidth.getDateOfExport()),
            Arguments.of(defaultWidth, tableColumnWidth.getDistrict()),
            Arguments.of(defaultWidth, tableColumnWidth.getGeneralDiscount()),
            Arguments.of(defaultWidth, tableColumnWidth.getOrderId()),
            Arguments.of(defaultWidth, tableColumnWidth.getIdOrderFromShop()),
            Arguments.of(defaultWidth, tableColumnWidth.getOrderCertificateCode()),
            Arguments.of(defaultWidth, tableColumnWidth.getOrderDate()),
            Arguments.of(defaultWidth, tableColumnWidth.getOrderPaymentStatus()),
            Arguments.of(defaultWidth, tableColumnWidth.getOrderStatus()),
            Arguments.of(defaultWidth, tableColumnWidth.getPaymentDate()),
            Arguments.of(defaultWidth, tableColumnWidth.getReceivingStatus()),
            Arguments.of(defaultWidth, tableColumnWidth.getRegion()),
            Arguments.of(defaultWidth, tableColumnWidth.getResponsibleCaller()),
            Arguments.of(defaultWidth, tableColumnWidth.getResponsibleDriver()),
            Arguments.of(defaultWidth, tableColumnWidth.getResponsibleLogicMan()),
            Arguments.of(defaultWidth, tableColumnWidth.getResponsibleNavigator()),
            Arguments.of(defaultWidth, tableColumnWidth.getSenderEmail()),
            Arguments.of(defaultWidth, tableColumnWidth.getSenderName()),
            Arguments.of(defaultWidth, tableColumnWidth.getSenderPhone()),
            Arguments.of(defaultWidth, tableColumnWidth.getTimeOfExport()),
            Arguments.of(defaultWidth, tableColumnWidth.getTotalOrderSum()),
            Arguments.of(defaultWidth, tableColumnWidth.getTotalPayment()),
            Arguments.of(defaultWidth, tableColumnWidth.getViolationsAmount()));
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
        assertEquals(employee, tableColumnWidth.getEmployee());
        assertEquals(100, tableColumnWidth.getAddress());
        assertEquals(200, tableColumnWidth.getAmountDue());
    }
}
