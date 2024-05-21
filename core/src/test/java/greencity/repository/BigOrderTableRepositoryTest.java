package greencity.repository;

import greencity.IntegrationTestBase;
import greencity.UbsApplication;
import greencity.entity.order.BigOrderTableViews;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.OrderStatusSortingTranslation;
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
import java.util.ArrayList;
import java.util.List;

@Sql(scripts = "/sqlFiles/bigOrderTableRepository/insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sqlFiles/bigOrderTableRepository/delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UbsApplication.class)
class BigOrderTableRepositoryTest extends IntegrationTestBase {
    @Autowired
    private BigOrderTableRepository bigOrderTableRepository;

    @Test
    void get_All_Orders_Sort_By_OrderStatus_UA_Localization_ASC() {
        OrderPage orderPage = new OrderPage().setPageNumber(0).setPageSize(15).setSortBy("orderStatus")
            .setSortDirection(Sort.Direction.ASC);
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        List<BigOrderTableViews> bigOrderTableViewsList = bigOrderTableRepository.findAll(orderPage,
            getOrderSearchCriteria(), tariffsInfoIds, "ua").getContent();
        Assertions.assertTrue(isListSortedCorrectlyByOrderStatusWithUALocalizationASC(bigOrderTableViewsList));
    }

    @Test
    void get_All_Orders_Sort_By_OrderStatus_UA_Localization_DESC() {
        OrderPage orderPage = new OrderPage().setPageNumber(0).setPageSize(15).setSortBy("orderStatus")
            .setSortDirection(Sort.Direction.DESC);
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        var result = bigOrderTableRepository.findAll(orderPage,
            getOrderSearchCriteria(), tariffsInfoIds, "ua").getContent();
        Assertions.assertTrue(isListSortedCorrectlyByOrderStatusWithUALocalizationDesc(result));
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
            boolean isSortedCorrectly = currentStatus.getSortOrder() >= nextStatus.getSortOrder();
            if (currentStatus.getSortOrder() < nextStatus.getSortOrder()) {
                return false;
            }
        }
        return true;
    }

    // @Test
    void get_All_Orders_Default_Page_ASC() {
        var orders = ModelUtils.getListBOTViewsStandardPageASC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders, bigOrderTableRepository.findAll(getOrderPageStandardASC(),
            getOrderSearchCriteria(), tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Default_Page_DESC() {
        var orders = ModelUtils.getListBOTViewsStandardPageDESC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders, bigOrderTableRepository.findAll(getOrderPageStandard(),
            getOrderSearchCriteria(), tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Page_Sorting_By_Order_Payment_Status_DESC() {
        var orders = ModelUtils.getListBOTViewsOSC_Sorting_By_Order_Payment_Status_DESC();
        var page = new OrderPage().setPageNumber(0).setPageSize(10).setSortBy("orderPaymentStatus")
            .setSortDirection(Sort.Direction.DESC);
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(page, getOrderSearchCriteria(), tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Size_Two_Page_One_DESC() {
        var orders = ModelUtils.getListBOTViewsSizeTwoPageOneDESC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders, bigOrderTableRepository.findAll(getOrderPageSizeTwoPageOneDESC(),
            getOrderSearchCriteria(), tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Order_Status_Is_Formed_DESC() {
        var filter = new OrderSearchCriteria().setOrderStatus(new OrderStatus[] {OrderStatus.FORMED});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Order_Status_Is_Formed();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Order_Status_Is_Formed_And_CONFIRMED_DESC() {
        var filter =
            new OrderSearchCriteria().setOrderStatus(new OrderStatus[] {OrderStatus.FORMED, OrderStatus.CONFIRMED});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Order_Status_Is_Formed_And_CONFIRMED();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Payment_Status_Is_PAID_DESC() {
        var filter =
            new OrderSearchCriteria().setOrderPaymentStatus(new OrderPaymentStatus[] {OrderPaymentStatus.PAID});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Payment_Status_Is_PAID();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Region_DESC() {
        var filter = new OrderSearchCriteria().setRegion(new String[] {"Київська область"});
        var orders = ModelUtils.getListBOTViewsStandardPageDESC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_City_DESC() {
        var filter = new OrderSearchCriteria().setCities(new String[] {"Київ"});
        var orders = ModelUtils.getListBOTViewsStandardPageDESC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Districts_DESC() {
        var filter = new OrderSearchCriteria().setDistricts(new String[] {"Печерський"});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_District();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Order_Date_Between_DESC() {
        var filter = new OrderSearchCriteria().setOrderDate(new DateFilter().setFrom("2022-02-01").setTo("2022-02-02"));
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Order_Date_Between();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Order_Date_Less_Then_Or_Equal() {
        var filter = new OrderSearchCriteria().setOrderDate(new DateFilter().setTo("2022-02-01"));
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Order_Date_Less_Then_Or_Equal();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Order_Date_Greater_Then_Or_Equal() {
        var filter = new OrderSearchCriteria().setOrderDate(new DateFilter().setFrom("2022-02-01"));
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Order_Date_Greater_Then_Or_Equal();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Date_Of_Export_Between() {
        var filter =
            new OrderSearchCriteria().setDeliveryDate(new DateFilter().setFrom("2022-02-03").setTo("2022-02-04"));
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Date_Of_Export_Between();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Payment_Date_Between() {
        var filter =
            new OrderSearchCriteria().setPaymentDate(new DateFilter().setFrom("2022-02-02").setTo("2022-02-02"));
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Payment_Date_Between();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Receiving_Station() {
        var filter = new OrderSearchCriteria().setReceivingStation(new Long[] {1L});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Receving_station();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Responsible_Caller() {
        var filter = new OrderSearchCriteria().setResponsibleCallerId(new Long[] {15L});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Responsible_Caller();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Responsible_Logic_Man() {
        var filter = new OrderSearchCriteria().setResponsibleLogicManId(new Long[] {10L});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Logic_Man();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Responsible_Driver() {
        var filter = new OrderSearchCriteria().setResponsibleDriverId(new Long[] {10L});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Responsible_Driver();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Filter_By_Responsible_Navigator() {
        var filter = new OrderSearchCriteria().setResponsibleNavigatorId(new Long[] {10L});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_By_Responsible_Navigator();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Search_by_phone() {
        var filter = new OrderSearchCriteria().setSearch(new String[] {"+380676666666"});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_Search();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Search_by_Client_Name() {
        var filter = new OrderSearchCriteria().setSearch(new String[] {"Myroslav", "Vir"});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_Search_by_Client_name();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Combination_Filter_DESC() {
        var filter = new OrderSearchCriteria()
            .setOrderPaymentStatus(new OrderPaymentStatus[] {OrderPaymentStatus.PAID})
            .setOrderStatus(new OrderStatus[] {OrderStatus.FORMED});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_Combination_DESC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandard(), filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_Combination_Filter_ASC() {
        var orderPageASC = new OrderPage().setSortDirection(Sort.Direction.ASC);
        var filter = new OrderSearchCriteria()
            .setOrderPaymentStatus(new OrderPaymentStatus[] {OrderPaymentStatus.PAID})
            .setOrderStatus(new OrderStatus[] {OrderStatus.FORMED});
        var orders = ModelUtils.getListBOTViewsOSC_Filter_Combination_ASC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(orderPageASC, filter, tariffsInfoIds, "eng").getContent());
    }

    // @Test
    void get_All_Orders_PageImpl_ASC() {
        var filter = new OrderSearchCriteria();
        var orders = ModelUtils.getPageableAllBOTViews_ASC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders,
            bigOrderTableRepository.findAll(getOrderPageStandardASC(), filter, tariffsInfoIds, "eng"));
    }

    // @Test
    void get_All_Orders_PageImpl_Total_Elements_ASC() {
        var filter = new OrderSearchCriteria();
        var page = getOrderPage_Two_Element_On_Page_ASC();
        var orders = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders.getTotalElements(),
            bigOrderTableRepository.findAll(page, filter, tariffsInfoIds, "eng").getTotalElements());
    }

    // @Test
    void get_All_Orders_PageImpl_Size_ASC() {
        var filter = new OrderSearchCriteria();
        var page = getOrderPage_Two_Element_On_Page_ASC();
        var orders = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders.getSize(),
            bigOrderTableRepository.findAll(page, filter, tariffsInfoIds, "eng").getSize());
    }

    // @Test
    void get_All_Orders_PageImpl_Number_ASC() {
        var filter = new OrderSearchCriteria();
        var page = getOrderPage_Two_Element_On_Page_ASC();
        var orders = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders.getNumber(),
            bigOrderTableRepository.findAll(page, filter, tariffsInfoIds, "eng").getNumber());
    }

    // @Test
    void get_All_Orders_PageImpl_Number_Of_Elements_ASC() {
        var filter = new OrderSearchCriteria();
        var page = getOrderPage_Two_Element_On_Page_ASC();
        var orders = ModelUtils.getPageableAllBOTViews_Two_Element_On_Page_ASC();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        Assertions.assertEquals(orders.getNumberOfElements(),
            bigOrderTableRepository.findAll(page, filter, tariffsInfoIds, "eng").getNumberOfElements());
    }

    private OrderPage getOrderPageSizeTwoPageOneDESC() {
        return new OrderPage().setPageNumber(1).setPageSize(2).setSortBy("id").setSortDirection(Sort.Direction.DESC);
    }

    private OrderPage getOrderPageStandardASC() {
        return new OrderPage().setPageNumber(0).setPageSize(15).setSortBy("id").setSortDirection(Sort.Direction.ASC);
    }

    private OrderPage getOrderPage_Two_Element_On_Page_ASC() {
        return new OrderPage().setPageNumber(3).setPageSize(2).setSortBy("id").setSortDirection(Sort.Direction.ASC);
    }

    private OrderPage getOrderPageStandard() {
        return new OrderPage();
    }

    private OrderSearchCriteria getOrderSearchCriteria() {
        return new OrderSearchCriteria();
    }
}
