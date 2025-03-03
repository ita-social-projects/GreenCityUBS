package greencity.service.ubs.pdf.exporter;

import greencity.dto.order.OrdersDataForUserDto;
import greencity.service.ubs.FileExporter;
import greencity.service.ubs.PdfExporterService;
import greencity.service.ubs.UBSClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PdfExporterServiceImpl implements PdfExporterService {
    private final FileExporter<OrdersDataForUserDto> pdfExporterService;
    private final UBSClientService ubsClientService;

    @Override
    public Resource exportById(long orderId, Locale locale, String userUuid) {
        OrdersDataForUserDto orderToWrite = ubsClientService.getOrderForUser(userUuid, orderId);
        return new ByteArrayResource(pdfExporterService.export(orderToWrite, locale));
    }
}
