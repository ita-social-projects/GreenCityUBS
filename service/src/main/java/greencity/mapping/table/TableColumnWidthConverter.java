package greencity.mapping.table;

import greencity.dto.table.ColumnWidthDto;
import greencity.entity.table.TableColumnWidthForEmployee;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class TableColumnWidthConverter extends AbstractConverter<ColumnWidthDto, TableColumnWidthForEmployee> {
    @Override
    protected TableColumnWidthForEmployee convert(ColumnWidthDto columnWidthDto) {
        return TableColumnWidthForEmployee.builder()
                .address(columnWidthDto.getAddress())
                .city(columnWidthDto.getCity())
                .amountDue(columnWidthDto.getAmountDue())
                .bagsAmount(columnWidthDto.getBagsAmount())
                .blockedBy(columnWidthDto.getBlockedBy())
                .clientEmail(columnWidthDto.getClientEmail())
                .clientName(columnWidthDto.getClientName())
                .clientPhone(columnWidthDto.getClientPhone())
                .commentForOrderByClient(columnWidthDto.getCommentForOrderByClient())
                .commentsForOrder(columnWidthDto.getCommentsForOrder())
                .commentToAddressForClient(columnWidthDto.getCommentToAddressForClient())
                .dateOfExport(columnWidthDto.getDateOfExport())
                .district(columnWidthDto.getDistrict())
                .generalDiscount(columnWidthDto.getGeneralDiscount())
                .orderCertificateCode(columnWidthDto.getOrderCertificateCode())
                .orderDate(columnWidthDto.getOrderDate())
                .idOrderFromShop(columnWidthDto.getIdOrderFromShop())
                .orderStatus(columnWidthDto.getOrderStatus())
                .orderPaymentStatus(columnWidthDto.getOrderPaymentStatus())
                .paymentDate(columnWidthDto.getPaymentDate())
                .region(columnWidthDto.getRegion())
                .orderId(columnWidthDto.getId())
                .receivingStatus(columnWidthDto.getReceivingStatus())
                .responsibleCaller(columnWidthDto.getResponsibleCaller())
                .responsibleDriver(columnWidthDto.getResponsibleDriver())
                .responsibleLogicMan(columnWidthDto.getResponsibleLogicMan())
                .responsibleNavigator(columnWidthDto.getResponsibleNavigator())
                .senderEmail(columnWidthDto.getSenderEmail())
                .senderName(columnWidthDto.getSenderName())
                .senderPhone(columnWidthDto.getSenderPhone())
                .timeOfExport(columnWidthDto.getTimeOfExport())
                .totalOrderSum(columnWidthDto.getTotalOrderSum())
                .totalPayment(columnWidthDto.getTotalPayment())
                .violationsAmount(columnWidthDto.getViolationsAmount())
                .build();
    }
}
