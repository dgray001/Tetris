class listBar extends scrollBar {
  private int selected = -1; // item selected on list
  listBar(float xi, float yi, float xf, float yf) {
    super(xi, yi, xf, yf);
    this.setDBLCLK(true);
  }
  void setSEL(int i) {
    this.selected = i;
  } int getSEL() {
    return this.selected;
  } void select() {
    this.selected = this.getHIGH();
  }
}

class scrollButton extends recButton {
  private float yPress = 0;
  private int delCurr = 0;
  scrollButton(float xi, float yi, float xf, float yf) {
    super("", 1, xi, yi, xf, yf);
    this.setREB(false); // allows for repeated clicks
    this.setTMS(999999, 999999); // won't make repeated clicks
    this.setRND(0);
  }
  void click() { // only purpose is to set yPress
    this.yPress = mouseY;
  }
  void setLOC(int hidden, int curr, float yi, float yf, float bSize, int tSize) {
    float sizeMax = yf - yi - 2*bSize; // max size it can be
    float actSize = sizeMax - (hidden*tSize); // actual size decreases with each unseen line
    if (actSize < bSize) { // give it minimum size
      actSize = bSize;
    }
    float fractAbove = (float)curr / hidden; // fraction of hidden lines above scroll bar
    float yInit = yi + bSize + (sizeMax - actSize)*fractAbove;
    this.yPress += yInit + actSize/2.0 - getY(); // move yPress how much bar is moving
    this.setY(yInit, yInit + actSize); // move bar
    if (this.getCLK()) { // if clicking bar
      if (mouseY > this.yPress + tSize) {
        this.delCurr++;
        float Y = mouseY - 3*tSize;
        while (true) {
          if (Y > this.yPress) {
            this.delCurr++;
            Y -= 2*tSize;
          } else {
            break;
          }
        }
      } else if (mouseY < this.yPress - tSize) {
        this.delCurr--;
        float Y = mouseY + 3*tSize;
        while (true) {
          if (Y < this.yPress) {
            this.delCurr--;
            Y += 2*tSize;
          } else {
            break;
          }
        }
      }
    }
  }
  int resetDEL(int curr, int max) {
    int i = this.delCurr;
    if ((curr + this.delCurr > max)||(curr + this.delCurr < 0)) { // exceeding limits
      if (i > 0) { // set i to be max possible value
        i = max - curr;
      } else {
        i = 0 - curr;
      }
    }
    this.delCurr = 0;
    return i;
  }
}

abstract class scrollBar {
  private wheelButton upButton;
  private wheelButton downButton;
  private scrollButton barButton;
  private float buttonSize = 16;
  private float xInitial = 0;
  private float xFinal = 0;
  private float yInitial = 0;
  private float yFinal = 0;
  private float yPress = 0; // where you pressed on the bar
  private String[] strings = new String[0]; // strings printed in box
  private int textSize = 12; // default text size
  private int currStart = 0;
  private int maxStart = 0;
  private int highlighted = -1; // start with none highlighted
  private boolean inBox = false;
  private float delay = 0;
  private boolean doubleClick = true; // requires double click to select
  
  scrollBar(float xi, float yi, float xf, float yf) {
    this.xInitial = xi;
    this.xFinal = xf;
    this.yInitial = yi;
    this.yFinal = yf;
    this.maxStart = this.strings.length;
    this.upButton = new wheelButton(0, false, this.xFinal - this.buttonSize, this.yInitial, this.xFinal, this.yInitial + this.buttonSize, 0);
    this.downButton = new wheelButton(this.maxStart, true, this.xFinal - this.buttonSize, this.yFinal - this.buttonSize, this.xFinal, this.yFinal, 0);
    this.barButton = new scrollButton(this.xFinal - this.buttonSize, this.yInitial + buttonSize, this.xFinal, this.yFinal - this.buttonSize);
  }
  
  public void setBOX(float xi, float xf, float yi, float yf) {
    this.xInitial = xi;
    this.xFinal = xf;
    this.yInitial = yi;
    this.yFinal = yf;
  }
  public void setXI(float xi) {
    this.xInitial = xi;
  } public float getXI() {
    return this.xInitial;
  }
  public void setXF(float xf) {
    this.xFinal = xf;
  } public float getXF() {
    return this.xFinal;
  }
  public void setYI(float yi) {
    this.yInitial = yi;
  } public float getYI() {
    return this.yInitial;
  }
  public void setYF(float yf) {
    this.yFinal = yf;
  } public float getYF() {
    return this.yFinal;
  }
  public void setYP(float yp) {
    this.yPress = yp;
  } public float getYP() {
    return this.yPress;
  }
  public void setSTR(String[] strs) {
    this.strings = strs;
  } public void addSTR(String str) {
    this.strings = append(this.strings, str);
  } public void shortenSTR() {
    this.strings = shorten(this.strings);
  } public String[] getSTRS() {
    return this.strings;
  } public String getSTR(int i) { // return an element of the string array
    return this.strings[i];
  } public int getLENG() {
    return this.strings.length;
  }
  public void setCURR(int curr) {
    this.currStart = curr;
  } public int getCURR() {
    return this.currStart;
  } public void currP() {
    this.currStart = this.currStart + 1;
  } public void currM() {
    this.currStart = this.currStart - 1;
  }
  public void setMAX(int max) {
    this.maxStart = max;
  } public int getMAX() {
    return this.maxStart;
  }
  public void setHIGH(int high) {
    this.highlighted = high;
  } public int getHIGH() {
    return this.highlighted;
  }
  public void setIN(boolean b) {
    this.inBox = b;
  } public boolean getIN() {
    return this.inBox;
  }
  public void setDELAY(float dy) {
    this.delay = dy;
  } public float getDELAY() {
    return this.delay;
  }
  public void setDBLCLK(boolean b) {
    this.doubleClick = b;
  }
  
  public void update(float x, float y) {
    if (this.highlighted >= this.strings.length) {
      this.highlighted = -1;
    }
    if ((x > this.xInitial)&&(x < this.xFinal)&&(y > this.yInitial)&&(y < this.yFinal)) { // see if in box
      this.setIN(true);
    } else {
      this.setIN(false);
    }
    rectMode(CORNERS);
    stroke(0);
    fill(255);
    rect(this.xInitial, this.yInitial, this.xFinal, this.yFinal); // draw white box
    textSize(this.textSize);
    int numSeen = (int)Math.floor((this.yFinal - this.yInitial) / (this.textSize + 5)); // number of lines seen at a given time
    int numTotal = this.strings.length;
    int numHidden = numTotal - numSeen; // lines not seen at a given time
    if (numHidden < 0) {
      numHidden = 0;
      numSeen = numTotal;
      this.currStart = 0;
    }
    this.maxStart = numHidden;
    fill(0);
    textAlign(LEFT, TOP);
    float yi = this.yInitial;
    for (int i = this.currStart; i < this.currStart + numSeen; i++) {
      if (i == this.highlighted) {
        fill(170, 170, 170);
        text(this.strings[i], this.xInitial+2, yi+2);
        fill(0);
      } else {
        text(this.strings[i], this.xInitial+2, yi+2);
      }
      yi += this.textSize + 5;
    }
    if (numHidden > 0) {
      this.barButton.setLOC(numHidden, this.currStart, this.yInitial, this.yFinal, this.buttonSize, this.textSize); // places barButton in correct location
      this.upButton.update(x, y);
      this.downButton.update(x, y);
      this.barButton.update(x, y); // update buttons
      this.downButton.setLIM(this.maxStart);
      this.currStart += this.upButton.resetDEL(this.currStart);
      this.currStart += this.downButton.resetDEL(this.currStart);
      this.currStart += this.barButton.resetDEL(this.currStart, this.maxStart);
    }
  }
  
  public void mousePress() {
    if (this.inBox) {
      if ((this.maxStart == 0)||(mouseX < this.xFinal - this.buttonSize)) { // select item on list
        int selected = this.currStart + (int)((mouseY - this.yInitial)/(this.textSize + 10));
        if (this.highlighted == selected) {
          this.select();
        } else {
          this.highlighted = selected;
          if (!(doubleClick)) {
            this.select();
          }
        }
      }
    } else {
      this.highlighted = -1;
    }
    this.upButton.mousePress();
    this.downButton.mousePress();
    this.barButton.mousePress();
  }
  
  public void mouseRelease() {
    this.upButton.mouseRelease();
    this.downButton.mouseRelease();
    this.barButton.mouseRelease();
  }
  
  abstract void select();
  
  public void scroll(int e) { // allows for scrolling
    if (this.inBox) {
      while (true) {
        if (e > 0) {
          if (this.currStart < this.maxStart) {
            currP();
            e--;
          } else {
            break;
          }
        }  else if (e < 0) {
          if (this.currStart > 0) {
            currM();
            e++;
          } else {
            break;
          }
        } else {
          break;
        }
      }
    }
  }
}
