package greencity.entity.table;

import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableColumnWidthForEmployeeTest {
    @Test
    void TableColumnWidthForEmployeeWithDefaultWidthTest() {
        Employee employee = Employee.builder().build();
        TableColumnWidthForEmployee tableColumnWidth = new TableColumnWidthForEmployee(employee);
        int defaultWidth = 120;
        assertEquals(employee, tableColumnWidth.getEmployee());
        assertEquals(defaultWidth, tableColumnWidth.getAddress());
        assertEquals(defaultWidth, tableColumnWidth.getAmountDue());
        assertEquals(defaultWidth, tableColumnWidth.getBagsAmount());
        assertEquals(defaultWidth, tableColumnWidth.getBlockedBy());
        assertEquals(defaultWidth, tableColumnWidth.getCity());
        assertEquals(defaultWidth, tableColumnWidth.getClientEmail());
        assertEquals(defaultWidth, tableColumnWidth.getClientName());
        assertEquals(defaultWidth, tableColumnWidth.getClientPhone());
        assertEquals(defaultWidth, tableColumnWidth.getCommentForOrderByClient());
        assertEquals(defaultWidth, tableColumnWidth.getCommentToAddressForClient());
        assertEquals(defaultWidth, tableColumnWidth.getCommentsForOrder());
        assertEquals(defaultWidth, tableColumnWidth.getDateOfExport());
        assertEquals(defaultWidth, tableColumnWidth.getDistrict());
        assertEquals(defaultWidth, tableColumnWidth.getGeneralDiscount());
        assertEquals(defaultWidth, tableColumnWidth.getOrderId());
        assertEquals(defaultWidth, tableColumnWidth.getIdOrderFromShop());
        assertEquals(defaultWidth, tableColumnWidth.getOrderCertificateCode());
        assertEquals(defaultWidth, tableColumnWidth.getOrderDate());
        assertEquals(defaultWidth, tableColumnWidth.getOrderPaymentStatus());
        assertEquals(defaultWidth, tableColumnWidth.getOrderStatus());
        assertEquals(defaultWidth, tableColumnWidth.getPaymentDate());
        assertEquals(defaultWidth, tableColumnWidth.getReceivingStatus());
        assertEquals(defaultWidth, tableColumnWidth.getRegion());
        assertEquals(defaultWidth, tableColumnWidth.getResponsibleCaller());
        assertEquals(defaultWidth, tableColumnWidth.getResponsibleDriver());
        assertEquals(defaultWidth, tableColumnWidth.getResponsibleLogicMan());
        assertEquals(defaultWidth, tableColumnWidth.getResponsibleNavigator());
        assertEquals(defaultWidth, tableColumnWidth.getSenderEmail());
        assertEquals(defaultWidth, tableColumnWidth.getSenderName());
        assertEquals(defaultWidth, tableColumnWidth.getSenderPhone());
        assertEquals(defaultWidth, tableColumnWidth.getTimeOfExport());
        assertEquals(defaultWidth, tableColumnWidth.getTotalOrderSum());
        assertEquals(defaultWidth, tableColumnWidth.getTotalPayment());
        assertEquals(defaultWidth, tableColumnWidth.getViolationsAmount());
    }
}
