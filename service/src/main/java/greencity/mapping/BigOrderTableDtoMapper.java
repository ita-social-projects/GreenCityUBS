package greencity.mapping;

import greencity.dto.BigOrderTableDTO;
import greencity.entity.order.BigOrderTableViews;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import static java.util.Objects.nonNull;

@Component
public class BigOrderTableDtoMapper extends AbstractConverter<BigOrderTableViews, BigOrderTableDTO> {
    @Override
    protected BigOrderTableDTO convert(BigOrderTableViews bigViews) {
        return new BigOrderTableDTO()
            .setId(bigViews.getId())
            .setOrderStatus(bigViews.getOrderStatus())
            .setOrderPaymentStatus(bigViews.getOrderPaymentStatus())
            .setOrderDate(nonNull(bigViews.getOrderDate()) ? bigViews.getOrderDate().toString() : "")
            .setPaymentDate(nonNull(bigViews.getPaymentDate()) ? bigViews.getPaymentDate().toString() : "")
            .setClientName(bigViews.getClientName())
            .setClientEmail(bigViews.getClientEmail())
            .setClientPhone(bigViews.getClientPhoneNumber())
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
            .setDateOfExport(nonNull(bigViews.getDateOfExport()) ? bigViews.getDateOfExport().toString() : "")
            .setTimeOfExport(bigViews.getTimeOfExport())
            .setIdOrderFromShop(bigViews.getIdOrderFromShop())
            .setResponsibleNavigator(bigViews.getResponsibleNavigatorId())
            .setResponsibleLogicMan(bigViews.getResponsibleLogicManId())
            .setResponsibleDriver(bigViews.getResponsibleDriverId())
            .setResponsibleCaller(bigViews.getResponsibleCallerId())
            .setReceivingStation(bigViews.getReceivingStation())
            .setIsBlocked(bigViews.getIsBlocked())
            .setBlockedBy(bigViews.getBlockedBy());
    }
}
