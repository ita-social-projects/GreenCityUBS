package greencity.mapping.table;

import greencity.dto.table.ColumnWidthDto;
import greencity.entity.table.TableColumnWidthForEmployee;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ColumnWidthDtoMapper extends AbstractConverter<TableColumnWidthForEmployee, ColumnWidthDto> {
    @Override
    protected ColumnWidthDto convert(TableColumnWidthForEmployee tableColumnWidthForEmployee) {
        return ColumnWidthDto.builder()
            .commentForOrderByClient(tableColumnWidthForEmployee.getCommentForOrderByClient())
            .clientPhone(tableColumnWidthForEmployee.getClientPhone())
            .address(tableColumnWidthForEmployee.getAddress())
            .city(tableColumnWidthForEmployee.getCity())
            .amountDue(tableColumnWidthForEmployee.getAmountDue())
            .bagsAmount(tableColumnWidthForEmployee.getBagsAmount())
            .blockedBy(tableColumnWidthForEmployee.getBlockedBy())
            .clientEmail(tableColumnWidthForEmployee.getClientEmail())
            .clientName(tableColumnWidthForEmployee.getClientName())
            .commentsForOrder(tableColumnWidthForEmployee.getCommentsForOrder())
            .district(tableColumnWidthForEmployee.getDistrict())
            .commentToAddressForClient(tableColumnWidthForEmployee.getCommentToAddressForClient())
            .dateOfExport(tableColumnWidthForEmployee.getDateOfExport())
            .generalDiscount(tableColumnWidthForEmployee.getGeneralDiscount())
            .orderDate(tableColumnWidthForEmployee.getOrderDate())
            .idOrderFromShop(tableColumnWidthForEmployee.getIdOrderFromShop())
            .orderStatus(tableColumnWidthForEmployee.getOrderStatus())
            .paymentDate(tableColumnWidthForEmployee.getPaymentDate())
            .receivingStatus(tableColumnWidthForEmployee.getReceivingStatus())
            .id(tableColumnWidthForEmployee.getOrderId())
            .orderCertificateCode(tableColumnWidthForEmployee.getOrderCertificateCode())
            .orderPaymentStatus(tableColumnWidthForEmployee.getOrderPaymentStatus())
            .region(tableColumnWidthForEmployee.getRegion())
            .responsibleCaller(tableColumnWidthForEmployee.getResponsibleCaller())
            .responsibleDriver(tableColumnWidthForEmployee.getResponsibleDriver())
            .responsibleLogicMan(tableColumnWidthForEmployee.getResponsibleLogicMan())
            .responsibleNavigator(tableColumnWidthForEmployee.getResponsibleNavigator())
            .senderEmail(tableColumnWidthForEmployee.getSenderEmail())
            .senderName(tableColumnWidthForEmployee.getSenderName())
            .senderPhone(tableColumnWidthForEmployee.getSenderPhone())
            .timeOfExport(tableColumnWidthForEmployee.getTimeOfExport())
            .totalOrderSum(tableColumnWidthForEmployee.getTotalOrderSum())
            .totalPayment(tableColumnWidthForEmployee.getTotalPayment())
            .violationsAmount(tableColumnWidthForEmployee.getViolationsAmount())
            .build();
    }
}
