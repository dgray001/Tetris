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

public color dynamicColorChanger(color c) {
  float c_r = c >> 16 & 0xFF;
  float c_g = c >> 8 & 0xFF;
  float c_b = c & 0xFF;
  float time = millis() / 3.0;
  if ((c_r + time) % (255 * 2) > 255) {
    c_r = 255 - (c_r + time) % 255;
  }
  else {
    c_r = (c_r + time) % 255;
  }
  if ((c_g + time) % (255 * 2) > 255) {
    c_g = 255 - (c_g + time) % 255;
  }
  else {
    c_g = (c_g + time) % 255;
  }
  if ((c_b + time) % (255 * 2) > 255) {
    c_b = 255 - (c_b + time) % 255;
  }
  else {
    c_b = (c_b + time) % 255;
  }
  color returnColor = color(c_r, c_g, c_b);
  return returnColor;
}

public color ColorToColor(Color c) {
  return stringToColor(c.getColorName());
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
      return color(255, 175, 0);
    case "pink":
      return color(255, 105, 180);
    case "gray":
      return color(128, 128, 128);
    case "tan":
      return color(210, 180, 140);
    case "black":
      return color(30, 30, 30);
    case "deep_black":
      return color(0, 0, 0);
    default:
      return color(0);
  }
}

public Color stringToColorEnum(String colorName) {
  switch(colorName) {
    case "blue":
      return Color.BLUE;
    case "red":
      return Color.RED;
    case "green":
      return Color.GREEN;
    case "yellow":
      return Color.YELLOW;
    case "cyan":
      return Color.CYAN;
    case "fuchsia":
      return Color.FUCHSIA;
    case "purple":
      return Color.PURPLE;
    case "orange":
      return Color.ORANGE;
    case "pink":
      return Color.PINK;
    case "gray":
      return Color.GRAY;
    case "tan":
      return Color.TAN;
    case "black":
      return Color.BLACK;
    default:
      return Color.BLACK;
  }
}
