public enum PieceStyle {
  FLAT_NORMAL("2D normal"), FLAT_SMOOTH("2D smooth"), FLAT_FADE("2D fade"), FLAT_DYNAMIC("2D dynamic"), RAISED_FADE("3D Fade"), RAISED_SHARP("3D Fade Sharp"), RAISED_NORMAL("3D normal"), RAISED_FAT("3D fat");
  private static final List<PieceStyle> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
  private String style;
  private PieceStyle(String style) {
    this.style = style;
  }
  public static String[] getStyleList() {
    String[] styleList = new String[PieceStyle.VALUES.size()];
    for (int i = 0; i < PieceStyle.VALUES.size(); i++) {
      styleList[i] = PieceStyle.VALUES.get(i).getStyle();
    }
    return styleList;
  }
  public String getStyle() {
    return this.style;
  }
}

public enum Color {
  BLUE("blue"), RED("red"), GREEN("green"), YELLOW("yellow"), CYAN("cyan"), FUCHSIA("fuchsia"), PURPLE("purple"),
  ORANGE("orange"), PINK("pink"), TAN("tan"), GRAY("gray"), BROWN("brown"), BLACK("black"), WHITE("white"), DEEP_BLACK("deep_black");
  private static final List<Color> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
  private String name;
  private Color(String name) {
    this.name = name;
  }
  public String getColorName() {
    return this.name;
  }
}

class Options {
  private boolean gridlines;
  private PieceStyle pieceStyle;
  private Color IFill;
  private Color JFill;
  private Color LFill;
  private Color OFill;
  private Color SFill;
  private Color TFill;
  private Color ZFill;
  
  Options(Constants constants) {
    this.gridlines = constants.defaultGridlines;
    this.pieceStyle = constants.defaultPieceStyle;
    this.IFill = constants.defaultIFill;
    this.JFill = constants.defaultJFill;
    this.LFill = constants.defaultLFill;
    this.OFill = constants.defaultOFill;
    this.SFill = constants.defaultSFill;
    this.TFill = constants.defaultTFill;
    this.ZFill = constants.defaultZFill;
    
    String[] optionsFile = loadStrings("options.tetris");
    if (optionsFile == null) {
      return;
    }
    for (String line : optionsFile) {
      String[] splitLine = split(line, ":");
      if (splitLine.length < 2) {
        continue;
      }
      String option = trim(splitLine[1]);
      switch(trim(splitLine[0])) {
        case "Gridlines":
          if (option.equals("true")) {
            this.gridlines = true;
          }
          else if (option.equals("false")) {
            this.gridlines = false;
          }
          break;
        case "PieceStyle":
          try {
            this.pieceStyle = PieceStyle.VALUES.get(Arrays.asList(PieceStyle.getStyleList()).indexOf(option));
          } catch (Exception e) {
          }
          break;
        case "IFill":
          if (isColor(option)) {
            this.IFill = stringToColorEnum(option);
          }
          break;
        case "JFill":
          if (isColor(option)) {
            this.JFill = stringToColorEnum(option);
          }
          break;
        case "LFill":
          if (isColor(option)) {
            this.LFill = stringToColorEnum(option);
          }
          break;
        case "OFill":
          if (isColor(option)) {
            this.OFill = stringToColorEnum(option);
          }
          break;
        case "SFill":
          if (isColor(option)) {
            this.SFill = stringToColorEnum(option);
          }
          break;
        case "TFill":
          if (isColor(option)) {
            this.TFill = stringToColorEnum(option);
          }
          break;
        case "ZFill":
          if (isColor(option)) {
            this.ZFill = stringToColorEnum(option);
          }
          break;
        default:
          break;
      }
    }
  }
  
  void saveOptions() {
    PrintWriter optionsFile = createWriter("options.tetris");
    optionsFile.println("Gridlines: " + this.gridlines);
    optionsFile.println("PieceStyle: " + this.pieceStyle.getStyle());
    optionsFile.println("IFill: " + this.IFill.getColorName());
    optionsFile.println("JFill: " + this.JFill.getColorName());
    optionsFile.println("LFill: " + this.LFill.getColorName());
    optionsFile.println("OFill: " + this.OFill.getColorName());
    optionsFile.println("SFill: " + this.SFill.getColorName());
    optionsFile.println("TFill: " + this.TFill.getColorName());
    optionsFile.println("ZFill: " + this.ZFill.getColorName());
    optionsFile.flush();
    optionsFile.close();
  }
}
