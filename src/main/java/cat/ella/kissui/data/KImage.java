package cat.ella.kissui.data;

import cat.ella.kissui.component.Component;
import cat.ella.kissui.unit.Vector2;
import cat.ella.kissui.util.IOUtility;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class KImage extends Component {
    protected Vector2 size = Vector2.ZERO;
    public final String path;
    public final ImageType imageType;

    public KImage(String path) {
        this(path, Vector2.ZERO);
    }

    public KImage(String path, Vector2 size) {
        super(Vector2.ZERO, size);
        this.path = ((path.startsWith("/") || path.startsWith("http")) ? "" : "/") + path;
        this.imageType = imageType(path);
    }

    @Override
    public void setSize(Vector2 value) {
        if (!value.isPositive()) throw new IllegalArgumentException("Size must be positive (" + value + ")");
        if (size.isPositive()) throw new IllegalStateException("Size already set to " + size + " (new: " + value + ")");
        size = value;
    }

    private ImageType imageType(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);
        String extension = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1).split("\\?")[0] : "";

        return switch (extension.toLowerCase()) {
            case "png", "jpg", "jpeg" -> ImageType.Raster;
            case "svg" -> ImageType.Vector;
            default -> ImageType.Unknown;
        };
    }


    public ImageType getImageType() {
        return imageType;
    }

    public String getPath() {
        return path;
    }

    public InputStream getData() {
        try {
            return IOUtility.getResourceStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public enum ImageType {
        Raster,
        Vector,
        Unknown
    }


    public static void setSize(KImage image, Vector2 size) {
        image.setSize(size);
    }

    public static String getPath(KImage image) {
        return image.path;
    }
}
