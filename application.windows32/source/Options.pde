class Options {
  private boolean gridlines;
  private String pieceType;
  private Color IFill;
  private Color JFill;
  private Color LFill;
  private Color OFill;
  private Color SFill;
  private Color TFill;
  private Color ZFill;
  
  Options(Constants constants) {
    this.gridlines = constants.defaultGridlines;
    this.pieceType = constants.defaultPieceType;
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

}
