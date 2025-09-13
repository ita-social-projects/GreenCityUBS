package greencity.service.ubs.pdf.exporter;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Chunk;
import com.lowagie.text.Paragraph;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import greencity.constant.pdf.PdfAddressConstants;
import greencity.constant.pdf.PdfFileHeaders;
import greencity.constant.pdf.PdfOrderDetailsHeaders;
import greencity.constant.pdf.PdfOrderContentDetailsHeaders;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.exceptions.exporting.pdf.PdfFileExportingException;
import greencity.service.ubs.file.export.FileExporter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import static greencity.constant.AppConstant.LOCALE_EN_NAME;
import static greencity.constant.AppConstant.LOCALE_UK_NAME;
import static greencity.constant.ErrorMessage.CANNOT_EXPORT_DATA_TO_PDF;
import static greencity.constant.pdf.PdfFileHeaders.ADDRESS_INFO;
import static greencity.constant.pdf.PdfFileHeaders.ORDER_COMMENT;
import static greencity.constant.pdf.PdfFileHeaders.SENDER_INFO;
import static greencity.constant.pdf.PdfFileHeaders.ORDER_DETAILS;

@Service
@AllArgsConstructor
public class OrdersDataPdfFileExporterImpl implements FileExporter<OrdersDataForUserDto> {
    private static final String DEFAULT_FONT_NAME = "Comic Sans MS";
    private static final Color DEFAULT_CELL_BACKGROUND_COLOR = Color.WHITE;
    private static final int DEFAULT_PARAGRAPH_FONT_SIZE = 10;
    private static final int DEFAULT_TABLE_HEADER_FONT_SIZE = 11;
    private static final int DEFAULT_HEADER_FONT_SIZE = 16;
    private static final DateTimeFormatter DATE_FORMATTER_UK = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_EN = DateTimeFormatter.ofPattern("MM.dd.yyyy");
    private static final int DEFAULT_SPACING_VALUE = 10;
    private static final float[] ORDER_DETAILS_TABLE_COLUMN_WIDTH = new float[] {50, 95, 100, 100, 80, 100, 80};
    private static final float[] ORDER_CONTENT_TABLE_COLUMN_WIDTH = new float[] {125, 120, 120, 120, 120};

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] export(OrdersDataForUserDto objectToWrite, Locale locale) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();
            document.setDocumentLanguage(locale.getLanguage());
            addHeader(PdfFileHeaders.getByLocale(ORDER_DETAILS, locale), document);
            addNewLine(document);
            addLineSeparator(document);
            PdfPTable tableOrderDetails = createTable(PdfOrderDetailsHeaders.getAllByLocale(locale),
                DEFAULT_TABLE_HEADER_FONT_SIZE,
                ORDER_DETAILS_TABLE_COLUMN_WIDTH);
            tableOrderDetails.setSpacingAfter(DEFAULT_SPACING_VALUE);
            tableOrderDetails.setSpacingBefore(DEFAULT_SPACING_VALUE);
            document.add(fillOrderInfoTable(objectToWrite, locale, tableOrderDetails, DEFAULT_PARAGRAPH_FONT_SIZE));
            addLineSeparator(document);
            PdfPTable table = createTable(PdfOrderContentDetailsHeaders.getAllByLocale(locale),
                DEFAULT_TABLE_HEADER_FONT_SIZE,
                ORDER_CONTENT_TABLE_COLUMN_WIDTH);
            document.add(fillOrdersTable(objectToWrite.getBags(), locale, table, DEFAULT_PARAGRAPH_FONT_SIZE));
            addNewLine(document);
            addLineSeparator(document);
            addFooter(objectToWrite, locale, document);
            document.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new PdfFileExportingException(CANNOT_EXPORT_DATA_TO_PDF, e);
        }
    }

    private void addLineSeparator(Document document) {
        LineSeparator lineSeparator = new LineSeparator();
        document.add(lineSeparator);
    }

    private void addNewLine(Document document) {
        document.add(Chunk.NEWLINE);
    }

    private PdfPTable createTable(List<String> parameters, int fontSize, float[] cellWidths) {
        PdfPTable table = new PdfPTable(parameters.size());
        table.setWidthPercentage(cellWidths, PageSize.A4);
        for (String parameter : parameters) {
            table.addCell(createCell(parameter, DEFAULT_FONT_NAME, fontSize, Color.LIGHT_GRAY, true));
        }
        return table;
    }

    private PdfPTable fillOrderInfoTable(OrdersDataForUserDto orderInfo, Locale locale, PdfPTable table, int fontSize) {
        table.addCell(createCell(orderInfo.getId(), DEFAULT_FONT_NAME, fontSize, DEFAULT_CELL_BACKGROUND_COLOR, false));
        table.addCell(createCell(orderInfo.getDatePaid().format(
            Objects.equals(LOCALE_UK_NAME, locale.getLanguage()) ? DATE_FORMATTER_UK : DATE_FORMATTER_EN),
            DEFAULT_FONT_NAME, fontSize, DEFAULT_CELL_BACKGROUND_COLOR, false));
        table.addCell(createCell(orderInfo.getDateForm().format(
            Objects.equals(LOCALE_UK_NAME, locale.getLanguage()) ? DATE_FORMATTER_UK : DATE_FORMATTER_EN),
            DEFAULT_FONT_NAME, fontSize, DEFAULT_CELL_BACKGROUND_COLOR, false));
        if (Objects.equals(LOCALE_EN_NAME, locale.getLanguage())) {
            table.addCell(createCell(orderInfo.getOrderStatusEn(), DEFAULT_FONT_NAME, fontSize,
                DEFAULT_CELL_BACKGROUND_COLOR, false));
            table.addCell(createCell(orderInfo.getPaymentStatusEn(), DEFAULT_FONT_NAME, fontSize,
                DEFAULT_CELL_BACKGROUND_COLOR, false));
        } else {
            table.addCell(createCell(orderInfo.getOrderStatusUk(), DEFAULT_FONT_NAME, fontSize,
                DEFAULT_CELL_BACKGROUND_COLOR, false));
            table.addCell(createCell(orderInfo.getPaymentStatusUk(), DEFAULT_FONT_NAME, fontSize,
                DEFAULT_CELL_BACKGROUND_COLOR, false));
        }
        table.addCell(createCell(orderInfo.getOrderFullPrice(),
            DEFAULT_FONT_NAME, fontSize, DEFAULT_CELL_BACKGROUND_COLOR, false));
        table.addCell(createCell(orderInfo.getAmountBeforePayment(),
            DEFAULT_FONT_NAME, fontSize, DEFAULT_CELL_BACKGROUND_COLOR, false));
        return table;
    }

    private PdfPTable fillOrdersTable(List<BagForUserDto> bags, Locale locale, PdfPTable table, int fontSize) {
        bags.stream().filter(bag -> bag.getCount() > 0).forEach(bag -> {
            table.addCell(createCell(Objects.equals(locale.getLanguage(), LOCALE_UK_NAME)
                ? bag.getServiceUk()
                : bag.getServiceEn(), DEFAULT_FONT_NAME, fontSize, DEFAULT_CELL_BACKGROUND_COLOR, false));
            table.addCell(createCell(
                bag.getCapacity(),
                DEFAULT_FONT_NAME, fontSize, DEFAULT_CELL_BACKGROUND_COLOR, false));
            table.addCell(createCell(bag.getFullPrice(),
                DEFAULT_FONT_NAME, fontSize, DEFAULT_CELL_BACKGROUND_COLOR, false));
            table.addCell(createCell(bag.getCount(),
                DEFAULT_FONT_NAME, fontSize, DEFAULT_CELL_BACKGROUND_COLOR, false));
            table.addCell(createCell(bag.getTotalPrice(),
                DEFAULT_FONT_NAME, fontSize, DEFAULT_CELL_BACKGROUND_COLOR, false));
        });
        table.setSpacingBefore(DEFAULT_PARAGRAPH_FONT_SIZE);
        return table;
    }

    private PdfPCell createCell(Object value, String fontName, Integer fontSize, Color cellColor, boolean bold) {
        PdfPCell cell = new PdfPCell(new Paragraph(value.toString(),
            FontFactory.getFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, fontSize, bold ? Font.BOLD : 0)));
        cell.setBorderColor(Color.black);
        cell.setBackgroundColor(cellColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(DEFAULT_SPACING_VALUE);
        cell.setMinimumHeight(DEFAULT_SPACING_VALUE * 2f);
        cell.setPadding(DEFAULT_SPACING_VALUE);
        cell.setUseBorderPadding(true);
        return cell;
    }

    private void addFooter(OrdersDataForUserDto orderDetails, Locale locale, Document document) {
        addCommentSection(orderDetails, locale, document);
        addSenderInfo(orderDetails, locale, document);
        addSenderAddress(orderDetails, locale, document);
    }

    private void addCommentSection(OrdersDataForUserDto orderDetails, Locale locale, Document document) {
        if (orderDetails.getOrderComment() != null
            && !orderDetails.getOrderComment().isEmpty()
            && !orderDetails.getOrderComment().isBlank()) {
            addHeader(PdfFileHeaders.getByLocale(ORDER_COMMENT, locale), document);
            addParagraph(document, orderDetails.getOrderComment(),
                DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
        }
    }

    private void addSenderInfo(OrdersDataForUserDto orderDetails, Locale locale, Document document) {
        addHeader(PdfFileHeaders.getByLocale(SENDER_INFO, locale), document);
        addParagraph(document, String.join(" ", orderDetails.getSender().getSenderName(),
            orderDetails.getSender().getSenderSurname()),
            DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
        addParagraph(document, orderDetails.getSender().getSenderPhone(), DEFAULT_FONT_NAME,
            DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
        addParagraph(document, orderDetails.getSender().getSenderEmail(), DEFAULT_FONT_NAME,
            DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
    }

    private void addSenderAddress(OrdersDataForUserDto orderDetails, Locale locale, Document document) {
        addHeader(PdfFileHeaders.getByLocale(ADDRESS_INFO, locale), document);
        if (Objects.equals(LOCALE_EN_NAME, locale.getLanguage())) {
            addParagraph(document, orderDetails.getAddress().getAddressCityEn(),
                DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
            addParagraph(document, orderDetails.getAddress().getAddressRegionEn(), DEFAULT_FONT_NAME,
                DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
            addParagraph(document, String.join(", ", orderDetails.getAddress().getAddressStreetEn(),
                orderDetails.getAddress().getHouseNumber()), DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE,
                false, Element.ALIGN_LEFT);
            addParagraph(document, orderDetails.getAddress().getAddressDistinctEn(),
                DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
        } else {
            addParagraph(document, orderDetails.getAddress().getAddressCityUk(),
                DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
            addParagraph(document, orderDetails.getAddress().getAddressRegionUk(),
                DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
            addParagraph(document, String.join(", ", orderDetails.getAddress().getAddressStreetUk(),
                orderDetails.getAddress().getHouseNumber()), DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE,
                false, Element.ALIGN_LEFT);
            addParagraph(document, orderDetails.getAddress().getAddressDistinctUk(),
                DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
        }
        if (Objects.nonNull(orderDetails.getAddress().getHouseCorpus())) {
            addParagraph(document, String.join(" ",
                PdfAddressConstants.getByLocale(PdfAddressConstants.HOUSE_CORPUS_NUMBER, locale),
                orderDetails.getAddress().getHouseCorpus()), DEFAULT_FONT_NAME,
                DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
            if (Objects.nonNull(orderDetails.getAddress().getEntranceNumber())) {
                addParagraph(document, String.join(" ",
                    PdfAddressConstants.getByLocale(PdfAddressConstants.ENTRANCE_NUMBER, locale),
                    orderDetails.getAddress().getEntranceNumber()), DEFAULT_FONT_NAME,
                    DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
            }
        }
    }

    private void addHeader(String text, Document document) {
        Paragraph paragraph = new Paragraph(text,
            FontFactory.getFont(DEFAULT_FONT_NAME, DEFAULT_HEADER_FONT_SIZE, Font.BOLD));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(DEFAULT_SPACING_VALUE);
        paragraph.setSpacingAfter(DEFAULT_SPACING_VALUE);
        document.add(paragraph);
    }

    private void addParagraph(Document document, String text, String fontName, Integer fontSize, boolean bold,
        int alignment) {
        Paragraph paragraph = new Paragraph(text, FontFactory.getFont(fontName, fontSize, bold ? Font.BOLD : 0));
        paragraph.setAlignment(alignment);
        document.add(paragraph);
    }
}