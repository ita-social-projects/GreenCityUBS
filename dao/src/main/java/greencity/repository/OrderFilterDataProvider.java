package greencity.repository;

import greencity.filters.DateFilter;
import greencity.filters.OrderSearchCriteria;
import java.util.Map;
import java.util.function.Function;

public class OrderFilterDataProvider {
    private static final Map<String, Function<OrderSearchCriteria, Enum<?>[]>> FILTERS_ENUM_MAP = Map.of(
        "orderStatus", OrderSearchCriteria::getOrderStatus,
        "orderPaymentStatus", OrderSearchCriteria::getOrderPaymentStatus);

    private static final Map<String, Function<OrderSearchCriteria, String[]>> FILTERS_STRING_MAP = Map.of(
        "region", OrderSearchCriteria::getRegion,
        "city", OrderSearchCriteria::getCities,
        "district", OrderSearchCriteria::getDistricts,
        "regionEn", OrderSearchCriteria::getRegionEn,
        "cityEn", OrderSearchCriteria::getCitiesEn,
        "districtEn", OrderSearchCriteria::getDistrictsEn);

    private static final Map<String, Function<OrderSearchCriteria, DateFilter>> FILTERS_DATEFILTER_MAP = Map.of(
        "orderDate", OrderSearchCriteria::getOrderDate,
        "dateOfExport", OrderSearchCriteria::getDeliveryDate,
        "paymentDate", OrderSearchCriteria::getPaymentDate);

    private static final Map<String, Function<OrderSearchCriteria, Long[]>> FILTERS_LONG_MAP = Map.of(
        "receivingStationId", OrderSearchCriteria::getReceivingStation,
        "responsibleCallerId", OrderSearchCriteria::getResponsibleCallerId,
        "responsibleLogicManId", OrderSearchCriteria::getResponsibleLogicManId,
        "responsibleNavigatorId", OrderSearchCriteria::getResponsibleNavigatorId,
        "responsibleDriverId", OrderSearchCriteria::getResponsibleDriverId,
        "regionId", OrderSearchCriteria::getRegionId,
        "cityId", OrderSearchCriteria::getCityId,
        "districtId", OrderSearchCriteria::getDistrictId);

    Map<String, Function<OrderSearchCriteria, Enum<?>[]>> getFiltersEnum() {
        return FILTERS_ENUM_MAP;
    }

    Map<String, Function<OrderSearchCriteria, String[]>> getFiltersString() {
        return FILTERS_STRING_MAP;
    }

    Map<String, Function<OrderSearchCriteria, DateFilter>> getFiltersDateFilter() {
        return FILTERS_DATEFILTER_MAP;
    }

    Map<String, Function<OrderSearchCriteria, Long[]>> getFiltersLong() {
        return FILTERS_LONG_MAP;
    }
}
