package greencity.service.ubs.pdf.exporter;

import com.lowagie.text.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PdfImageUtil {
    private PdfImageUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Image convertBufferedImageToImage(BufferedImage bufferedImage) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            boolean written = ImageIO.write(bufferedImage, "png", out);
            if (!written) {
                throw new IOException("No ImageIO writer found for format: png");
            }
            return Image.getInstance(out.toByteArray());
        }
    }
}
