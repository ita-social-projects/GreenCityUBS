package greencity.mapping.bag;

import greencity.ModelUtils;
import greencity.dto.bag.BagMappingDto;
import greencity.entity.order.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BagMappingMapperTest {
    @InjectMocks
    BagMappingMapper bagMappingMapper;

    @Test
    void convertReturnsEmptyList() {
        Order order = ModelUtils.getOrder();
        order.setAmountOfBagsOrdered(new HashMap<>());
        order.setExportedQuantity(new HashMap<>());
        order.setConfirmedQuantity(new HashMap<>());
        List<BagMappingDto> list = bagMappingMapper.convert(order);
        assertEquals(0, list.size());
    }

    @Test
    void convert() {
        Order order = ModelUtils.getOrder();

        Map<Integer, Integer> amountOfBagsOrdered = new HashMap<>();
        amountOfBagsOrdered.put(1, 1);
        order.setAmountOfBagsOrdered(amountOfBagsOrdered);

        Map<Integer, Integer> exportedQuantity = new HashMap<>();
        exportedQuantity.put(1, 1);
        order.setExportedQuantity(exportedQuantity);

        Map<Integer, Integer> confirmedQuantity = new HashMap<>();
        confirmedQuantity.put(1, 1);
        order.setConfirmedQuantity(confirmedQuantity);

        List<BagMappingDto> list = bagMappingMapper.convert(order);
        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getAmount());
        assertEquals(1, list.get(0).getConfirmed());
        assertEquals(1, list.get(0).getExported());
    }

}
