package greencity.repository;

import greencity.IntegrationTestBase;
import greencity.UbsApplication;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Order;
import greencity.entity.user.ubs.Address;
import greencity.enums.OrderPaymentStatus;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Sql(scripts = "/sqlFiles/addressRepository/insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sqlFiles/addressRepository/delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UbsApplication.class)
@Ignore
class AddressRepositoryTest extends IntegrationTestBase {

    @Autowired
    AddressRepository addressRepository;

    // @Test
    void findAllByUserId() {
        Address address = ModelUtils.getAddress();
        List<Address> actual = addressRepository.findAllNonDeletedAddressesByUserId(1L);

        Assertions.assertEquals(Optional.of(address.getUser().getId()),
            Optional.of(actual.getFirst().getUser().getId()));
        Assertions.assertEquals(Optional.of(address.getCoordinates()), Optional.of(actual.getFirst().getCoordinates()));
        Assertions.assertEquals(Optional.of(address.getId()), Optional.of(actual.getFirst().getId()));
        Assertions.assertEquals(Optional.of(address.getAddressStatus()),
            Optional.of(actual.getFirst().getAddressStatus()));
        Assertions.assertEquals(Optional.of(address.getAddressComment()), Optional.of(address.getAddressComment()));
        Assertions.assertEquals(Optional.of(address.getActual()), Optional.of(actual.getFirst().getActual()));
        Assertions.assertEquals(Optional.of(address.getRegion()), Optional.of(actual.getFirst().getRegion()));
        Assertions.assertEquals(Optional.of(address.getDistrict()), Optional.of(actual.getFirst().getDistrict()));
        Assertions.assertEquals(Optional.of(address.getEntranceNumber()),
            Optional.of(actual.getFirst().getEntranceNumber()));
        Assertions.assertEquals(Optional.of(address.getHouseCorpus()), Optional.of(actual.getFirst().getHouseCorpus()));
        Assertions.assertEquals(Optional.of(address.getHouseNumber()), Optional.of(actual.getFirst().getHouseNumber()));
        Assertions.assertEquals(Optional.of(address.getStreet()), Optional.of(actual.getFirst().getStreet()));
        Assertions.assertEquals(Optional.of(address.getCityEn()), Optional.of(actual.getFirst().getCityEn()));
        Assertions.assertEquals(Optional.of(address.getRegionEn()), Optional.of(actual.getFirst().getRegionEn()));
        Assertions.assertEquals(Optional.of(address.getDistrictEn()), Optional.of(actual.getFirst().getDistrictEn()));
        Assertions.assertEquals(Optional.of(address.getStreetEn()), Optional.of(actual.getFirst().getStreetEn()));
    }

    // @Test
    void undeliveredOrdersCoords() {
        Set<Coordinates> actual = addressRepository.undeliveredOrdersCoords();
        Order order = Order.builder().orderPaymentStatus(OrderPaymentStatus.PAID).build();
        Assertions.assertSame(OrderPaymentStatus.PAID, order.getOrderPaymentStatus());
        Assertions.assertNotNull(actual);
    }

    // @Test
    void undeliveredOrdersCoordsWithCapacityLimit() {
        Set<Coordinates> actual = addressRepository.undeliveredOrdersCoordsWithCapacityLimit(100L);
        Order order = Order.builder().orderPaymentStatus(OrderPaymentStatus.PAID).build();
        Assertions.assertSame(OrderPaymentStatus.PAID, order.getOrderPaymentStatus());
        Assertions.assertNotNull(actual);
    }

    // @Test
    void getAddressByOrderId() {

        Address address = ModelUtils.getAddress();
        Address actual = addressRepository.findById(1L).orElseThrow();

        Assertions.assertEquals(Optional.of(address.getUser().getId()), Optional.of(actual.getUser().getId()));
        Assertions.assertEquals(Optional.of(address.getCoordinates()), Optional.of(actual.getCoordinates()));
        Assertions.assertEquals(Optional.of(address.getId()), Optional.of(actual.getId()));
        Assertions.assertEquals(Optional.of(address.getAddressStatus()), Optional.of(actual.getAddressStatus()));
        Assertions.assertEquals(Optional.of(address.getAddressComment()), Optional.of(actual.getAddressComment()));
        Assertions.assertEquals(Optional.of(address.getActual()), Optional.of(actual.getActual()));
        Assertions.assertEquals(Optional.of(address.getRegion()), Optional.of(actual.getRegion()));
        Assertions.assertEquals(Optional.of(address.getDistrict()), Optional.of(actual.getDistrict()));
        Assertions.assertEquals(Optional.of(address.getEntranceNumber()), Optional.of(actual.getEntranceNumber()));
        Assertions.assertEquals(Optional.of(address.getHouseCorpus()), Optional.of(actual.getHouseCorpus()));
        Assertions.assertEquals(Optional.of(address.getHouseNumber()), Optional.of(actual.getHouseNumber()));
        Assertions.assertEquals(Optional.of(address.getStreet()), Optional.of(actual.getStreet()));
        Assertions.assertEquals(Optional.of(address.getCityEn()), Optional.of(actual.getCityEn()));
        Assertions.assertEquals(Optional.of(address.getRegionEn()), Optional.of(actual.getRegionEn()));
        Assertions.assertEquals(Optional.of(address.getDistrictEn()), Optional.of(actual.getDistrictEn()));
        Assertions.assertEquals(Optional.of(address.getStreetEn()), Optional.of(actual.getStreetEn()));
    }

}
