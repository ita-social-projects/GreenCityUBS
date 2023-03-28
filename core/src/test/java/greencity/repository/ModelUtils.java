package greencity.repository;

import greencity.entity.coords.Coordinates;
import greencity.enums.AddressStatus;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.entity.order.BigOrderTableViews;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ModelUtils {
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyy-MM-d");

    public static User getUser() {
        return User.builder()
            .uuid(UUID.randomUUID().toString())
            .orders(getOrderList())
            .currentPoints(0)
            .recipientName("Ivan")
            .recipientSurname("Ivanov")
            .recipientEmail("ivan@gmail.com")
            .recipientPhone("+380981099667")
            .violations(0)
            .build();
    }

    public static List<Order> getOrderList() {
        List<Order> orderList = new ArrayList<>();
        orderList.add(Order.builder()
            .orderStatus(OrderStatus.FORMED)
            .orderPaymentStatus(OrderPaymentStatus.PAID)
            .user(User.builder().id(1L).build())
            .ubsUser(UBSuser.builder().id(1L).build())
            .orderDate(LocalDateTime.now())
            .build());
        return orderList;
    }

    public static List<User> getUsers() {
        List<User> users = new ArrayList<>();
        users.add(User.builder()
            .uuid(UUID.randomUUID().toString())
            .orders(getOrderList())
            .currentPoints(0)
            .recipientName("Ivan")
            .recipientSurname("Ivanov")
            .recipientEmail("ivan@gmail.com")
            .recipientPhone("+380981099667")
            .violations(0)
            .build());
        return users;
    }

    public static Address getAddress() {
        return Address.builder()
            .id(1L)
            .user(User.builder().id(1L).build())
            .city("Київ")
            .addressComment("").coordinates(Coordinates.builder()
                .latitude(50.446509500000005)
                .longitude(30.510173).build())
            .district("Шевченківський")
            .entranceNumber("2")
            .houseCorpus("44")
            .houseNumber("3")
            .street("Богдана Хмельницького вулиця")
            .actual(true)
            .addressStatus(AddressStatus.IN_ORDER)
            .region("Київська область")
            .cityEn("Kyiv")
            .regionEn("Kyiv region")
            .streetEn("Bohdana Khmelnytskoho Street")
            .districtEn("Shevchenkivskyi")
            .build();
    }

    public static BigOrderTableViews getBOTViews_82() {
        return new BigOrderTableViews()
            .setId(82L)
            .setOrderStatus("CONFIRMED")
            .setOrderPaymentStatus("PAID")
            .setOrderDate(LocalDate.parse("2022-02-02", DATE_TIME_FORMATTER))
            .setPaymentDate(LocalDate.parse("2022-02-02", DATE_TIME_FORMATTER))
            .setClientName("Anna Maria")
            .setClientPhoneNumber("+380631144678")
            .setClientEmail("testgreencity323@gmail.com")
            .setSenderName("")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn("Kyiv region")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Дарницький")
            .setDistrictEn("Darnyts'kyi")
            .setAddress("проспект Петра Григоренка 3, корп.- , п.- ")
            .setAddressEn("Petra Hryhorenka Avenue 3, b.- , e.- ")
            .setCommentToAddressForClient("")
            .setBagAmount("20л - 1шт; 120л - 2шт")
            .setTotalOrderSum(600L)
            .setAmountDue(0L)
            .setGeneralDiscount(0L)
            .setCommentForOrderByClient("hey")
            .setTotalPayment(600L)
            .setDateOfExport(LocalDate.parse("2022-02-04", DATE_TIME_FORMATTER))
            .setTimeOfExport("21:22:04-23:22:22")
            .setReceivingStationId(1L)
            .setReceivingStation("Саперно-Слобідська")
            .setResponsibleLogicManId(2L)
            .setResponsibleLogicMan("Tapsoy, Ipsi")
            .setResponsibleDriverId(10L)
            .setResponsibleDriver("Abu, Dabi")
            .setResponsibleCallerId(15L)
            .setResponsibleCaller("Test, User")
            .setResponsibleNavigatorId(3L)
            .setResponsibleNavigator("Migno, Tekku")
            .setCommentsForOrder("criteria")
            .setIsBlocked(false)
            .setBlockedBy("")
            .setTariffsInfoId(1L);

    }

    public static BigOrderTableViews getBOTViews_83() {
        return new BigOrderTableViews()
            .setId(83L)
            .setOrderStatus("DONE")
            .setOrderPaymentStatus("PAID")
            .setOrderDate(LocalDate.parse("2022-02-02", DATE_TIME_FORMATTER))
            .setPaymentDate(LocalDate.parse("2022-02-02", DATE_TIME_FORMATTER))
            .setClientName("Anna Maria")
            .setClientPhoneNumber("+380631144678")
            .setClientEmail("testgreencity323@gmail.com")
            .setSenderName("")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn("Kyiv region")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Дарницький")
            .setDistrictEn("Darnyts'kyi")
            .setAddress("проспект Петра Григоренка 3, корп.- , п.- ")
            .setAddressEn("Petra Hryhorenka Avenue 3, b.- , e.- ")
            .setCommentToAddressForClient("")
            .setBagAmount("20л - 5шт; 120л - 3шт")
            .setTotalOrderSum(1050L)
            .setGeneralDiscount(0L)
            .setAmountDue(0L)
            .setCommentForOrderByClient("hi")
            .setTotalPayment(1050L)
            .setDateOfExport(LocalDate.parse("2022-02-03", DATE_TIME_FORMATTER))
            .setTimeOfExport("07:00:00-18:00:10")
            .setReceivingStationId(2L)
            .setReceivingStation("Грибовицька")
            .setResponsibleLogicManId(3L)
            .setResponsibleLogicMan("Migno, Tekku")
            .setResponsibleDriverId(15L)
            .setResponsibleDriver("Test, User")
            .setResponsibleCallerId(2L)
            .setResponsibleCaller("Tapsoy, Ipsi")
            .setResponsibleNavigatorId(10L)
            .setResponsibleNavigator("Abu, Dabi")
            .setCommentsForOrder("criteria")
            .setIsBlocked(false)
            .setBlockedBy("")
            .setTariffsInfoId(1L);
    }

    public static BigOrderTableViews getBOTViews_84() {
        return new BigOrderTableViews()
            .setId(84L)
            .setOrderStatus("FORMED")
            .setOrderPaymentStatus("UNPAID")
            .setOrderDate(LocalDate.parse("2022-02-01", DATE_TIME_FORMATTER))
            .setClientName("Migno Tekku")
            .setClientPhoneNumber("+380508003301")
            .setClientEmail("mignotekku@vusra.com")
            .setSenderName("")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn("Kyiv region")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Печерський")
            .setDistrictEn("Pechers'kyi")
            .setAddress("Khorolska Street 7, корп.8, п.- ")
            .setAddressEn("Khorolska Street 7, b.8, e.- ")
            .setCommentToAddressForClient("")
            .setBagAmount("120л - 4шт")
            .setTotalOrderSum(1200L)
            .setGeneralDiscount(0L)
            .setAmountDue(1200L)
            .setCommentForOrderByClient("by")
            .setTotalPayment(0L)
            .setDateOfExport(LocalDate.parse("2022-02-05", DATE_TIME_FORMATTER))
            .setTimeOfExport("10:00:00-18:00:10")
            .setCommentsForOrder("criteria")
            .setIsBlocked(true)
            .setBlockedBy("Abu, Dabi")
            .setTariffsInfoId(1L);
    }

    public static BigOrderTableViews getBOTViews_85() {
        return new BigOrderTableViews()
            .setId(85L)
            .setOrderStatus("FORMED")
            .setOrderPaymentStatus("UNPAID")
            .setOrderDate(LocalDate.parse("2022-02-04", DATE_TIME_FORMATTER))
            .setClientName("Abu Dabi")
            .setClientPhoneNumber("+380380634654")
            .setClientEmail("lvnmwyvsrvcruhfwbn@sdvgeft.com")
            .setSenderName("")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn(null)
            .setCity("Київ")
            .setCityEn(null)
            .setDistrict("Дніпровський")
            .setDistrictEn(null)
            .setAddress("Kharkivs'ke Highway 5, корп.4, п.- ")
            .setAddressEn(" 5, b.4, e.- ")
            .setBagAmount("120л - 2шт")
            .setTotalOrderSum(600L)
            .setGeneralDiscount(0L)
            .setAmountDue(600L)
            .setCommentForOrderByClient("hey")
            .setTotalPayment(0L)
            .setDateOfExport(LocalDate.parse("2022-02-05", DATE_TIME_FORMATTER))
            .setTimeOfExport("10:00:00-18:00:10")
            .setCommentsForOrder("sorting")
            .setIsBlocked(false)
            .setBlockedBy("")
            .setTariffsInfoId(1L);
    }

    public static BigOrderTableViews getBOTViews_86() {
        return new BigOrderTableViews()
            .setId(86L)
            .setOrderStatus("CONFIRMED")
            .setOrderPaymentStatus("PAID")
            .setOrderDate(LocalDate.parse("2022-02-05", DATE_TIME_FORMATTER))
            .setPaymentDate(LocalDate.parse("2022-02-05", DATE_TIME_FORMATTER))
            .setClientName("Abu Dabi")
            .setClientPhoneNumber("+380380634654")
            .setClientEmail("lvnmwyvsrvcruhfwbn@sdvgeft.com")
            .setSenderName("")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn(null)
            .setCity("Київ")
            .setCityEn(null)
            .setDistrict("Дніпровський")
            .setDistrictEn(null)
            .setAddress("Kharkivs'ke Highway 5, корп.4, п.- ")
            .setAddressEn(" 5, b.4, e.- ")
            .setBagAmount("120л - 2шт")
            .setTotalOrderSum(600L)
            .setOrderCertificateCode("3003-1992")
            .setGeneralDiscount(500L)
            .setAmountDue(-400L)
            .setCommentForOrderByClient("hey")
            .setTotalPayment(500L)
            .setDateOfExport(LocalDate.parse("2022-02-06", DATE_TIME_FORMATTER))
            .setTimeOfExport("22:27:00-22:27:03")
            .setReceivingStationId(2L)
            .setReceivingStation("Грибовицька")
            .setResponsibleLogicManId(3L)
            .setResponsibleLogicMan("Migno, Tekku")
            .setResponsibleDriverId(10L)
            .setResponsibleDriver("Abu, Dabi")
            .setResponsibleCallerId(2L)
            .setResponsibleCaller("Tapsoy, Ipsi")
            .setResponsibleNavigatorId(15L)
            .setResponsibleNavigator("Test, User")
            .setCommentsForOrder("filtering")
            .setIsBlocked(false)
            .setBlockedBy("")
            .setTariffsInfoId(1L);
    }

    public static BigOrderTableViews getBOTViews_87() {
        return new BigOrderTableViews()
            .setId(87L)
            .setOrderStatus("CONFIRMED")
            .setOrderPaymentStatus("PAID")
            .setOrderDate(LocalDate.parse("2022-02-07", DATE_TIME_FORMATTER))
            .setPaymentDate(LocalDate.parse("2022-03-07", DATE_TIME_FORMATTER))
            .setClientName("name surname")
            .setClientPhoneNumber("+380972222222")
            .setClientEmail("testgreencity323@gmail.com")
            .setSenderName("")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn("Kyiv region")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Дарницький")
            .setDistrictEn("Darnyts'kyi")
            .setAddress("проспект Петра Григоренка 3, корп.- , п.- ")
            .setAddressEn("Petra Hryhorenka Avenue 3, b.- , e.- ")
            .setCommentToAddressForClient("")
            .setBagAmount("20л - 5шт; 120л - 10шт")
            .setTotalOrderSum(3000L)
            .setOrderCertificateCode("3113-3113, 3113-3114")
            .setGeneralDiscount(2400L)
            .setAmountDue(0L)
            .setCommentForOrderByClient("by")
            .setTotalPayment(600L)
            .setDateOfExport(LocalDate.parse("2022-02-08", DATE_TIME_FORMATTER))
            .setTimeOfExport("22:27:07-22:27:12")
            .setReceivingStationId(2L)
            .setReceivingStation("Грибовицька")
            .setResponsibleLogicManId(3L)
            .setResponsibleLogicMan("Migno, Tekku")
            .setResponsibleDriverId(10L)
            .setResponsibleDriver("Abu, Dabi")
            .setResponsibleCallerId(2L)
            .setResponsibleCaller("Tapsoy, Ipsi")
            .setResponsibleNavigatorId(15L)
            .setResponsibleNavigator("Test, User")
            .setCommentsForOrder("searching")
            .setIsBlocked(false)
            .setBlockedBy("")
            .setTariffsInfoId(1L);
    }

    public static BigOrderTableViews getBOTViews_88() {
        return new BigOrderTableViews()
            .setId(88L)
            .setOrderStatus("DONE")
            .setOrderPaymentStatus("PAID")
            .setOrderDate(LocalDate.parse("2022-01-30", DATE_TIME_FORMATTER))
            .setPaymentDate(LocalDate.parse("2022-01-30", DATE_TIME_FORMATTER))
            .setClientName("Anna Maria")
            .setClientPhoneNumber("+380631144678")
            .setClientEmail("testgreencity323@gmail.com")
            .setSenderName("замовник інша")
            .setSenderPhone("+380979875456")
            .setSenderEmail("test@ukr.net")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn("Kyiv region")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Дарницький")
            .setDistrictEn("Darnyts'kyi")
            .setAddress("проспект Петра Григоренка 3, корп.- , п.- ")
            .setAddressEn("Petra Hryhorenka Avenue 3, b.- , e.- ")
            .setCommentToAddressForClient("")
            .setBagAmount("20л - 1шт; 120л - 2шт")
            .setTotalOrderSum(600L)
            .setGeneralDiscount(0L)
            .setAmountDue(0L)
            .setCommentForOrderByClient("by")
            .setTotalPayment(600L)
            .setDateOfExport(LocalDate.parse("2022-02-08", DATE_TIME_FORMATTER))
            .setTimeOfExport("22:27:16-22:27:21")
            .setReceivingStationId(1L)
            .setReceivingStation("Саперно-Слобідська")
            .setResponsibleLogicManId(10L)
            .setResponsibleLogicMan("Abu, Dabi")
            .setResponsibleDriverId(15L)
            .setResponsibleDriver("Test, User")
            .setResponsibleCallerId(2L)
            .setResponsibleCaller("Tapsoy, Ipsi")
            .setResponsibleNavigatorId(3L)
            .setResponsibleNavigator("Migno, Tekku")
            .setIsBlocked(false)
            .setBlockedBy("")
            .setTariffsInfoId(1L);
    }

    public static BigOrderTableViews getBOTViews_89() {
        return new BigOrderTableViews()
            .setId(89L)
            .setOrderStatus("FORMED")
            .setOrderPaymentStatus("UNPAID")
            .setOrderDate(LocalDate.parse("2022-02-08", DATE_TIME_FORMATTER))
            .setClientName("Anna Maria")
            .setClientPhoneNumber("+380631144678")
            .setClientEmail("testgreencity323@gmail.com")
            .setSenderName("")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn("Kyiv region")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Солом'янський")
            .setDistrictEn("Solom'yans'kyi")
            .setAddress("Севастопольська площа 19, корп.- , п.- ")
            .setAddressEn("Sevastopol's'ka Square 19, b.- , e.- ")
            .setCommentToAddressForClient("")
            .setBagAmount("20л - 1шт; 120л - 3шт")
            .setTotalOrderSum(850L)
            .setGeneralDiscount(0L)
            .setAmountDue(850L)
            .setCommentForOrderByClient("by")
            .setTotalPayment(0L)
            .setDateOfExport(LocalDate.parse("2022-02-09", DATE_TIME_FORMATTER))
            .setTimeOfExport("22:27:25-22:27:29")
            .setIsBlocked(false)
            .setBlockedBy("")
            .setTariffsInfoId(1L);
    }

    public static BigOrderTableViews getBOTViews_90() {
        return new BigOrderTableViews()
            .setId(90L)
            .setOrderStatus("ON_THE_ROUTE")
            .setOrderPaymentStatus("PAID")
            .setOrderDate(LocalDate.parse("2022-02-08", DATE_TIME_FORMATTER))
            .setPaymentDate(LocalDate.parse("2022-02-08", DATE_TIME_FORMATTER))
            .setClientName("Ross Sihovsk")
            .setClientPhoneNumber("+380676666666")
            .setClientEmail("rsihovskiy@gmail.com")
            .setSenderName("TestlastForSearch testLast")
            .setSenderPhone("+380974563223")
            .setSenderEmail("test@email.ua")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn("Kyiv region")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Солом'янський")
            .setDistrictEn("Solom'yans'kyi")
            .setAddress("Вулиця Федора Ернста 10, корп.16, п.- ")
            .setAddressEn("Fedora Ernsta Street 10, b.16, e.- ")
            .setCommentToAddressForClient("Ого, який коментар до адреси")
            .setBagAmount("20л - 6шт; 120л - 14шт")
            .setTotalOrderSum(4200L)
            .setOrderCertificateCode("7777-7777, 1212-1212, 1111-2222")
            .setGeneralDiscount(2050L)
            .setAmountDue(0L)
            .setCommentForOrderByClient("234")
            .setTotalPayment(2150L)
            .setDateOfExport(LocalDate.parse("2022-02-10", DATE_TIME_FORMATTER))
            .setTimeOfExport("07:00:00-15:00:00")
            .setReceivingStationId(2L)
            .setReceivingStation("Грибовицька")
            .setResponsibleLogicManId(3L)
            .setResponsibleLogicMan("Migno, Tekku")
            .setResponsibleDriverId(15L)
            .setResponsibleDriver("Test, User")
            .setResponsibleCallerId(2L)
            .setResponsibleCaller("Tapsoy, Ipsi")
            .setResponsibleNavigatorId(10L)
            .setResponsibleNavigator("Abu, Dabi")
            .setIsBlocked(false)
            .setBlockedBy("")
            .setTariffsInfoId(1L);
    }

    public static BigOrderTableViews getBOTViews_91() {
        return new BigOrderTableViews()
            .setId(91L)
            .setOrderStatus("FORMED")
            .setOrderPaymentStatus("PAID")
            .setOrderDate(LocalDate.parse("2022-01-30", DATE_TIME_FORMATTER))
            .setPaymentDate(LocalDate.parse("2022-01-30", DATE_TIME_FORMATTER))
            .setClientName("John Doe")
            .setClientPhoneNumber("+380631144678")
            .setClientEmail("testgreencity323@gmail.com")
            .setSenderName("")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn("Kyiv region")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Солом'янський")
            .setDistrictEn("Solom'yans'kyi")
            .setAddress("Севастопольська площа 19, корп.- , п.- ")
            .setAddressEn("Sevastopol's'ka Square 19, b.- , e.- ")
            .setCommentToAddressForClient("")
            .setBagAmount("20л - 2шт; 120л - 2шт")
            .setTotalOrderSum(650L)
            .setGeneralDiscount(0L)
            .setAmountDue(0L)
            .setCommentForOrderByClient("")
            .setTotalPayment(650L)
            .setDateOfExport(LocalDate.parse("2022-02-10", DATE_TIME_FORMATTER))
            .setTimeOfExport("07:00:00-15:00:00")
            .setIsBlocked(true)
            .setBlockedBy("Abu, Dabi")
            .setTariffsInfoId(1L);
    }

    public static BigOrderTableViews getBOTViews_92() {
        return new BigOrderTableViews()
            .setId(92L)
            .setOrderStatus("FORMED")
            .setOrderPaymentStatus("PAID")
            .setOrderDate(LocalDate.parse("2022-01-30", DATE_TIME_FORMATTER))
            .setPaymentDate(LocalDate.parse("2022-01-30", DATE_TIME_FORMATTER))
            .setClientName("John Doe")
            .setClientPhoneNumber("+380631144678")
            .setClientEmail("testgreencity323@gmail.com")
            .setSenderName("")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn("Kyiv region")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Дарницький")
            .setDistrictEn("Darnyts'kyi")
            .setAddress("проспект Петра Григоренка 3, корп.- , п.- ")
            .setAddressEn("Petra Hryhorenka Avenue 3, b.- , e.- ")
            .setCommentToAddressForClient("")
            .setBagAmount("20л - 2шт; 120л - 2шт")
            .setTotalOrderSum(650L)
            .setGeneralDiscount(0L)
            .setAmountDue(0L)
            .setCommentForOrderByClient("")
            .setTotalPayment(650L)
            .setDateOfExport(LocalDate.parse("2022-02-18", DATE_TIME_FORMATTER))
            .setTimeOfExport("22:27:59-22:28:11")
            .setIsBlocked(false)
            .setBlockedBy("")
            .setTariffsInfoId(1L);
    }

    public static BigOrderTableViews getBOTViews_93() {
        return new BigOrderTableViews()
            .setId(93L)
            .setOrderStatus("CONFIRMED")
            .setOrderPaymentStatus("PAID")
            .setOrderDate(LocalDate.parse("2022-01-27", DATE_TIME_FORMATTER))
            .setPaymentDate(LocalDate.parse("2022-01-27", DATE_TIME_FORMATTER))
            .setClientName("Myroslav Vir")
            .setClientPhoneNumber("+380676079847")
            .setClientEmail("xaratan242@nahetech.com")
            .setSenderName("")
            .setViolationsAmount(0)
            .setRegion("Київська область")
            .setRegionEn("Kyiv region")
            .setCity("Київ")
            .setCityEn("Kyiv")
            .setDistrict("Шевченківський")
            .setDistrictEn("Shevchenkivs'kyi")
            .setAddress("улица Владимира Винниченко 10, корп.- , п.1")
            .setAddressEn("Vladimir Vinnichenko Street 10, b.- , e.1")
            .setCommentToAddressForClient("")
            .setBagAmount("20л - 30шт; 120л - 70шт")
            .setTotalOrderSum(5000L)
            .setGeneralDiscount(0L)
            .setAmountDue(-7650L)
            .setCommentForOrderByClient("")
            .setTotalPayment(12650L)
            .setDateOfExport(LocalDate.parse("2022-02-03", DATE_TIME_FORMATTER))
            .setTimeOfExport("22:28:18-22:28:24")
            .setReceivingStationId(2L)
            .setReceivingStation("Грибовицька")
            .setResponsibleLogicManId(3L)
            .setResponsibleLogicMan("Migno, Tekku")
            .setResponsibleDriverId(2L)
            .setResponsibleDriver("Tapsoy, Ipsi")
            .setResponsibleCallerId(15L)
            .setResponsibleCaller("Test, User")
            .setResponsibleNavigatorId(10L)
            .setResponsibleNavigator("Abu, Dabi")
            .setIsBlocked(false)
            .setBlockedBy("")
            .setTariffsInfoId(1L);
    }

    public static List<BigOrderTableViews> getListBOTViewsStandardPageASC() {
        return Arrays.asList(
            getBOTViews_82(),
            getBOTViews_83(),
            getBOTViews_84(),
            getBOTViews_85(),
            getBOTViews_86(),
            getBOTViews_87(),
            getBOTViews_88(),
            getBOTViews_89(),
            getBOTViews_90(),
            getBOTViews_91());
    }

    public static List<BigOrderTableViews> getListBOTViewsStandardPageASC_by_pageSize_Two() {
        return Arrays.asList(
            getBOTViews_82(),
            getBOTViews_83());
    }

    public static List<BigOrderTableViews> getListBOTViewsStandardPageDESC() {
        return Arrays.asList(
            getBOTViews_93(),
            getBOTViews_92(),
            getBOTViews_91(),
            getBOTViews_90(),
            getBOTViews_89(),
            getBOTViews_88(),
            getBOTViews_87(),
            getBOTViews_86(),
            getBOTViews_85(),
            getBOTViews_84());
    }

    public static List<BigOrderTableViews> getListBOTViewsSizeTwoPageOneDESC() {
        return Arrays.asList(
            getBOTViews_91(),
            getBOTViews_90());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Order_Status_Is_Formed() {
        return Arrays.asList(
            getBOTViews_92(),
            getBOTViews_91(),
            getBOTViews_89(),
            getBOTViews_85(),
            getBOTViews_84());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Order_Status_Is_Formed_And_CONFIRMED() {
        return Arrays.asList(
            getBOTViews_93(),
            getBOTViews_92(),
            getBOTViews_91(),
            getBOTViews_89(),
            getBOTViews_87(),
            getBOTViews_86(),
            getBOTViews_85(),
            getBOTViews_84(),
            getBOTViews_82());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Payment_Status_Is_PAID() {
        return Arrays.asList(
            getBOTViews_93(),
            getBOTViews_92(),
            getBOTViews_91(),
            getBOTViews_90(),
            getBOTViews_88(),
            getBOTViews_87(),
            getBOTViews_86(),
            getBOTViews_83(),
            getBOTViews_82());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Order_Date_Between() {
        return Arrays.asList(
            getBOTViews_84(),
            getBOTViews_83(),
            getBOTViews_82());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Order_Date_Less_Then_Or_Equal() {
        return Arrays.asList(
            getBOTViews_93(),
            getBOTViews_92(),
            getBOTViews_91(),
            getBOTViews_88(),
            getBOTViews_84());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Order_Date_Greater_Then_Or_Equal() {
        return Arrays.asList(
            getBOTViews_90(),
            getBOTViews_89(),
            getBOTViews_87(),
            getBOTViews_86(),
            getBOTViews_85(),
            getBOTViews_84(),
            getBOTViews_83(),
            getBOTViews_82());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Date_Of_Export_Between() {
        return Arrays.asList(
            getBOTViews_93(),
            getBOTViews_83(),
            getBOTViews_82());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Payment_Date_Between() {
        return Arrays.asList(
            getBOTViews_83(),
            getBOTViews_82());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Receving_station() {
        return Arrays.asList(
            getBOTViews_88(),
            getBOTViews_82());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_District() {
        return List.of(getBOTViews_84());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Responsible_Caller() {
        return Arrays.asList(
            getBOTViews_93(),
            getBOTViews_82());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Logic_Man() {
        return List.of(
            getBOTViews_88());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Responsible_Driver() {
        return Arrays.asList(
            getBOTViews_87(),
            getBOTViews_86(),
            getBOTViews_82());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_By_Responsible_Navigator() {
        return Arrays.asList(
            getBOTViews_93(),
            getBOTViews_90(),
            getBOTViews_83());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_Search() {
        return List.of(
            getBOTViews_90());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_Search_by_Client_name() {
        return List.of(
            getBOTViews_93());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_Combination_DESC() {
        return Arrays.asList(
            getBOTViews_92(),
            getBOTViews_91());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Filter_Combination_ASC() {
        return Arrays.asList(
            getBOTViews_91(),
            getBOTViews_92());
    }

    public static List<BigOrderTableViews> getListBOTViewsOSC_Sorting_By_Order_Payment_Status_DESC() {
        return Arrays.asList(
            getBOTViews_89(),
            getBOTViews_85(),
            getBOTViews_84(),
            getBOTViews_91(),
            getBOTViews_86(),
            getBOTViews_93(),
            getBOTViews_87(),
            getBOTViews_88(),
            getBOTViews_83(),
            getBOTViews_92());
    }

    public static Page<BigOrderTableViews> getPageableAllBOTViews_ASC() {
        var sort = Sort.by(Sort.Direction.ASC, "id");
        return new PageImpl<>(getListBOTViewsStandardPageASC(), PageRequest.of(0, 10, sort), 12L);
    }

    public static Page<BigOrderTableViews> getPageableAllBOTViews_Two_Element_On_Page_ASC() {
        var sort = Sort.by(Sort.Direction.ASC, "id");
        return new PageImpl<>(getListBOTViewsStandardPageASC_by_pageSize_Two(), PageRequest.of(3, 2, sort), 12L);
    }
}
