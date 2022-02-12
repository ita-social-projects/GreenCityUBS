package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.BagMappingDto;
import greencity.entity.order.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class BagMappingMapperTest {
    @InjectMocks
    BagMappingMapper bagMappingMapper;

    @Test
    void convertReturnsEmptyList() {
        Order order = ModelUtils.getOrder();
        order.setAmountOfBagsOrdered(new HashMap<>());
        order.setExportedQuantity(new HashMap<>());
        order.setConfirmedQuantity(new HashMap<>());
        List<BagMappingDto> list = bagMappingMapper.convert(order);
        assertTrue(list.size() == 0);
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
        assertTrue(list.size() == 1);
        assertTrue(list.get(0).getAmount() == 1);
        assertTrue(list.get(0).getConfirmed() == 1);
        assertTrue(list.get(0).getExported() == 1);
    }

}
