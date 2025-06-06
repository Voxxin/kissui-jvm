package cat.ella.kissui.data;

import cat.ella.kissui.component.Component;
import cat.ella.kissui.unit.Vector2;

public class KImage extends Component {

    public KImage(String path) {
        super(new Vector2(0F, 0F), new Vector2(0F, 0F));

    }

    public KImage(String path, ImageType type) {
        super(new Vector2(0F, 0F), new Vector2(0F, 0F));

    }

    private ImageType imageType(String path) {
        String extension = path.split("\\.")[1];

        return switch (extension) {
            case "png", "jpg", "jpeg", "" -> ImageType.Raster;
            case "svg" -> ImageType.Vector;
            default -> ImageType.Unknown;
        };

    }


    public enum ImageType {
        Raster,
        Vector,
        Unknown
    }
}
