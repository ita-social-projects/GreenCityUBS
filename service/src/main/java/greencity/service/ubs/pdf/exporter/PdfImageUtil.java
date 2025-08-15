package greencity.service.ubs.pdf.exporter;

import com.lowagie.text.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PdfImageUtil {
    public static Image convertBufferedImageToImage(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        return Image.getInstance(byteArrayOutputStream.toByteArray());
    }
}
