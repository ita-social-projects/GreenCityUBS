package greencity.mapping;

import greencity.dto.BigOrderTableDTO;
import greencity.entity.order.BigOrderTableViews;
import org.modelmapper.AbstractConverter;

public class BigOrderTableDtoMapper extends AbstractConverter<BigOrderTableViews, BigOrderTableDTO> {
    @Override
    protected BigOrderTableDTO convert(BigOrderTableViews bigViews) {
        return new BigOrderTableDTO()
            .setId(bigViews.getId())
            .setOrderStatus(bigViews.getOrderStatus())
            .setOrderPaymentStatus(bigViews.getOrderPaymentStatus())
            .setOrderDate(bigViews.getOrderDate().toString())
            .setPaymentDate(bigViews.getPaymentDate().toString())
            .setClientName(bigViews.getClientName())
            .setClientPhone(bigViews.getClientPhoneNumber())
            .setClientEmail(bigViews.getClientEmail())
            .setSenderName(bigViews.getSenderName())
            .setSenderPhone(bigViews.getSenderPhone())
            .setSenderEmail(bigViews.getSenderEmail())
            .setViolationsAmount(bigViews.getViolationsAmount())
            .setRegion(bigViews.getRegion())
            .setSettlement(bigViews.getSettlement())
            .setDistrict(bigViews.getDistrict())
            .setAddress(bigViews.getAddress())
            .setCommentToAddressForClient(bigViews.getCommentToAddressForClient())
            .setCommentForOrderByClient(bigViews.getCommentForOrderByClient())
            .setCommentsForOrder(bigViews.getCommentsForOrder())
            .setBagsAmount(bigViews.getBagAmount())
            .setTotalOrderSum(bigViews.getTotalOrderSum())
            .setOrderCertificateCode(bigViews.getOrderCertificateCode())
            .setGeneralDiscount(bigViews.getGeneralDiscount())
            .setAmountDue(bigViews.getAmountDue())
            .setTotalPayment(bigViews.getTotalPayment())
            .setDateOfExport(bigViews.getDateOfExport().toString())
            .setTimeOfExport(bigViews.getTimeOfExport())
            .setIdOrderFromShop(bigViews.getIdOrderFromShop())
            .setResponsibleNavigator(bigViews.getResponsibleNavigator())
            .setResponsibleLogicMan(bigViews.getResponsibleLogicMan())
            .setResponsibleDriver(bigViews.getResponsibleDriver())
            .setResponsibleCaller(bigViews.getResponsibleCaller())
            .setReceivingStation(bigViews.getReceivingStation())
            .setIsBlocked(bigViews.getIsBlocked())
            .setBlockedBy(bigViews.getBlockedBy());
    }
}
