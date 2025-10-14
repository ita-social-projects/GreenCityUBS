package greencity.service.ubs.pdf.exporter;

import greencity.dto.order.OrdersDataForUserDto;
import greencity.service.ubs.PdfExporterService;
import greencity.service.ubs.file.export.FileExporter;
import greencity.service.ubs.order.OrderService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPdfExporterServiceImpl implements PdfExporterService {
    private final FileExporter<OrdersDataForUserDto> pdfExporterService;
    private final OrderService orderService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource generatePdfFileByObjectId(long objectId, Locale locale, String userUuid) {
        OrdersDataForUserDto orderToWrite = orderService.getOrderForUser(userUuid, objectId);
        return new ByteArrayResource(pdfExporterService.export(orderToWrite, locale));
    }
}