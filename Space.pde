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
