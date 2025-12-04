package greencity.service.ubs.pdf.exporter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.entity.order.Order;
import greencity.enums.pdf.PdfQrCodeText;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.exceptions.exporting.pdf.PdfFileExportingException;
import greencity.properties.RemoteWebClientProperties;
import greencity.repository.OrderRepository;
import greencity.service.ubs.payment.ProcessPaymentService;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class OrdersDataPdfFileExporterImplTest {
    @Mock
    private ProcessPaymentService processPaymentService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RemoteWebClientProperties remoteWebClientProperties;

    @InjectMocks
    private OrdersDataPdfFileExporterImpl pdfFileExporter;

    private void invokeAddQrCode(OrdersDataForUserDto dto, Document document, Locale locale) throws Exception {
        Method m = OrdersDataPdfFileExporterImpl.class
            .getDeclaredMethod("addQrCode", OrdersDataForUserDto.class, Document.class, Locale.class);
        m.setAccessible(true);
        m.invoke(pdfFileExporter, dto, document, locale);
    }

    @Test
    void exportValidEnPdf() throws IOException {
        OrdersDataForUserDto orderData = ModelUtils.getOrdersDataForUserDto();
        orderData.setPaymentLink("");
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
        orderData.setPaymentLink(" ");
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
        orderData.setPaymentLink("https://pay.example.com/invoice/TEST123");
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
    void addQrCode_whenSumIsZeroOrNegative_shouldShowAlreadyPaidMessage() throws Exception {
        long orderId = 1L;
        Locale locale = Locale.UK;

        OrdersDataForUserDto orderDetails = mock(OrdersDataForUserDto.class);
        when(orderDetails.getId()).thenReturn(orderId);
        when(orderDetails.getAmountBeforePayment()).thenReturn(0.0); // => sumInCoins = 0

        Order order = new Order();
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Document document = mock(Document.class);

        invokeAddQrCode(orderDetails, document, locale);

        ArgumentCaptor<Paragraph> paragraphCaptor = ArgumentCaptor.forClass(Paragraph.class);
        verify(document, times(1)).add(paragraphCaptor.capture());

        Paragraph paragraph = paragraphCaptor.getValue();
        String actualText = paragraph.getContent().trim();

        String expectedText =
            PdfQrCodeText.getByLocale(PdfQrCodeText.ALREADY_PAID, locale);

        assertEquals(expectedText, actualText);

        verify(document, never()).add(isA(PdfPTable.class));
    }

    @Test
    void addQrCode_whenSumPositive_shouldGenerateQrAndAddTable() throws Exception {
        long orderId = 2L;
        Locale locale = Locale.ENGLISH;

        OrdersDataForUserDto orderDetails = mock(OrdersDataForUserDto.class);
        when(orderDetails.getId()).thenReturn(orderId);
        when(orderDetails.getAmountBeforePayment()).thenReturn(123.45); // > 0

        Order order = new Order();
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(remoteWebClientProperties.getGreenCityUbsAddress()).thenReturn("/");

        Document document = mock(Document.class);

        BufferedImage fakeQrImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB);
        Image fakePdfImage = mock(Image.class);

        try (MockedStatic<QrCodeGenerator> qrCodeGenMock = mockStatic(QrCodeGenerator.class);
            MockedStatic<PdfImageUtil> pdfImageUtilMock = mockStatic(PdfImageUtil.class)) {

            qrCodeGenMock
                .when(() -> QrCodeGenerator.generateQrCodeImage(anyString(), eq(150), eq(150)))
                .thenReturn(fakeQrImage);

            pdfImageUtilMock
                .when(() -> PdfImageUtil.convertBufferedImageToImage(fakeQrImage))
                .thenReturn(fakePdfImage);

            invokeAddQrCode(orderDetails, document, locale);

            verify(document, atLeastOnce()).add(isA(PdfPTable.class));

            ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);

            qrCodeGenMock.verify(
                () -> QrCodeGenerator.generateQrCodeImage(linkCaptor.capture(), eq(150), eq(150)));

            String actualLink = linkCaptor.getValue();

            String expectedLink = UriComponentsBuilder
                .fromPath("/")
                .path("ubs/redirect/{orderId}")
                .buildAndExpand(orderId)
                .toUriString();

            assertEquals(expectedLink, actualLink);

            verify(document, never()).add(isA(Paragraph.class));
        }
    }
}