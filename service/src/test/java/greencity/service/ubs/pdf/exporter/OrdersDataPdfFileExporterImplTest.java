package greencity.service.ubs.pdf.exporter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.constant.pdf.PdfQrCodeText;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.exceptions.exporting.pdf.PdfFileExportingException;
import greencity.repository.OrderRepository;
import greencity.service.ubs.payment.ProcessPaymentService;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrdersDataPdfFileExporterImplTest {
    @Mock
    private ProcessPaymentService processPaymentService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrdersDataPdfFileExporterImpl pdfFileExporter;

    @Test
    void exportValidEnPdf() throws IOException {
        OrdersDataForUserDto orderData = ModelUtils.getOrdersDataForUserDto();
        when(orderRepository.findById(anyLong()))
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
        when(orderRepository.findById(anyLong()))
            .thenReturn(Optional.of(mock(greencity.entity.order.Order.class)));
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
        when(orderRepository.findById(anyLong()))
            .thenReturn(Optional.of(mock(greencity.entity.order.Order.class)));
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

    @Test
    void exportWhenAmountIsZerShowsAlreadyPaidNoLinks() throws Exception {
        OrdersDataForUserDto dto = ModelUtils.getOrdersDataForUserDto();
        dto.setAmountBeforePayment(0.0);
        when(orderRepository.findById(anyLong()))
            .thenReturn(Optional.of(mock(greencity.entity.order.Order.class)));
        byte[] pdf = pdfFileExporter.export(dto, Locale.of("uk"));
        var text = new PdfTextExtractor(new PdfReader(pdf)).getTextFromPage(1, true);
        assertTrue(text.contains(PdfQrCodeText.getByLocale(PdfQrCodeText.ALREADY_PAID, Locale.of("uk"))));
    }

    @Test
    void exportWhenLinkBlankShowsLinkNotGenerated() throws Exception {
        var dto = ModelUtils.getOrdersDataForUserDto();
        dto.setAmountBeforePayment(10.0);
        when(orderRepository.findById(anyLong()))
            .thenReturn(Optional.of(mock(greencity.entity.order.Order.class)));
        when(processPaymentService.formedLink(any(), anyLong())).thenReturn("   ");

        byte[] pdf = pdfFileExporter.export(dto, Locale.ENGLISH);
        var text = new PdfTextExtractor(new PdfReader(pdf)).getTextFromPage(1, true);

        assertTrue(text.contains(PdfQrCodeText.getByLocale(PdfQrCodeText.LINK_NOT_GENERATED, Locale.ENGLISH)));
    }

    @Test
    void export_whenLinkPresent_addsQrHint_andCallsFormedLink() throws Exception {
        OrdersDataForUserDto dto = ModelUtils.getOrdersDataForUserDto();
        dto.setAmountBeforePayment(10.00);
        when(orderRepository.findById(anyLong()))
            .thenReturn(Optional.of(mock(greencity.entity.order.Order.class)));
        String url = "https://pay.example.com/invoice/TEST123";
        when(processPaymentService.formedLink(any(), anyLong())).thenReturn(url);
        Locale locale = Locale.ENGLISH;
        byte[] pdf = pdfFileExporter.export(dto, locale);
        String pageText = new PdfTextExtractor(new PdfReader(pdf)).getTextFromPage(1, true);
        String expectedHint = PdfQrCodeText.getByLocale(PdfQrCodeText.QR_CODE_HINT, locale);
        assertTrue(pageText.contains(expectedHint));
        verify(processPaymentService).formedLink(any(), anyLong());
    }
}