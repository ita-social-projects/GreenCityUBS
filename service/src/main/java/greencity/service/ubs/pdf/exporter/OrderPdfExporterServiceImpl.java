package greencity.service.ubs.pdf.exporter;

import greencity.dto.order.OrdersDataForUserDto;
import greencity.service.ubs.PdfExporterService;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.file.export.FileExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OrderPdfExporterServiceImpl implements PdfExporterService {
    private final FileExporter<OrdersDataForUserDto> pdfExporterService;
    private final UBSClientService ubsClientService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource generatePdfFileByObjectId(long objectId, Locale locale, String userUuid) {
        OrdersDataForUserDto orderToWrite = ubsClientService.getOrderForUser(userUuid, objectId);
        return new ByteArrayResource(pdfExporterService.export(orderToWrite, locale));
    }
}