package cat.ella.kissui.data;

public record Font(String path, float lineSpacing, float letterSpacing, FontFamily family, Font.Weight weight, boolean italic) {
  public Font(String path, FontFamily family, boolean italic, Font.Weight weight) {
	 this(path, 1.2F, 0F, family, weight, italic);
  }

  @Override
  public String toString() {
	 return "Font{" +
				"path='" + path + '\'' +
				", lineSpacing=" + lineSpacing +
				", letterSpacing=" + letterSpacing +
				", weight=" + weight +
				", italic=" + italic +
				'}';
  }

  public enum Weight {
	 REGULAR(400, null),
	 BOLD(700, null),

	 MEDIUM(500, REGULAR),
	 LIGHT(300, REGULAR),

	 SEMI_BOLD(600, BOLD),
	 EXTRA_BOLD(800, BOLD),
	 BLACK(900, EXTRA_BOLD),

	 EXTRA_LIGHT(200, LIGHT),
	 THIN(100, EXTRA_LIGHT),
	 ;

	 public final int value;
	 public final Weight weight;

	 Weight(int value, Weight weight) {
		this.value = value;
		this.weight = weight;
	 }

	 public static Weight byWeight(int weight) {
		return switch (weight / 100) {
		  case 1 -> Weight.THIN;
		  case 2 -> Weight.EXTRA_LIGHT;
		  case 3 -> Weight.LIGHT;
		  case 5 -> Weight.MEDIUM;
		  case 6 -> Weight.SEMI_BOLD;
		  case 7 -> Weight.BOLD;
		  case 8 -> Weight.EXTRA_BOLD;
		  case 9 -> Weight.BLACK;
		  default -> Weight.REGULAR;
		};
	 }
  }
}
