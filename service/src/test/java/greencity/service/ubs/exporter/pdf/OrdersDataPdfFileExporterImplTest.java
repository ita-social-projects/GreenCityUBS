package greencity.service.ubs.exporter.pdf;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.dto.order.OrdersDataForUserDto;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import greencity.service.ubs.pdf.exporter.OrdersDataPdfFileExporterImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.Locale;

class OrdersDataPdfFileExporterImplTest {
    private OrdersDataPdfFileExporterImpl pdfFileExporter;

    @BeforeEach
    void setUp() {
        pdfFileExporter = new OrdersDataPdfFileExporterImpl();
    }

    @Test
    void exportValidEnPdf() throws IOException {
        OrdersDataForUserDto orderData = ModelUtils.getOrdersDataForUserDto();
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
        assertTrue(pdfText.contains(orderData.getOrderStatusEng()));
        assertTrue(pdfText.contains("#"));
        assertTrue(pdfText.contains(orderData.getPaymentStatusEng()));
        assertTrue(pdfText.contains("Order date"));
        assertTrue(pdfText.contains(orderData.getBags().getFirst().getServiceEng()));
    }

    @Test
    void exportValidUaPdf() throws IOException {
        OrdersDataForUserDto orderData = ModelUtils.getOrdersDataForUserDto();
        byte[] pdfBytes = pdfFileExporter.export(orderData, Locale.of(AppConstant.LOCALE_UA_NAME));
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
        assertTrue(pdfText.contains(orderData.getOrderStatus()));
        assertTrue(pdfText.contains("№"));
        assertTrue(pdfText.contains(orderData.getPaymentStatus()));
        assertTrue(pdfText.contains("Дата оплати"));
        assertTrue(pdfText.contains(orderData.getBags().getFirst().getService()));
    }

    @Test
    void exportPdfWithEmptyComment() throws IOException {
        OrdersDataForUserDto orderData = ModelUtils.getOrdersDataForUserDtoWithNullComment();
        byte[] pdfBytes = pdfFileExporter.export(orderData, Locale.of(AppConstant.LOCALE_UA_NAME));
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(new PdfReader(pdfBytes));
        String pdfText = pdfTextExtractor.getTextFromPage(1, true);
        assertFalse(pdfText.contains("Коментар до замовлення"));
    }
}