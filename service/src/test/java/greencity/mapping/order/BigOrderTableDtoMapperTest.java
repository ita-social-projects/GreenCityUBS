package greencity.mapping.order;

import greencity.ModelUtils;
import greencity.dto.order.BigOrderTableDTO;
import greencity.dto.order.SenderLocation;
import greencity.entity.order.BigOrderTableViews;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class BigOrderTableDtoMapperTest {
    @InjectMocks
    BigOrderTableDtoMapper bigOrderTableDtoMapper;

    @Test
    void convert() {
        var bigOrderTableDto = ModelUtils.getBigOrderTableDto();
        var bigOrderTableView = ModelUtils.getBigOrderTableViews();
        assertEquals(bigOrderTableDto, bigOrderTableDtoMapper.convert(bigOrderTableView));
    }

    @Test
    void convertNullDateValue() {
        var bigOrderTableDto = ModelUtils.getBigOrderTableDtoByDateNullTest();
        var bigOrderTableView = ModelUtils.getBigOrderTableViewsByDateNullTest();
        assertEquals(bigOrderTableDto, bigOrderTableDtoMapper.convert(bigOrderTableView));
    }

    @Test
    void convertTest() {
        BigOrderTableViews bigViews = ModelUtils.getBigOrderTableViews1();

        BigOrderTableDTO result = bigOrderTableDtoMapper.convert(bigViews);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PENDING", result.getOrderStatus());
        assertEquals("PAID", result.getOrderPaymentStatus());
        assertEquals("2024-12-13", result.getOrderDate());
        assertEquals("2024-12-14", result.getPaymentDate());
        assertEquals("John Doe", result.getClientName());
        assertEquals("johndoe@example.com", result.getClientEmail());
        assertEquals("1234567890", result.getClientPhone());
        assertEquals("Jane Smith", result.getSenderName());
        assertEquals("0987654321", result.getSenderPhone());
        assertEquals("janesmith@example.com", result.getSenderEmail());
        assertEquals(2, result.getViolationsAmount());
        assertEquals(new SenderLocation("Kyiv Region", "Kyiv Oblast"), result.getRegion());
        assertEquals(new SenderLocation("Kyiv", "Kyiv"), result.getCity());
        assertEquals(new SenderLocation("Pecherskyi", "Pecherskyi"), result.getDistrict());
        assertEquals(new SenderLocation("Khreshchatyk 1", "Khreshchatyk 1"), result.getAddress());
        assertEquals("-", result.getTextileWaste60L());
        assertEquals("-", result.getTextileWaste20L());
        assertEquals("5", result.getMixedWaste120L());
        assertEquals(15.00, result.getTotalOrderSum());
        assertEquals(12.00, result.getAmountDue());
        assertEquals(3.00, result.getTotalPayment());
    }
}
