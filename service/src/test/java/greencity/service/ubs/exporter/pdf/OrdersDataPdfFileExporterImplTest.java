package greencity.service.ubs.exporter.pdf;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.dto.order.OrdersDataForUserDto;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import greencity.exceptions.exporting.pdf.PdfFileExportingException;
import greencity.repository.OrderRepository;
import greencity.service.ubs.UBSClientServiceImpl;
import greencity.service.ubs.pdf.exporter.OrdersDataPdfFileExporterImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrdersDataPdfFileExporterImplTest {
    @Mock
    private UBSClientServiceImpl ubsClientService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrdersDataPdfFileExporterImpl pdfFileExporter;

    @Test
    void exportValidEnPdf() throws IOException {
        OrdersDataForUserDto orderData = ModelUtils.getOrdersDataForUserDto();
        when(orderRepository.findById(orderData.getId()))
            .thenReturn(Optional.of(mock(greencity.entity.order.Order.class)));
        byte[] pdfBytes = pdfFileExporter.export(orderData, Locale.ENGLISH);
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(new PdfReader(pdfBytes));
        String pdfText = pdfTextExtractor.getTextFromPage(1, true);
        assertTrue(pdfText.contains("Order details"));
        assertTrue(pdfText.contains("Comment to the order"));
        assertTrue(pdfText.contains("Sender"));
        assertTrue(pdfText.contains(orderData.getSender().getSenderEmail()));
        assertTrue(pdfText.contains(orderData.getSender().getSenderPhone()));
        assertTrue(pdfText.contains(String.join(" ",
            orderData.getSender().getSenderName(),
            orderData.getSender().getSenderSurname())));
        assertTrue(pdfText.contains("The address of export of the ordered services"));
        assertTrue(pdfText.contains(orderData.getOrderStatusEn()));
        assertTrue(pdfText.contains("#"));
        assertTrue(pdfText.contains(orderData.getPaymentStatusEn()));
        assertTrue(pdfText.contains("Order date"));
        assertTrue(pdfText.contains(orderData.getBags().getFirst().getServiceEn()));
    }

    @Test
    void exportValidUaPdf() throws IOException {
        OrdersDataForUserDto orderData = ModelUtils.getOrdersDataForUserDto();
        byte[] pdfBytes = pdfFileExporter.export(orderData, Locale.of(AppConstant.LOCALE_UK_NAME));
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(new PdfReader(pdfBytes));
        String pdfText = pdfTextExtractor.getTextFromPage(1, true);
        assertTrue(pdfText.contains("Деталі замовлення"));
        assertTrue(pdfText.contains("Коментар до замовлення"));
        assertTrue(pdfText.contains("Відправник"));
        assertTrue(pdfText.contains(orderData.getSender().getSenderEmail()));
        assertTrue(pdfText.contains(orderData.getSender().getSenderPhone()));
        assertTrue(pdfText.contains(String.join(" ",
            orderData.getSender().getSenderName(),
            orderData.getSender().getSenderSurname())));
        assertTrue(pdfText.contains("Адреса вивезення відходів"));
        assertTrue(pdfText.contains(orderData.getOrderStatusUk()));
        assertTrue(pdfText.contains("№"));
        assertTrue(pdfText.contains(orderData.getPaymentStatusUk()));
        assertTrue(pdfText.contains("Дата оплати"));
        assertTrue(pdfText.contains(orderData.getBags().getFirst().getServiceUk()));
    }

    @Test
    void exportPdfWithEmptyComment() throws IOException {
        OrdersDataForUserDto orderData = ModelUtils.getOrdersDataForUserDtoWithNullComment();
        byte[] pdfBytes = pdfFileExporter.export(orderData, Locale.of(AppConstant.LOCALE_UK_NAME));
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(new PdfReader(pdfBytes));
        String pdfText = pdfTextExtractor.getTextFromPage(1, true);
        assertFalse(pdfText.contains("Коментар до замовлення"));
    }

    @Test
    void exportShouldThrowPdfFileExportingExceptionWhenIOExceptionOccurs() {
        OrdersDataForUserDto mockDto = mock(OrdersDataForUserDto.class);
        Locale mockLocale = mock(Locale.class);
        OrdersDataPdfFileExporterImpl spyExporter = spy(pdfFileExporter);
        doThrow(new PdfFileExportingException())
            .when(spyExporter)
            .export(mockDto, mockLocale);
        assertThrows(PdfFileExportingException.class, () -> spyExporter.export(mockDto, mockLocale));
    }
}