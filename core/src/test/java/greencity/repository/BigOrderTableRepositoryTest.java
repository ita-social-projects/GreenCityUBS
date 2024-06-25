package greencity.repository;

import com.google.common.collect.Comparators;
import greencity.IntegrationTestBase;
import greencity.UbsApplication;
import greencity.entity.order.BigOrderTableViews;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderPaymentStatusSortingTranslation;
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
    private static final String USER_LANGUAGE_ENG = "eng";
    private static final String USER_LANGUAGE_UA = "ua";
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
        List<BigOrderTableViews> expectedValue = ModelUtils.getAllBOTViewsASC();
        var actualValue = bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, USER_LANGUAGE_UA).getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Default_Page_ASC() {
        var expectedValue = ModelUtils.getListBOTViewsStandardPageASC();
        var actualValue = bigOrderTableRepository.findAll(DEFAULT_ORDER_PAGE_ASC,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, USER_LANGUAGE_ENG).getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Default_Page_DESC() {
        var expectedValue = ModelUtils.getListBOTViewsStandardPageDESC();
        var actualValue = bigOrderTableRepository.findAll(DEFAULT_ORDER_PAGE_DESC,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, USER_LANGUAGE_ENG).getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Page_Sorting_By_Order_Payment_Status_DESC() {
        var page = new OrderPage().setPageNumber(0).setPageSize(12).setSortBy("orderPaymentStatus")
            .setSortDirection(Sort.Direction.DESC);
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .sorted(Comparator.comparing(BigOrderTableViews::getOrderPaymentStatus).reversed())
            .map(BigOrderTableViews::getOrderPaymentStatus)
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository.findAll(page, DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent()
                .stream()
                .map(BigOrderTableViews::getOrderPaymentStatus)
                .collect(Collectors.toList());
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Order_Status_Is_Formed_DESC() {
        var filter = new OrderSearchCriteria().setOrderStatus(new OrderStatus[] {OrderStatus.FORMED});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(a -> a.getOrderStatus().equals(OrderStatus.FORMED.name()))
            .collect(Collectors.toList());
        var actualValue = bigOrderTableRepository
            .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
            .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Size_Two_Page_One_DESC() {
        var expectedValue = ModelUtils.getListBOTViewsSizeTwoPageOneDESC();
        var actualValue = bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_1_PAGE_SIZE_2_DESC,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, USER_LANGUAGE_ENG).getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Order_Status_Is_Formed_And_CONFIRMED_DESC() {
        var filter =
            new OrderSearchCriteria().setOrderStatus(new OrderStatus[] {OrderStatus.FORMED, OrderStatus.CONFIRMED});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(a -> a.getOrderStatus().equals(OrderStatus.FORMED.name())
                || a.getOrderStatus().equals(OrderStatus.CONFIRMED.name()))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository.findAll(DEFAULT_ORDER_PAGE_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Payment_Status_Is_PAID_DESC() {
        var filter =
            new OrderSearchCriteria().setOrderPaymentStatus(new OrderPaymentStatus[] {OrderPaymentStatus.PAID});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getOrderPaymentStatus().equals(OrderPaymentStatus.PAID.name()))
            .collect(Collectors.toList());
        var actualValue = bigOrderTableRepository
            .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
            .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_City_DESC() {
        var filter = new OrderSearchCriteria().setCities(new String[] {"Київ"});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(a -> a.getCity().equals("Київ"))
            .collect(Collectors.toList());
        var actualValue = bigOrderTableRepository
            .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
            .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Region_DESC() {
        var filter = new OrderSearchCriteria().setRegion(new String[] {"Київська область"});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(a -> a.getRegion().equals("Київська область"))
            .collect(Collectors.toList());
        var actualValue = bigOrderTableRepository
            .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
            .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Districts_DESC() {
        var filter = new OrderSearchCriteria().setDistricts(new String[] {"Подільський"});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .map(BigOrderTableViews::getDistrict)
            .filter(district -> district.equals("Подільський")).collect(Collectors.toList());
        var actualValue = bigOrderTableRepository
            .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
            .getContent().stream().map(BigOrderTableViews::getDistrict).collect(Collectors.toList());
        Assertions.assertEquals(
            expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Order_Date_Between_DESC() {
        var filter = new OrderSearchCriteria().setOrderDate(new DateFilter().setFrom("2022-02-01").setTo("2022-02-02"));
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getOrderDate().isBefore(ChronoLocalDate.from(ORDER_DATE_END))
                && order.getOrderDate().isAfter(ChronoLocalDate.from(ORDER_DATE_START)))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Order_Date_Less_Then_Or_Equal_DESC() {
        var filter = new OrderSearchCriteria().setOrderDate(new DateFilter().setTo("2022-02-01"));
        LocalDateTime endDate = LocalDateTime.of(2022, 2, 2, 0, 0, 1);
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getOrderDate().isBefore(ChronoLocalDate.from(endDate)))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Order_Date_Greater_Then_Or_Equal_DESC() {
        var filter = new OrderSearchCriteria().setOrderDate(new DateFilter().setFrom("2022-02-01"));
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getOrderDate().isAfter(ChronoLocalDate.from(ORDER_DATE_START)))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Date_Of_Export_Between_DESC() {
        var filter =
            new OrderSearchCriteria().setDeliveryDate(new DateFilter().setFrom("2022-02-03").setTo("2022-02-04"));
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getDateOfExport() != null &&
                order.getDateOfExport().isAfter(ChronoLocalDate.from(ORDER_EXPORT_DATE_START)) &&
                order.getDateOfExport().isBefore(ChronoLocalDate.from(ORDER_EXPORT_DATE_END)))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Payment_Date_Between() {
        var filter =
            new OrderSearchCriteria().setPaymentDate(new DateFilter().setFrom("2022-02-02").setTo("2022-02-02"));
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getPaymentDate() != null &&
                order.getPaymentDate().isAfter(ChronoLocalDate.from(ORDER_DATE_START))
                && order.getPaymentDate().isBefore(ChronoLocalDate.from(ORDER_DATE_END)))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Receiving_Station_DESC() {
        var filter = new OrderSearchCriteria().setReceivingStation(new Long[] {1L});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getReceivingStationId() != null && order.getReceivingStationId().equals(1L))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Responsible_Logic_Man_ASC() {
        var filter = new OrderSearchCriteria().setResponsibleLogicManId(new Long[] {10L});
        var expectedValue = ModelUtils.getAllBOTViewsASC().stream()
            .filter(order -> order.getResponsibleLogicManId() != null && order.getResponsibleLogicManId().equals(10L))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Responsible_Caller_ASC() {
        var filter = new OrderSearchCriteria().setResponsibleCallerId(new Long[] {15L});
        var expectedValue = ModelUtils.getAllBOTViewsASC().stream()
            .filter(order -> order.getResponsibleCallerId() != null && order.getResponsibleCallerId().equals(15L))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Responsible_Driver_ASC() {
        var filter = new OrderSearchCriteria().setResponsibleDriverId(new Long[] {10L});
        var expectedValue = ModelUtils.getAllBOTViewsASC().stream()
            .filter(order -> order.getResponsibleDriverId() != null && order.getResponsibleDriverId().equals(10L))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Filter_By_Responsible_Navigator_DESC() {
        var filter = new OrderSearchCriteria().setResponsibleNavigatorId(new Long[] {10L});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getResponsibleNavigatorId() != null && order.getResponsibleNavigatorId().equals(10L))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Search_by_phone_DESC() {
        var filter = new OrderSearchCriteria().setSearch(new String[] {"+380631144678"});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getClientPhoneNumber().equals("+380631144678"))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Sort_By_OrderStatus_UA_Localization_ASC() {
        OrderPage orderPage = new OrderPage().setPageNumber(0).setPageSize(15).setSortBy("orderStatus")
            .setSortDirection(Sort.Direction.ASC);
        List<BigOrderTableViews> bigOrderTableViewsList = bigOrderTableRepository.findAll(orderPage,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, USER_LANGUAGE_UA).getContent();
        boolean isListCorrectlySorted =
            Comparators.isInOrder(bigOrderTableViewsList, orderStatusTranslationComparator(true));
        Assertions.assertTrue(isListCorrectlySorted);
    }

    @Test
    void get_All_Orders_Sort_By_OrderStatus_UA_Localization_DESC() {
        OrderPage orderPage = new OrderPage().setPageNumber(0).setPageSize(15).setSortBy("orderStatus")
            .setSortDirection(Sort.Direction.DESC);
        var bigOrderTableViewsList = bigOrderTableRepository.findAll(orderPage,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, USER_LANGUAGE_UA).getContent();
        boolean isListCorrectlySorted =
            Comparators.isInOrder(bigOrderTableViewsList, orderStatusTranslationComparator(false));
        Assertions.assertTrue(isListCorrectlySorted);
    }

    @Test
    void get_All_Orders_Search_by_Client_Name_DESC() {
        var filter = new OrderSearchCriteria().setSearch(new String[] {"Anna Maria"});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getClientName() != null && order.getClientName().equals("Anna Maria"))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Combination_Filter_DESC() {
        var filter = new OrderSearchCriteria()
            .setOrderPaymentStatus(new OrderPaymentStatus[] {OrderPaymentStatus.PAID})
            .setOrderStatus(new OrderStatus[] {OrderStatus.FORMED});
        var expectedValue = ModelUtils.getAllBOTViewsDESC().stream()
            .filter(order -> order.getOrderPaymentStatus().equals(PaymentStatus.PAID.name()) &&
                order.getOrderStatus().equals(OrderStatus.FORMED.name()))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_DESC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Combination_Filter_ASC() {
        var filter = new OrderSearchCriteria()
            .setOrderPaymentStatus(new OrderPaymentStatus[] {OrderPaymentStatus.PAID})
            .setOrderStatus(new OrderStatus[] {OrderStatus.FORMED});
        var expectedValue = ModelUtils.getAllBOTViewsASC().stream()
            .filter(order -> order.getOrderPaymentStatus().equals(PaymentStatus.PAID.name()) &&
                order.getOrderStatus().equals(OrderStatus.FORMED.name()))
            .collect(Collectors.toList());
        var actualValue =
            bigOrderTableRepository
                .findAll(ORDER_PAGE_PAGE_NUMBER_0_PAGE_SIZE_12_ASC, filter, TARIFFS_ID_LIST, USER_LANGUAGE_ENG)
                .getContent();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_PageImpl_ASC() {
        var expectedValue = ModelUtils.getPageableAllBOTViews_ASC();
        var actualValue =
            bigOrderTableRepository.findAll(DEFAULT_ORDER_PAGE_ASC, DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST,
                USER_LANGUAGE_ENG);
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_PageImpl_Total_Elements_ASC() {
        var expectedValue = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC().getTotalElements();
        var actualValue =
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_3_PAGE_SIZE_2_ASC, DEFAULT_ORDER_SEARCH_CRITERIA,
                TARIFFS_ID_LIST, USER_LANGUAGE_ENG).getTotalElements();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_PageImpl_Size_ASC() {
        var expectedValue = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC().getSize();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        var actualValue = bigOrderTableRepository
            .findAll(ORDER_PAGE_PAGE_NUMBER_3_PAGE_SIZE_2_ASC, DEFAULT_ORDER_SEARCH_CRITERIA, tariffsInfoIds,
                USER_LANGUAGE_ENG)
            .getSize();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_PageImpl_Number_ASC() {
        var expectedValue = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC().getNumber();
        var actualValue =
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_3_PAGE_SIZE_2_ASC, DEFAULT_ORDER_SEARCH_CRITERIA,
                TARIFFS_ID_LIST, USER_LANGUAGE_ENG).getNumber();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    @Test
    void get_All_Orders_Sort_By_OrderPaymentStatus_UA_Localization_ASC() {
        OrderPage orderPage = new OrderPage().setPageNumber(0).setPageSize(15).setSortBy("orderPaymentStatus")
            .setSortDirection(Sort.Direction.ASC);
        List<BigOrderTableViews> bigOrderTableViewsList = bigOrderTableRepository.findAll(orderPage,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, USER_LANGUAGE_UA).getContent();
        boolean isListCorrectlySorted =
            Comparators.isInOrder(bigOrderTableViewsList, orderPaymentStatusTranslationComparator(false));
        Assertions.assertTrue(isListCorrectlySorted);
    }

    @Test
    void get_All_Orders_Sort_By_OrderPaymentStatus_UA_Localization_DESC() {
        OrderPage orderPage = new OrderPage().setPageNumber(0).setPageSize(15).setSortBy("orderPaymentStatus")
            .setSortDirection(Sort.Direction.DESC);
        var bigOrderTableViewsList = bigOrderTableRepository.findAll(orderPage,
            DEFAULT_ORDER_SEARCH_CRITERIA, TARIFFS_ID_LIST, USER_LANGUAGE_UA).getContent();
        boolean isListCorrectlySorted =
            Comparators.isInOrder(bigOrderTableViewsList, orderPaymentStatusTranslationComparator(true));
        Assertions.assertTrue(isListCorrectlySorted);
    }

    @Test
    void get_All_Orders_PageImpl_Number_Of_Elements_ASC() {
        var expectedValue = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC().getNumberOfElements();
        var actualValue =
            bigOrderTableRepository.findAll(ORDER_PAGE_PAGE_NUMBER_3_PAGE_SIZE_2_ASC, DEFAULT_ORDER_SEARCH_CRITERIA,
                TARIFFS_ID_LIST, USER_LANGUAGE_ENG).getNumberOfElements();
        Assertions.assertEquals(expectedValue, actualValue);
    }

    private Comparator<BigOrderTableViews> orderStatusTranslationComparator(boolean ascending) {
        Comparator<BigOrderTableViews> comparator = Comparator.comparingInt(
            view -> OrderStatusSortingTranslation.valueOf(view.getOrderStatus()).getSortOrder());
        return ascending ? comparator.reversed() : comparator;
    }

    private Comparator<BigOrderTableViews> orderPaymentStatusTranslationComparator(boolean descending) {
        Comparator<BigOrderTableViews> comparator = Comparator.comparingInt(
            view -> OrderPaymentStatusSortingTranslation.valueOf(view.getOrderPaymentStatus()).getSortOrder());
        return descending ? comparator.reversed() : comparator;
    }
}
