package greencity.mapping.bag;

import greencity.dto.bag.BagMappingDto;
import greencity.entity.order.Order;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BagMappingMapper extends AbstractConverter<Order, List<BagMappingDto>> {
    @Override
    protected List<BagMappingDto> convert(Order order) {
        List<Integer> amountValues = new ArrayList<>();
        List<Integer> exportedValues = new ArrayList<>();
        List<Integer> confirmedValues = new ArrayList<>();
        for (Map.Entry<Integer, Integer> pair : order.getAmountOfBagsOrdered().entrySet()) {
            amountValues.add(pair.getValue());
        }
        for (Map.Entry<Integer, Integer> pair : order.getExportedQuantity().entrySet()) {
            exportedValues.add(pair.getValue());
        }
        for (Map.Entry<Integer, Integer> pair : order.getConfirmedQuantity().entrySet()) {
            confirmedValues.add(pair.getValue());
        }

        List<BagMappingDto> build = new ArrayList<>();
        for (int i = 0; i < amountValues.size(); i++) {
            if (exportedValues.isEmpty() || amountValues.size() > exportedValues.size()) {
                exportedValues.add(null);
            }
            if (confirmedValues.isEmpty() || amountValues.size() > confirmedValues.size()) {
                confirmedValues.add(null);
            }
            build.add(BagMappingDto.builder()
                .amount(amountValues.get(i))
                .exported(exportedValues.get(i))
                .confirmed(confirmedValues.get(i))
                .build());
        }
        return build;
    }
}