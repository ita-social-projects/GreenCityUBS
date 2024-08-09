package greencity.service.ubs;

import greencity.constant.AppConstant;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.exceptions.NotFoundException;
import greencity.repository.CertificateRepository;
import greencity.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;

@Slf4j
public final class PaymentUtil {
    private PaymentUtil() {
    }

    public static Long convertBillsIntoCoins(Double bills) {
        return bills == null ? 0
            : BigDecimal.valueOf(bills)
                .movePointRight(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .setScale(AppConstant.NO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
                .longValue();
    }

    public static Double convertCoinsIntoBills(Long coins) {
        return coins == null ? 0
            : BigDecimal.valueOf(coins)
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .setScale(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static Double setTotalPrice(CounterOrderDetailsDto dto) {
        if (isContainsExportedBags(dto)) {
            return dto.getSumExported();
        }
        if (isContainsConfirmedBags(dto)) {
            return dto.getSumConfirmed();
        }
        return dto.getSumAmount();
    }

    public static boolean isContainsConfirmedBags(CounterOrderDetailsDto dto) {
        return dto.getSumConfirmed() != 0;
    }

    public static boolean isContainsExportedBags(CounterOrderDetailsDto dto) {
        return dto.getSumExported() != 0;
    }

    /**
     * Method that calculates overpayment on user's order in coins.
     *
     * @param order           of {@link Order} order;
     * @param sumToPayInCoins of {@link Long} sum to pay in coins;
     * @return {@link Long }
     * @author Ostap Mykhailivskyi
     */
    public static Long calculateOverpayment(Order order, Long sumToPayInCoins) {
        long paidAmountInCoins = calculatePaidAmount(order);

        long certificateSum = order.getCertificates().stream()
            .map(Certificate::getPoints)
            .reduce(0, Integer::sum);
        long bonusOverpaymentInCoins = 100L * (certificateSum + order.getPointsToUse()) - sumToPayInCoins;

        long overpaymentInCoins = paidAmountInCoins + bonusOverpaymentInCoins;

        return OrderStatus.CANCELED == order.getOrderStatus()
            ? paidAmountInCoins
            : Math.max(overpaymentInCoins, 0L);
    }

    /**
     * Method that calculate paid amount in coins.
     *
     * @param order of {@link Order} order id;
     * @return {@link Long } paid amount in coins;
     * @author Ostap Mykhailivskyi
     */
    public static Long calculatePaidAmount(Order order) {
        return order.getPayment().stream()
            .filter(x -> x.getPaymentStatus().equals(PaymentStatus.PAID))
            .map(Payment::getAmount)
            .reduce(0L, Long::sum);
    }

    /**
     * Method that calculate unpaid amount in coins.
     *
     * @param order             of {@link Order} order id;
     * @param sumToPayInCoins   of {@link Long} sum to pay in coins;
     * @param paidAmountInCoins of {@link Long} paid amount in coins;
     * @return {@link Long } unpaid amount in coins
     * @author Ostap Mykhailivskyi
     */
    public static Long calculateUnpaidAmount(Order order, Long sumToPayInCoins, Long paidAmountInCoins) {
        long unpaidAmountInCoins = sumToPayInCoins - paidAmountInCoins
            - 100L * (order.getPointsToUse() + (order.getCertificates().stream()
                .map(Certificate::getPoints)
                .reduce(Integer::sum)
                .orElse(0)));

        return Math.max(unpaidAmountInCoins, 0);
    }

    public static Long getUbsCourierOrWriteOffStationSum(Order order) {
        if (order.getUbsCourierSum() != null && order.getWriteOffStationSum() == null) {
            return order.getUbsCourierSum();
        } else if (order.getWriteOffStationSum() != null && order.getUbsCourierSum() == null) {
            return order.getWriteOffStationSum();
        } else if (order.getWriteOffStationSum() != null) {
            return order.getWriteOffStationSum() + order.getUbsCourierSum();
        } else {
            return 0L;
        }
    }

    public static CounterOrderDetailsDto getPriceDetails(Long id, OrderRepository orderRepository,
        OrderBagService orderBagService, CertificateRepository certificateRepository) {
        CounterOrderDetailsDto dto = new CounterOrderDetailsDto();
        Order order = orderRepository.getOrderDetails(id)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + id));
        List<Bag> bag = orderBagService.findAllBagsInOrderBagsList(order.getOrderBags());
        final List<Certificate> currentCertificate = certificateRepository.findCertificate(id);

        long sumAmountInCoins = 0;
        long sumConfirmedInCoins = 0;
        long sumExportedInCoins = 0;
        long totalSumAmountInCoins;
        long totalSumConfirmedInCoins;
        long totalSumExportedInCoins;
        if (!bag.isEmpty()) {
            sumAmountInCoins = getSumInCoins(order.getAmountOfBagsOrdered().entrySet(), bag);
            sumConfirmedInCoins = getSumInCoins(order.getConfirmedQuantity().entrySet(), bag);
            sumExportedInCoins = getSumInCoins(order.getExportedQuantity().entrySet(), bag);

            if (!order.getExportedQuantity().isEmpty()) {
                sumExportedInCoins += PaymentUtil.getUbsCourierOrWriteOffStationSum(order);
            } else if (!order.getConfirmedQuantity().isEmpty()) {
                sumConfirmedInCoins += PaymentUtil.getUbsCourierOrWriteOffStationSum(order);
            } else {
                sumAmountInCoins += PaymentUtil.getUbsCourierOrWriteOffStationSum(order);
            }
        }

        if (!currentCertificate.isEmpty()) {
            Integer certificateBonus = currentCertificate.stream()
                .map(Certificate::getPoints).reduce(Integer::sum).orElse(0);
            long certificatesAndBonusesInCoins = 100L * (certificateBonus + order.getPointsToUse());
            long totalSumAmountInCoinsToCheck = sumAmountInCoins - certificatesAndBonusesInCoins;
            totalSumAmountInCoins = totalSumAmountInCoinsToCheck <= 0 ? 0 : totalSumAmountInCoinsToCheck;
            totalSumConfirmedInCoins = sumConfirmedInCoins - certificatesAndBonusesInCoins;
            totalSumExportedInCoins = sumExportedInCoins - certificatesAndBonusesInCoins;
            dto.setCertificateBonus(certificateBonus.doubleValue());
            dto.setCertificate(currentCertificate.stream().map(Certificate::getCode).collect(Collectors.toList()));
        } else {
            dto.setCertificateBonus((double) 0);
            long bonusesInCoins = 100L * order.getPointsToUse();
            totalSumAmountInCoins = sumAmountInCoins - bonusesInCoins;
            totalSumConfirmedInCoins = sumConfirmedInCoins - bonusesInCoins;
            totalSumExportedInCoins = sumExportedInCoins - bonusesInCoins;
        }
        if (order.getConfirmedQuantity().isEmpty()) {
            totalSumConfirmedInCoins = 0;
        }
        if (order.getExportedQuantity().isEmpty()) {
            totalSumExportedInCoins = 0;
        }
        dto.setTotalAmount(
            order.getAmountOfBagsOrdered().values()
                .stream().reduce(Integer::sum).orElse(0).doubleValue());
        dto.setTotalConfirmed(
            order.getConfirmedQuantity().values()
                .stream().reduce(Integer::sum).orElse(0).doubleValue());
        dto.setTotalExported(
            order.getExportedQuantity().values()
                .stream().reduce(Integer::sum).orElse(0).doubleValue());

        setDtoInfoFromOrder(dto, order);
        setDtoInfo(dto, sumAmountInCoins, sumExportedInCoins, sumConfirmedInCoins,
            totalSumAmountInCoins, totalSumConfirmedInCoins, totalSumExportedInCoins);
        return dto;
    }

    /**
     * Helper method of
     * {@link #getPriceDetails(Long, OrderRepository, OrderBagService, CertificateRepository)}
     * } which calculates the total price in coins based on the provided entries and
     * bag items.
     *
     * @param entries the set of entries representing the quantity of bags of
     *                {@link java.util.Map.Entry}
     * @param bag     the list of bags to calculate the price from {@link Bag}
     * @return the total price in coins {@link Long}
     * @throws NotFoundException if a bag with a specific ID was not found
     * @author Yurii Midianyi
     */
    public static Long getSumInCoins(Set<Map.Entry<Integer, Integer>> entries, List<Bag> bag) {
        long result = 0L;
        for (Map.Entry<Integer, Integer> entry : entries) {
            result += entry.getValue() * bag
                .stream()
                .filter(b -> b.getId().equals(entry.getKey()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + entry.getKey()))
                .getFullPrice();
        }
        return result;
    }

    public static void setDtoInfoFromOrder(CounterOrderDetailsDto dto, Order order) {
        dto.setOrderComment(order.getComment());
        dto.setNumberOrderFromShop(order.getAdditionalOrders());
        dto.setBonus(order.getPointsToUse().doubleValue());
    }

    public static void setDtoInfo(CounterOrderDetailsDto dto, long sumAmountInCoins, long sumExportedInCoins,
        long sumConfirmedInCoins, long totalSumAmountInCoins, long totalSumConfirmedInCoins,
        long totalSumExportedInCoins) {
        dto.setSumAmount(PaymentUtil.convertCoinsIntoBills(sumAmountInCoins));
        dto.setSumConfirmed(PaymentUtil.convertCoinsIntoBills(sumConfirmedInCoins));
        dto.setSumExported(PaymentUtil.convertCoinsIntoBills(sumExportedInCoins));
        dto.setTotalSumAmount(PaymentUtil.convertCoinsIntoBills(totalSumAmountInCoins));
        dto.setTotalSumConfirmed(PaymentUtil.convertCoinsIntoBills(totalSumConfirmedInCoins));
        dto.setTotalSumExported(PaymentUtil.convertCoinsIntoBills(totalSumExportedInCoins));
    }
}
