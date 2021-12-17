class Space {
  public boolean occupied = false;
  public color spaceFill = constants.defaultSpaceFill;
  public color spaceStroke = constants.defaultSpaceStroke;
  public boolean shadow = false;
  public color shadowFill = constants.defaultSpaceFill;
  public color shadowStroke = constants.defaultSpaceStroke;
  
  Space() {
  }
  Space(Space space) {
    this.occupied = space.occupied;
    this.spaceFill = space.spaceFill;
    this.spaceStroke = space.spaceStroke;
  }
  
  boolean getOccupied() {
    return this.occupied;
  }
  
  void setOccupied(boolean x) {
    this.occupied = x;
  }
  void setSpaceFill(color x) {
    this.spaceFill = x;
  }
  void setSpaceStroke(color x) {
    this.spaceStroke = x;
  }
  void setShadow(boolean x) {
    this.shadow = x;
  }
  void setShadowFill(color x) {
    this.shadowFill = x;
  }
  void setShadowStroke(color x) {
    this.shadowStroke = x;
  }
  
  void drawSpace(float xi, float yi, float sideLength) {
      fill(this.spaceFill);
      stroke(this.spaceStroke);
      square(xi, yi, sideLength);
    if (this.shadow) {
      fill(this.shadowFill, constants.shadowOpacity);
      stroke(this.shadowStroke);
      square(xi, yi, sideLength);
    }
  }
}
