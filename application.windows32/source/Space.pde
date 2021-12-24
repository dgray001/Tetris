class Space {
  public boolean occupied = false;
  public boolean shadow = false;
  public Color spaceColor = constants.defaultSpaceColor;
  
  Space() {
  }
  Space(Space space) {
    this.occupied = space.occupied;
    this.shadow = space.shadow;
    this.spaceColor = space.spaceColor;
  }
  
  boolean getOccupied() {
    return this.occupied;
  }
  
  void setShadow(Color c) {
    this.shadow = true;
    this.spaceColor = c;
  }
  void removeShadow() {
    this.shadow = false;
    this.spaceColor = constants.defaultSpaceColor;
  }
  void setColor(Color c) {
    this.occupied = true;
    this.shadow = false;
    this.spaceColor = c;
  }
  void removeColor() {
    this.occupied = false;
    this.spaceColor = constants.defaultSpaceColor;
  }
  
  void drawSpace(float xi, float yi, float sideLength) {
    color fillColor = stringToColor(this.spaceColor.getColorName());
    if (!occupied && !shadow) {
      strokeWeight(0.5);
      fill(fillColor);
      stroke(constants.defaultSpaceStroke);
      if (!options.gridlines) {
        stroke(stringToColor(this.spaceColor.getColorName()));
      }
      square(xi, yi, sideLength);
      strokeWeight(1);
      return;
    }
    switch(options.pieceStyle) {
      case FLAT_NORMAL:
        if (this.shadow) {
          fill(fillColor, constants.shadowOpacity);
        }
        else {
          fill(fillColor);
        }
        stroke(constants.defaultPieceStroke);
        rectMode(CORNER);
        square(xi, yi, sideLength);
        break;
      case FLAT_SMOOTH:
        if (this.shadow) {
          fill(fillColor, constants.shadowOpacity);
          stroke(fillColor, 0);
        }
        else {
          fill(fillColor);
          stroke(fillColor);
        }
        rectMode(CORNER);
        square(xi, yi, sideLength);
        break;
      case FLAT_DYNAMIC:
        if (this.shadow) {
          fill(dynamicColorChanger(fillColor), constants.shadowOpacity);
        }
        else {
          fill(dynamicColorChanger(fillColor));
        }
        stroke(constants.defaultPieceStroke);
        rectMode(CORNER);
        square(xi, yi, sideLength);
        break;
      case FLAT_FADE:
        imageMode(CORNER);
        PImage image_2d_fade = null;
        switch(this.spaceColor) {
          case BLUE:
            image_2d_fade = constants.fade_2D_blue;
            break;
          case RED:
            image_2d_fade = constants.fade_2D_red;
            break;
          case GREEN:
            image_2d_fade = constants.fade_2D_green;
            break;
          case YELLOW:
            image_2d_fade = constants.fade_2D_yellow;
            break;
          case CYAN:
            image_2d_fade = constants.fade_2D_cyan;
            break;
          case FUCHSIA:
            image_2d_fade = constants.fade_2D_fuchsia;
            break;
          case PURPLE:
            image_2d_fade = constants.fade_2D_purple;
            break;
          case ORANGE:
            image_2d_fade = constants.fade_2D_orange;
            break;
          case TAN:
            image_2d_fade = constants.fade_2D_tan;
            break;
          case PINK:
            image_2d_fade = constants.fade_2D_pink;
            break;
          case GRAY:
            image_2d_fade = constants.fade_2D_gray;
            break;
          case BROWN:
            //image_2d_fade = constants.fade_2D_brown;
            break;
          case BLACK:
            image_2d_fade = constants.fade_2D_black;
            break;
          case WHITE:
            //image_2d_fade = constants.fade_2D_white;
            break;
          default:
            println("ERROR: piece color not recognized");
            break;
        }
        if (image_2d_fade != null) {
          if (this.shadow) {
            tint(255, constants.shadowOpacity);
          }
          else {
            tint(255);
          }
          image(image_2d_fade, xi, yi, sideLength, sideLength);
        }
        break;
      case RAISED_FADE:
        imageMode(CORNER);
        PImage image_3d_fade_soft = null;
        switch(this.spaceColor) {
          case BLUE:
            image_3d_fade_soft = constants.fade_3D_soft_blue;
            break;
          case RED:
            image_3d_fade_soft = constants.fade_3D_soft_red;
            break;
          case GREEN:
            image_3d_fade_soft = constants.fade_3D_soft_green;
            break;
          case YELLOW:
            image_3d_fade_soft = constants.fade_3D_soft_yellow;
            break;
          case CYAN:
            image_3d_fade_soft = constants.fade_3D_soft_cyan;
            break;
          case FUCHSIA:
            image_3d_fade_soft = constants.fade_3D_soft_fuchsia;
            break;
          case PURPLE:
            image_3d_fade_soft = constants.fade_3D_soft_purple;
            break;
          case ORANGE:
            image_3d_fade_soft = constants.fade_3D_soft_orange;
            break;
          case TAN:
            image_3d_fade_soft = constants.fade_3D_soft_tan;
            break;
          case PINK:
            image_3d_fade_soft = constants.fade_3D_soft_pink;
            break;
          case GRAY:
            image_3d_fade_soft = constants.fade_3D_soft_gray;
            break;
          case BROWN:
            //image_3d_fade_soft = constants.fade_3D_soft_brown;
            break;
          case BLACK:
            image_3d_fade_soft = constants.fade_3D_soft_black;
            break;
          case WHITE:
            //image_3d_fade_soft = constants.fade_3D_soft_white;
            break;
          default:
            println("ERROR: piece color not recognized");
            break;
        }
        if (image_3d_fade_soft != null) {
          if (this.shadow) {
            tint(255, constants.shadowOpacity);
          }
          else {
            tint(255);
          }
          image(image_3d_fade_soft, xi, yi, sideLength, sideLength);
        }
        break;
      case RAISED_SHARP:
        imageMode(CORNER);
        PImage image_3d_fade_sharp = null;
        switch(this.spaceColor) {
          case BLUE:
            image_3d_fade_sharp = constants.fade_3D_sharp_blue;
            break;
          case RED:
            image_3d_fade_sharp = constants.fade_3D_sharp_red;
            break;
          case GREEN:
            image_3d_fade_sharp = constants.fade_3D_sharp_green;
            break;
          case YELLOW:
            image_3d_fade_sharp = constants.fade_3D_sharp_yellow;
            break;
          case CYAN:
            image_3d_fade_sharp = constants.fade_3D_sharp_cyan;
            break;
          case FUCHSIA:
            image_3d_fade_sharp = constants.fade_3D_sharp_fuchsia;
            break;
          case PURPLE:
            image_3d_fade_sharp = constants.fade_3D_sharp_purple;
            break;
          case ORANGE:
            image_3d_fade_sharp = constants.fade_3D_sharp_orange;
            break;
          case TAN:
            image_3d_fade_sharp = constants.fade_3D_sharp_tan;
            break;
          case PINK:
            image_3d_fade_sharp = constants.fade_3D_sharp_pink;
            break;
          case GRAY:
            image_3d_fade_sharp = constants.fade_3D_sharp_gray;
            break;
          case BROWN:
            //image_3d_fade_sharp = constants.fade_3D_sharp_brown;
            break;
          case BLACK:
            image_3d_fade_sharp = constants.fade_3D_sharp_black;
            break;
          case WHITE:
            //image_3d_fade_sharp = constants.fade_3D_sharp_white;
            break;
          default:
            println("ERROR: piece color not recognized");
            break;
        }
        if (image_3d_fade_sharp != null) {
          if (this.shadow) {
            tint(255, constants.shadowOpacity);
          }
          else {
            tint(255);
          }
          image(image_3d_fade_sharp, xi, yi, sideLength, sideLength);
        }
        break;
      case RAISED_NORMAL:
        imageMode(CORNER);
        PImage image_3d_normal = null;
        switch(this.spaceColor) {
          case BLUE:
            image_3d_normal = constants.normal_3D_blue;
            break;
          case RED:
            image_3d_normal = constants.normal_3D_red;
            break;
          case GREEN:
            image_3d_normal = constants.normal_3D_green;
            break;
          case YELLOW:
            image_3d_normal = constants.normal_3D_yellow;
            break;
          case CYAN:
            image_3d_normal = constants.normal_3D_cyan;
            break;
          case FUCHSIA:
            image_3d_normal = constants.normal_3D_fuchsia;
            break;
          case PURPLE:
            image_3d_normal = constants.normal_3D_purple;
            break;
          case ORANGE:
            image_3d_normal = constants.normal_3D_orange;
            break;
          case TAN:
            image_3d_normal = constants.normal_3D_tan;
            break;
          case PINK:
            image_3d_normal = constants.normal_3D_pink;
            break;
          case GRAY:
            image_3d_normal = constants.normal_3D_gray;
            break;
          case BROWN:
            //image_3d_normal = constants.normal_3D_brown;
            break;
          case BLACK:
            image_3d_normal = constants.normal_3D_black;
            break;
          case WHITE:
            //image_3d_normal = constants.normal_3D_white;
            break;
          default:
            println("ERROR: piece color not recognized");
            break;
        }
        if (image_3d_normal != null) {
          if (this.shadow) {
            tint(255, constants.shadowOpacity);
          }
          else {
            tint(255);
          }
          image(image_3d_normal, xi, yi, sideLength, sideLength);
        }
        break;
      case RAISED_FAT:
        imageMode(CORNER);
        PImage image_3d_fat = null;
        switch(this.spaceColor) {
          case BLUE:
            image_3d_fat = constants.fat_3D_blue;
            break;
          case RED:
            image_3d_fat = constants.fat_3D_red;
            break;
          case GREEN:
            image_3d_fat = constants.fat_3D_green;
            break;
          case YELLOW:
            image_3d_fat = constants.fat_3D_yellow;
            break;
          case CYAN:
            image_3d_fat = constants.fat_3D_cyan;
            break;
          case FUCHSIA:
            image_3d_fat = constants.fat_3D_fuchsia;
            break;
          case PURPLE:
            image_3d_fat = constants.fat_3D_purple;
            break;
          case ORANGE:
            image_3d_fat = constants.fat_3D_orange;
            break;
          case TAN:
            image_3d_fat = constants.fat_3D_tan;
            break;
          case PINK:
            image_3d_fat = constants.fat_3D_pink;
            break;
          case GRAY:
            image_3d_fat = constants.fat_3D_gray;
            break;
          case BROWN:
            //image_3d_fat = constants.fat_3D_brown;
            break;
          case BLACK:
            image_3d_fat = constants.fat_3D_black;
            break;
          case WHITE:
            //image_3d_fat = constants.fat_3D_white;
            break;
          default:
            println("ERROR: piece color not recognized");
            break;
        }
        if (image_3d_fat != null) {
          if (this.shadow) {
            tint(255, constants.shadowOpacity);
          }
          else {
            tint(255);
          }
          image(image_3d_fat, xi, yi, sideLength, sideLength);
        }
        break;
      default:
        println("ERROR: piecetype not recognized.");
        break;
    }
  }
}
