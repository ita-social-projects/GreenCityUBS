package greencity.repository;

import greencity.IntegrationTestBase;
import greencity.UbsApplication;
import greencity.entity.order.BigOrderTableViews;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.OrderStatusSortingTranslation;
import greencity.enums.PaymentStatus;
import greencity.filters.DateFilter;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Sql(scripts = "/sqlFiles/bigOrderTableRepository/insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sqlFiles/bigOrderTableRepository/delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UbsApplication.class)
class BigOrderTableRepositoryTest extends IntegrationTestBase {
    @Autowired
    private BigOrderTableRepository bigOrderTableRepository;
    private static final List<Long> TARIFFS_ID_LIST = Collections.singletonList(1L);
    private static final OrderSearchCriteria DEFAULT_ORDER_SEARCH_CRITERIA = new OrderSearchCriteria();
    private static final LocalDateTime ORDER_DATE_START = LocalDateTime.of(2022, 1, 31, 23, 59, 59);
    private static final LocalDateTime ORDER_DATE_END = LocalDateTime.of(2022, 2, 3, 0, 0);
    private static final LocalDateTime ORDER_EXPORT_DATE_START = LocalDateTime.of(2022, 2, 2, 23, 59, 59);
    private static final LocalDateTime ORDER_EXPORT_DATE_END = LocalDateTime.of(2022, 2, 5, 0, 0, 1);
    private static final OrderPage DEFAULT_ORDER_PAGE_DESC = new OrderPage();
    private static final OrderPage DEFAULT_ORDER_PAGE_ASC = new OrderPage().setSortDirection(Sort.Direction.ASC);
    private static final OrderPage ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC =
        new OrderPage().setPageNumber(0).setPageSize(12).setSortBy("id").setSortDirection(Sort.Direction.ASC);
    private static final OrderPage ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC =
        new OrderPage().setPageNumber(0).setPageSize(12).setSortBy("id").setSortDirection(Sort.Direction.DESC);
    private static final OrderPage ORDER_PAGE_PAGE_NUMBER_1_PAGE_SIZE_2_DESC =
        new OrderPage().setPageNumber(1).setPageSize(2).setSortBy("id").setSortDirection(Sort.Direction.DESC);
    private static final OrderPage ORDER_PAGE_PAGE_NUMBER_3_PAGE_SIZE_2_ASC =
        new OrderPage().setPageNumber(3).setPageSize(2).setSortBy("id").setSortDirection(Sort.Direction.ASC);

    @Test
    void is_ModelUtil_Data_Equal_SQL() {
        List<BigOrderTableViews> expectedOrderList = ModelUtils.getAllBOTViewsASC();
        var actualOrderList = bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, "ua").getContent();
        for (int i = 0; i < expectedOrderList.size(); i++) {
            Assertions.assertEquals(expectedOrderList.get(i), actualOrderList.get(i));
        }
    }

    @Test
    void get_All_Orders_Default_Page_ASC() {
        Assertions.assertEquals(ModelUtils.getListBOTViewsStandardPageASC(),
            bigOrderTableRepository.findAll(DEFAULT_ORDER_PAGE_ASC,
                DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, "eng").getContent());
    }

    @Test
    void get_All_Orders_Default_Page_DESC() {
        Assertions.assertEquals(ModelUtils.getListBOTViewsStandardPageDESC(),
            bigOrderTableRepository.findAll(DEFAULT_ORDER_PAGE_DESC,
                DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, "eng").getContent());
    }

    @Test
    void get_All_Orders_Page_Sorting_By_Order_Payment_Status_DESC() {
        var page = new OrderPage().setPageNumber(0).setPageSize(12).setSortBy("orderPaymentStatus")
            .setSortDirection(Sort.Direction.DESC);
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .sorted(Comparator.comparing(BigOrderTableViews::getOrderPaymentStatus).reversed())
            .toList();
        Assertions.assertEquals(expectedOrdersList.stream()
            .map(BigOrderTableViews::getOrderPaymentStatus)
            .collect(Collectors.toList()),
            bigOrderTableRepository.findAll(page, DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, "eng").getContent()
                .stream()
                .map(BigOrderTableViews::getOrderPaymentStatus)
                .collect(Collectors.toList()));
    }

    @Test
    void get_All_Orders_Filter_By_Order_Status_Is_Formed_DESC() {
        var filter = new OrderSearchCriteria().setOrderStatus(new OrderStatus[] {OrderStatus.FORMED});
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(a -> a.getOrderStatus().equals(OrderStatus.FORMED.name()))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng").getContent());
    }

    @Test
    void get_All_Orders_Size_Two_Page_One_DESC() {
        Assertions.assertEquals(ModelUtils.getListBOTViewsSizeTwoPageOneDESC(),
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_1_PAGE_SIZE_2_DESC,
                DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, "eng").getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Order_Status_Is_Formed_And_CONFIRMED_DESC() {
        var filter =
            new OrderSearchCriteria().setOrderStatus(new OrderStatus[] {OrderStatus.FORMED, OrderStatus.CONFIRMED});
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(a -> a.getOrderStatus().equals(OrderStatus.FORMED.name())
                || a.getOrderStatus().equals(OrderStatus.CONFIRMED.name()))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(DEFAULT_ORDER_PAGE_DESC, filter, TARIFFS_ID_LIST, "eng").getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Payment_Status_Is_PAID_DESC() {
        var filter =
            new OrderSearchCriteria().setOrderPaymentStatus(new OrderPaymentStatus[] {OrderPaymentStatus.PAID});
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getOrderPaymentStatus().equals(OrderPaymentStatus.PAID.name()))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_City_DESC() {
        var filter = new OrderSearchCriteria().setCities(new String[] {"Київ"});
        var expectedOrderList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(a -> a.getCity().equals("Київ"))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrderList,
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Region_DESC() {
        var filter = new OrderSearchCriteria().setRegion(new String[] {"Київська область"});
        var expectedOrderList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(a -> a.getRegion().equals("Київська область"))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrderList,
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng").getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Districts_DESC() {
        var filter = new OrderSearchCriteria().setDistricts(new String[] {"Подільський"});
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(a -> a.getDistrict().equals("Подільський"))
            .toList();
        Assertions.assertEquals(
            expectedOrdersList.stream().map(BigOrderTableViews::getDistrict).collect(Collectors.toList()),
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent().stream().map(BigOrderTableViews::getDistrict).collect(Collectors.toList()));
    }

    @Test
    void get_All_Orders_Filter_By_Order_Date_Between_DESC() {
        var filter = new OrderSearchCriteria().setOrderDate(new DateFilter().setFrom("2022-02-01").setTo("2022-02-02"));
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getOrderDate().isBefore(ChronoLocalDate.from(ORDER_DATE_END))
                && order.getOrderDate().isAfter(ChronoLocalDate.from(ORDER_DATE_START)))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Order_Date_Less_Then_Or_Equal_DESC() {
        var filter = new OrderSearchCriteria().setOrderDate(new DateFilter().setTo("2022-02-01"));
        LocalDateTime endDate = LocalDateTime.of(2022, 2, 2, 0, 0, 1);
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getOrderDate().isBefore(ChronoLocalDate.from(endDate)))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Order_Date_Greater_Then_Or_Equal_DESC() {
        var filter = new OrderSearchCriteria().setOrderDate(new DateFilter().setFrom("2022-02-01"));
        var orders = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getOrderDate().isAfter(ChronoLocalDate.from(ORDER_DATE_START)))
            .collect(Collectors.toList());
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Date_Of_Export_Between_DESC() {
        var filter =
            new OrderSearchCriteria().setDeliveryDate(new DateFilter().setFrom("2022-02-03").setTo("2022-02-04"));
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getDateOfExport() != null &&
                order.getDateOfExport().isAfter(ChronoLocalDate.from(ORDER_EXPORT_DATE_START)) &&
                order.getDateOfExport().isBefore(ChronoLocalDate.from(ORDER_EXPORT_DATE_END)))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Payment_Date_Between() {
        var filter =
            new OrderSearchCriteria().setPaymentDate(new DateFilter().setFrom("2022-02-02").setTo("2022-02-02"));
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getPaymentDate() != null &&
                order.getPaymentDate().isAfter(ChronoLocalDate.from(ORDER_DATE_START))
                && order.getPaymentDate().isBefore(ChronoLocalDate.from(ORDER_DATE_END)))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Receiving_Station_DESC() {
        var filter = new OrderSearchCriteria().setReceivingStation(new Long[] {1L});
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getReceivingStationId() != null && order.getReceivingStationId().equals(1L))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Responsible_Logic_Man_ASC() {
        var filter = new OrderSearchCriteria().setResponsibleLogicManId(new Long[] {10L});
        var expectedOrdersList = ModelUtils.getAllBOTViewsASC().stream()
            .filter(order -> order.getResponsibleLogicManId() != null && order.getResponsibleLogicManId().equals(10L))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Responsible_Caller_ASC() {
        var filter = new OrderSearchCriteria().setResponsibleCallerId(new Long[] {15L});
        var expectedOrdersList = ModelUtils.getAllBOTViewsASC().stream()
            .filter(order -> order.getResponsibleCallerId() != null && order.getResponsibleCallerId().equals(15L))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Responsible_Driver_ASC() {
        var filter = new OrderSearchCriteria().setResponsibleDriverId(new Long[] {10L});
        var expectedOrdersList = ModelUtils.getAllBOTViewsASC().stream()
            .filter(order -> order.getResponsibleDriverId() != null && order.getResponsibleDriverId().equals(10L))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Filter_By_Responsible_Navigator_DESC() {
        var filter = new OrderSearchCriteria().setResponsibleNavigatorId(new Long[] {10L});
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getResponsibleNavigatorId() != null && order.getResponsibleNavigatorId().equals(10L))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Search_by_phone_DESC() {
        var filter = new OrderSearchCriteria().setSearch(new String[] {"+380631144678"});
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getClientPhoneNumber().equals("+380631144678"))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Sort_By_OrderStatus_UA_Localization_ASC() {
        OrderPage orderPage = new OrderPage().setPageNumber(0).setPageSize(15).setSortBy("orderStatus")
            .setSortDirection(Sort.Direction.ASC);
        List<BigOrderTableViews> bigOrderTableViewsList = bigOrderTableRepository.findAll(orderPage,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, "ua").getContent();
        Assertions.assertTrue(isListSortedCorrectlyByOrderStatusWithUALocalizationASC(bigOrderTableViewsList));
    }

    @Test
    void get_All_Orders_Sort_By_OrderStatus_UA_Localization_DESC() {
        OrderPage orderPage = new OrderPage().setPageNumber(0).setPageSize(15).setSortBy("orderStatus")
            .setSortDirection(Sort.Direction.DESC);
        var result = bigOrderTableRepository.findAll(orderPage,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, "ua").getContent();
        Assertions.assertTrue(isListSortedCorrectlyByOrderStatusWithUALocalizationDesc(result));
    }

    @Test
    void get_All_Orders_Search_by_Client_Name_DESC() {
        var filter = new OrderSearchCriteria().setSearch(new String[] {"Anna Maria"});
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getClientName() != null && order.getClientName().equals("Anna Maria"))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Combination_Filter_DESC() {
        var filter = new OrderSearchCriteria()
            .setOrderPaymentStatus(new OrderPaymentStatus[] {OrderPaymentStatus.PAID})
            .setOrderStatus(new OrderStatus[] {OrderStatus.FORMED});
        var expectedOrdersList = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getOrderPaymentStatus().equals(PaymentStatus.PAID.name()) &&
                order.getOrderStatus().equals(OrderStatus.FORMED.name()))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_Combination_Filter_ASC() {
        var filter = new OrderSearchCriteria()
            .setOrderPaymentStatus(new OrderPaymentStatus[] {OrderPaymentStatus.PAID})
            .setOrderStatus(new OrderStatus[] {OrderStatus.FORMED});
        var expectedOrdersList = ModelUtils.getAllBOTViewsASC().stream()
            .filter(order -> order.getOrderPaymentStatus().equals(PaymentStatus.PAID.name()) &&
                order.getOrderStatus().equals(OrderStatus.FORMED.name()))
            .collect(Collectors.toList());
        Assertions.assertEquals(expectedOrdersList,
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC, filter, TARIFFS_ID_LIST, "eng")
                .getContent());
    }

    @Test
    void get_All_Orders_PageImpl_ASC() {
        var orders = ModelUtils.getPageableAllBOTViews_ASC();
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(DEFAULT_ORDER_PAGE_ASC, DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST,
                "eng"));
    }

    @Test
    void get_All_Orders_PageImpl_Total_Elements_ASC() {
        var orders = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC();
        Assertions.assertEquals(orders.getTotalElements(),
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_3_PAGE_SIZE_2_ASC, DEFAULT_ORDER_SEARCH_CRITERIA,
                TARIFFS_ID_LIST, "eng").getTotalElements());
    }

    @Test
    void get_All_Orders_PageImpl_Size_ASC() {
        var orders = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders.getSize(),
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_3_PAGE_SIZE_2_ASC, DEFAULT_ORDER_SEARCH_CRITERIA, tariffsInfoIds, "eng")
                .getSize());
    }

    @Test
    void get_All_Orders_PageImpl_Number_ASC() {
        var orders = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC();
        Assertions.assertEquals(orders.getNumber(),
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_3_PAGE_SIZE_2_ASC, DEFAULT_ORDER_SEARCH_CRITERIA,
                TARIFFS_ID_LIST, "eng").getNumber());
    }

    @Test
    void get_All_Orders_PageImpl_Number_Of_Elements_ASC() {
        var orders = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC();
        Assertions.assertEquals(orders.getNumberOfElements(),
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_3_PAGE_SIZE_2_ASC, DEFAULT_ORDER_SEARCH_CRITERIA,
                TARIFFS_ID_LIST, "eng").getNumberOfElements());
    }

    private boolean isListSortedCorrectlyByOrderStatusWithUALocalizationASC(
        List<BigOrderTableViews> bigOrderTableViewsList) {
        for (int i = 0; i < bigOrderTableViewsList.size() - 1; i++) {
            OrderStatusSortingTranslation currentStatus =
                OrderStatusSortingTranslation.valueOf(bigOrderTableViewsList.get(i).getOrderStatus());
            OrderStatusSortingTranslation nextStatus =
                OrderStatusSortingTranslation.valueOf(bigOrderTableViewsList.get(i + 1).getOrderStatus());
            if (currentStatus.getSortOrder() > nextStatus.getSortOrder()) {
                return false;
            }
        }
        return true;
    }

    private boolean isListSortedCorrectlyByOrderStatusWithUALocalizationDesc(
        List<BigOrderTableViews> bigOrderTableViewsList) {
        for (int i = 0; i < bigOrderTableViewsList.size() - 1; i++) {
            OrderStatusSortingTranslation currentStatus =
                OrderStatusSortingTranslation.valueOf(bigOrderTableViewsList.get(i).getOrderStatus());
            OrderStatusSortingTranslation nextStatus =
                OrderStatusSortingTranslation.valueOf(bigOrderTableViewsList.get(i + 1).getOrderStatus());
            if (currentStatus.getSortOrder() < nextStatus.getSortOrder()) {
                return false;
            }
        }
        return true;
    }
}
