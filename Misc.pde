public String multiplyString(String string, int times) {
  String multipliedString = "";
  for (int i = 0; i < times; i++) {
     multipliedString += string;
  }
  return multipliedString;
}

public boolean isColor(String colorName) {
  switch(colorName) {
    case "blue":
    case "red":
    case "green":
    case "yellow":
    case "cyan":
    case "purple":
    case "orange":
    case "pink":
    case "gray":
    case "tan":
    case "black":
      return true;
    default:
      return false;
  }
}

public color stringToColor(String colorName) {
  switch(colorName) {
    case "blue":
      return color(0, 0, 255);
    case "red":
      return color(255, 0, 0);
    case "green":
      return color(0, 255, 0);
    case "yellow":
      return color(255, 255, 0);
    case "cyan":
      return color(0, 255, 255);
    case "fuchsia":
      return color(255, 0, 255);
    case "purple":
      return color(165, 0, 165);
    case "orange":
      return color(255, 165, 0);
    case "pink":
      return color(255, 105, 180);
    case "gray":
      return color(128, 128, 128);
    case "tan":
      return color(210, 180, 140);
    case "black":
      return color(30, 30, 30);
    default:
      return color(0);
  }
}
