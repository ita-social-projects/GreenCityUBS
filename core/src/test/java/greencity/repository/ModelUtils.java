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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelUtils {
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
        return BigOrderTableViews.builder()
            .id(82L)
            .orderStatus("CONFIRMED")
            .orderPaymentStatus("PAID")
            .orderDate(LocalDate.parse("2022-02-02"))
            .paymentDate(LocalDate.parse("2022-02-02"))
            .clientName("Abu Dabi")
            .clientPhoneNumber("+380380634654")
            .clientEmail("lvnmwyvsrvcruhfwbn@sdvgeft.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Одеська область")
            .city("Одеса")
            .district("Приморський")
            .address("Дерибасівська 33, корп.- , п.4")
            .regionEn("Odessa Oblast")
            .cityEn("Odessa")
            .districtEn("Primorskyi")
            .addressEn("Deribasivska 33, b.- , e.4")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 5")
            .bagAmount("120л - 1шт; 60л - 1шт; 20л - 1шт")
            .totalOrderSum(600L)
            .orderCertificateCode(null)
            .generalDiscount(0L)
            .amountDue(-59400L)
            .commentForOrderByClient("hey")
            .commentForOrderByAdmin(null)
            .totalPayment(60000L)
            .dateOfExport(null)
            .timeOfExport("21:22:04-23:22:22")
            .idOrderFromShop(null)
            .receivingStation("Саперно-Слобідська")
            .receivingStationId(1L)
            .responsibleLogicMan("Tapsoy, Ipsi")
            .responsibleLogicManId(2L)
            .responsibleDriver("Abu, Dabi")
            .responsibleDriverId(10L)
            .responsibleCaller("Test, User")
            .responsibleCallerId(15L)
            .responsibleNavigator("Migno, Tekku")
            .responsibleNavigatorId(3L)
            .isBlocked(false)
            .blockedBy("")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_83() {
        return BigOrderTableViews.builder()
            .id(83L)
            .orderStatus("DONE")
            .orderPaymentStatus("PAID")
            .orderDate(LocalDate.parse("2022-02-02"))
            .paymentDate(LocalDate.parse("2022-02-02"))
            .clientName("Anna Maria")
            .clientPhoneNumber("+380631144678")
            .clientEmail("testgreencity323@gmail.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Київська область")
            .city("Київ")
            .district("Шевченківський")
            .address("Хрещатик 27, корп.- , п.1")
            .regionEn("Kyiv Oblast")
            .cityEn("Kyiv")
            .districtEn("Shevchenkivskyi")
            .addressEn("Khreshchatyk 27, b.- , e.1")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 2")
            .bagAmount("120л - 2шт; 60л - 5шт; 20л - 1шт")
            .totalOrderSum(1050L)
            .orderCertificateCode(null)
            .generalDiscount(0L)
            .amountDue(-43950L)
            .commentForOrderByClient("hi")
            .commentForOrderByAdmin(null)
            .totalPayment(45000L)
            .dateOfExport(null)
            .timeOfExport("07:00:00-18:00:10")
            .idOrderFromShop(null)
            .receivingStation("Грибовицька")
            .receivingStationId(2L)
            .responsibleLogicMan("Migno, Tekku")
            .responsibleLogicManId(3L)
            .responsibleDriver("Test, User")
            .responsibleDriverId(15L)
            .responsibleCaller("Tapsoy, Ipsi")
            .responsibleCallerId(2L)
            .responsibleNavigator("Abu, Dabi")
            .responsibleNavigatorId(10L)
            .isBlocked(false)
            .blockedBy("")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_84() {
        return BigOrderTableViews.builder()
            .id(84L)
            .orderStatus("FORMED")
            .orderPaymentStatus("UNPAID")
            .orderDate(LocalDate.parse("2022-02-01"))
            .paymentDate(null)
            .clientName("Abu Dabi")
            .clientPhoneNumber("+380380634654")
            .clientEmail("lvnmwyvsrvcruhfwbn@sdvgeft.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Львівська область")
            .city("Львів")
            .district("Галицький")
            .address("Вулиця Крушельницької 10, корп.- , п.2")
            .regionEn("Lviv Oblast")
            .cityEn("Lviv")
            .districtEn("Halychskyi")
            .addressEn("Krushelnytska Street 10, b.- , e.2")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 4")
            .bagAmount("20л - 4шт")
            .totalOrderSum(1200L)
            .orderCertificateCode(null)
            .generalDiscount(0L)
            .amountDue(1200L)
            .commentForOrderByClient("by")
            .commentForOrderByAdmin(null)
            .totalPayment(0L)
            .dateOfExport(null)
            .timeOfExport("10:00:00-18:00:10")
            .idOrderFromShop(null)
            .receivingStation(null)
            .receivingStationId(null)
            .responsibleLogicMan(null)
            .responsibleLogicManId(null)
            .responsibleDriver(null)
            .responsibleDriverId(null)
            .responsibleCaller(null)
            .responsibleCallerId(null)
            .responsibleNavigator(null)
            .responsibleNavigatorId(null)
            .isBlocked(true)
            .blockedBy("Abu, Dabi")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_85() {
        return BigOrderTableViews.builder()
            .id(85L)
            .orderStatus("FORMED")
            .orderPaymentStatus("UNPAID")
            .orderDate(LocalDate.parse("2022-02-04"))
            .paymentDate(null)
            .clientName("Anna Maria")
            .clientPhoneNumber("+380631144678")
            .clientEmail("testgreencity323@gmail.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(0)
            .region("Київська область")
            .city("Київ")
            .district("Подільський")
            .address("Велика Васильківська 14, корп.Б, п.2")
            .regionEn("Kyiv Oblast")
            .cityEn("Kyiv")
            .districtEn("Podilskyi")
            .addressEn("Velyka Vasylkivska 14, b.Б, e.2")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 1")
            .bagAmount("20л - 2шт")
            .totalOrderSum(600L)
            .orderCertificateCode(null)
            .generalDiscount(0L)
            .amountDue(600L)
            .commentForOrderByClient("hey")
            .commentForOrderByAdmin(null)
            .totalPayment(0L)
            .dateOfExport(null)
            .timeOfExport("10:00:00-18:00:10")
            .idOrderFromShop(null)
            .receivingStation(null)
            .receivingStationId(null)
            .responsibleLogicMan(null)
            .responsibleLogicManId(null)
            .responsibleDriver(null)
            .responsibleDriverId(null)
            .responsibleCaller(null)
            .responsibleCallerId(null)
            .responsibleNavigator(null)
            .responsibleNavigatorId(null)
            .isBlocked(false)
            .blockedBy("")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_86() {
        return BigOrderTableViews.builder()
            .id(86L)
            .orderStatus("CONFIRMED")
            .orderPaymentStatus("PAID")
            .orderDate(LocalDate.parse("2022-02-05"))
            .paymentDate(LocalDate.parse("2022-02-05"))
            .clientName("Anna Maria")
            .clientPhoneNumber("+380631144678")
            .clientEmail("testgreencity323@gmail.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Київська область")
            .city("Київ")
            .district("Шевченківський")
            .address("Хрещатик 27, корп.- , п.1")
            .regionEn("Kyiv Oblast")
            .cityEn("Kyiv")
            .districtEn("Shevchenkivskyi")
            .addressEn("Khreshchatyk 27, b.- , e.1")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 2")
            .bagAmount("20л - 2шт")
            .totalOrderSum(600L)
            .orderCertificateCode("3003-1992")
            .generalDiscount(500L)
            .amountDue(-49400L)
            .commentForOrderByClient("hey")
            .commentForOrderByAdmin(null)
            .totalPayment(50000L)
            .dateOfExport(null)
            .timeOfExport("22:27:00-22:27:03")
            .idOrderFromShop(null)
            .receivingStation("Грибовицька")
            .receivingStationId(2L)
            .responsibleLogicMan("Migno, Tekku")
            .responsibleLogicManId(3L)
            .responsibleDriver("Abu, Dabi")
            .responsibleDriverId(10L)
            .responsibleCaller("Tapsoy, Ipsi")
            .responsibleCallerId(2L)
            .responsibleNavigator("Test, User")
            .responsibleNavigatorId(15L)
            .isBlocked(false)
            .blockedBy("")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_87() {
        return BigOrderTableViews.builder()
            .id(87L)
            .orderStatus("CONFIRMED")
            .orderPaymentStatus("PAID")
            .orderDate(LocalDate.parse("2022-02-07"))
            .paymentDate(LocalDate.parse("2022-03-07"))
            .clientName("Migno Tekku")
            .clientPhoneNumber("+380508003301")
            .clientEmail("mignotekku@vusra.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Львівська область")
            .city("Львів")
            .district("Шевченківський")
            .address("Площа Ринок 5, корп.А, п.3")
            .regionEn("Lviv Oblast")
            .cityEn("Lviv")
            .districtEn("Shevchenkivskyi")
            .addressEn("Rynok Square 5, b.А, e.3")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 3")
            .bagAmount("120л - 5шт; 60л - 5шт; 20л - 5шт")
            .totalOrderSum(3000L)
            .orderCertificateCode("3113-3113, 3113-3114")
            .generalDiscount(2000L)
            .amountDue(-207000L)
            .commentForOrderByClient("by")
            .commentForOrderByAdmin(null)
            .totalPayment(210000L)
            .dateOfExport(null)
            .timeOfExport("22:27:07-22:27:12")
            .idOrderFromShop(null)
            .receivingStation("Грибовицька")
            .receivingStationId(2L)
            .responsibleLogicMan("Migno, Tekku")
            .responsibleLogicManId(3L)
            .responsibleDriver("Abu, Dabi")
            .responsibleDriverId(10L)
            .responsibleCaller("Tapsoy, Ipsi")
            .responsibleCallerId(2L)
            .responsibleNavigator("Test, User")
            .responsibleNavigatorId(15L)
            .isBlocked(false)
            .blockedBy("")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_88() {
        return BigOrderTableViews.builder()
            .id(88L)
            .orderStatus("DONE")
            .orderPaymentStatus("PAID")
            .orderDate(LocalDate.parse("2022-01-30"))
            .paymentDate(LocalDate.parse("2022-01-30"))
            .clientName("Anna Maria")
            .clientPhoneNumber("+380631144678")
            .clientEmail("testgreencity323@gmail.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Київська область")
            .city("Київ")
            .district("Подільський")
            .address("Велика Васильківська 14, корп.Б, п.2")
            .regionEn("Kyiv Oblast")
            .cityEn("Kyiv")
            .districtEn("Podilskyi")
            .addressEn("Velyka Vasylkivska 14, b.Б, e.2")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 1")
            .bagAmount("120л - 1шт; 60л - 1шт; 20л - 1шт")
            .totalOrderSum(600L)
            .orderCertificateCode(null)
            .generalDiscount(0L)
            .amountDue(-59400L)
            .commentForOrderByClient("by")
            .commentForOrderByAdmin(null)
            .totalPayment(60000L)
            .dateOfExport(null)
            .timeOfExport("22:27:16-22:27:21")
            .idOrderFromShop(null)
            .receivingStation("Саперно-Слобідська")
            .receivingStationId(1L)
            .responsibleLogicMan("Abu, Dabi")
            .responsibleLogicManId(10L)
            .responsibleDriver("Test, User")
            .responsibleDriverId(15L)
            .responsibleCaller("Tapsoy, Ipsi")
            .responsibleCallerId(2L)
            .responsibleNavigator("Migno, Tekku")
            .responsibleNavigatorId(3L)
            .isBlocked(false)
            .blockedBy("")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_89() {
        return BigOrderTableViews.builder()
            .id(89L)
            .orderStatus("FORMED")
            .orderPaymentStatus("UNPAID")
            .orderDate(LocalDate.parse("2022-02-08"))
            .paymentDate(null)
            .clientName("Migno Tekku")
            .clientPhoneNumber("+380508003301")
            .clientEmail("mignotekku@vusra.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Львівська область")
            .city("Львів")
            .district("Шевченківський")
            .address("Площа Ринок 5, корп.А, п.3")
            .regionEn("Lviv Oblast")
            .cityEn("Lviv")
            .districtEn("Shevchenkivskyi")
            .addressEn("Rynok Square 5, b.А, e.3")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 3")
            .bagAmount("120л - 2шт; 60л - 1шт; 20л - 1шт")
            .totalOrderSum(850L)
            .orderCertificateCode(null)
            .generalDiscount(0L)
            .amountDue(850L)
            .commentForOrderByClient("by")
            .commentForOrderByAdmin(null)
            .totalPayment(0L)
            .dateOfExport(null)
            .timeOfExport("22:27:25-22:27:29")
            .idOrderFromShop(null)
            .receivingStation(null)
            .receivingStationId(null)
            .responsibleLogicMan(null)
            .responsibleLogicManId(null)
            .responsibleDriver(null)
            .responsibleDriverId(null)
            .responsibleCaller(null)
            .responsibleCallerId(null)
            .responsibleNavigator(null)
            .responsibleNavigatorId(null)
            .isBlocked(false)
            .blockedBy("")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_90() {
        return BigOrderTableViews.builder()
            .id(90L)
            .orderStatus("ON_THE_ROUTE")
            .orderPaymentStatus("PAID")
            .orderDate(LocalDate.parse("2022-02-08"))
            .paymentDate(LocalDate.parse("2022-02-08"))
            .clientName("Anna Maria")
            .clientPhoneNumber("+380631144678")
            .clientEmail("testgreencity323@gmail.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Київська область")
            .city("Київ")
            .district("Подільський")
            .address("Велика Васильківська 14, корп.Б, п.2")
            .regionEn("Kyiv Oblast")
            .cityEn("Kyiv")
            .districtEn("Podilskyi")
            .addressEn("Velyka Vasylkivska 14, b.Б, e.2")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 1")
            .bagAmount("120л - 6шт; 60л - 6шт; 20л - 8шт")
            .totalOrderSum(4200L)
            .orderCertificateCode("7777-7777, 1212-1212, 1111-2222")
            .generalDiscount(2050L)
            .amountDue(-200800L)
            .commentForOrderByClient("234")
            .commentForOrderByAdmin(null)
            .totalPayment(205000L)
            .dateOfExport(null)
            .timeOfExport("07:00:00-15:00:00")
            .idOrderFromShop(null)
            .receivingStation("Грибовицька")
            .receivingStationId(2L)
            .responsibleLogicMan("Migno, Tekku")
            .responsibleLogicManId(3L)
            .responsibleDriver("Test, User")
            .responsibleDriverId(15L)
            .responsibleCaller("Tapsoy, Ipsi")
            .responsibleCallerId(2L)
            .responsibleNavigator("Abu, Dabi")
            .responsibleNavigatorId(10L)
            .isBlocked(false)
            .blockedBy("")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_91() {
        return BigOrderTableViews.builder()
            .id(91L)
            .orderStatus("FORMED")
            .orderPaymentStatus("PAID")
            .orderDate(LocalDate.parse("2022-01-30"))
            .paymentDate(LocalDate.parse("2022-01-30"))
            .clientName("Anna Maria")
            .clientPhoneNumber("+380631144678")
            .clientEmail("testgreencity323@gmail.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Київська область")
            .city("Київ")
            .district("Подільський")
            .address("Велика Васильківська 14, корп.Б, п.2")
            .regionEn("Kyiv Oblast")
            .cityEn("Kyiv")
            .districtEn("Podilskyi")
            .addressEn("Velyka Vasylkivska 14, b.Б, e.2")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 1")
            .bagAmount("120л - 1шт; 60л - 2шт; 20л - 1шт")
            .totalOrderSum(650L)
            .orderCertificateCode(null)
            .generalDiscount(0L)
            .amountDue(-64350L)
            .commentForOrderByClient("")
            .commentForOrderByAdmin(null)
            .totalPayment(65000L)
            .dateOfExport(null)
            .timeOfExport("07:00:00-15:00:00")
            .idOrderFromShop(null)
            .receivingStation(null)
            .receivingStationId(null)
            .responsibleLogicMan(null)
            .responsibleLogicManId(null)
            .responsibleDriver(null)
            .responsibleDriverId(null)
            .responsibleCaller(null)
            .responsibleCallerId(null)
            .responsibleNavigator(null)
            .responsibleNavigatorId(null)
            .isBlocked(true)
            .blockedBy("Abu, Dabi")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_92() {
        return BigOrderTableViews.builder()
            .id(92L)
            .orderStatus("FORMED")
            .orderPaymentStatus("PAID")
            .orderDate(LocalDate.parse("2022-01-30"))
            .paymentDate(LocalDate.parse("2022-01-30"))
            .clientName("Anna Maria")
            .clientPhoneNumber("+380631144678")
            .clientEmail("testgreencity323@gmail.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Київська область")
            .city("Київ")
            .district("Подільський")
            .address("Велика Васильківська 14, корп.Б, п.2")
            .regionEn("Kyiv Oblast")
            .cityEn("Kyiv")
            .districtEn("Podilskyi")
            .addressEn("Velyka Vasylkivska 14, b.Б, e.2")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 1")
            .bagAmount("120л - 1шт; 60л - 2шт; 20л - 1шт")
            .totalOrderSum(650L)
            .orderCertificateCode(null)
            .generalDiscount(0L)
            .amountDue(-64350L)
            .commentForOrderByClient("")
            .commentForOrderByAdmin(null)
            .totalPayment(65000L)
            .dateOfExport(null)
            .timeOfExport("22:27:59-22:28:11")
            .idOrderFromShop(null)
            .receivingStation(null)
            .receivingStationId(null)
            .responsibleLogicMan(null)
            .responsibleLogicManId(null)
            .responsibleDriver(null)
            .responsibleDriverId(null)
            .responsibleCaller(null)
            .responsibleCallerId(null)
            .responsibleNavigator(null)
            .responsibleNavigatorId(null)
            .isBlocked(false)
            .blockedBy("")
            .tariffsInfoId(1L)
            .build();
    }

    public static BigOrderTableViews getBOTViews_93() {
        return BigOrderTableViews.builder()
            .id(93L)
            .orderStatus("CONFIRMED")
            .orderPaymentStatus("PAID")
            .orderDate(LocalDate.parse("2022-01-27"))
            .paymentDate(LocalDate.parse("2022-01-27"))
            .clientName("Anna Maria")
            .clientPhoneNumber("+380631144678")
            .clientEmail("testgreencity323@gmail.com")
            .senderName("")
            .senderPhone(null)
            .senderEmail(null)
            .violationsAmount(345)
            .region("Київська область")
            .city("Київ")
            .district("Подільський")
            .address("Велика Васильківська 14, корп.Б, п.2")
            .regionEn("Kyiv Oblast")
            .cityEn("Kyiv")
            .districtEn("Podilskyi")
            .addressEn("Velyka Vasylkivska 14, b.Б, e.2")
            .regionId(1L)
            .cityId(1L)
            .districtId(1L)
            .commentToAddressForClient("Коментар до адреси 1")
            .bagAmount("120л - 20шт; 60л - 30шт; 20л - 50шт")
            .totalOrderSum(5000L)
            .orderCertificateCode(null)
            .generalDiscount(0L)
            .amountDue(-1260000L)
            .commentForOrderByClient("")
            .commentForOrderByAdmin(null)
            .totalPayment(1265000L)
            .dateOfExport(null)
            .timeOfExport("22:28:18-22:28:24")
            .idOrderFromShop(null)
            .receivingStation("Грибовицька")
            .receivingStationId(2L)
            .responsibleLogicMan("Migno, Tekku")
            .responsibleLogicManId(3L)
            .responsibleDriver("Tapsoy, Ipsi")
            .responsibleDriverId(2L)
            .responsibleCaller("Test, User")
            .responsibleCallerId(15L)
            .responsibleNavigator("Abu, Dabi")
            .responsibleNavigatorId(10L)
            .isBlocked(false)
            .blockedBy("")
            .tariffsInfoId(1L)
            .build();
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
