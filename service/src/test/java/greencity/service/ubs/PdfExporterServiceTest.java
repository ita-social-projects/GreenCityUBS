package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.service.ubs.file.export.FileExporter;
import greencity.service.ubs.pdf.exporter.PdfExporterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PdfExporterServiceTest {
    @Mock
    private FileExporter<OrdersDataForUserDto> fileExporterService;
    @Mock
    private UBSClientService ubsClientService;
    @InjectMocks
    private PdfExporterServiceImpl pdfExporterService;

    @Test
    void testExportById() {
        OrdersDataForUserDto orderToWrite = ModelUtils.getOrdersDataForUserDto();
        byte[] pdfBytes = {1, 2, 3};
        when(ubsClientService.getOrderForUser(anyString(), anyLong())).thenReturn(orderToWrite);
        when(fileExporterService.export(any(), any())).thenReturn(pdfBytes);
        Resource resource = pdfExporterService.exportById(1L, Locale.ENGLISH, "user-uuid");
        assertArrayEquals(pdfBytes, ((ByteArrayResource) resource).getByteArray());
        verify(ubsClientService, times(1)).getOrderForUser("user-uuid", 1L);
        verify(fileExporterService, times(1)).export(orderToWrite, Locale.ENGLISH);
    }
}