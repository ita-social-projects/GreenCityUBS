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
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(82L);
        botView.setOrderStatus("CONFIRMED");
        botView.setOrderPaymentStatus("PAID");
        botView.setOrderDate(LocalDate.parse("2022-02-02"));
        botView.setPaymentDate(LocalDate.parse("2022-02-02"));
        botView.setClientName("Abu Dabi");
        botView.setClientPhoneNumber("+380380634654");
        botView.setClientEmail("lvnmwyvsrvcruhfwbn@sdvgeft.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Одеська область");
        botView.setCity("Одеса");
        botView.setDistrict("Приморський");
        botView.setAddress("Дерибасівська 33, корп.- , п.4");
        botView.setRegionEn("Odessa Oblast");
        botView.setCityEn("Odessa");
        botView.setDistrictEn("Primorskyi");
        botView.setAddressEn("Deribasivska 33, b.- , e.4");
        botView.setCommentToAddressForClient("Коментар до адреси 5");
        botView.setBagAmount("120л - 1шт; 60л - 1шт; 20л - 1шт");
        botView.setTotalOrderSum(600L);
        botView.setOrderCertificateCode(null);
        botView.setGeneralDiscount(0L);
        botView.setAmountDue(-59400L);
        botView.setCommentForOrderByClient("hey");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(60000L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("21:22:04-23:22:22");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation("Саперно-Слобідська");
        botView.setReceivingStationId(1L);
        botView.setResponsibleLogicMan("Tapsoy, Ipsi");
        botView.setResponsibleLogicManId(2L);
        botView.setResponsibleDriver("Abu, Dabi");
        botView.setResponsibleDriverId(10L);
        botView.setResponsibleCaller("Test, User");
        botView.setResponsibleCallerId(15L);
        botView.setResponsibleNavigator("Migno, Tekku");
        botView.setResponsibleNavigatorId(3L);
        botView.setIsBlocked(false);
        botView.setBlockedBy("");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_83() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(83L);
        botView.setOrderStatus("DONE");
        botView.setOrderPaymentStatus("PAID");
        botView.setOrderDate(LocalDate.parse("2022-02-02"));
        botView.setPaymentDate(LocalDate.parse("2022-02-02"));
        botView.setClientName("Anna Maria");
        botView.setClientPhoneNumber("+380631144678");
        botView.setClientEmail("testgreencity323@gmail.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Київська область");
        botView.setCity("Київ");
        botView.setDistrict("Шевченківський");
        botView.setAddress("Хрещатик 27, корп.- , п.1");
        botView.setRegionEn("Kyiv Oblast");
        botView.setCityEn("Kyiv");
        botView.setDistrictEn("Shevchenkivskyi");
        botView.setAddressEn("Khreshchatyk 27, b.- , e.1");
        botView.setCommentToAddressForClient("Коментар до адреси 2");
        botView.setBagAmount("120л - 2шт; 60л - 5шт; 20л - 1шт");
        botView.setTotalOrderSum(1050L);
        botView.setOrderCertificateCode(null);
        botView.setGeneralDiscount(0L);
        botView.setAmountDue(-43950L);
        botView.setCommentForOrderByClient("hi");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(45000L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("07:00:00-18:00:10");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation("Грибовицька");
        botView.setReceivingStationId(2L);
        botView.setResponsibleLogicMan("Migno, Tekku");
        botView.setResponsibleLogicManId(3L);
        botView.setResponsibleDriver("Test, User");
        botView.setResponsibleDriverId(15L);
        botView.setResponsibleCaller("Tapsoy, Ipsi");
        botView.setResponsibleCallerId(2L);
        botView.setResponsibleNavigator("Abu, Dabi");
        botView.setResponsibleNavigatorId(10L);
        botView.setIsBlocked(false);
        botView.setBlockedBy("");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_84() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(84L);
        botView.setOrderStatus("FORMED");
        botView.setOrderPaymentStatus("UNPAID");
        botView.setOrderDate(LocalDate.parse("2022-02-01"));
        botView.setPaymentDate(null);
        botView.setClientName("Abu Dabi");
        botView.setClientPhoneNumber("+380380634654");
        botView.setClientEmail("lvnmwyvsrvcruhfwbn@sdvgeft.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Львівська область");
        botView.setCity("Львів");
        botView.setDistrict("Галицький");
        botView.setAddress("Вулиця Крушельницької 10, корп.- , п.2");
        botView.setRegionEn("Lviv Oblast");
        botView.setCityEn("Lviv");
        botView.setDistrictEn("Halychskyi");
        botView.setAddressEn("Krushelnytska Street 10, b.- , e.2");
        botView.setCommentToAddressForClient("Коментар до адреси 4");
        botView.setBagAmount("20л - 4шт");
        botView.setTotalOrderSum(1200L);
        botView.setOrderCertificateCode(null);
        botView.setGeneralDiscount(0L);
        botView.setAmountDue(1200L);
        botView.setCommentForOrderByClient("by");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(0L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("10:00:00-18:00:10");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation(null);
        botView.setReceivingStationId(null);
        botView.setResponsibleLogicMan(null);
        botView.setResponsibleLogicManId(null);
        botView.setResponsibleDriver(null);
        botView.setResponsibleDriverId(null);
        botView.setResponsibleCaller(null);
        botView.setResponsibleCallerId(null);
        botView.setResponsibleNavigator(null);
        botView.setResponsibleNavigatorId(null);
        botView.setIsBlocked(true);
        botView.setBlockedBy("Abu, Dabi");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_85() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(85L);
        botView.setOrderStatus("FORMED");
        botView.setOrderPaymentStatus("UNPAID");
        botView.setOrderDate(LocalDate.parse("2022-02-04"));
        botView.setPaymentDate(null);
        botView.setClientName("Anna Maria");
        botView.setClientPhoneNumber("+380631144678");
        botView.setClientEmail("testgreencity323@gmail.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(0);
        botView.setRegion("Київська область");
        botView.setCity("Київ");
        botView.setDistrict("Подільський");
        botView.setAddress("Велика Васильківська 14, корп.Б, п.2");
        botView.setRegionEn("Kyiv Oblast");
        botView.setCityEn("Kyiv");
        botView.setDistrictEn("Podilskyi");
        botView.setAddressEn("Velyka Vasylkivska 14, b.Б, e.2");
        botView.setCommentToAddressForClient("Коментар до адреси 1");
        botView.setBagAmount("20л - 2шт");
        botView.setTotalOrderSum(600L);
        botView.setOrderCertificateCode(null);
        botView.setGeneralDiscount(0L);
        botView.setAmountDue(600L);
        botView.setCommentForOrderByClient("hey");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(0L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("10:00:00-18:00:10");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation(null);
        botView.setReceivingStationId(null);
        botView.setResponsibleLogicMan(null);
        botView.setResponsibleLogicManId(null);
        botView.setResponsibleDriver(null);
        botView.setResponsibleDriverId(null);
        botView.setResponsibleCaller(null);
        botView.setResponsibleCallerId(null);
        botView.setResponsibleNavigator(null);
        botView.setResponsibleNavigatorId(null);
        botView.setIsBlocked(false);
        botView.setBlockedBy("");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_86() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(86L);
        botView.setOrderStatus("CONFIRMED");
        botView.setOrderPaymentStatus("PAID");
        botView.setOrderDate(LocalDate.parse("2022-02-05"));
        botView.setPaymentDate(LocalDate.parse("2022-02-05"));
        botView.setClientName("Anna Maria");
        botView.setClientPhoneNumber("+380631144678");
        botView.setClientEmail("testgreencity323@gmail.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Київська область");
        botView.setCity("Київ");
        botView.setDistrict("Шевченківський");
        botView.setAddress("Хрещатик 27, корп.- , п.1");
        botView.setRegionEn("Kyiv Oblast");
        botView.setCityEn("Kyiv");
        botView.setDistrictEn("Shevchenkivskyi");
        botView.setAddressEn("Khreshchatyk 27, b.- , e.1");
        botView.setCommentToAddressForClient("Коментар до адреси 2");
        botView.setBagAmount("20л - 2шт");
        botView.setTotalOrderSum(600L);
        botView.setOrderCertificateCode("3003-1992");
        botView.setGeneralDiscount(500L);
        botView.setAmountDue(-49400L);
        botView.setCommentForOrderByClient("hey");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(50000L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("22:27:00-22:27:03");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation("Грибовицька");
        botView.setReceivingStationId(2L);
        botView.setResponsibleLogicMan("Migno, Tekku");
        botView.setResponsibleLogicManId(3L);
        botView.setResponsibleDriver("Abu, Dabi");
        botView.setResponsibleDriverId(10L);
        botView.setResponsibleCaller("Tapsoy, Ipsi");
        botView.setResponsibleCallerId(2L);
        botView.setResponsibleNavigator("Test, User");
        botView.setResponsibleNavigatorId(15L);
        botView.setIsBlocked(false);
        botView.setBlockedBy("");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_87() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(87L);
        botView.setOrderStatus("CONFIRMED");
        botView.setOrderPaymentStatus("PAID");
        botView.setOrderDate(LocalDate.parse("2022-02-07"));
        botView.setPaymentDate(LocalDate.parse("2022-03-07"));
        botView.setClientName("Migno Tekku");
        botView.setClientPhoneNumber("+380508003301");
        botView.setClientEmail("mignotekku@vusra.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Львівська область");
        botView.setCity("Львів");
        botView.setDistrict("Шевченківський");
        botView.setAddress("Площа Ринок 5, корп.А, п.3");
        botView.setRegionEn("Lviv Oblast");
        botView.setCityEn("Lviv");
        botView.setDistrictEn("Shevchenkivskyi");
        botView.setAddressEn("Rynok Square 5, b.А, e.3");
        botView.setCommentToAddressForClient("Коментар до адреси 3");
        botView.setBagAmount("120л - 5шт; 60л - 5шт; 20л - 5шт");
        botView.setTotalOrderSum(3000L);
        botView.setOrderCertificateCode("3113-3113, 3113-3114");
        botView.setGeneralDiscount(2000L);
        botView.setAmountDue(-207000L);
        botView.setCommentForOrderByClient("by");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(210000L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("22:27:07-22:27:12");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation("Грибовицька");
        botView.setReceivingStationId(2L);
        botView.setResponsibleLogicMan("Migno, Tekku");
        botView.setResponsibleLogicManId(3L);
        botView.setResponsibleDriver("Abu, Dabi");
        botView.setResponsibleDriverId(10L);
        botView.setResponsibleCaller("Tapsoy, Ipsi");
        botView.setResponsibleCallerId(2L);
        botView.setResponsibleNavigator("Test, User");
        botView.setResponsibleNavigatorId(15L);
        botView.setIsBlocked(false);
        botView.setBlockedBy("");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_88() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(88L);
        botView.setOrderStatus("DONE");
        botView.setOrderPaymentStatus("PAID");
        botView.setOrderDate(LocalDate.parse("2022-01-30"));
        botView.setPaymentDate(LocalDate.parse("2022-01-30"));
        botView.setClientName("Anna Maria");
        botView.setClientPhoneNumber("+380631144678");
        botView.setClientEmail("testgreencity323@gmail.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Київська область");
        botView.setCity("Київ");
        botView.setDistrict("Подільський");
        botView.setAddress("Велика Васильківська 14, корп.Б, п.2");
        botView.setRegionEn("Kyiv Oblast");
        botView.setCityEn("Kyiv");
        botView.setDistrictEn("Podilskyi");
        botView.setAddressEn("Velyka Vasylkivska 14, b.Б, e.2");
        botView.setCommentToAddressForClient("Коментар до адреси 1");
        botView.setBagAmount("120л - 1шт; 60л - 1шт; 20л - 1шт");
        botView.setTotalOrderSum(600L);
        botView.setOrderCertificateCode(null);
        botView.setGeneralDiscount(0L);
        botView.setAmountDue(-59400L);
        botView.setCommentForOrderByClient("by");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(60000L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("22:27:16-22:27:21");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation("Саперно-Слобідська");
        botView.setReceivingStationId(1L);
        botView.setResponsibleLogicMan("Abu, Dabi");
        botView.setResponsibleLogicManId(10L);
        botView.setResponsibleDriver("Test, User");
        botView.setResponsibleDriverId(15L);
        botView.setResponsibleCaller("Tapsoy, Ipsi");
        botView.setResponsibleCallerId(2L);
        botView.setResponsibleNavigator("Migno, Tekku");
        botView.setResponsibleNavigatorId(3L);
        botView.setIsBlocked(false);
        botView.setBlockedBy("");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_89() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(89L);
        botView.setOrderStatus("FORMED");
        botView.setOrderPaymentStatus("UNPAID");
        botView.setOrderDate(LocalDate.parse("2022-02-08"));
        botView.setPaymentDate(null);
        botView.setClientName("Migno Tekku");
        botView.setClientPhoneNumber("+380508003301");
        botView.setClientEmail("mignotekku@vusra.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Львівська область");
        botView.setCity("Львів");
        botView.setDistrict("Шевченківський");
        botView.setAddress("Площа Ринок 5, корп.А, п.3");
        botView.setRegionEn("Lviv Oblast");
        botView.setCityEn("Lviv");
        botView.setDistrictEn("Shevchenkivskyi");
        botView.setAddressEn("Rynok Square 5, b.А, e.3");
        botView.setCommentToAddressForClient("Коментар до адреси 3");
        botView.setBagAmount("120л - 2шт; 60л - 1шт; 20л - 1шт");
        botView.setTotalOrderSum(850L);
        botView.setOrderCertificateCode(null);
        botView.setGeneralDiscount(0L);
        botView.setAmountDue(850L);
        botView.setCommentForOrderByClient("by");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(0L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("22:27:25-22:27:29");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation(null);
        botView.setReceivingStationId(null);
        botView.setResponsibleLogicMan(null);
        botView.setResponsibleLogicManId(null);
        botView.setResponsibleDriver(null);
        botView.setResponsibleDriverId(null);
        botView.setResponsibleCaller(null);
        botView.setResponsibleCallerId(null);
        botView.setResponsibleNavigator(null);
        botView.setResponsibleNavigatorId(null);
        botView.setIsBlocked(false);
        botView.setBlockedBy("");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_90() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(90L);
        botView.setOrderStatus("ON_THE_ROUTE");
        botView.setOrderPaymentStatus("PAID");
        botView.setOrderDate(LocalDate.parse("2022-02-08"));
        botView.setPaymentDate(LocalDate.parse("2022-02-08"));
        botView.setClientName("Anna Maria");
        botView.setClientPhoneNumber("+380631144678");
        botView.setClientEmail("testgreencity323@gmail.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Київська область");
        botView.setCity("Київ");
        botView.setDistrict("Подільський");
        botView.setAddress("Велика Васильківська 14, корп.Б, п.2");
        botView.setRegionEn("Kyiv Oblast");
        botView.setCityEn("Kyiv");
        botView.setDistrictEn("Podilskyi");
        botView.setAddressEn("Velyka Vasylkivska 14, b.Б, e.2");
        botView.setCommentToAddressForClient("Коментар до адреси 1");
        botView.setBagAmount("120л - 6шт; 60л - 6шт; 20л - 8шт");
        botView.setTotalOrderSum(4200L);
        botView.setOrderCertificateCode("7777-7777, 1212-1212, 1111-2222");
        botView.setGeneralDiscount(2050L);
        botView.setAmountDue(-200800L);
        botView.setCommentForOrderByClient("234");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(205000L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("07:00:00-15:00:00");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation("Грибовицька");
        botView.setReceivingStationId(2L);
        botView.setResponsibleLogicMan("Migno, Tekku");
        botView.setResponsibleLogicManId(3L);
        botView.setResponsibleDriver("Test, User");
        botView.setResponsibleDriverId(15L);
        botView.setResponsibleCaller("Tapsoy, Ipsi");
        botView.setResponsibleCallerId(2L);
        botView.setResponsibleNavigator("Abu, Dabi");
        botView.setResponsibleNavigatorId(10L);
        botView.setIsBlocked(false);
        botView.setBlockedBy("");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_91() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(91L);
        botView.setOrderStatus("FORMED");
        botView.setOrderPaymentStatus("PAID");
        botView.setOrderDate(LocalDate.parse("2022-01-30"));
        botView.setPaymentDate(LocalDate.parse("2022-01-30"));
        botView.setClientName("Anna Maria");
        botView.setClientPhoneNumber("+380631144678");
        botView.setClientEmail("testgreencity323@gmail.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Київська область");
        botView.setCity("Київ");
        botView.setDistrict("Подільський");
        botView.setAddress("Велика Васильківська 14, корп.Б, п.2");
        botView.setRegionEn("Kyiv Oblast");
        botView.setCityEn("Kyiv");
        botView.setDistrictEn("Podilskyi");
        botView.setAddressEn("Velyka Vasylkivska 14, b.Б, e.2");
        botView.setCommentToAddressForClient("Коментар до адреси 1");
        botView.setBagAmount("120л - 1шт; 60л - 2шт; 20л - 1шт");
        botView.setTotalOrderSum(650L);
        botView.setOrderCertificateCode(null);
        botView.setGeneralDiscount(0L);
        botView.setAmountDue(-64350L);
        botView.setCommentForOrderByClient("");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(65000L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("07:00:00-15:00:00");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation(null);
        botView.setReceivingStationId(null);
        botView.setResponsibleLogicMan(null);
        botView.setResponsibleLogicManId(null);
        botView.setResponsibleDriver(null);
        botView.setResponsibleDriverId(null);
        botView.setResponsibleCaller(null);
        botView.setResponsibleCallerId(null);
        botView.setResponsibleNavigator(null);
        botView.setResponsibleNavigatorId(null);
        botView.setIsBlocked(true);
        botView.setBlockedBy("Abu, Dabi");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_92() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(92L);
        botView.setOrderStatus("FORMED");
        botView.setOrderPaymentStatus("PAID");
        botView.setOrderDate(LocalDate.parse("2022-01-30"));
        botView.setPaymentDate(LocalDate.parse("2022-01-30"));
        botView.setClientName("Anna Maria");
        botView.setClientPhoneNumber("+380631144678");
        botView.setClientEmail("testgreencity323@gmail.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Київська область");
        botView.setCity("Київ");
        botView.setDistrict("Подільський");
        botView.setAddress("Велика Васильківська 14, корп.Б, п.2");
        botView.setRegionEn("Kyiv Oblast");
        botView.setCityEn("Kyiv");
        botView.setDistrictEn("Podilskyi");
        botView.setAddressEn("Velyka Vasylkivska 14, b.Б, e.2");
        botView.setCommentToAddressForClient("Коментар до адреси 1");
        botView.setBagAmount("120л - 1шт; 60л - 2шт; 20л - 1шт");
        botView.setTotalOrderSum(650L);
        botView.setOrderCertificateCode(null);
        botView.setGeneralDiscount(0L);
        botView.setAmountDue(-64350L);
        botView.setCommentForOrderByClient("");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(65000L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("22:27:59-22:28:11");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation(null);
        botView.setReceivingStationId(null);
        botView.setResponsibleLogicMan(null);
        botView.setResponsibleLogicManId(null);
        botView.setResponsibleDriver(null);
        botView.setResponsibleDriverId(null);
        botView.setResponsibleCaller(null);
        botView.setResponsibleCallerId(null);
        botView.setResponsibleNavigator(null);
        botView.setResponsibleNavigatorId(null);
        botView.setIsBlocked(false);
        botView.setBlockedBy("");
        botView.setTariffsInfoId(1L);

        return botView;
    }

    public static BigOrderTableViews getBOTViews_93() {
        BigOrderTableViews botView = new BigOrderTableViews();
        botView.setId(93L);
        botView.setOrderStatus("CONFIRMED");
        botView.setOrderPaymentStatus("PAID");
        botView.setOrderDate(LocalDate.parse("2022-01-27"));
        botView.setPaymentDate(LocalDate.parse("2022-01-27"));
        botView.setClientName("Anna Maria");
        botView.setClientPhoneNumber("+380631144678");
        botView.setClientEmail("testgreencity323@gmail.com");
        botView.setSenderName("");
        botView.setSenderPhone(null);
        botView.setSenderEmail(null);
        botView.setViolationsAmount(345);
        botView.setRegion("Київська область");
        botView.setCity("Київ");
        botView.setDistrict("Подільський");
        botView.setAddress("Велика Васильківська 14, корп.Б, п.2");
        botView.setRegionEn("Kyiv Oblast");
        botView.setCityEn("Kyiv");
        botView.setDistrictEn("Podilskyi");
        botView.setAddressEn("Velyka Vasylkivska 14, b.Б, e.2");
        botView.setCommentToAddressForClient("Коментар до адреси 1");
        botView.setBagAmount("120л - 20шт; 60л - 30шт; 20л - 50шт");
        botView.setTotalOrderSum(5000L);
        botView.setOrderCertificateCode(null);
        botView.setGeneralDiscount(0L);
        botView.setAmountDue(-1260000L);
        botView.setCommentForOrderByClient("");
        botView.setCommentForOrderByAdmin(null);
        botView.setTotalPayment(1265000L);
        botView.setDateOfExport(null);
        botView.setTimeOfExport("22:28:18-22:28:24");
        botView.setIdOrderFromShop(null);
        botView.setReceivingStation("Грибовицька");
        botView.setReceivingStationId(2L);
        botView.setResponsibleLogicMan("Migno, Tekku");
        botView.setResponsibleLogicManId(3L);
        botView.setResponsibleDriver("Tapsoy, Ipsi");
        botView.setResponsibleDriverId(2L);
        botView.setResponsibleCaller("Test, User");
        botView.setResponsibleCallerId(15L);
        botView.setResponsibleNavigator("Abu, Dabi");
        botView.setResponsibleNavigatorId(10L);
        botView.setIsBlocked(false);
        botView.setBlockedBy("");
        botView.setTariffsInfoId(1L);

        return botView;
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

    public static List<BigOrderTableViews> getAllBOTViewsDESC() {
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
            getBOTViews_84(),
            getBOTViews_83(),
            getBOTViews_82());
    }

    public static List<BigOrderTableViews> getAllBOTViewsASC() {
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
            getBOTViews_91(),
            getBOTViews_92(),
            getBOTViews_93());
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

    public static Page<BigOrderTableViews> getPageableAllBOTViews_ASC() {
        var sort = Sort.by(Sort.Direction.ASC, "id");
        return new PageImpl<>(getListBOTViewsStandardPageASC(), PageRequest.of(0, 10, sort), 12L);
    }

    public static Page<BigOrderTableViews> getPageableAllBOTViews_Two_Element_On_Page_ASC() {
        var sort = Sort.by(Sort.Direction.ASC, "id");
        return new PageImpl<>(getListBOTViewsStandardPageASC_by_pageSize_Two(), PageRequest.of(3, 2, sort), 12L);
    }
}
