package greencity.service.ubs.exporter.pdf;

import greencity.ModelUtils;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.exceptions.NotFoundException;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.file.export.FileExporter;
import greencity.service.ubs.pdf.exporter.OrderPdfExporterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OrderPdfExporterServiceTest {
    @Mock
    private FileExporter<OrdersDataForUserDto> fileExporterService;
    @Mock
    private UBSClientService ubsClientService;
    @InjectMocks
    private OrderPdfExporterServiceImpl pdfExporterService;

    @Test
    void testGeneratePdfFileWithValidParameters() {
        OrdersDataForUserDto orderToWrite = ModelUtils.getOrdersDataForUserDto();
        byte[] pdfBytes = {1, 2, 3};
        when(ubsClientService.getOrderForUser(anyString(), anyLong())).thenReturn(orderToWrite);
        when(fileExporterService.export(any(), any())).thenReturn(pdfBytes);
        Resource resource = pdfExporterService.generatePdfFileByObjectId(1L, Locale.ENGLISH, "user-uuid");
        assertArrayEquals(pdfBytes, ((ByteArrayResource) resource).getByteArray());
        verify(ubsClientService, times(1)).getOrderForUser("user-uuid", 1L);
        verify(fileExporterService, times(1)).export(orderToWrite, Locale.ENGLISH);
    }

    @Test
    void testGeneratePdfFileWithInvalidId() {
        doThrow(NotFoundException.class).when(ubsClientService).getOrderForUser(any(), any());
        assertThrows(NotFoundException.class,
            () -> pdfExporterService.generatePdfFileByObjectId(Integer.MAX_VALUE, Locale.ENGLISH, "user-uuid"));
        verify(ubsClientService, times(1)).getOrderForUser(any(), any());
    }

}