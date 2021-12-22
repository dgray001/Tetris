class Options {
  private boolean gridlines;
  private color IFill;
  private color JFill;
  private color LFill;
  private color OFill;
  private color SFill;
  private color TFill;
  private color ZFill;
  
  Options(Constants constants) {
    this.gridlines = constants.defaultGridlines;
    this.IFill = constants.IFill;
    this.JFill = constants.JFill;
    this.LFill = constants.LFill;
    this.OFill = constants.OFill;
    this.SFill = constants.SFill;
    this.TFill = constants.TFill;
    this.ZFill = constants.ZFill;
    
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
          continue;
        case "IFill":
          if (isColor(option)) {
            this.IFill = stringToColor(option);
          }
          continue;
        case "JFill":
          if (isColor(option)) {
            this.JFill = stringToColor(option);
          }
          continue;
        case "LFill":
          if (isColor(option)) {
            this.LFill = stringToColor(option);
          }
          continue;
        case "OFill":
          if (isColor(option)) {
            this.OFill = stringToColor(option);
          }
          continue;
        case "SFill":
          if (isColor(option)) {
            this.SFill = stringToColor(option);
          }
          continue;
        case "TFill":
          if (isColor(option)) {
            this.TFill = stringToColor(option);
          }
          continue;
        case "ZFill":
          if (isColor(option)) {
            this.ZFill = stringToColor(option);
          }
          continue;
        default:
          continue;
      }
    }
  }

}
