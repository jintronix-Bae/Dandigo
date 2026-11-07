package Dandigo.SOS_map.global.infra.image;

import Dandigo.SOS_map.global.error.CustomException;
import Dandigo.SOS_map.global.error.ErrorCode;
import org.springframework.stereotype.Component;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

// 사진 형식 판별, 얼굴 영역 모자이크, 썸네일 생성 (jpg/png만 지원)
@Component
public class ImageProcessor {

    public static final String JPEG = "image/jpeg";
    public static final String PNG = "image/png";

    private static final int THUMBNAIL_MAX_WIDTH = 400;
    private static final float BOX_MARGIN_RATIO = 0.15f;   // 얼굴 영역을 조금 넓게 가린다
    private static final int MOSAIC_BLOCKS = 8;            // 얼굴 한 변을 8칸 정도로 뭉갠다
    private static final int DETECTION_MAX_BYTES = 4_500_000; // Rekognition 한도(5MB)보다 조금 작게
    private static final int DETECTION_MAX_SIDE = 2048;

    // 파일 앞부분(매직 넘버)으로 실제 형식을 판별한다. 지원하지 않으면 null
    public String detectContentType(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return JPEG;
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return PNG;
        }
        return null;
    }

    public static String extensionOf(String contentType) {
        return PNG.equals(contentType) ? "png" : "jpg";
    }

    public BufferedImage read(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
            }
            return image;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }
    }

    // 휴대폰 JPEG 사진은 "옆으로 저장 + 회전 정보(EXIF)"인 경우가 많다.
    // 다시 저장하면 회전 정보가 사라지므로, 업로드 시점에 픽셀 자체를 바로 세워 둔다.
    public byte[] normalizeOrientation(byte[] bytes, String contentType) {
        if (!JPEG.equals(contentType)) {
            return bytes;
        }
        int orientation = readOrientation(bytes);
        if (orientation <= 1 || orientation > 8) {
            return bytes;
        }
        BufferedImage source = read(bytes);
        int w = source.getWidth();
        int h = source.getHeight();
        boolean swap = orientation >= 5;   // 5~8은 가로·세로가 바뀐다

        // AffineTransform(m00, m10, m01, m11, m02, m12): x' = m00*x + m01*y + m02, y' = m10*x + m11*y + m12
        AffineTransform transform = switch (orientation) {
            case 2 -> new AffineTransform(-1, 0, 0, 1, w, 0);   // 좌우 반전
            case 3 -> new AffineTransform(-1, 0, 0, -1, w, h);  // 180도
            case 4 -> new AffineTransform(1, 0, 0, -1, 0, h);   // 상하 반전
            case 5 -> new AffineTransform(0, 1, 1, 0, 0, 0);    // 대각 반전
            case 6 -> new AffineTransform(0, 1, -1, 0, h, 0);   // 시계 방향 90도
            case 7 -> new AffineTransform(0, -1, -1, 0, h, w);  // 반대 대각 반전
            default -> new AffineTransform(0, -1, 1, 0, 0, w);  // 8: 반시계 방향 90도
        };

        BufferedImage rotated = new BufferedImage(swap ? h : w, swap ? w : h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rotated.createGraphics();
        g.drawImage(source, transform, null);
        g.dispose();
        return write(rotated, contentType);
    }

    private int readOrientation(byte[] bytes) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(bytes));
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                return directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        } catch (Exception e) {
            // 회전 정보를 못 읽으면 그대로 둔다
        }
        return 1;
    }

    // Rekognition은 5MB 이하 사진만 받는다. 크면 얼굴 감지용으로만 줄인 JPEG를 만든다.
    // 얼굴 위치는 비율(0~1)로 돌아오므로, 블러는 원래 크기 사진에 그대로 적용할 수 있다.
    public byte[] prepareForDetection(byte[] bytes) {
        if (bytes.length <= DETECTION_MAX_BYTES) {
            return bytes;
        }
        BufferedImage source = read(bytes);
        double scale = Math.min(1.0, (double) DETECTION_MAX_SIDE / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return write(resized, JPEG);
    }

    // 얼굴 영역들을 모자이크 처리한 새 사진을 만든다
    public byte[] mosaic(byte[] bytes, String contentType, List<RelativeBox> boxes) {
        BufferedImage source = read(bytes);
        BufferedImage result = copy(source, contentType);
        int imageWidth = result.getWidth();
        int imageHeight = result.getHeight();

        for (RelativeBox box : boxes) {
            int marginX = Math.round(box.width() * imageWidth * BOX_MARGIN_RATIO);
            int marginY = Math.round(box.height() * imageHeight * BOX_MARGIN_RATIO);
            int x = clamp(Math.round(box.left() * imageWidth) - marginX, 0, imageWidth - 1);
            int y = clamp(Math.round(box.top() * imageHeight) - marginY, 0, imageHeight - 1);
            int right = clamp(Math.round((box.left() + box.width()) * imageWidth) + marginX, x + 1, imageWidth);
            int bottom = clamp(Math.round((box.top() + box.height()) * imageHeight) + marginY, y + 1, imageHeight);
            pixelate(result, x, y, right - x, bottom - y);
        }
        return write(result, contentType);
    }

    public byte[] thumbnail(byte[] bytes, String contentType) {
        BufferedImage source = read(bytes);
        if (source.getWidth() <= THUMBNAIL_MAX_WIDTH) {
            return write(copy(source, contentType), contentType);
        }
        int width = THUMBNAIL_MAX_WIDTH;
        int height = Math.max(1, Math.round((float) source.getHeight() * width / source.getWidth()));
        BufferedImage resized = new BufferedImage(width, height, imageTypeFor(contentType));
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return write(resized, contentType);
    }

    private void pixelate(BufferedImage image, int x, int y, int width, int height) {
        int block = Math.max(4, Math.min(width, height) / MOSAIC_BLOCKS);
        for (int by = y; by < y + height; by += block) {
            for (int bx = x; bx < x + width; bx += block) {
                int bw = Math.min(block, x + width - bx);
                int bh = Math.min(block, y + height - by);
                int rgb = averageColor(image, bx, by, bw, bh);
                for (int py = by; py < by + bh; py++) {
                    for (int px = bx; px < bx + bw; px++) {
                        image.setRGB(px, py, rgb);
                    }
                }
            }
        }
    }

    private int averageColor(BufferedImage image, int x, int y, int width, int height) {
        long a = 0, r = 0, g = 0, b = 0;
        int count = width * height;
        for (int py = y; py < y + height; py++) {
            for (int px = x; px < x + width; px++) {
                int argb = image.getRGB(px, py);
                a += (argb >>> 24) & 0xFF;
                r += (argb >> 16) & 0xFF;
                g += (argb >> 8) & 0xFF;
                b += argb & 0xFF;
            }
        }
        return (int) ((a / count) << 24 | (r / count) << 16 | (g / count) << 8 | (b / count));
    }

    // JPEG는 투명도가 없는 RGB로, PNG는 투명도를 유지한 ARGB로 복사한다
    private BufferedImage copy(BufferedImage source, String contentType) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), imageTypeFor(contentType));
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    private int imageTypeFor(String contentType) {
        return PNG.equals(contentType) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
    }

    private byte[] write(BufferedImage image, String contentType) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, extensionOf(contentType), out)) {
                throw new IllegalStateException("이미지 인코더가 없습니다: " + contentType);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 저장 실패", e);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
