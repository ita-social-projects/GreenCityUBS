package greencity.service;

import greencity.ModelUtils;
import greencity.dto.GroupedOrderDto;
import greencity.dto.OrderDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Order;
import greencity.repository.AddressRepository;
import greencity.repository.OrderRepository;
import greencity.service.ubs.UBSManagementServiceImpl;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
public class UBSManagementServiceImplTest {
    @Mock
    AddressRepository addressRepository;
    double distance = 2;
    int litres = 1000;

    @Mock
    OrderRepository orderRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    UBSManagementServiceImpl ubsManagementService;

    private void getMocksBehavior() {

        when(addressRepository.capacity(anyDouble(), anyDouble())).thenReturn(25);

        for (Coordinates coordinate : ModelUtils.getCoordinatesSet()) {
            List<Order> orders = ModelUtils.getOrdersToGroupThem().stream()
                .filter(e -> e.getUbsUser().getUserAddress().getCoordinates().equals(coordinate)).collect(
                    Collectors.toList());
            when(orderRepository.undeliveredOrdersGroupThem(coordinate.getLatitude(), coordinate.getLongitude()))
                .thenReturn(orders);
            for (Order order : orders) {
                when(modelMapper.map(order, OrderDto.class)).thenReturn(OrderDto.builder()
                    .latitude(order.getUbsUser().getUserAddress().getCoordinates().getLatitude())
                    .longitude(order.getUbsUser().getUserAddress().getCoordinates().getLongitude())
                    .build());
            }
        }
    }

    private static Stream<Arguments> provideDistanceAndLitres() {
        return Stream.of(Arguments.of(-1, 5),
            Arguments.of(25, 5),
            Arguments.of(10, -5),
            Arguments.of(10, 20000));
    }

    @ParameterizedTest
    @MethodSource("provideDistanceAndLitres")
    void getClusteredCoordsInvalidParametersTest(double invalidDistance, int invalidLitres) {
        assertThrows(Exception.class, () -> ubsManagementService.getClusteredCoords(invalidDistance, invalidLitres));
    }

    @Test
    void getClusteredCoordsTest() {
        when(addressRepository.undeliveredOrdersCoordsWithCapacityLimit(litres))
            .thenReturn(ModelUtils.getCoordinatesSet());
        getMocksBehavior();
        List<GroupedOrderDto> expected = ModelUtils.getGroupedOrders();
        List<GroupedOrderDto> actual = ubsManagementService.getClusteredCoords(distance, litres);

        assertEquals(expected, actual);
    }

    @Test
    void getClusteredCoordsWithBiggerClusterLitresTest() {
        when(addressRepository.undeliveredOrdersCoordsWithCapacityLimit(60)).thenReturn(ModelUtils.getCoordinatesSet());
        getMocksBehavior();
        List<GroupedOrderDto> expected = ModelUtils.getGroupedOrdersFor60LitresLimit();
        List<GroupedOrderDto> actual = ubsManagementService.getClusteredCoords(distance, 60);

        assertEquals(expected, actual);
    }

}