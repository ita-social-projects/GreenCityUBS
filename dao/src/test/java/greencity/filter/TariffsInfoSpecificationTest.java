package greencity.filter;

import greencity.entity.order.*;
import greencity.entity.order.TariffLocation_;
import greencity.entity.order.TariffsInfo_;
import greencity.entity.user.Location_;
import greencity.entity.user.employee.ReceivingStation_;
import greencity.enums.TariffStatus;
import greencity.filters.TariffsInfoFilterCriteria;
import greencity.filters.TariffsInfoSpecification;
import jakarta.persistence.criteria.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TariffsInfoSpecificationTest {
    private TariffsInfoSpecification tariffsInfoSpecification;

    @Mock
    private Root<TariffsInfo> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private Join<Object, Object> objectJoin;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> location;

    @Mock
    private Path<Object> region;

    @Mock
    private Path<Object> courier;

    @Mock
    private Join<Object, Object> receivingStation;

    @Mock
    private Path<Object> status;

    @Mock
    private Path<Object> locationId;

    @Mock
    private Path<Object> receivingStationId;

    @Test
    void toPredicateWithAllDateDoesNotThrowExceptionTest() {
        var criteria = getTariffsInfoFilterCriteria();
        tariffsInfoSpecification = new TariffsInfoSpecification(criteria);

        mockMethodVariables();

        when(criteriaBuilder.equal(any(), any(Integer.class))).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.equal(any(), any(TariffStatus.class))).thenReturn(mock(Predicate.class));

        when(location.get(Location_.ID)).thenReturn(locationId);
        when(locationId.in(any(Object[].class))).thenReturn(mock(Predicate.class));

        when(receivingStation.get(ReceivingStation_.ID)).thenReturn(receivingStationId);
        when(receivingStationId.in(any(Object[].class))).thenReturn(mock(Predicate.class));

        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        assertDoesNotThrow(() -> tariffsInfoSpecification.toPredicate(root, query, criteriaBuilder));
        verifyMockInteractions();

        verify(criteriaBuilder, times(2)).equal(any(), any(Integer.class));
        verify(criteriaBuilder, times(1)).equal(any(), any(TariffStatus.class));

        verify(location).get(Location_.ID);
        verify(locationId).in(any(Object[].class));

        verify(receivingStation).get(anyString());
        verify(receivingStationId).in(any(Object[].class));

        verify(criteriaBuilder).and(any(Predicate[].class));

    }

    private TariffsInfoFilterCriteria getTariffsInfoFilterCriteria() {
        return TariffsInfoFilterCriteria.builder()
            .region(1)
            .location(new Integer[] {1, 2, 3})
            .courier(1)
            .receivingStation(new Integer[] {1, 2})
            .status(TariffStatus.ACTIVE)
            .build();
    }

    @Test
    void toPredicateWithNullParamsOfCriteriaDoesNotThrowExceptionTest() {
        tariffsInfoSpecification = new TariffsInfoSpecification(new TariffsInfoFilterCriteria());
        mockMethodVariables();

        assertDoesNotThrow(() -> tariffsInfoSpecification.toPredicate(root, query, criteriaBuilder));

        verifyMockInteractions();
    }

    @ParameterizedTest
    @NullSource
    void toPredicateWithNullCriteriaDoesNotThrowsNullPointerTest(TariffsInfoFilterCriteria criteria) {
        tariffsInfoSpecification = new TariffsInfoSpecification(criteria);
        mockMethodVariables();

        assertThrows(NullPointerException.class,
            () -> tariffsInfoSpecification.toPredicate(root, query, criteriaBuilder));

        verifyMockInteractions();
    }

    private void mockMethodVariables() {
        when(root.join(TariffsInfo_.TARIFF_LOCATIONS)).thenReturn(objectJoin);
        when(objectJoin.get(TariffLocation_.LOCATION)).thenReturn(location);
        when(location.get(Location_.REGION)).thenReturn(region);
        when(root.get(TariffsInfo_.COURIER)).thenReturn(courier);
        when(root.join(TariffsInfo_.RECEIVING_STATION_LIST)).thenReturn(receivingStation);
        when(root.get(TariffsInfo_.TARIFF_STATUS)).thenReturn(status);
    }

    private void verifyMockInteractions() {
        verify(root).join(TariffsInfo_.TARIFF_LOCATIONS);
        verify(objectJoin).get(TariffLocation_.LOCATION);
        verify(location).get(Location_.REGION);
        verify(root).get(TariffsInfo_.COURIER);
        verify(root).join(TariffsInfo_.RECEIVING_STATION_LIST);
        verify(root).get(TariffsInfo_.TARIFF_STATUS);
    }
}
