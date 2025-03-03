package greencity.service.ubs.pdf.exporter;

import com.lowagie.text.pdf.PdfWriter;
import greencity.service.ubs.UBSClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;

@ExtendWith(MockitoExtension.class)
class PdfFileExporterImplTest {

    @Mock
    private PdfWriter pdfWriter;
    @Mock
    private UBSClientService ubsClientService;
    @InjectMocks
    private PdfFileExporterImpl pdfFileExporterService;

    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        outputStream = new ByteArrayOutputStream();
    }

//    @Test
//    void testExportPdfSuccess() throws IOException, DocumentException {
//        OrdersDataForUserDto orderToExport = ModelUtils.getOrdersDataForUserDto();
//        Locale locale = Locale.ENGLISH;
//        when(ubsClientService.getOrderForUser(anyString(), anyLong())).thenReturn(orderToExport);
//        assertNotNull(resource);
//        assertTrue(resource.contentLength() > 0);
//    }

//    @Test
//    void testExportPdfThrowsException() {
//        OrdersDataForUserDto orderToExport = ModelUtils.getOrdersDataForUserDto();
//        Locale locale = Locale.ENGLISH;
//        when(ubsClientService.getOrderForUser(anyString(), anyLong())).thenReturn(orderToExport);
//        PdfFileExporterServiceImpl faultyExporter = spy(pdfFileExporterService);
//        doThrow(new IOException("Test exception")).when(faultyExporter).export(anyLong(), any(Locale.class), anyString());
//
//        assertThrows(PdfFileExportingException.class, () -> faultyExporter.export(anyLong(), any(Locale.class), anyString()));
//    }
}
