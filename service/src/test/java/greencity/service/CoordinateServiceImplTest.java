package greencity.service;

import greencity.ModelUtils;
import greencity.dto.GroupedOrderDto;
import greencity.dto.OrderDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Order;
import greencity.exceptions.ActiveOrdersNotFoundException;
import greencity.exceptions.IncorrectValueException;
import greencity.repository.AddressRepository;
import greencity.repository.OrderRepository;
import greencity.service.ubs.CoordinateServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static greencity.ModelUtils.getOrderDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoordinateServiceImplTest {

    @InjectMocks
    CoordinateServiceImpl coordinateService;

    @Mock
    AddressRepository addressRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock(lenient = true)
    private ModelMapper modelMapper;

    int litres = 1000;
    double distance = 2;

    private void getMocksBehavior() {

        when(addressRepository.capacity(anyDouble(), anyDouble())).thenReturn(25);

        for (Coordinates coordinate : ModelUtils.getCoordinatesSet()) {
            List<Order> orders = ModelUtils.getOrdersToGroupThem().stream()
                .filter(e -> e.getUbsUser().getAddress().getCoordinates().equals(coordinate)).collect(
                    Collectors.toList());
            when(orderRepository.undeliveredOrdersGroupThem(coordinate.getLatitude(), coordinate.getLongitude()))
                .thenReturn(orders);
            for (Order order : orders) {
                when(modelMapper.map(order, OrderDto.class)).thenReturn(OrderDto.builder()
                    .latitude(order.getUbsUser().getAddress().getCoordinates().getLatitude())
                    .longitude(order.getUbsUser().getAddress().getCoordinates().getLongitude())
                    .build());
            }
        }
    }

    @Test
    void testAllUndeliveredOrdersWithLitersThrowException() {
        when(addressRepository.undeliveredOrdersCoords()).thenReturn(ModelUtils.getCoordinatesSet());

        List<Order> undeliveredOrders = new ArrayList<>();

        when(orderRepository.undeliveredAddresses()).thenReturn(undeliveredOrders);

        assertThrows(ActiveOrdersNotFoundException.class,
            () -> coordinateService.getAllUndeliveredOrdersWithLiters());
    }

    @Test
    void testGetAllUndeliveredOrdersWithLiters() {
        List<Order> allUndeliveredOrders = ModelUtils.getOrdersToGroupThem();

        when(addressRepository.undeliveredOrdersCoords()).thenReturn(ModelUtils.getCoordinatesSet());
        when(orderRepository.undeliveredAddresses()).thenReturn(allUndeliveredOrders);
        when(addressRepository.capacity(anyDouble(), anyDouble())).thenReturn(75, 25);

        for (Coordinates cord : ModelUtils.getCoordinatesSet()) {
            List<Order> currentOrders = allUndeliveredOrders.stream().filter(
                o -> o.getUbsUser().getAddress().getCoordinates().equals(cord)).collect(Collectors.toList());
            for (Order order : currentOrders) {
                when(modelMapper.map(order, OrderDto.class)).thenReturn(
                    OrderDto.builder().latitude(order.getUbsUser().getAddress().getCoordinates().getLatitude())
                        .longitude(order.getUbsUser().getAddress().getCoordinates().getLongitude()).build());
            }
        }

        List<GroupedOrderDto> expected = coordinateService.getAllUndeliveredOrdersWithLiters();
        List<GroupedOrderDto> actual = ModelUtils.getGroupedOrdersWithLiters();

        assertEquals(expected, actual);
    }

    @Test
    void getClusteredCoordsTest() {
        when(addressRepository.undeliveredOrdersCoordsWithCapacityLimit(litres))
            .thenReturn(ModelUtils.getCoordinatesSet());
        getMocksBehavior();
        List<GroupedOrderDto> expected = ModelUtils.getGroupedOrders();
        List<GroupedOrderDto> actual = coordinateService.getClusteredCoords(distance, litres);
        assertEquals(expected, actual);
    }

    @Test
    void getClusterCoordsThrowExceptionToDistanceTest() {
        Assertions.assertThrows(IncorrectValueException.class, () -> {
            coordinateService.getClusteredCoords(-1, 20);
        });
    }

    @Test
    void getClusterCoordsThrowExceptionToLitresTest() {
        Assertions.assertThrows(IncorrectValueException.class, () -> {
            coordinateService.getClusteredCoords(2, -1);
        });
    }

    @Test
    void getClusteredCoordsWithBiggerClusterLitresTest() {
        when(addressRepository.undeliveredOrdersCoordsWithCapacityLimit(60)).thenReturn(ModelUtils.getCoordinatesSet());
        getMocksBehavior();
        List<GroupedOrderDto> expected = ModelUtils.getGroupedOrdersFor60LitresLimit();
        List<GroupedOrderDto> actual = coordinateService.getClusteredCoords(distance, 60);

        assertEquals(expected, actual);
    }

    @Test
    void getClusteredCoordsAlongWithSpecifiedTest() {
        Coordinates coord = ModelUtils.getCoordinates();
        Set<Coordinates> result = new HashSet<>();
        result.add(coord);
        List<Order> orderList = new ArrayList<>();
        orderList.add(ModelUtils.getOrderTest());
        when(addressRepository.undeliveredOrdersCoords()).thenReturn(result);
        when(addressRepository.capacity(anyDouble(), anyDouble())).thenReturn(300);
        when(orderRepository.undeliveredOrdersGroupThem(anyDouble(), anyDouble())).thenReturn(orderList);
        when(modelMapper.map(any(), any())).thenAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                if (count == 0) {
                    count++;
                    return coord;
                }
                return ModelUtils.getOrderDto();
            }
        });
        GroupedOrderDto groupedOrderDto = coordinateService
            .getClusteredCoordsAlongWithSpecified(ModelUtils.getCoordinatesDtoSet(), 3000, 15).get(0);
        assertEquals(300, groupedOrderDto.getAmountOfLitres());
        assertEquals(groupedOrderDto.getGroupOfOrders().get(0), getOrderDto());
    }

    @Test
    void getClusteredCoordsAlongWithSpecifiedThrowExceptionToDistanceTest() {
        Assertions.assertThrows(IncorrectValueException.class, () -> {
            coordinateService.getClusteredCoords(-1, 20);
        });
    }

    @Test
    void getClusteredCoordsAlongWithSpecifiedThrowExceptionToLitresTest() {
        Assertions.assertThrows(IncorrectValueException.class, () -> {
            coordinateService.getClusteredCoords(2, -1);
        });
    }

    @Test
    void getClusteredCoordsAlongWithSpecifiedThrowExceptionTest() {
        Coordinates coord = ModelUtils.getCoordinates();
        Set<Coordinates> result = new HashSet<>();
        result.add(coord);
        List<Order> orderList = new ArrayList<>();
        orderList.add(ModelUtils.getOrderTest());

        when(modelMapper.map(any(), any())).thenAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                if (count == 0) {
                    count++;
                    return coord;
                }
                return ModelUtils.getOrderDto();
            }
        });

        Assertions.assertThrows(IncorrectValueException.class, () -> {
            coordinateService.getClusteredCoordsAlongWithSpecified(
                ModelUtils.getCoordinatesDtoSet(), 3000, 15).get(0);
        });

    }
}
