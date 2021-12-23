abstract class button {
  private String message; // text on button
  private int tSize; // size of text
  private color dColor = color(220, 220, 220); // default color
  private color tColor = color(0); // text color
  private color mColor = color(170, 170, 170); // mouseOn color
  private color cColor = color(120, 120, 120); // click color
  private boolean mouseOn = false; // when cursor is overtop button
  private boolean clicked = false; // when being clicked
  private boolean releaseButton = false; // true if need to release on button to click it, and false if only need to press
  private boolean activated = false; // allows repeated clicks when holding down a non-release button
  private boolean extendActivate = false; // allows for repeated clicks when mosueOn becomes false
  private boolean disabled = false;
  private boolean mouseOnText = false;
  private String mouseOnMessage = "";
  private long ms = 0; // timer to allow for multiple clicks
  private long tm1 = 600; // ms to activate non-release button
  private long tm2 = 200; // frequency once button is activated
  
  button(String mes, int size) {
    this.message = mes;
    this.tSize = size;
  }
  
  void changeColor(color dC, color tC, color mC, color cC) {
    this.dColor = dC;
    this.tColor = tC;
    this.mColor = mC;
    this.cColor = cC;
  }
  void defaultColors() {
  this.dColor = color(220, 220, 220);
  this.tColor = color(0);
  this.mColor = color(170, 170, 170);
  this.cColor = color(120, 120, 120);
  }
  
  void setMES(String mes) {
    this.message = mes;
  } void setMOMES(String mes) {
    this.mouseOnMessage = mes;
  }
  void setACT(boolean b) {
    this.activated = b;
  } void setMON(boolean b) {
    this.mouseOn = b;
  } boolean getMON() {
    return this.mouseOn;
  } void setCLK(boolean b) {
    this.clicked = b;
  } boolean getCLK() {
    return this.clicked;
  } void setREB(boolean b) {
    this.releaseButton = b;
  } void setEAC(boolean b) {
    this.extendActivate = b;
  } void setDIS(boolean b) {
    this.disabled = b;
  } void setMOT(boolean b) {
    this.mouseOnText = b;
  }
  void setMS(long l) {
    this.ms = l;
  } void setTMS(long i, long j) {
    this.tm1 = i;
    this.tm2 = j;
  }
  
  abstract void drawBut();
  abstract void mouseUpdate(float x, float y);
  abstract void click(); // function of button
  
  void update(float mX, float mY) {
    this.drawBut();
    this.mouseUpdate(mX, mY);
    if ((!(this.releaseButton))&&(this.clicked)&&((this.mouseOn)||(this.extendActivate))) { // allows for repeated clicks for non-release buttons
      if (this.activated) { // already activated
        if (millis() - this.ms > this.tm2) {
          this.click();
          this.setMS(millis());
        }
      } else { // second click
        if (millis() - this.ms > this.tm1) {
          this.setACT(true);
          this.click();
          this.setMS(millis());
        }
      }
    }
  }
  
  void mousePress() {
    if (this.mouseOn && !this.disabled) {
      this.setCLK(true);
      if (!(this.releaseButton)) {
        this.click();
        this.setMS(millis()); // start timer for repeated clicks
      }
    }
  }
  
  void mouseRelease() {
    this.setACT(false);
    if ((this.releaseButton)&&(this.mouseOn)&&(this.clicked)&&(!this.disabled)) {
      this.click();
      this.setCLK(false);
    } else {
      this.setCLK(false);
    }
  }
}

abstract class recButton extends button {
  private float xInitial;
  private float yInitial;
  private float xFinal;
  private float yFinal;
  private int roundness = 8; // default roundness of recButtons
  
  recButton(String mes, int size, float xi, float yi, float xf, float yf) {
    super(mes, size);
    this.xInitial = xi;
    this.yInitial = yi;
    this.xFinal = xf;
    this.yFinal = yf;
  }
  
  void setRND(int i) {
    this.roundness = i;
  }
  float getX() {
    return this.xInitial + (this.xFinal - this.xInitial)/2.0; // returns center of x-value
  } void setX(float xi, float xf) {
    this.xInitial = xi;
    this.xFinal = xf;
  } float getY() {
    return this.yInitial + (this.yFinal - this.yInitial)/2.0;
  } void setY(float yi, float yf) {
    this.yInitial = yi;
    this.yFinal = yf;
  }
  
  void drawBut() {
    stroke(0);
    String mes = super.message;
    if (super.mouseOn) {
      if (super.mouseOnText) {
        mes = super.mouseOnMessage;
      }
      if (super.clicked) {
        fill(super.cColor);
      } else {
        fill(super.mColor);
      }
    } else {
      fill(super.dColor);
    }
    rectMode(CORNERS);
    rect(this.xInitial, this.yInitial, this.xFinal, this.yFinal, this.roundness);
    fill(super.tColor);
    textSize(super.tSize);
    textAlign(CENTER, CENTER);
    text(mes, this.xInitial + (this.xFinal-this.xInitial)/2.0, this.yInitial + (this.yFinal-this.yInitial)/2.0);
  }
  
  void mouseUpdate(float mX, float mY) {
    if ((mX > this.xInitial)&&(mX < this.xFinal)&&(mY > this.yInitial)&&(mY < this.yFinal)) {
      this.setMON(true);
    } else {
      this.setMON(false);
    }
  }
}

abstract class circButton extends button {
  private float xCenter;
  private float yCenter;
  private float radius;
  
  circButton(String mes, int size, float xC, float yC, float r) {
    super(mes, size);
    this.xCenter = xC;
    this.yCenter = yC;
    this.radius = r;
  }
  
  void drawBut() {
    stroke(0);
    String mes = super.message;
    if (super.mouseOn) {
      if (super.mouseOnText) {
        mes = super.mouseOnMessage;
      }
      if (super.clicked) {
        fill(super.cColor);
      } else {
        fill(super.mColor);
      }
    } else {
      fill(super.dColor);
    }
    ellipseMode(RADIUS);
    ellipse(this.xCenter, this.yCenter, this.radius, this.radius);
    fill(super.tColor);
    textSize(super.tSize);
    textAlign(CENTER, CENTER);
    text(mes, this.xCenter, this.yCenter);
  }
  
  void mouseUpdate(float mX, float mY) {
    double dr = Math.sqrt((mX - this.xCenter)*(mX - this.xCenter) + (mY - this.yCenter)*(mY - this.yCenter));
    if (dr < this.radius) {
      this.setMON(true);
    } else {
      this.setMON(false);
    }
  }
}

class RBclick extends recButton { // general click rectangular button
  private boolean clicked = false;
  RBclick(String mes, int size, float xi, float yi, float xf, float yf, int round) {
    super(mes, size, xi, yi, xf, yf);
    this.setREB(false);
    this.setTMS(999999, 999999);
    this.setRND(round);
  }
  void click() {
    this.clicked = true;
  }
  void doneAction() {
    this.clicked = false;
  }
  boolean getCLKED() {
    return this.clicked;
  }
}

class RBrep extends recButton {
  private int clicked = 0;
  RBrep(String mes, int size, float xi, float yi, float xf, float yf, int round) {
    super(mes, size, xi, yi, xf, yf);
    this.setREB(false);
    this.setRND(round);
  }
  void click() {
    this.clicked++;
  }
  void doneAction() {
    this.clicked--;
  }
  void resetClick() {
    this.clicked = 0;
  }
  int getCLKED() {
    return this.clicked;
  }
}

class wheelButton extends recButton { // to increment things to a max value
  private int limit = 0; // can be a max or a min
  private int delCurr = 0;
  private boolean max; // true if max, false if min
  wheelButton(int lim, boolean mx, float xi, float yi, float xf, float yf, int round) {
    super("", 1, xi, yi, xf, yf);
    this.setREB(false);
    this.setTMS(300, 80);
    this.setRND(round);
    this.limit = lim;
    this.max = mx;
  }
  void setLIM(int i) {
    this.limit = i;
  }
  void click() {
    if (max) {
      this.delCurr++;
    } else {
      this.delCurr--;
    }
  }
  int resetDEL(int curr) {
    int i = this.delCurr;
    if (((max)&&(curr+this.delCurr > this.limit))||((!(max))&&(curr+this.delCurr < this.limit))) { // exceeding limit
      i = 0;
    }
    this.delCurr = 0;
    return i;
  }
}
