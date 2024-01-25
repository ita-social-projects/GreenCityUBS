package greencity.mapping.order;

import greencity.constant.AppConstant;
import greencity.dto.order.BigOrderTableDTO;
import greencity.dto.order.SenderLocation;
import greencity.entity.order.BigOrderTableViews;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
            .setRegion(new SenderLocation().setUa(bigViews.getRegion()).setEn(bigViews.getRegionEn()))
            .setCity(new SenderLocation().setUa(bigViews.getCity()).setEn(bigViews.getCityEn()))
            .setDistrict(new SenderLocation().setUa(bigViews.getDistrict()).setEn(bigViews.getDistrictEn()))
            .setAddress(new SenderLocation().setUa(bigViews.getAddress()).setEn(bigViews.getAddressEn()))
            .setCommentToAddressForClient(bigViews.getCommentToAddressForClient())
            .setCommentForOrderByClient(bigViews.getCommentForOrderByClient())
            .setCommentsForOrder(bigViews.getCommentForOrderByAdmin())
            .setBagsAmount(bigViews.getBagAmount())
            .setTotalOrderSum(convertCoinsIntoBills(bigViews.getTotalOrderSum()))
            .setOrderCertificateCode(bigViews.getOrderCertificateCode())
            .setGeneralDiscount(bigViews.getGeneralDiscount())
            .setAmountDue(convertCoinsIntoBills(bigViews.getAmountDue()))
            .setTotalPayment(convertCoinsIntoBills(bigViews.getTotalPayment()))
            .setDateOfExport(nonNull(bigViews.getDateOfExport()) ? bigViews.getDateOfExport().toString() : "")
            .setTimeOfExport(bigViews.getTimeOfExport())
            .setIdOrderFromShop(bigViews.getIdOrderFromShop())
            .setResponsibleNavigator(nonNull(bigViews.getResponsibleNavigatorId())
                ? bigViews.getResponsibleNavigatorId().toString()
                : "")
            .setResponsibleLogicMan(nonNull(bigViews.getResponsibleLogicManId())
                ? bigViews.getResponsibleLogicManId().toString()
                : "")
            .setResponsibleDriver(nonNull(bigViews.getResponsibleDriverId())
                ? bigViews.getResponsibleDriverId().toString()
                : "")
            .setResponsibleCaller(nonNull(bigViews.getResponsibleCallerId())
                ? bigViews.getResponsibleCallerId().toString()
                : "")
            .setReceivingStation(nonNull(bigViews.getReceivingStationId())
                ? bigViews.getReceivingStationId().toString()
                : "")
            .setIsBlocked(bigViews.getIsBlocked())
            .setBlockedBy(bigViews.getBlockedBy());
    }

    private Double convertCoinsIntoBills(Long coins) {
        return coins == null ? null
            : BigDecimal.valueOf(coins)
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .setScale(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
