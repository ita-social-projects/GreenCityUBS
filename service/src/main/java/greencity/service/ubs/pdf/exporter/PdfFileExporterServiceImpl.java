package greencity.service.ubs.pdf.exporter;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import greencity.constant.PdfExportingConstants;
import greencity.constant.pdf.*;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.exceptions.files.PdfFileExportingException;
import greencity.service.FileExporterService;
import greencity.service.ubs.UBSClientService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static greencity.constant.AppConstant.LOCALE_ENG_NAME;
import static greencity.constant.AppConstant.LOCALE_UA_NAME;
import static greencity.constant.PdfExportingConstants.*;
import static greencity.constant.pdf.PdfFileHeaders.*;
import static greencity.constant.pdf.PdfUnitsOfMeasurement.*;

@Service
@AllArgsConstructor
public class PdfFileExporterServiceImpl implements FileExporterService {
    private UBSClientService ubsClientService;

    @Override
    public Resource export(Long objectId, Locale locale, String uuid) {
        OrdersDataForUserDto orderToExport = ubsClientService.getOrderForUser(uuid, objectId);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();
            addHeader(PdfFileHeaders.getByLocale(ORDER_DETAILS, locale), document);
            addNewLine(document);
            addLineSeparator(document);
            PdfPTable tableOrderDetails = createTable(PdfOrderDetailsHeaders.getAllByLocale(locale),
                    DEFAULT_TABLE_HEADER_FONT_SIZE,
                    ORDER_DETAILS_TABLE_COLUMN_WIDTH);
            tableOrderDetails.setSpacingAfter(DEFAULT_SPACING_VALUE);
            tableOrderDetails.setSpacingBefore(DEFAULT_SPACING_VALUE);
            document.add(fillOrderInfoTable(orderToExport, locale, tableOrderDetails, DEFAULT_PARAGRAPH_FONT_SIZE));
            addLineSeparator(document);
            PdfPTable table = createTable(PdfOrderContentDetailsHeaders.getAllByLocale(locale),
                    DEFAULT_TABLE_HEADER_FONT_SIZE,
                    ORDER_CONTENT_TABLE_COLUMN_WIDTH);
            document.add(fillOrdersTable(orderToExport.getBags(), locale, table, DEFAULT_PARAGRAPH_FONT_SIZE));
            addNewLine(document);
            addLineSeparator(document);
            addFooter(orderToExport, locale, document);
            document.close();
            return new ByteArrayResource(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new PdfFileExportingException();
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
            table.addCell(createCell(parameter, DEFAULT_FONT_NAME, fontSize, true));
        }
        return table;
    }

    private PdfPTable fillOrderInfoTable(OrdersDataForUserDto orderInfo, Locale locale, PdfPTable table, int fontSize) {
        table.addCell(createCell(orderInfo.getId(), DEFAULT_FONT_NAME, fontSize, false));
        table.addCell(createCell(orderInfo.getDatePaid().format(DATE_FORMATTER), DEFAULT_FONT_NAME, fontSize, false));
        table.addCell(createCell(orderInfo.getDateForm().format(DATE_FORMATTER), DEFAULT_FONT_NAME, fontSize, false));
        if (locale.getLanguage().equals(LOCALE_ENG_NAME)) {
            table.addCell(createCell(orderInfo.getOrderStatusEng(), DEFAULT_FONT_NAME, fontSize, false));
            table.addCell(createCell(orderInfo.getPaymentStatusEng(), DEFAULT_FONT_NAME, fontSize, false));
        } else {
            table.addCell(createCell(orderInfo.getOrderStatus(), DEFAULT_FONT_NAME, fontSize, false));
            table.addCell(createCell(orderInfo.getPaymentStatus(), DEFAULT_FONT_NAME, fontSize, false));
        }
        table.addCell(createCell(orderInfo.getAmountBeforePayment(), DEFAULT_FONT_NAME, fontSize, false));
        table.addCell(createCell(orderInfo.getAmountBeforePayment(), DEFAULT_FONT_NAME, fontSize, false));
        return table;
    }

    private PdfPTable fillOrdersTable(List<BagForUserDto> bags, Locale locale, PdfPTable table, int fontSize) {
        for (BagForUserDto bag : bags) {
            table.addCell(createCell(Objects.equals(locale.getLanguage(), LOCALE_UA_NAME)
                    ? bag.getService() : bag.getServiceEng(), DEFAULT_FONT_NAME, fontSize, false));
            table.addCell(createCell(bag.getCapacity() + PdfUnitsOfMeasurement.getByLocale(VOLUME, locale), DEFAULT_FONT_NAME, fontSize, false));
            table.addCell(createCell(bag.getFullPrice() + PdfUnitsOfMeasurement.getByLocale(CURRENCY, locale), DEFAULT_FONT_NAME, fontSize, false));
            table.addCell(createCell(bag.getCount() + PdfUnitsOfMeasurement.getByLocale(UNITS, locale), DEFAULT_FONT_NAME, fontSize, false));
            table.addCell(createCell(bag.getTotalPrice() + PdfUnitsOfMeasurement.getByLocale(CURRENCY, locale), DEFAULT_FONT_NAME, fontSize, false));
        }
        table.setSpacingBefore(PdfExportingConstants.DEFAULT_PARAGRAPH_FONT_SIZE);
        return table;
    }

    private PdfPCell createCell(Object value, String fontName, Integer fontSize, boolean bold) {
        PdfPCell cell = new PdfPCell(new Paragraph(value.toString(),
                FontFactory.getFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, fontSize, bold ? Font.BOLD : 0)));
        cell.setBorderColor(Color.white);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(DEFAULT_SPACING_VALUE);
        cell.setMinimumHeight(20f);
        cell.setPadding(10);
        cell.setUseBorderPadding(true);
        return cell;
    }

    private void addFooter(OrdersDataForUserDto orderDetails, Locale locale, Document document) {
        addCommentSection(orderDetails, locale, document);
        addSenderInfo(orderDetails, locale, document);
        addSenderAddress(orderDetails, locale, document);
    }

    private void addCommentSection(OrdersDataForUserDto orderDetails, Locale locale, Document document) {
        if (orderDetails.getOrderComment() != null &&
                !orderDetails.getOrderComment().isEmpty() &&
                !orderDetails.getOrderComment().isBlank()) {
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
        if (Objects.equals(LOCALE_ENG_NAME, locale.getLanguage())) {
            addParagraph(document, orderDetails.getAddress().getAddressCityEng(),
                    DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
            addParagraph(document, orderDetails.getAddress().getAddressRegionEng(), DEFAULT_FONT_NAME,
                    DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
            addParagraph(document, String.join(" ", orderDetails.getAddress().getAddressStreetEng(),
                            orderDetails.getAddress().getHouseNumber()), DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE,
                    false, Element.ALIGN_LEFT);
            addParagraph(document, orderDetails.getAddress().getAddressDistinctEng(),
                    DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
        } else {
            addParagraph(document, orderDetails.getAddress().getAddressCity(),
                    DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
            addParagraph(document, orderDetails.getAddress().getAddressRegion(),
                    DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
            addParagraph(document, String.join(" ", orderDetails.getAddress().getAddressStreet(),
                            orderDetails.getAddress().getHouseNumber()), DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE,
                    false, Element.ALIGN_LEFT);
            addParagraph(document, orderDetails.getAddress().getAddressDistinct(),
                    DEFAULT_FONT_NAME, DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
        }
        addParagraph(document, String.join(" ",
                PdfAddressConstants.getByLocale(PdfAddressConstants.HOUSE_CORPUS_NUMBER, locale),
                orderDetails.getAddress().getHouseCorpus()), DEFAULT_FONT_NAME,
                DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
        addParagraph(document, String.join(" ",
                        PdfAddressConstants.getByLocale(PdfAddressConstants.ENTRANCE_NUMBER, locale),
                        orderDetails.getAddress().getEntranceNumber()), DEFAULT_FONT_NAME,
                DEFAULT_PARAGRAPH_FONT_SIZE, false, Element.ALIGN_LEFT);
    }

    private void addHeader(String text, Document document) {
        Paragraph paragraph = new Paragraph(text,
                FontFactory.getFont(DEFAULT_FONT_NAME, DEFAULT_HEADER_FONT_SIZE, Font.BOLD));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(DEFAULT_SPACING_VALUE);
        paragraph.setSpacingAfter(DEFAULT_SPACING_VALUE);
        document.add(paragraph);
    }

    private void addParagraph(Document document, String text, String fontName, Integer fontSize, boolean bold, int alignment) {
        Paragraph paragraph = new Paragraph(text, FontFactory.getFont(fontName, fontSize, bold ? Font.BOLD : 0));
        paragraph.setAlignment(alignment);
        document.add(paragraph);
    }
}
