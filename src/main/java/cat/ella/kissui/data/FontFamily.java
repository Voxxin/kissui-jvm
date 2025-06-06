package cat.ella.kissui.data;

import java.net.URI;
import java.net.URL;

public class FontFamily {
  private final String name;
  private final URI path;

  public FontFamily(String name, String path) {
	 this.name = name;
	 this.path = URI.create((path.startsWith("/") ? "" : "/") + path + (path.endsWith("/") ? "" : "/"));
  }

	public Font get(Font.Weight weight) {
		return get(weight, false);
	}

  public Font get(Font.Weight weight, boolean italic) {
	 return switch (weight) {
		case Font.Weight.THIN -> thin(italic);
		case Font.Weight.EXTRA_LIGHT -> extraLight(italic);
		case Font.Weight.LIGHT -> light(italic);
		case Font.Weight.REGULAR -> regular(italic);
		case Font.Weight.MEDIUM -> medium(italic);
		case Font.Weight.SEMI_BOLD -> semiBold(italic);
		case Font.Weight.BOLD -> bold(italic);
		case Font.Weight.EXTRA_BOLD -> extraBold(italic);
		case Font.Weight.BLACK -> black(italic);
	 };
  }

  public Font regular(boolean italic) {
	 return this.load(Font.Weight.REGULAR, italic);
  }

  public Font bold(boolean italic) {
	 return this.load(Font.Weight.BOLD, italic);
  }

  public Font medium(boolean italic) {
	 return this.load(Font.Weight.MEDIUM, italic);
  }

  public Font light(boolean italic) {
	 return this.load(Font.Weight.LIGHT, italic);
  }

  public Font semiBold(boolean italic) {
	 return this.load(Font.Weight.SEMI_BOLD, italic);
  }

  public Font extraBold(boolean italic) {
	 return this.load(Font.Weight.EXTRA_BOLD, italic);
  }

  public Font black(boolean italic) {
	 return this.load(Font.Weight.BLACK, italic);
  }

  public Font extraLight(boolean italic) {
	 return this.load(Font.Weight.EXTRA_LIGHT, italic);
  }

  public Font thin(boolean italic) {
	 return this.load(Font.Weight.THIN, italic);
  }

  private String getStyle(Font.Weight weight, boolean italic) {
	 if (italic) {
		if (weight == Font.Weight.REGULAR) return "Italic";
		return weight + "Italic";
	 }

	 return "" + weight;
  }

  private Font load(Font.Weight weight, boolean italic) {
	 String style = getStyle(weight, italic);
	 String name = String.format("%s-%s.ttf", this.name, style);
	 String filePath = this.path.resolve(name).toString();

	 URL url = getClass().getResource(filePath.toLowerCase());
	 if (url != null) {
		return new Font(filePath, this, italic, weight);
	 } else {
		return new Font("/assets/kissui/fonts/JetBrainsMono/JetBrainsMono-Regular.ttf", this, italic, weight);
	 }
  }
}
