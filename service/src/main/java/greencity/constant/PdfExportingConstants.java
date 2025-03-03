package greencity.constant;

import java.awt.*;
import java.time.format.DateTimeFormatter;

public class PdfExportingConstants {
    public static final String DEFAULT_FONT_NAME = "Comic Sans MS";
    public static final Color DEFAULT_CELL_BACKGROUND_COLOR = Color.WHITE;
    public static final int DEFAULT_PARAGRAPH_FONT_SIZE = 10;
    public static final int DEFAULT_TABLE_HEADER_FONT_SIZE = 11;
    public static final int DEFAULT_HEADER_FONT_SIZE = 16;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final int DEFAULT_SPACING_VALUE = 10;
    public static final float[] ORDER_DETAILS_TABLE_COLUMN_WIDTH = new float[]{50, 95, 100, 100, 80, 100, 80};
    public static final float[] ORDER_CONTENT_TABLE_COLUMN_WIDTH = new float[]{125, 120, 120, 120, 120};
    private PdfExportingConstants() {
    }
}
