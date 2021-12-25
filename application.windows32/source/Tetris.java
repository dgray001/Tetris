import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 
import javafx.util.*; 
import static javax.swing.JFrame.*; 
import static javax.swing.JOptionPane.*; 
import static javax.swing.JPanel.*; 
import java.net.*; 
import java.io.*; 
import processing.net.*; 
import processing.sound.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Tetris extends PApplet {











Constants constants = new Constants();
Options options;
CurrGame currGame = new CurrGame(this);
int frameTimer = millis();
int frameCounter = frameCount;
float lastFPS = constants.maxFPS;

public void setup() {
  
  frameRate(constants.maxFPS);
  background(constants.defaultBackgroundColor);
  constants.loadImages();
  options = new Options(constants);
  fill(0);
  textSize(14);
  textAlign(LEFT, BOTTOM);
  text("Customize", 1240, 707);
  textSize(12);
  textAlign(RIGHT, TOP);
  text(constants.version, 1325, 5);
  currGame.initiateUser();
}

public void draw() {
  currGame.update();
  // FPS counter
  if (millis() - frameTimer > constants.frameUpdateTime) {
    lastFPS = (constants.frameAverageCache * lastFPS + PApplet.parseFloat(frameCount - frameCounter) * (1000.0f / constants.frameUpdateTime)) / (constants.frameAverageCache + 1);
    textSize(12);
    textAlign(LEFT, TOP);
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rectMode(CORNERS);
    rect(5, 5, 50, 25);
    fill(0);
    text(PApplet.parseInt(lastFPS) + " FPS", 5, 5);
    frameCounter = frameCount + 1;
    frameTimer = millis();
  }
}

public void keyPressed() {
  currGame.keyPress();
}

public void mousePressed() {
  currGame.mousePress();
}

public void mouseReleased() {
  currGame.mouseRelease();
}

public void mouseWheel(MouseEvent event) {
  currGame.scroll(event.getCount());
}

// Occurs when client connects to server
public void serverEvent(Server someServer, Client someClient) {
  currGame.clientConnects(someClient);
}

// Occurs when client disconnects from server
public void disconnectEvent(Client someClient) {
  currGame.clientDisconnects(someClient);
}

// Occurs when server sends a value to an existing client
public void clientEvent(Client someClient) {
  currGame.clientEvent(someClient);
}
class Board {
  private Space[][] spaces;
  private Piece piece;
  float xi = 0;
  float yi = 0;
  float xf = 0;
  float yf = 0;
  boolean pieceOverflow = false;
  ArrayList<VisualEffect> visualEffects = new ArrayList<VisualEffect>();
  
  Board(float xi, float yi, float xf, float yf) {
    this.spaces = new Space[constants.defaultBoardColumns][constants.defaultBoardRows];
    for (int i = 0; i < this.spaces.length; i++) {
      for (int j = 0; j < this.spaces[0].length; j++) {
        this.spaces[i][j] = new Space();
      }
    }
    this.xi = xi;
    this.yi = yi;
    this.xf = xf;
    this.yf = yf;
    this.drawBoard();
  }
  
  public Piece getPiece() {
    return this.piece;
  }
  public boolean getPieceOverflow() {
    return this.pieceOverflow;
  }
  public void setBoardLocation(float xi, float yi, float xf, float yf) {
    this.xi = xi;
    this.yi = yi;
    this.xf = xf;
    this.yf = yf;
  }
  
  public void drawBoard() {
    float squareSize = min((this.xf - this.xi) / (this.spaces.length + 2), (this.yf - this.yi) / (this.spaces[0].length + 2));
    rectMode(CORNERS);
    fill(constants.boardBackground);
    stroke(constants.boardBackground);
    rect(this.xi, this.yi, this.xf, this.yf);
    rectMode(CORNER);
    fill(constants.boardBorderFill);
    stroke(constants.boardBorderStroke);
    float xCurr = this.xi;
    float yCurr = this.yi;
    for (int i = 0; i < this.spaces.length + 1; i++) {
      square(xCurr, yCurr, squareSize);
      xCurr += squareSize;
    }
    for (int i = 0; i < this.spaces[0].length + 1; i++) {
      square(xCurr, yCurr, squareSize);
      yCurr += squareSize;
    }
    for (int i = 0; i < this.spaces.length + 1; i++) {
      square(xCurr, yCurr, squareSize);
      xCurr -= squareSize;
    }
    for (int i = 0; i < this.spaces[0].length + 1; i++) {
      square(xCurr, yCurr, squareSize);
      yCurr -= squareSize;
    }
    xCurr += squareSize;
    yCurr += squareSize;
    for (int i = 0; i < this.spaces.length; i++) {
      for (int j = 0; j < this.spaces[0].length; j++) {
        this.spaces[i][j].drawSpace(xCurr + squareSize * i, yCurr + squareSize * j, squareSize);
      }
    }
    this.drawVisualEffects();
  }
  
  public void drawVisualEffects() {
    for (int i = 0; i < this.visualEffects.size(); i++) {
      if (this.visualEffects.get(i).drawVisualEffect(this)) {
        this.visualEffects.remove(i);
        i--;
      }
    }
  }
  public void clearVisualEffects() {
    this.visualEffects.clear();
  }
  
  public void addPiece() {
    this.addPiece(new Piece(this.spaces.length));
  }
  public void addPiece(Piece piece) {
    piece.setXLocation(this.spaces.length / 2 - 1);
    piece.setYLocation(-2);
    // add to piece list
    this.piece = piece;
    // resolve space logic
    this.addPieceLogic();
  }
  
  public void addPieceLogic() {
    if (this.piece == null) {
      return;
    }
    // first calculate piece shadow if falling
    // make copy of piece fall
    Piece pieceCopy = new Piece(this.piece);
    while(this.canMove(pieceCopy, Direction.DIRECTION_DOWN)) {
      pieceCopy.movePiece(Direction.DIRECTION_DOWN);
    }
    // then add shadow
    ArrayList<Pair<Integer, Integer>> pieceSpaces = pieceCopy.getPieceSpace();
    for(Pair<Integer, Integer> i : pieceSpaces) {
      int x = i.getKey();
      int y = i.getValue();
      if (y < 0) {
        continue;
      }
      if ((x < 0) || (x >= this.spaces.length) || (y >= this.spaces[0].length)) {
        println("ERROR: Piece shadow erroneously off the board.");
      }
      if (this.spaces[x][y].getOccupied()) {
        println("ERROR: Space is occupied.");
      }
      this.spaces[x][y].setShadow(this.piece.pieceColor);
    }
    // then calculate actual piece
    pieceSpaces = this.piece.getPieceSpace();
    for(Pair<Integer, Integer> i : pieceSpaces) {
      int x = i.getKey();
      int y = i.getValue();
      if (y < 0) {
        this.pieceOverflow = true;
        continue;
      }
      if ((x < 0) || (x >= this.spaces.length) || (y >= this.spaces[0].length)) {
        println("ERROR: Piece erroneously off the board.");
      }
      if (this.spaces[x][y].getOccupied()) {
        println("ERROR: Space is occupied.");
      }
      this.spaces[x][y].setColor(this.piece.pieceColor);
    }
  }
  public void removePieceLogic() {
    if (this.piece == null) {
      return;
    }
    // first calculate actual piece
    ArrayList<Pair<Integer, Integer>> pieceSpaces = this.piece.getPieceSpace();
    for(Pair<Integer, Integer> i : pieceSpaces) {
      int x = i.getKey();
      int y = i.getValue();
      if (y < 0) {
        this.pieceOverflow = false;
        continue;
      }
      if ((x < 0) || (x >= this.spaces.length) || (y >= this.spaces[0].length)) {
        println("ERROR: Piece erroneously off the board.");
      }
      if (!this.spaces[x][y].getOccupied()) {
        println("ERROR: Space not occupied.");
      }
      this.spaces[x][y].removeColor();
    }
    // then calculate piece shadow if falling
    // make copy of piece fall
    Piece pieceCopy = new Piece(this.piece);
    while(this.canMove(pieceCopy, Direction.DIRECTION_DOWN)) {
      pieceCopy.movePiece(Direction.DIRECTION_DOWN);
    }
    // then add shadow
    pieceSpaces = pieceCopy.getPieceSpace();
    for(Pair<Integer, Integer> i : pieceSpaces) {
      int x = i.getKey();
      int y = i.getValue();
      if (y < 0) {
        continue;
      }
      if ((x < 0) || (x >= this.spaces.length) || (y >= this.spaces[0].length)) {
        println("ERROR: Piece shadow erroneously off the board.");
      }
      if (this.spaces[x][y].getOccupied()) {
        println("ERROR: Space is occupied.");
      }
      this.spaces[x][y].removeShadow();
    }
  }
  public boolean aPieceFalling() {
    if (this.piece == null) {
      return false;
    }
    return true;
  }
  public void dropPiece() {
    if (this.aPieceFalling()) {
      VisualEffect ve = new VisualEffect(VisualEffectType.PIECE_DROP, new Piece(this.piece));
      while(this.aPieceFalling()) {
        if (this.movePiece(Direction.DIRECTION_DOWN, true)) {
          ve.integer1++ ;
        }
      }
      this.visualEffects.add(ve);
    }
  }
  public boolean movePiece() {
    return this.movePiece(Direction.DIRECTION_DOWN, true);
  }
  public boolean movePiece(Direction dir, boolean stopFalling) {
    boolean movedPiece = false;
    this.removePieceLogic();
    if (this.canMove(this.piece, dir)) {
      movedPiece = true;
      this.piece.movePiece(dir);
    }
    this.addPieceLogic();
    if ((!movedPiece) && (stopFalling) && (dir == Direction.DIRECTION_DOWN)) {
      this.piece = null;
    }
    return movedPiece;
  }
  public boolean canMove(Piece p) {
    return this.canMove(p, Direction.DIRECTION_DOWN);
  }
  public boolean canMove(Piece p, Direction dir) {
    if (p == null) {
      return false;
    }
    Piece pieceCopy = new Piece(p);
    pieceCopy.movePiece(dir);
    return this.canBe(pieceCopy);
  }
  public boolean rotatePiece() {
    return this.rotatePiece(true);
  }
  public boolean rotatePiece(boolean clockwise) {
    boolean rotatedPiece = false;
    this.removePieceLogic();
    if (this.canRotate(this.piece, clockwise)) {
      rotatedPiece = true;
      this.piece.rotatePiece(clockwise);
    }
    this.addPieceLogic();
    return rotatedPiece;
  }
  public boolean canRotate(Piece p) {
    return this.canRotate(p, true);
  }
  public boolean canRotate(Piece p, boolean clockwise) {
    if (p == null) {
      return false;
    }
    Piece pieceCopy = new Piece(p);
    pieceCopy.rotatePiece(clockwise);
    return this.canBe(pieceCopy);
  }
  public boolean canBe(Piece p) {
    boolean canBe = true;
    ArrayList<Pair<Integer, Integer>> pieceSpaces = p.getPieceSpace();
    for(Pair<Integer, Integer> i : pieceSpaces) {
      int x = i.getKey();
      int y = i.getValue();
      if ((y >= this.spaces[0].length) || (x < 0) || (x >= this.spaces.length)) {
        canBe = false;
        continue;
      }
      if (y < 0) {
        continue;
      }
      if (this.spaces[x][y].getOccupied()) {
        canBe = false;
      }
    }
    return canBe;
  }
  
  public Piece replaceSavedPiece(Piece p) {
    if (this.piece == null) {
      return p;
    }
    this.removePieceLogic();
    if (p == null) {
      Piece tPiece = new Piece(this.piece);
      this.piece = null;
      return tPiece;
    }
    Piece tPiece = new Piece(p);
    tPiece.setXLocation(this.piece.getXLocation());
    tPiece.setYLocation(this.piece.getYLocation());
    if (this.canBe(tPiece)) {
      p = this.piece;
      this.piece = tPiece;
    }
    this.addPieceLogic();
    return p;
  }
  
  public int checkFilledRows() {
    int rowsFilled = 0;
    // start checking rows from bottom
    for (int j = this.spaces[0].length - 1; j >= 0; j--) {
      boolean rowFilled = true;
      for (int i = 0; i < this.spaces.length; i++) {
        if (!this.spaces[i][j].getOccupied()) {
          rowFilled = false;
          break;
        }
      }
      if (rowFilled) {
        // remove current row
        for (int i = 0; i < this.spaces.length; i++) {
          this.spaces[i][j].removeColor();
        }
        // drop other rows 1 space
        for (int k = j; k > 0; k--) {
          for (int i = 0; i < this.spaces.length; i++) {
            this.spaces[i][k] = new Space(this.spaces[i][k - 1]);
          }
        }
        for (int i = 0; i < this.spaces.length; i++) {
          this.spaces[i][0] = new Space();
        }
        this.visualEffects.add(new VisualEffect(VisualEffectType.ROW_CLEARED, j - rowsFilled));
        rowsFilled++;
        j++; // have to adjust row
      }
    }
    return rowsFilled;
  }
}
abstract class button {
  private String message; // text on button
  private int tSize; // size of text
  private int dColor = color(220, 220, 220); // default color
  private int tColor = color(0); // text color
  private int mColor = color(170, 170, 170); // mouseOn color
  private int cColor = color(120, 120, 120); // click color
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
  
  public void changeColor(int dC, int tC, int mC, int cC) {
    this.dColor = dC;
    this.tColor = tC;
    this.mColor = mC;
    this.cColor = cC;
  }
  public void defaultColors() {
  this.dColor = color(220, 220, 220);
  this.tColor = color(0);
  this.mColor = color(170, 170, 170);
  this.cColor = color(120, 120, 120);
  }
  
  public void setMES(String mes) {
    this.message = mes;
  } public void setMOMES(String mes) {
    this.mouseOnMessage = mes;
  }
  public void setACT(boolean b) {
    this.activated = b;
  } public void setMON(boolean b) {
    this.mouseOn = b;
  } public boolean getMON() {
    return this.mouseOn;
  } public void setCLK(boolean b) {
    this.clicked = b;
  } public boolean getCLK() {
    return this.clicked;
  } public void setREB(boolean b) {
    this.releaseButton = b;
  } public void setEAC(boolean b) {
    this.extendActivate = b;
  } public void setDIS(boolean b) {
    this.disabled = b;
  } public void setMOT(boolean b) {
    this.mouseOnText = b;
  }
  public void setMS(long l) {
    this.ms = l;
  } public void setTMS(long i, long j) {
    this.tm1 = i;
    this.tm2 = j;
  }
  
  public abstract void drawBut();
  public abstract void mouseUpdate(float x, float y);
  public abstract void click(); // function of button
  
  public void update(float mX, float mY) {
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
  
  public void mousePress() {
    if (this.mouseOn && !this.disabled) {
      this.setCLK(true);
      if (!(this.releaseButton)) {
        this.click();
        this.setMS(millis()); // start timer for repeated clicks
      }
    }
  }
  
  public void mouseRelease() {
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
  
  public void setRND(int i) {
    this.roundness = i;
  }
  public float getX() {
    return this.xInitial + (this.xFinal - this.xInitial)/2.0f; // returns center of x-value
  } public void setX(float xi, float xf) {
    this.xInitial = xi;
    this.xFinal = xf;
  } public float getY() {
    return this.yInitial + (this.yFinal - this.yInitial)/2.0f;
  } public void setY(float yi, float yf) {
    this.yInitial = yi;
    this.yFinal = yf;
  }
  
  public void drawBut() {
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
    text(mes, this.xInitial + (this.xFinal-this.xInitial)/2.0f, this.yInitial + (this.yFinal-this.yInitial)/2.0f);
  }
  
  public void mouseUpdate(float mX, float mY) {
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
  
  public void drawBut() {
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
  
  public void mouseUpdate(float mX, float mY) {
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
  public void click() {
    this.clicked = true;
  }
  public void doneAction() {
    this.clicked = false;
  }
  public boolean getCLKED() {
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
  public void click() {
    this.clicked++;
  }
  public void doneAction() {
    this.clicked--;
  }
  public void resetClick() {
    this.clicked = 0;
  }
  public int getCLKED() {
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
  public void setLIM(int i) {
    this.limit = i;
  }
  public void click() {
    if (max) {
      this.delCurr++;
    } else {
      this.delCurr--;
    }
  }
  public int resetDEL(int curr) {
    int i = this.delCurr;
    if (((max)&&(curr+this.delCurr > this.limit))||((!(max))&&(curr+this.delCurr < this.limit))) { // exceeding limit
      i = 0;
    }
    this.delCurr = 0;
    return i;
  }
}
class AllButtons {
  private quitButton qB = new quitButton();
  
  private newGameButton ngB = new newGameButton();
  private pauseGameButton pgB = new pauseGameButton();
  private leaveLobbyButton llB = new leaveLobbyButton();
  
  private endGameButton egB = new endGameButton();
  private hostGameButton hgB = new hostGameButton();
  private kickPlayerButton kpB = new kickPlayerButton();
  private playAgainButton paB = new playAgainButton();
  
  private findGameButton fgB = new findGameButton();
  private startGameButton sgB = new startGameButton();
  
  private listBar cSB = new listBar(10, 740, 260, 798);
  
  private joinLobbyButton jlB = new joinLobbyButton();
  private showInfoButton siB = new showInfoButton();
  
  private findIpButton fiB = new findIpButton();
  
  private customizePieceButton cpB = new customizePieceButton();
  private customizeBoardButton cbB = new customizeBoardButton();
  private customizeSoundButton csB = new customizeSoundButton();
  private customizeKeysButton ckB = new customizeKeysButton();
  
  AllButtons() {
  }
  
  public void update(GameState state) {
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rectMode(CORNERS);
    rect(0, 685, 600, 720);
    qB.update(mouseX, mouseY);
    cpB.update(mouseX, mouseY);
    cbB.update(mouseX, mouseY);
    csB.update(mouseX, mouseY);
    ckB.update(mouseX, mouseY);
    switch(state) {
      case MAIN_MENU:
        this.ngB.update(mouseX, mouseY);
        this.hgB.update(mouseX, mouseY);
        this.fgB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        if (this.cSB.getHIGH() != -1) {
          this.jlB.changeColor(color(220), color(0), color(170), color(120));
        }
        else {
          this.jlB.changeColor(color(235), color(80), color(170), color(120));
        }
        this.jlB.update(mouseX, mouseY);
        this.fiB.update(mouseX, mouseY);
        break;
      case OPTIONS:
        break;
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.pgB.update(mouseX, mouseY);
        this.egB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.update(mouseX, mouseY);
        if (this.cSB.getHIGH() > 0) {
          this.kpB.changeColor(color(220), color(0), color(170), color(120));
        } else {
          this.kpB.changeColor(color(235), color(80), color(170), color(120));
        }
        this.kpB.update(mouseX, mouseY);
        if (this.cSB.getSTRS().length == 2) {
          this.sgB.changeColor(color(220), color(0), color(170), color(120));
        } else {
          this.sgB.changeColor(color(235), color(80), color(170), color(120));
        }
        this.sgB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        this.siB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        this.llB.update(mouseX, mouseY);
        this.siB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_HOSTING:
        this.llB.update(mouseX, mouseY);
        this.paB.update(mouseX, mouseY);
        this.siB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_JOINED:
        this.llB.update(mouseX, mouseY);
        this.paB.update(mouseX, mouseY);
        this.siB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        break;
    }
  }
  public void mousePress(GameState state) {
    qB.mousePress();
    cpB.mousePress();
    cbB.mousePress();
    csB.mousePress();
    ckB.mousePress();
    switch(state) {
      case MAIN_MENU:
        this.ngB.mousePress();
        this.hgB.mousePress();
        this.fgB.mousePress();
        if (this.cSB.getHIGH() != -1) {
          if (this.jlB.getMON()) {
            this.jlB.mousePress();
          } else {
            this.cSB.mousePress();
          }
        }
        else {
          this.cSB.mousePress();
        }
        this.fiB.mousePress();
        break;
      case OPTIONS:
        break;
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.pgB.mousePress();
        this.egB.mousePress();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.mousePress();
        if (this.cSB.getSTRS().length == 2) {
          this.sgB.mousePress();
        }
        this.siB.mousePress();
        if (this.cSB.getHIGH() > 0) {
          if (this.kpB.getMON()) {
            this.kpB.mousePress();
          } else {
            this.cSB.mousePress();
          }
        }
        else {
          this.cSB.mousePress();
        }
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        this.llB.mousePress();
        this.siB.mousePress();
        this.cSB.mousePress();
        break;
      case MULTIPLAYER_HOSTING:
        this.llB.mousePress();
        this.paB.mousePress();
        this.siB.mousePress();
        this.cSB.mousePress();
        break;
      case MULTIPLAYER_JOINED:
        this.llB.mousePress();
        this.paB.mousePress();
        this.siB.mousePress();
        this.cSB.mousePress();
        break;
    }
  }
  public void mouseRelease(GameState state) {
    qB.mouseRelease();
    cpB.mouseRelease();
    cbB.mouseRelease();
    csB.mouseRelease();
    ckB.mouseRelease();
    switch(state) {
      case MAIN_MENU:
        this.ngB.mouseRelease();
        this.hgB.mouseRelease();
        this.fgB.mouseRelease();
        this.cSB.mouseRelease();
        if (this.cSB.getHIGH() != -1) {
          this.jlB.mouseRelease();
        }
        this.fiB.mouseRelease();
        break;
      case OPTIONS:
        break;
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.pgB.mouseRelease();
        this.egB.mouseRelease();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.mouseRelease();
        if (this.cSB.getHIGH() > 0) {
          this.kpB.mouseRelease();
        }
        if (this.cSB.getSTRS().length == 2) {
          this.sgB.mouseRelease();
        }
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        this.llB.mouseRelease();
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        break;
      case MULTIPLAYER_HOSTING:
        this.llB.mouseRelease();
        this.paB.mouseRelease();
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        break;
      case MULTIPLAYER_JOINED:
        this.llB.mouseRelease();
        this.paB.mouseRelease();
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        break;
    }
  }
  public void scroll(int count) {
    this.cSB.scroll(count);
  }
  
  public void clearButtons(int[] ids) {
    for (int id : ids) {
      this.clearButton(id);
    }
  }
  public void clearButton(int id) {
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rectMode(CORNERS);
    switch(id) {
      case 0: // scrollbar
        rect(10, 720, 265, 800);
        break;
      case 1:
        rect(10, 690, 60, 715);
        break;
      case 2:
        rect(70, 690, 160, 715);
        break;
      case 3:
        rect(170, 690, 260, 715);
        break;
      case 4:
        rect(270, 690, 360, 715);
        break;
      case 5:
        rect(270, 740, 360, 765);
        break;
      case 6:
        rect(270, 773, 360, 798);
        break;
      default:
        println("ERROR: button id " + id + " not found to clear.");
        break;
    }
  }
}

// Button 1
class quitButton extends recButton {
  quitButton() {
    super("Exit", 14, 10, 690, 60, 715);
    this.setREB(true);
  }
  public void click() {
    int response = showConfirmDialog(null, "Are you sure you want to exit?", "Tetris", YES_NO_OPTION, PLAIN_MESSAGE);
    if (response == YES_OPTION) {
      exit();
    }
  }
}

// Button 2
class newGameButton extends recButton {
  newGameButton() {
    super("New Game", 14, 70, 690, 160, 715);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.startSinglePlayerGame();
  }
}
class pauseGameButton extends recButton {
  pauseGameButton() {
    super("Pause Game", 14, 70, 690, 160, 715);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.pauseGame();
  }
}
class leaveLobbyButton extends recButton {
  leaveLobbyButton() {
    super("Leave Lobby", 14, 70, 690, 160, 715);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.leaveLobby();
  }
}

// Button 3
class endGameButton extends recButton {
  endGameButton() {
    super("End Game", 14, 170, 690, 260, 715);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.endGame();
  }
}
class hostGameButton extends recButton {
  hostGameButton() {
    super("Host Game", 14, 170, 690, 260, 715);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.hostTwoPlayerGame();
  }
}
class kickPlayerButton extends recButton {
  kickPlayerButton() {
    super("Kick Player", 14, 170, 690, 260, 715);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.kickPlayer();
  }
}
class playAgainButton extends recButton {
  playAgainButton() {
    super("Rematch", 14, 170, 690, 260, 715);
    super.setREB(true);
    super.setDIS(true);
    super.changeColor(color(235), color(80), color(170), color(120));
    super.setMOT(true);
    super.setMOMES("Rematch");
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.toggleRematch();
  }
}

// Button 4
class findGameButton extends recButton {
  findGameButton() {
    super("Find Game", 14, 270, 690, 360, 715);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.findMultiPlayerGame();
  }
}
class startGameButton extends recButton {
  startGameButton() {
    super("Start Game", 14, 270, 690, 360, 715);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.startGame();
  }
}

// Button 5
class joinLobbyButton extends recButton {
  joinLobbyButton() {
    super("Join Lobby", 14, 270, 740, 360, 765);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.joinSelectedLobby();
  }
}
class showInfoButton extends recButton {
  showInfoButton() {
    super("Lobby Info", 14, 270, 740, 360, 765);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.showLobbyInfo();
  }
}

// Button 6
class findIpButton extends recButton {
  findIpButton() {
    super("Find IP", 14, 270, 773, 360, 798);
    this.setREB(true);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    currGame.directConnect();
  }
}

// Option Buttons
class customizePieceButton extends recButton {
  customizePieceButton() {
    super("Pieces", 14, 1240, 709, 1320, 729);
    this.setREB(true);
    this.setRND(3);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    String[] styles = PieceStyle.getStyleList();
    String response = (String)showInputDialog(null, "Piece Style", "Tetris", PLAIN_MESSAGE, null, styles, options.pieceStyle.getStyle());
    if (response == null) {
      return;
    }
    try {
      options.pieceStyle = PieceStyle.VALUES.get(Arrays.asList(styles).indexOf(response));
    } catch (Exception e) {
      showMessageDialog(null, "Couldn't adjust piece style.", "Tetris", PLAIN_MESSAGE);
    }
    options.saveOptions();
  }
}
class customizeBoardButton extends recButton {
  customizeBoardButton() {
    super("Board", 14, 1240, 732, 1320, 752);
    this.setREB(true);
    this.setRND(3);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
    int response = showConfirmDialog(null, "Gridlines?", "Tetris", YES_NO_OPTION, PLAIN_MESSAGE);
    if (response == YES_OPTION) {
      options.gridlines = true;
    }
    else if (response == NO_OPTION) {
      options.gridlines = false;
    }
    options.saveOptions();
  }
}
class customizeSoundButton extends recButton {
  customizeSoundButton() {
    super("Sounds", 14, 1240, 755, 1320, 775);
    this.setREB(true);
    this.setRND(3);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
  }
}
class customizeKeysButton extends recButton {
  customizeKeysButton() {
    super("Hotkeys", 14, 1240, 778, 1320, 798);
    this.setREB(true);
    this.setRND(3);
  }
  public void click() {
    this.setMON(false);
    this.setCLK(false);
  }
}
class Constants {
  // Tetris
  public final String version = "Tetris v0.3.6a";
  public final int defaultTickLength = 400;
  public final int maxFPS = 60;
  public final int frameUpdateTime = 100;
  public final int frameAverageCache = 5;
  public final int defaultBackgroundColor = color(200);
  
  // CurrGame
  public final int portRangeFirst = 48620;
  public final int portRangeLast = 48650; // 49150 is max for future reference
  public final int nextPieceQueueLength = 3;
  public final float[] game1Borders = { 10, 30, 660, 680 };
  public final float[] game2Borders = { 670, 30, 1320, 680 };
  public final int defaultPingTimeout = 3000;
  public final int pingRequestFrequency = 100;
  public final int maxPingRequestsMissed = 3;
  
  // Board
  public final boolean defaultGridlines = false;
  public final int defaultBoardColumns = 10;
  public final int defaultBoardRows = 20;
  public final int boardBackground = color(0);
  public final int boardBorderFill = color(200, 200, 200);
  public final int boardBorderStroke = color(100, 100, 100);
  
  // Space
  public final Color defaultSpaceColor = Color.DEEP_BLACK;
  public final int defaultSpaceStroke = color(255);
  public final float shadowOpacity = 100.0f;
  
  // Piece
  public final PieceStyle defaultPieceStyle = PieceStyle.RAISED_FADE;
  public final int defaultPieceFill = color(255);
  public final int defaultPieceStroke = color(0);
  public final Color defaultIFill = Color.CYAN;
  public final Color defaultJFill = Color.FUCHSIA;
  public final Color defaultLFill = Color.ORANGE;
  public final Color defaultOFill = Color.YELLOW;
  public final Color defaultSFill = Color.RED;
  public final Color defaultTFill = Color.PURPLE;
  public final Color defaultZFill = Color.GREEN;
  public final int minPieceDisplayGridSize = 3;
  
  // Game
  public final int scoreTick = 1;
  public final int scorePiece= 10;
  public final int scoreRow = 30;
  public final int scoreDouble = 30;
  public final int scoreTriple = 90;
  public final int scoreQuadruple = 180;
  
  // Visual Effects
  public final int effectMaxLength_RowCleared = 200;
  public final int effectMaxth_PieceDrop = 200;
  
  // Images
  public PImage lightning;
  
  public PImage fade_2D_blue;
  public PImage fade_2D_red;
  public PImage fade_2D_green;
  public PImage fade_2D_yellow;
  public PImage fade_2D_cyan;
  public PImage fade_2D_purple;
  public PImage fade_2D_fuchsia;
  public PImage fade_2D_orange;
  public PImage fade_2D_pink;
  public PImage fade_2D_gray;
  public PImage fade_2D_tan;
  public PImage fade_2D_black;
    
  public PImage fat_3D_blue;
  public PImage fat_3D_red;
  public PImage fat_3D_green;
  public PImage fat_3D_yellow;
  public PImage fat_3D_cyan;
  public PImage fat_3D_purple;
  public PImage fat_3D_fuchsia;
  public PImage fat_3D_orange;
  public PImage fat_3D_pink;
  public PImage fat_3D_gray;
  public PImage fat_3D_tan;
  public PImage fat_3D_black;
  
  public PImage normal_3D_blue;
  public PImage normal_3D_red;
  public PImage normal_3D_green;
  public PImage normal_3D_yellow;
  public PImage normal_3D_cyan;
  public PImage normal_3D_purple;
  public PImage normal_3D_fuchsia;
  public PImage normal_3D_orange;
  public PImage normal_3D_pink;
  public PImage normal_3D_gray;
  public PImage normal_3D_tan;
  public PImage normal_3D_black;
  
  public PImage fade_3D_soft_blue;
  public PImage fade_3D_soft_red;
  public PImage fade_3D_soft_green;
  public PImage fade_3D_soft_yellow;
  public PImage fade_3D_soft_cyan;
  public PImage fade_3D_soft_purple;
  public PImage fade_3D_soft_fuchsia;
  public PImage fade_3D_soft_orange;
  public PImage fade_3D_soft_pink;
  public PImage fade_3D_soft_gray;
  public PImage fade_3D_soft_tan;
  public PImage fade_3D_soft_black;
  
  public PImage fade_3D_sharp_blue;
  public PImage fade_3D_sharp_red;
  public PImage fade_3D_sharp_green;
  public PImage fade_3D_sharp_yellow;
  public PImage fade_3D_sharp_cyan;
  public PImage fade_3D_sharp_purple;
  public PImage fade_3D_sharp_fuchsia;
  public PImage fade_3D_sharp_orange;
  public PImage fade_3D_sharp_pink;
  public PImage fade_3D_sharp_gray;
  public PImage fade_3D_sharp_tan;
  public PImage fade_3D_sharp_black;
  
  Constants() {
  }
  
  public void loadImages() {
    this.lightning = loadImage(sketchPath("") + "data/lightning.png");
    
    this.fade_2D_blue = loadImage(sketchPath("") + "data/pieces/2D_fade_blue.jpg");
    this.fade_2D_red = loadImage(sketchPath("") + "data/pieces/2D_fade_red.jpg");
    this.fade_2D_green = loadImage(sketchPath("") + "data/pieces/2D_fade_green.jpg");
    this.fade_2D_yellow = loadImage(sketchPath("") + "data/pieces/2D_fade_yellow.jpg");
    this.fade_2D_cyan = loadImage(sketchPath("") + "data/pieces/2D_fade_cyan.jpg");
    this.fade_2D_fuchsia = loadImage(sketchPath("") + "data/pieces/2D_fade_fuchsia.jpg");
    this.fade_2D_purple = loadImage(sketchPath("") + "data/pieces/2D_fade_purple.jpg");
    this.fade_2D_pink = loadImage(sketchPath("") + "data/pieces/2D_fade_pink.jpg");
    this.fade_2D_orange = loadImage(sketchPath("") + "data/pieces/2D_fade_orange.jpg");
    this.fade_2D_gray = loadImage(sketchPath("") + "data/pieces/2D_fade_gray.jpg");
    this.fade_2D_tan = loadImage(sketchPath("") + "data/pieces/2D_fade_tan.jpg");
    this.fade_2D_black = loadImage(sketchPath("") + "data/pieces/2D_fade_black.jpg");
    
    this.fat_3D_blue = loadImage(sketchPath("") + "data/pieces/3d_fat_blue.jpg");
    this.fat_3D_red = loadImage(sketchPath("") + "data/pieces/3d_fat_red.jpg");
    this.fat_3D_green = loadImage(sketchPath("") + "data/pieces/3d_fat_green.jpg");
    this.fat_3D_yellow = loadImage(sketchPath("") + "data/pieces/3d_fat_yellow.jpg");
    this.fat_3D_cyan = loadImage(sketchPath("") + "data/pieces/3d_fat_cyan.jpg");
    this.fat_3D_fuchsia = loadImage(sketchPath("") + "data/pieces/3d_fat_fuchsia.jpg");
    this.fat_3D_purple = loadImage(sketchPath("") + "data/pieces/3d_fat_purple.jpg");
    this.fat_3D_pink = loadImage(sketchPath("") + "data/pieces/3d_fat_pink.jpg");
    this.fat_3D_orange = loadImage(sketchPath("") + "data/pieces/3d_fat_orange.jpg");
    this.fat_3D_gray = loadImage(sketchPath("") + "data/pieces/3d_fat_gray.jpg");
    this.fat_3D_tan = loadImage(sketchPath("") + "data/pieces/3d_fat_tan.jpg");
    this.fat_3D_black = loadImage(sketchPath("") + "data/pieces/3d_fat_black.jpg");
    
    this.normal_3D_blue = loadImage(sketchPath("") + "data/pieces/3d_normal_blue.jpg");
    this.normal_3D_red = loadImage(sketchPath("") + "data/pieces/3d_normal_red.jpg");
    this.normal_3D_green = loadImage(sketchPath("") + "data/pieces/3d_normal_green.jpg");
    this.normal_3D_yellow = loadImage(sketchPath("") + "data/pieces/3d_normal_yellow.jpg");
    this.normal_3D_cyan = loadImage(sketchPath("") + "data/pieces/3d_normal_cyan.jpg");
    this.normal_3D_fuchsia = loadImage(sketchPath("") + "data/pieces/3d_normal_fuchsia.jpg");
    this.normal_3D_purple = loadImage(sketchPath("") + "data/pieces/3d_normal_purple.jpg");
    this.normal_3D_pink = loadImage(sketchPath("") + "data/pieces/3d_normal_pink.jpg");
    this.normal_3D_orange = loadImage(sketchPath("") + "data/pieces/3d_normal_orange.jpg");
    this.normal_3D_gray = loadImage(sketchPath("") + "data/pieces/3d_normal_gray.jpg");
    this.normal_3D_tan = loadImage(sketchPath("") + "data/pieces/3d_normal_tan.jpg");
    this.normal_3D_black = loadImage(sketchPath("") + "data/pieces/3d_normal_black.jpg");
    
    this.fade_3D_soft_blue = loadImage(sketchPath("") + "data/pieces/3D_soft_blue.jpg");
    this.fade_3D_soft_red = loadImage(sketchPath("") + "data/pieces/3D_soft_red.jpg");
    this.fade_3D_soft_green = loadImage(sketchPath("") + "data/pieces/3D_soft_green.jpg");
    this.fade_3D_soft_yellow = loadImage(sketchPath("") + "data/pieces/3D_soft_yellow.jpg");
    this.fade_3D_soft_cyan = loadImage(sketchPath("") + "data/pieces/3D_soft_cyan.jpg");
    this.fade_3D_soft_fuchsia = loadImage(sketchPath("") + "data/pieces/3D_soft_fuchsia.jpg");
    this.fade_3D_soft_purple = loadImage(sketchPath("") + "data/pieces/3D_soft_purple.jpg");
    this.fade_3D_soft_pink = loadImage(sketchPath("") + "data/pieces/3D_soft_pink.jpg");
    this.fade_3D_soft_orange = loadImage(sketchPath("") + "data/pieces/3D_soft_orange.jpg");
    this.fade_3D_soft_gray = loadImage(sketchPath("") + "data/pieces/3D_soft_gray.jpg");
    this.fade_3D_soft_tan = loadImage(sketchPath("") + "data/pieces/3D_soft_tan.jpg");
    this.fade_3D_soft_black = loadImage(sketchPath("") + "data/pieces/3D_soft_black.jpg");
    
    this.fade_3D_sharp_blue = loadImage(sketchPath("") + "data/pieces/3D_sharp_blue.jpg");
    this.fade_3D_sharp_red = loadImage(sketchPath("") + "data/pieces/3D_sharp_red.jpg");
    this.fade_3D_sharp_green = loadImage(sketchPath("") + "data/pieces/3D_sharp_green.jpg");
    this.fade_3D_sharp_yellow = loadImage(sketchPath("") + "data/pieces/3D_sharp_yellow.jpg");
    this.fade_3D_sharp_cyan = loadImage(sketchPath("") + "data/pieces/3D_sharp_cyan.jpg");
    this.fade_3D_sharp_fuchsia = loadImage(sketchPath("") + "data/pieces/3D_sharp_fuchsia.jpg");
    this.fade_3D_sharp_purple = loadImage(sketchPath("") + "data/pieces/3D_sharp_purple.jpg");
    this.fade_3D_sharp_pink = loadImage(sketchPath("") + "data/pieces/3D_sharp_pink.jpg");
    this.fade_3D_sharp_orange = loadImage(sketchPath("") + "data/pieces/3D_sharp_orange.jpg");
    this.fade_3D_sharp_gray = loadImage(sketchPath("") + "data/pieces/3D_sharp_gray.jpg");
    this.fade_3D_sharp_tan = loadImage(sketchPath("") + "data/pieces/3D_sharp_tan.jpg");
    this.fade_3D_sharp_black = loadImage(sketchPath("") + "data/pieces/3D_sharp_black.jpg");
  }
  
  public HashMap<Color, PImage> generateImages(PImage inputImage) {
    HashMap<Color, PImage> returnMap = new HashMap<Color, PImage>();
    for (Color c : Color.VALUES) {
      int c_color = ColorToColor(c);
      float c_r = c_color >> 16 & 0xFF;
      float c_g = c_color >> 8 & 0xFF;
      float c_b = c_color & 0xFF;
      PImage imageCopy = inputImage.copy();
      imageCopy.loadPixels();
      for(int i = 0; i < imageCopy.pixels.length; i++) {
        float p_r = red(imageCopy.pixels[i]);
        float p_g = green(imageCopy.pixels[i]);
        float p_b = blue(imageCopy.pixels[i]);
        p_r = sqrt(p_r * c_r);
        p_g = sqrt(p_g * c_g);
        p_b = sqrt(p_b * c_b);
        imageCopy.pixels[i] = color(p_r, p_g, p_b);
      }
      imageCopy.updatePixels();
      returnMap.put(c, imageCopy);
    }
    return returnMap;
  }
}
public enum GameState {
  MAIN_MENU, OPTIONS, CONNECTING_TO_LOBBY, SINGLEPLAYER, MULTIPLAYER_LOBBY_HOSTING, MULTIPLAYER_LOBBY_JOINED, MULTIPLAYER_HOSTING, MULTIPLAYER_JOINED;
}

class CurrGame {
  private User user;
  private Game myGame;
  private Game otherGame;
  private Server server;
  private int portHosting;
  private String lobbyName;
  private Joinee otherPlayer = null;
  private ArrayList<Joinee> lobbyClients = new ArrayList<Joinee>();
  private Tetris thisInstance;
  private GameState state = GameState.MAIN_MENU;
  AllButtons buttons = new AllButtons();
  private Queue<String> messageQ = new LinkedList<String>();
  private boolean[] wantRematch = new boolean[2];
  private boolean paused = false;
  
  CurrGame(Tetris thisInstance) {
    this.thisInstance = thisInstance;
  }
  public void initiateUser() {
    if (options.defaultUsername == null) {
      if (options.usernames.size() == 0) {
        this.user = createNewUser(options.usernames);
      } else {
        this.user = options.chooseDefaultUsername();
      }
      this.user.saveUser();
      options.defaultUsername = this.user.name;
      options.saveOptions();
      return;
    }
    String[] userFile = loadStrings("data/users/" + options.defaultUsername + ".user.tetris");
    if (userFile == null) {
      if (options.usernames.size() == 0) {
        this.user = createNewUser(options.usernames);
      } else {
        this.user = options.chooseDefaultUsername();
      }
      this.user.saveUser();
      options.defaultUsername = this.user.name;
      options.saveOptions();
      return;
    }
    this.user = new User(options.defaultUsername);
    for (String line : userFile) {
      String[] splitLine = split(line, ":");
      String value = trim(splitLine[1]);
      switch(trim(splitLine[0])) {
        default:
          break;
      }
    }
  }
  
  public void goToMainMenu() {
    this.buttons.cSB.clearSTRS();
    this.buttons.clearButton(0);
    this.state = GameState.MAIN_MENU;
  }
  
  public void startSinglePlayerGame() {
    this.myGame = new Game(constants.game1Borders);
    this.state = GameState.SINGLEPLAYER;
    this.buttons.clearButtons(new int[]{0, 5, 6});
  }
  
  public void pauseGame() {
    if (this.paused) {
      this.paused = false;
      this.myGame.clearGameOverMessage();
      this.buttons.pgB.setMES("Pause Game");
      this.buttons.pgB.defaultColors();
    }
    else {
      this.paused = true;
      this.myGame.addGameOverMessage("Game", "Paused");
      this.buttons.pgB.setMES("Resume");
      this.buttons.pgB.changeColor(color(120), color(0), color(170), color(220));
    }
  }
  
  public void endGame() {
    if (showConfirmDialog(null, "Are you sure you want to quit this game?", "Tetris", YES_NO_OPTION, PLAIN_MESSAGE) == YES_OPTION) {
      this.myGame = null;
      this.goToMainMenu();
    }
  }
  
  public void hostTwoPlayerGame() {
    for (int port = constants.portRangeFirst; port <= constants.portRangeLast; port++) {
      try {
        this.server = new Server(this.thisInstance, port);
        if (this.server.active()) {
          this.lobbyName = showInputDialog("Choose a name for this lobby");
          if (this.lobbyName == null) {
            this.server.stop();
            this.server = null;
            return;
          }
          this.lobbyName += ": " + millis();
          println("Server active on port " + port + " with ip: " + Server.ip() + ".");
          this.portHosting = port;
          this.state = GameState.MULTIPLAYER_LOBBY_HOSTING;
          break;
        }
      } catch(Exception e) {}
    }
    if ((this.server == null) || (!this.server.active())) {
      this.state = GameState.MAIN_MENU;
      println("ERROR: Server not created.");
    }
    this.buttons.clearButton(5);
    this.buttons.clearButton(6);
  }
  
  public void showLobbyInfo() {
    if (this.server != null && this.server.active()) {
      showMessageDialog(null, "IP address: " + Server.ip() + "\nPort: " + this.portHosting, "", PLAIN_MESSAGE);
    }
    else {
      showMessageDialog(null, "IP address: " + this.otherPlayer.id + "\nPort: " + this.otherPlayer.port, "", PLAIN_MESSAGE);
    }
  }
  
  public void directConnect() {
    String possibleIP = showInputDialog("Choose IP address");
    if (possibleIP == null) {
      return;
    }
    String portString = showInputDialog("Enter the port number");
    if (portString == null) {
      return;
    }
    int port = 0;
    try {
      port = Integer.valueOf(portString);
    } catch(NumberFormatException e) {
      showMessageDialog(null, "Not a number", "", PLAIN_MESSAGE);
      return;
    }
    if ((port < constants.portRangeFirst) || (port > constants.portRangeLast)) {
      showMessageDialog(null, "Invalid port number", "", PLAIN_MESSAGE);
      return;
    }
    final ArrayList<Joinee> possibleLobby = new ArrayList<Joinee>();
    final Tetris inst = this.thisInstance;
    final String ipTesting = possibleIP;
    final int portTesting = port;
    Thread thread = new Thread(new Runnable() {
      public void run() {
        Client testClient = new Client(inst, ipTesting, portTesting);
        if (!testClient.active()) {
          testClient.stop();
          testClient = null;
          showMessageDialog(null, "Couldn't connect to that IP / port", "", PLAIN_MESSAGE);
          return;
        }
        possibleLobby.add(new Joinee(testClient, portTesting, "LOBBY: "));
      }
    });
    thread.start();
    delay(constants.defaultPingTimeout);
    try {
      thread.interrupt();
    } catch(Exception e) {
      //e.printStackTrace();
    }
    if (possibleLobby.size() > 0) {
      if (possibleLobby.get(0).client.active()) {
        this.otherPlayer = possibleLobby.get(0);
        this.state = GameState.CONNECTING_TO_LOBBY;
      }
    }
  }
  
  public void findMultiPlayerGame() {
    // First search for IPs
    ArrayList<String> IPs = this.findAddressesOnLAN();
    // Then search for open ports
    this.lobbyClients = this.findHosts(IPs);
    // Reset timers so clients not removed before initial ping request comes in
    for (int i = 0; i < this.lobbyClients.size(); i++) {
      this.lobbyClients.get(i).lastPingRequest = millis();
    }
    // Message if no connections found
    rectMode(CORNERS);
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rect(10, 720, 265, 738);
    if (this.lobbyClients.size() == 0) {
      textSize(13);
      textAlign(LEFT, TOP);
      fill(0);
      text("No connections found. Try \"Find IP\"", 10, 723);
    }
  }
  public ArrayList<String> findAddressesOnLAN() {
    final ArrayList<String> IPs = new ArrayList<String>();
    final ArrayList<Thread> threads = new ArrayList<Thread>();
    for (int i = 1; i < 255; i++) {
      final int j = i;
      Thread thread1 = new Thread(new Runnable() {
        public void run() {
          try {
            if (InetAddress.getByName("192.168.1." + j).isReachable(constants.defaultPingTimeout)) {
              IPs.add("192.168.1." + j);
            }
          } catch (Exception e) {
            //e.printStackTrace();
          }
        }
      });
      Thread thread2 = new Thread(new Runnable() {
        public void run() {
          try {
            if (InetAddress.getByName("192.168.56." + j).isReachable(constants.defaultPingTimeout)) {
              IPs.add("192.168.56." + j);
            }
          } catch (Exception e) {
            //e.printStackTrace();
          }
        }
      });
      threads.add(thread1);
      threads.add(thread2);
      thread1.start();
      thread2.start();
    }
    for(Thread thread : threads) {
      try {
        thread.join();
      } catch(InterruptedException e) {
        thread.interrupt();
        //e.printStackTrace();
      }
    }
    println("IPs found: " + IPs);
    return IPs;
  }
  public ArrayList<Joinee> findHosts(ArrayList<String> IPs) {
    ArrayList<Thread> threads = new ArrayList<Thread>();
    final ArrayList<Joinee> possibleLobbies = new ArrayList<Joinee>();
    final Tetris inst = this.thisInstance;
    final int[] ports = new int[constants.portRangeLast - constants.portRangeFirst];
    for (int i = 0; i < ports.length; i++) {
      ports[i] = constants.portRangeFirst + i;
    }
    for(final String ip : IPs) {
      for (final int port : ports) {
        Thread thread = new Thread(new Runnable() {
          public void run() {
            try {
              Client testClient = new Client(inst, ip, port);
              if (testClient.active()) {
                println("Connected at " + ip + " with port " + port + ".");
                possibleLobbies.add(new Joinee(testClient, port, "LOBBY: "));
              }
              else {
                testClient.stop();
                testClient = null;
              }
            } catch(Exception e) {
              //e.printStackTrace();
            } finally {
              try {
              } catch(Exception e) {}
            }
          }
        });
        threads.add(thread);
        thread.start();
      }
    }
    for(Thread thread : threads) {
      try {
        thread.join();
      } catch(Exception e) {
        thread.interrupt();
        //e.printStackTrace();
      }
    }
    return possibleLobbies;
  }
  
  public void joinSelectedLobby() {
    int index = this.buttons.cSB.getHIGH();
    if ((index < 0) || (index >= this.lobbyClients.size())) {
      println("ERROR: selected lobby " + index + " but only " + this.lobbyClients.size() + " lobbies exist.");
      return;
    }
    this.lobbyClients.get(this.buttons.cSB.getHIGH()).write("Join Lobby");
  }
  
  public void toggleRematch() {
    this.wantRematch[0] = !this.wantRematch[0];
    if (this.wantRematch[0]) {
      this.buttons.paB.setMES("Offer Sent!");
      this.buttons.paB.changeColor(color(0, 255, 0), color(0), color(200, 100, 100), color(255, 40, 20));
      this.buttons.paB.setMOMES("Cancel");
      if (this.state == GameState.MULTIPLAYER_HOSTING) {
        this.server.write("| LOBBY: Host Rematch Sent");
        this.checkRematches();
      }
      if (this.state == GameState.MULTIPLAYER_JOINED) {
        this.otherPlayer.write("Joinee Rematch Sent");
      }
    }
    else {
      this.buttons.paB.setMES("Canceled");
      this.buttons.paB.changeColor(color(255, 10, 10), color(0), color(100, 200, 100), color(40, 255, 20));
      this.buttons.paB.setMOMES("Resend Offer");
      if (this.state == GameState.MULTIPLAYER_HOSTING) {
        this.server.write("| LOBBY: Host Rematch Revoked");
      }
      if (this.state == GameState.MULTIPLAYER_JOINED) {
        this.otherPlayer.write("Joinee Rematch Revoked");
      }
    }
  }
  public void checkRematches() {
    if ((this.wantRematch[0]) && (this.wantRematch[1])) {
      if (this.state == GameState.MULTIPLAYER_HOSTING) {
        this.buttons.paB = new playAgainButton();
        this.wantRematch[0] = false;
        this.wantRematch[1] = false;
        this.myGame = new Game(constants.game1Borders);
        this.otherGame = new Game(constants.game2Borders);
        for (Joinee j : this.lobbyClients) {
          if (j != null) {
            if (j.client != null) {
              j.client.stop();
            }
          }
        }
        this.server.write("| LOBBY: Start Game");
      }
    }
  }
  
  public void clientConnects(Client someClient) {
    println("Client connected with IP address: " + someClient.ip());
  }
  public void clientDisconnects(Client someClient) {
    println("Client with IP address " + someClient.ip() + " has disconnected.");
    if (this.state == GameState.MULTIPLAYER_LOBBY_HOSTING) {
      for(int i = 0; i < this.lobbyClients.size(); i++) {
        if (this.lobbyClients.get(i).id.equals(someClient.ip())) {
          this.lobbyClients.remove(i);
          i--;
        }
      }
    }
  }
  public void leaveLobby() {
    switch(this.state) {
      case MULTIPLAYER_LOBBY_HOSTING:
      case MULTIPLAYER_HOSTING:
        if (this.server != null) {
          this.server.write("| LOBBY: Quit Lobby");
          this.server.stop();
          this.server = null;
        }
      case MULTIPLAYER_LOBBY_JOINED:
      case MULTIPLAYER_JOINED:
        if (this.otherPlayer != null) {
          this.otherPlayer.write("Quit Lobby");
          this.otherPlayer.client.stop();
          this.otherPlayer = null;
        }
        for (int i = 0; i < this.lobbyClients.size(); i++) {
          if (this.lobbyClients.get(i) != null) {
            this.lobbyClients.get(i).client.stop();
            this.lobbyClients.get(i).client = null;
          }
        }
        this.buttons.cSB.clearSTRS();
        this.buttons.clearButton(0);
        this.lobbyClients.clear();
        break;
      default:
        println("ERROR: Leave lobby button pressed but state not recognized");
        break;
    }
    this.goToMainMenu();
  }
  
  public void kickPlayer() {
    if (this.otherPlayer == null) {
      println("No client to kick.");
    }
    else {
      println("Kicked client with ID: " + this.otherPlayer.id);
      this.server.write("| LOBBY: Kick Player");
      this.server.disconnect(this.otherPlayer.client);
      this.otherPlayer = null;
    }
  }
  
  public void startGame() {
    this.myGame = new Game(constants.game1Borders);
    this.otherGame = new Game(constants.game2Borders);
    this.state = GameState.MULTIPLAYER_HOSTING;
    for (Joinee j : this.lobbyClients) {
      if (j != null) {
        if (j.client != null) {
          j.client.stop();
        }
      }
    }
    this.server.write("| LOBBY: Start Game");
  }
  
  public void clientEvent(Client someClient) {
    String[] messages = split(someClient.readString(), '|');
    for(String message : messages) {
      message = trim(message);
      if (message.equals("")) {
        continue;
      }
      if (this.state == GameState.MULTIPLAYER_LOBBY_HOSTING) {
        if (message.contains("LOBBY: Initial Request: ")) {
          if (split(message, ":").length > 2) {
            this.lobbyClients.add(new Joinee(someClient, this.portHosting, "LOBBY: ", trim(split(message, ":")[2])));
          }
        }
      }
      this.messageQ.add(message);
    }
  }
  
  public int getJoineeIndex(String[] splitMessage) {
    if (splitMessage.length < 3) {
      return -1;
    }
    for(int i = 0; i < this.lobbyClients.size(); i++) {
      if (this.lobbyClients.get(i).id.equals(trim(splitMessage[2]))) {
        return i;
      }
    }
    return -1;
  }
  
  public void update() {
    this.buttons.update(this.state);
    String[] lbStrings;
    switch(this.state) {
      case MAIN_MENU:
        if (this.lobbyClients.size() == 0) {
          break;
        }
        rectMode(CORNERS);
        fill(constants.defaultBackgroundColor);
        stroke(constants.defaultBackgroundColor);
        rect(10, 720, 265, 738);
        textSize(13);
        textAlign(LEFT, TOP);
        fill(0);
        float currY = 725;
        String s1 = "Game             ";
        String s2 = "IP Address         ";
        float width1 = textWidth(s1);
        float width2 = textWidth(s1 + s2);
        text(s1 + s2 + "Ping", 10, currY);
        textSize(12);
        lbStrings = new String[0];
        for(int i = 0; i < this.lobbyClients.size(); i++) {
          Joinee j = this.lobbyClients.get(i);
          if ((j == null) || (j.client == null)) {
            println("A client was null and was removed.");
            this.lobbyClients.remove(i);
            i--;
            continue;
          }
          boolean displayMessage = true;
          boolean removeClient = false;
          if (j.client.active()) {
            if (j.waitingForResponse) {
              if (j.ping < 0) {
                displayMessage = false;
              }
              if (millis() - j.lastPingRequest > constants.defaultPingTimeout) {
                if (!j.receivedInitialResponse) {
                  println("Client with id " + j.id + " has not received initial ping resolve so was removed");
                  removeClient = true;
                }
                j.missedPingRequest();
                if (j.pingRequestsMissed > constants.maxPingRequestsMissed) {
                  println("Client with id " + j.id + " has missed " + j.pingRequestsMissed + " ping requests so was removed.");
                  removeClient = true;
                }
              }
            }
            else if (millis() - j.lastPingRequest > constants.pingRequestFrequency) {
              j.pingRequest();
            }
          }
          else {
            removeClient = true;
          }
          if (removeClient) {
            j.client.stop();
            this.lobbyClients.remove(i);
            i--;
          }
          else if (displayMessage) {
            int firstGap = round((width1 - textWidth(j.name + " ")) / textWidth(" "));
            int secondGap = round((width2 - textWidth(j.name + " " + multiplyString(" ", firstGap) + j.client.ip() + ": ")) / textWidth(" "));
            lbStrings = append(lbStrings, j.name + " " + multiplyString(" ", firstGap) + j.client.ip() + " " + multiplyString(" ", secondGap) + j.ping + " ms");
          }
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      case CONNECTING_TO_LOBBY:
        if (!this.otherPlayer.client.active()) {
          this.otherPlayer.client.stop();
          this.otherPlayer = null;
          this.goToMainMenu();
          break;
        }
        if (this.otherPlayer.receivedInitialResponse && !this.otherPlayer.waitingForResponse) {
          this.otherPlayer.write("Join Lobby");
          this.otherPlayer.waitingForResponse = true;
          break;
        }
        break;
      case SINGLEPLAYER:
        if (this.paused) {
          return;
        }
        if (this.myGame.isOver()) {
          this.myGame = null;
          this.goToMainMenu();
          break;
        }
        this.myGame.update();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        rectMode(CORNERS);
        fill(constants.defaultBackgroundColor);
        stroke(constants.defaultBackgroundColor);
        rect(10, 720, 265, 738);
        textSize(13);
        textAlign(LEFT, TOP);
        fill(0);
        text("Players in lobby", 10, 725);
        lbStrings = new String[0];
        lbStrings = append(lbStrings, "You (0 ms)");
        boolean removeClient = false;
        if (this.otherPlayer != null) {
          if (this.otherPlayer.client.active()) {
            lbStrings = append(lbStrings, "Other player (" + this.otherPlayer.ping + " ms)");
            if (this.otherPlayer.waitingForResponse) {
              if (millis() - this.otherPlayer.lastPingRequest > constants.defaultPingTimeout) {
                this.otherPlayer.missedPingRequest();
                if (this.otherPlayer.pingRequestsMissed > constants.maxPingRequestsMissed) {
                  removeClient = true;
                }
              }
            }
            else if (millis() - this.otherPlayer.lastPingRequest > constants.pingRequestFrequency) {
              this.otherPlayer.pingRequest();
            }
          }
          else {
            removeClient = true;
          }
        }
        if (removeClient) {
          this.otherPlayer.client.stop();
          this.otherPlayer = null;
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      case MULTIPLAYER_LOBBY_JOINED:
        rectMode(CORNERS);
        fill(constants.defaultBackgroundColor);
        stroke(constants.defaultBackgroundColor);
        rect(10, 720, 265, 738);
        textSize(13);
        textAlign(LEFT, TOP);
        fill(0);
        text("Connection to lobby", 10, 725);
        lbStrings = new String[0];
        boolean leaveLobby = false;
        if (this.otherPlayer != null) {
          if (this.otherPlayer.client.active()) {
            lbStrings = append(lbStrings, trim(split(this.otherPlayer.name, ":")[0]) + "  (" + this.otherPlayer.ping + " ms)");
            if (this.otherPlayer.waitingForResponse) {
              if (millis() - this.otherPlayer.lastPingRequest > constants.defaultPingTimeout) {
                if (!this.otherPlayer.receivedInitialResponse) {
                  leaveLobby = true;
                }
                this.otherPlayer.missedPingRequest();
                if (this.otherPlayer.pingRequestsMissed > constants.maxPingRequestsMissed) {
                  leaveLobby = true;
                }
              }
            }
            else if (millis() - this.otherPlayer.lastPingRequest > constants.pingRequestFrequency) {
              this.otherPlayer.pingRequest();
            }
          }
          else {
            leaveLobby = true;
          }
          if (leaveLobby) {
            this.otherPlayer.client.stop();
            this.otherPlayer = null;
            showMessageDialog(null, "You lost connection to the lobby", "", PLAIN_MESSAGE);
            this.goToMainMenu();
          }
        }
        else {
          showMessageDialog(null, "There was an error connecting to the lobby", "", PLAIN_MESSAGE);
          this.goToMainMenu();
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      case MULTIPLAYER_HOSTING:
        boolean hostGameOver = this.myGame.gameOver;
        boolean joineeGameOver = this.otherGame.gameOver;
        String myGameChanges = this.myGame.update("| HOST_GAME: ", false);
        String otherGameChanges = this.otherGame.update("| JOINEE_GAME: ", false);
        // check for game overs
        if ((!hostGameOver) && (this.myGame.gameOver)) {
          this.buttons.paB.setDIS(false);
          this.buttons.paB.changeColor(color(220), color(0), color(170), color(120));
          if (!joineeGameOver) {
            this.myGame.addGameOverMessage("You", "Lost");
            this.myGame.drawBoard();
            myGameChanges += "| HOST_GAME: addGameOverMessage=You, Lost";
            this.otherGame.addGameOverMessage("You", "Won");
            otherGameChanges += "| JOINEE_GAME: addGameOverMessage=You, Won";
          }
        }
        if ((!joineeGameOver) && (this.otherGame.gameOver)) {
          if (!hostGameOver) {
            this.myGame.addGameOverMessage("You", "Won");
            myGameChanges += "| HOST_GAME: addGameOverMessage=You, Won";
            this.otherGame.addGameOverMessage("You", "Lost");
            this.otherGame.drawBoard();
            otherGameChanges += "| JOINEE_GAME: addGameOverMessage=You, Lost";
          }
        }
        if (!myGameChanges.equals("")) {
          this.server.write(myGameChanges);
        }
        if (!otherGameChanges.equals("")) {
          this.server.write(otherGameChanges);
        }
        rectMode(CORNERS);
        fill(constants.defaultBackgroundColor);
        stroke(constants.defaultBackgroundColor);
        rect(10, 720, 265, 738);
        textSize(13);
        textAlign(LEFT, TOP);
        fill(0);
        text("Connection to Players", 10, 725);
        lbStrings = new String[0];
        boolean kickOpponent = false;
        if (this.otherPlayer != null) {
          if (this.otherPlayer.client.active()) {
            lbStrings = append(lbStrings, "Other Player (" + this.otherPlayer.ping + " ms)");
            if (this.otherPlayer.waitingForResponse) {
              if (millis() - this.otherPlayer.lastPingRequest > constants.defaultPingTimeout) {
                this.otherPlayer.missedPingRequest();
                if (this.otherPlayer.pingRequestsMissed > constants.maxPingRequestsMissed) {
                  kickOpponent = true;
                }
              }
            }
            else if (millis() - this.otherPlayer.lastPingRequest > constants.pingRequestFrequency) {
              this.otherPlayer.pingRequest();
            }
          }
          else {
            kickOpponent = true;
          }
          if (kickOpponent) {
            this.otherPlayer.client.stop();
            this.otherPlayer = null;
            showMessageDialog(null, "You lost connection to your opponent", "", PLAIN_MESSAGE);
            this.goToMainMenu();
          }
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      case MULTIPLAYER_JOINED:
        rectMode(CORNERS);
        fill(constants.defaultBackgroundColor);
        stroke(constants.defaultBackgroundColor);
        rect(10, 720, 265, 738);
        textSize(13);
        textAlign(LEFT, TOP);
        fill(0);
        text("Connection to Host", 10, 725);
        lbStrings = new String[0];
        boolean leaveGame = false;
        if (this.otherPlayer != null) {
          if (this.otherPlayer.client.active()) {
            lbStrings = append(lbStrings, trim(split(this.otherPlayer.name, ":")[0]) + " (" + this.otherPlayer.ping + " ms)");
            if (this.otherPlayer.waitingForResponse) {
              if (millis() - this.otherPlayer.lastPingRequest > constants.defaultPingTimeout) {
                this.otherPlayer.missedPingRequest();
                if (this.otherPlayer.pingRequestsMissed > constants.maxPingRequestsMissed) {
                  leaveGame = true;
                }
              }
            }
            else if (millis() - this.otherPlayer.lastPingRequest > constants.pingRequestFrequency) {
              this.otherPlayer.pingRequest();
            }
          }
          else {
            leaveGame = true;
          }
          if (leaveGame) {
            this.otherPlayer.client.stop();
            this.otherPlayer = null;
            showMessageDialog(null, "You lost connection to the game", "", PLAIN_MESSAGE);
            this.goToMainMenu();
          }
        }
        else {
          showMessageDialog(null, "There was an error connecting to the game", "", PLAIN_MESSAGE);
          this.goToMainMenu();
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      default:
        break;
    }
    while(this.messageQ.peek() != null) {
      String message = this.messageQ.remove();
      println("time: " + millis() + "\n  " + message);
      String[] splitMessage = split(message, ':');
      if (splitMessage.length < 2) {
        println("ERROR: invalid message: " + splitMessage);
        continue;
      }
      int index = this.getJoineeIndex(splitMessage);
      switch(this.state) {
        case MAIN_MENU:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              if (index == -1) {
                println("ERROR: client ID not found.");
                break;
              }
              switch(trim(splitMessage[1])) {
                case "Ping Resolve":
                  if (this.lobbyClients.get(index).messageForMe(splitMessage)) {
                    this.lobbyClients.get(index).resolvePingRequest();
                  }
                  break;
                case "Initial Resolve":
                  if (splitMessage.length < 5) {
                    println("ERROR: initial resolve message invalid");
                    break;
                  }
                  if (this.lobbyClients.get(index).messageForMe(splitMessage)) {
                    boolean duplicateConnection = false;
                    for (int i = 0; i < this.lobbyClients.size(); i++) {
                      if (i == index) {
                        continue;
                      }
                      Joinee j = lobbyClients.get(i);
                      if ((j.receivedInitialResponse) && (j.name.equals(trim(splitMessage[3])))) {
                        duplicateConnection = true;
                        break;
                      }
                    }
                    if (duplicateConnection) {
                      println("Duplicate connection so removing " + this.lobbyClients.get(index).name);
                      this.lobbyClients.get(index).client.stop();
                      this.lobbyClients.remove(index);
                    }
                    else {
                      this.lobbyClients.get(index).resolveInitialRequest(trim(splitMessage[3]), splitMessage[4]);
                    }
                  }
                  break;
                case "Join Lobby":
                  if (this.lobbyClients.get(index).messageForMe(splitMessage)) {
                    this.otherPlayer = this.lobbyClients.get(index);
                    this.lobbyClients.remove(index);
                    for (int i = 0; i < this.lobbyClients.size(); i++) {
                      this.lobbyClients.get(i).client.stop();
                    }
                    this.lobbyClients.clear();
                    this.messageQ.clear();
                    this.buttons.clearButton(0);
                    this.buttons.clearButton(6);
                    this.buttons.cSB.setHIGH(-1);
                    this.state = GameState.MULTIPLAYER_LOBBY_JOINED;
                  }
                  break;
                case "Lobby Full":
                  showMessageDialog(null, "Lobby already has a player", "", PLAIN_MESSAGE);
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        case CONNECTING_TO_LOBBY:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              switch(trim(splitMessage[1])) {
                case "Ping Resolve":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolvePingRequest();
                  }
                  break;
                case "Initial Resolve":
                  if (splitMessage.length < 5) {
                    println("ERROR: initial resolve message invalid");
                    break;
                  }
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolveInitialRequest(trim(splitMessage[3]), splitMessage[4]);
                  }
                  break;
                case "Initial Request":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    if (splitMessage.length < 3) {
                      println("ERROR: No IP address for initial request");
                      break;
                    }
                    this.otherPlayer.write("Initial Resolve");
                  }
                  break;
                case "Join Lobby":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.lobbyClients.clear();
                    this.messageQ.clear();
                    this.otherPlayer.waitingForResponse = false;
                    this.state = GameState.MULTIPLAYER_LOBBY_JOINED;
                    this.buttons.clearButton(5);
                    this.buttons.clearButton(6);
                  }
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        case MULTIPLAYER_LOBBY_HOSTING:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              switch(trim(splitMessage[1])) {
                case "Quit Lobby":
                  println("Your client quit the lobby");
                  if (this.otherPlayer != null) {
                    if (this.otherPlayer.client != null) {
                      this.otherPlayer.client.stop();
                    }
                    this.otherPlayer = null;
                  }
                  break;
                case "Ping Request":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for ping request");
                    break;
                  }
                  this.server.write("| LOBBY: Ping Resolve: " + trim(splitMessage[2]));
                  break;
                case "Ping Resolve":
                  if (this.otherPlayer != null) {
                    if (this.otherPlayer.messageForMe(splitMessage)) {
                      this.otherPlayer.resolvePingRequest();
                    }
                  }
                  break;
                case "Initial Request":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for initial request");
                    break;
                  }
                  this.server.write("| LOBBY: Initial Resolve: " + trim(splitMessage[2]) + ": " + this.lobbyName + ":Game             :");
                  break;
                case "Initial Resolve":
                  if (splitMessage.length < 3) {
                    println("ERROR: initial resolve message invalid");
                    break;
                  }
                  if (this.otherPlayer != null) {
                    if (this.otherPlayer.messageForMe(splitMessage)) {
                      this.otherPlayer.resolveInitialRequest();
                    }
                  }
                  break;
                case "Join Lobby":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for ping request");
                    break;
                  }
                  if (this.otherPlayer == null) {
                    if (index == -1) {
                      println("ERROR: client ID not found.");
                      break;
                    }
                    this.server.write("| LOBBY: Join Lobby: " + trim(splitMessage[2]));
                    this.otherPlayer = this.lobbyClients.get(index);
                    this.lobbyClients.remove(index);
                  }
                  else {
                    this.server.write("| LOBBY: Lobby Full: " + trim(splitMessage[2]));
                  }
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        case MULTIPLAYER_LOBBY_JOINED:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              switch(trim(splitMessage[1])) {
                case "Start Game":
                  println("Game is starting");
                  this.myGame = new Game(constants.game1Borders);
                  this.otherGame = new Game(constants.game2Borders);
                  this.messageQ.clear();
                  this.state = GameState.MULTIPLAYER_JOINED;
                  break;
                case "Kick Player":
                  println("You were kicked from the lobby");
                  this.otherPlayer = null;
                  this.messageQ.clear();
                  this.goToMainMenu();
                  break;
                case "Quit Lobby":
                  println("The host quit the lobby");
                  this.otherPlayer = null;
                  this.messageQ.clear();
                  this.goToMainMenu();
                  break;
                case "Initial Request":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    if (splitMessage.length < 3) {
                      println("ERROR: No IP address for initial request");
                      break;
                    }
                    this.otherPlayer.write("Initial Resolve");
                  }
                  break;
                case "Ping Request":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    if (splitMessage.length < 3) {
                      println("ERROR: No IP address for ping request");
                      break;
                    }
                    this.otherPlayer.write("Ping Resolve");
                  }
                  break;
                case "Ping Resolve":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolvePingRequest();
                  }
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        case MULTIPLAYER_HOSTING:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              switch(trim(splitMessage[1])) {
                case "Quit Lobby":
                  println("Your opponent quit the game");
                  if (this.otherPlayer != null) {
                    if (this.otherPlayer.client != null) {
                      this.otherPlayer.client.stop();
                    }
                    this.otherPlayer = null;
                  }
                  break;
                case "Ping Request":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for ping request");
                    break;
                  }
                  this.server.write("| LOBBY: Ping Resolve: " + trim(splitMessage[2]));
                  break;
                case "Ping Resolve":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolvePingRequest();
                  }
                  break;
                case "Joinee Rematch Sent":
                  this.wantRematch[1] = true;
                  this.checkRematches();
                  break;
                case "Joinee Rematch Revoked":
                  this.wantRematch[1] = false;
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            case "HOST_GAME":
              if (this.myGame.executeMessage(trim(splitMessage[1]))) {
                this.server.write("|" + message);
              }
              else {
                println("ERROR: HOST_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            case "JOINEE_GAME":
              if (this.otherGame.executeMessage(trim(splitMessage[1]))) {
                this.server.write("|" + message);
              }
              else {
                println("ERROR: JOINEE_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        case MULTIPLAYER_JOINED:
          switch(trim(splitMessage[0])) {
            case "LOBBY":
              switch(trim(splitMessage[1])) {
                case "Quit Lobby":
                  println("Your host quit the game");
                  if (this.otherPlayer != null) {
                    if (this.otherPlayer.client != null) {
                      this.otherPlayer.client.stop();
                    }
                    this.otherPlayer = null;
                  }
                  this.goToMainMenu();
                  break;
                case "Start Game":
                  println("Game is starting");
                  this.buttons.paB = new playAgainButton();
                  this.wantRematch[0] = false;
                  this.wantRematch[1] = false;
                  this.myGame = new Game(constants.game1Borders);
                  this.otherGame = new Game(constants.game2Borders);
                  this.messageQ.clear();
                  break;
                case "Ping Request":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for ping request");
                    break;
                  }
                  this.otherPlayer.write("Ping Resolve");
                  break;
                case "Ping Resolve":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolvePingRequest();
                  }
                  break;
                case "Host Rematch Sent":
                  this.wantRematch[1] = true;
                  break;
                case "Host Rematch Revoked":
                  this.wantRematch[1] = false;
                  break;
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            case "HOST_GAME":
              if (!this.otherGame.executeMessage(trim(splitMessage[1]))) {
                println("ERROR: HOST_GAMEfdas message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            case "JOINEE_GAME":
              if (!this.myGame.executeMessage(trim(splitMessage[1]))) {
                println("ERROR: JOINEE_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              if (splitMessage[1].contains("gameOver")) {
                this.buttons.paB.setDIS(false);
                this.buttons.paB.changeColor(color(220), color(0), color(170), color(120));
              }
              break;
            default:
              println("ERROR: message header not recognized -> " + trim(splitMessage[0]));
              break;
          }
          break;
        default:
          break;
      }
    }
  }
  
  public void keyPress() {
    switch(this.state) {
      case SINGLEPLAYER:
        this.myGame.pressedKey();
        break;
      case MULTIPLAYER_HOSTING:
        String myGameChanges = this.myGame.pressedKey("| HOST_GAME: ");
        if (!myGameChanges.equals("")) {
          this.server.write(myGameChanges);
        }
        break;
      case MULTIPLAYER_JOINED:
        String gameChanges = this.myGame.pressedKey("| JOINEE_GAME: ", false);
        if (!gameChanges.equals("")) {
          this.otherPlayer.client.write(gameChanges);
        }
        break;
      default:
        break;
    }
  }
  
  public void mousePress() {
    this.buttons.mousePress(this.state);
  }
  public void mouseRelease() {
    this.buttons.mouseRelease(this.state);
  }
  public void scroll(int count) {
    this.buttons.scroll(count);
  }
}
class Game {
  private Board board;
  private ArrayList<Piece> nextPieces = new ArrayList<Piece>();
  private Piece savedPiece = null;
  private int tickLength = constants.defaultTickLength;
  private int lastTick;
  private float xi = 0;
  private float yi = 0;
  private float xf = 0;
  private float yf = 0;
  private boolean gameOver = false;
  private boolean displayGameOverMessage = false;
  private String[] gameOverMessage = new String[]{"", ""};
  private HashMap<String, Integer> statistics = new HashMap<String, Integer>();
  
  Game(float[] borders) {
    float xStart = borders[0] + (PApplet.parseFloat(constants.defaultBoardColumns + 2) / (constants.defaultBoardRows + 2)) * (borders[2] - borders[0]);
    this.board = new Board(borders[0], borders[1], xStart, borders[3]);
    this.xi = xStart;
    this.yi = borders[1];
    this.xf = borders[2];
    this.yf = borders[3];
    this.lastTick = millis();
    this.initializeStatistics();
    this.board.drawBoard();
    this.drawPanel();
  }
  
  public void initializeStatistics() {
    this.statistics.put("Points", 0);
    this.statistics.put("Ticks", 0);
    this.statistics.put("Pieces", 0);
    this.statistics.put("Rows Cleared", 0);
    this.statistics.put("Double Combos", 0);
    this.statistics.put("Triple Combos", 0);
    this.statistics.put("Quadruple Combos", 0);
  }
  
  public void incrementStatistic(String statistic) {
    this.increaseStatistic(statistic, 1);
  }
  public void increaseStatistic(String statistic, int amount) {
    Integer stat = this.statistics.get(statistic);
    if (stat == null) {
      println("ERROR: statistic " + statistic + " is not defined.");
      return;
    }
    this.statistics.put(statistic, stat + amount);
  }
  
  public boolean isOver() {
    return this.gameOver;
  }
  
  // Update returns string with all game changes
  public String update() {
    return this.update("", true);
  }
  public String update(String gameName, boolean singlePlayer) {
    String updates = "";
    if (this.gameOver) {
      return "";
    }
    if (millis() - this.lastTick > this.tickLength) {
      updates += gameName + "tick";
      this.incrementStatistic("Ticks");
      this.increaseStatistic("Points", constants.scoreTick);
      if (this.board.aPieceFalling()) {
        this.movePieces(Direction.DIRECTION_DOWN, true);
        updates += gameName + "movePieces=DOWN, true";
      }
      else {
        this.gameOver = this.board.getPieceOverflow();
        if (this.gameOver) {
          if (singlePlayer) {
            this.addGameOverMessage("Game", "Over");
          }
          this.drawBoard();
          this.showStats();
          updates += gameName + "gameOver";
          return updates;
        }
        this.checkFilledRows();
        updates += gameName + "checkFilledRows";
        Piece newPiece = new Piece(0);
        this.addPiece(newPiece);
        updates += gameName + "addPiece=" + newPiece.getShapeName();
      }
      this.lastTick = millis();
    }
    this.drawBoard();
    updates += gameName + "drawBoard";
    return updates;
  }
  
  public void drawBoard() {
    this.board.drawBoard();
    if (this.gameOver) {
      this.showStats();
    }
    else {
      this.drawPanel();
    }
    if (this.displayGameOverMessage) {
      this.drawGameOverMessage();
    }
  }
  
  public void drawPanel() {
    // background
    fill(0);
    stroke(0);
    rectMode(CORNERS);
    rect(this.xi, this.yi, this.xf, this.yf);
    // Points
    fill(255);
    textSize(26);
    textAlign(CENTER, TOP);
    text("Points", this.xi + 0.5f * (this.xf - this.xi), this.yi);
    textSize(20);
    text(this.statistics.get("Points"), this.xi + 0.5f * (this.xf - this.xi), this.yi + 30);
    textSize(26);
    textAlign(CENTER, BOTTOM);
    // next pieces
    float gapSize = 0.3f * (this.xf - this.xi);
    /*
    float pieceLength = (this.xf - this.xi) / constants.nextPieceQueueLength;
    for(int i = 0; i < this.nextPieces.size(); i++) {
      this.nextPieces.get(i).drawPiece(xi + gapSize + 0.9 * i * pieceLength, yi + gapSize, xi + gapSize + 0.9 * (i + 1) * pieceLength, yi + 0.25 * (yf - yi));
    }
    */
    text("Next Piece", this.xi + 0.5f * (this.xf - this.xi), this.yi + 0.34f * (this.yf - this.yi));
    if (this.nextPieces.size() > 0) {
      this.nextPieces.get(0).drawPiece(this.xi + gapSize, this.yi + 0.35f * (this.yf - this.yi) + 0.2f * gapSize, this.xf - gapSize, this.yi + 0.6f * (this.yf - this.yi));
    }
    // saved piece
    fill(255);
    text("Saved Piece", this.xi + 0.5f * (this.xf - this.xi), this.yi + 0.74f * (this.yf - this.yi));
    if (this.savedPiece != null) {
      this.savedPiece.drawPiece(this.xi + 1.2f * gapSize, this.yi + 0.75f * (this.yf - this.yi) + 0.2f * gapSize, this.xf - 1.2f * gapSize, this.yi + 0.95f * (this.yf - this.yi));
    }
  }
  
  public void showStats() {
    // background
    fill(0);
    stroke(0);
    rectMode(CORNERS);
    rect(this.xi, this.yi, this.xf, this.yf);
    int textHeight = 30;
    // Points
    fill(255);
    textSize(26);
    textAlign(CENTER, TOP);
    text("Points", this.xi + 0.5f * (this.xf - this.xi), this.yi);
    textSize(20);
    text(this.statistics.get("Points"), this.xi + 0.5f * (this.xf - this.xi), this.yi + textHeight);
    // Other stat headers
    textSize(24);
    textAlign(LEFT, TOP);
    text("Time Survived", this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 3);
    text("Pieces Used", this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 6);
    text("Rows Cleared", this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 9);
    text("Doubles", this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 12);
    text("Triples", this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 15);
    text("Quadrupels", this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 18);
    // Other stats
    textSize(18);
    text(this.statistics.get("Ticks") + " ticks", this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 4);
    text(this.statistics.get("Pieces"), this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 7);
    text(this.statistics.get("Rows Cleared"), this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 10);
    text(this.statistics.get("Double Combos"), this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 13);
    text(this.statistics.get("Triple Combos"), this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 16);
    text(this.statistics.get("Quadruple Combos"), this.xi + 0.1f * (this.xf - this.xi), this.yi + textHeight * 19);
  }
  
  public void addGameOverMessage(String s1, String s2) {
    this.displayGameOverMessage = true;
    this.gameOverMessage[0] = s1;
    this.gameOverMessage[1] = s2;
  }
  public void clearGameOverMessage() {
    this.displayGameOverMessage = false;
  }
  public void drawGameOverMessage() {
    fill(color(0), 150);
    stroke(color(0), 150);
    rectMode(CORNERS);
    rect(this.board.xi, this.board.yi, this.board.xf, this.board.yf);
    fill(255);
    textSize(60);
    textAlign(CENTER, BOTTOM);
    text(this.gameOverMessage[0], this.board.xi + 0.5f * (this.board.xf - this.board.xi), this.board.yi + 0.5f * (this.board.yf - this.board.yi));
    textAlign(CENTER, TOP);
    text(this.gameOverMessage[1], this.board.xi + 0.5f * (this.board.xf - this.board.xi), this.board.yi + 0.5f * (this.board.yf - this.board.yi));
  }
  
  public void addPiece(Piece p) {
    if (this.nextPieces.size() == constants.nextPieceQueueLength) {
      this.board.addPiece(this.nextPieces.get(0));
      this.incrementStatistic("Pieces");
      this.increaseStatistic("Points", constants.scorePiece);
      this.nextPieces.remove(0);
    }
    this.nextPieces.add(p);
  }
  public void movePieces() {
    this.board.movePiece();
  }
  public void movePieces(Direction dir, boolean stopFalling) {
    this.board.movePiece(dir, stopFalling);
  }
  public void rotatePieces() {
    this.board.rotatePiece();
  }
  public void rotatePieces(boolean clockwise) {
    this.board.rotatePiece(clockwise);
  }
  public void savePiece() {
    this.savedPiece = this.board.replaceSavedPiece(this.savedPiece);
    this.drawPanel();
  }
  
  public void checkFilledRows() {
    int rows = this.board.checkFilledRows();
    this.increaseStatistic("Rows Cleared", rows);
    this.increaseStatistic("Points", rows * constants.scoreRow);
    switch(rows) {
      case 2:
        this.incrementStatistic("Double Combos");
        this.increaseStatistic("Points", constants.scoreDouble);
        break;
      case 3:
        this.incrementStatistic("Triple Combos");
        this.increaseStatistic("Points", constants.scoreTriple);
        break;
      case 4:
        this.incrementStatistic("Quadruple Combos");
        this.increaseStatistic("Points", constants.scoreQuadruple);
        break;
    }
  }
  
  public String pressedKey() {
    return this.pressedKey("", true);
  }
  public String pressedKey(String gameName) {
    return this.pressedKey(gameName, true);
  }
  public String pressedKey(String gameName, boolean executeActions) {
    if (this.gameOver) {
      return "";
    }
    String updates = "";
    if (key == CODED) {
      switch(keyCode) {
        case UP:
          if (executeActions) {
            this.rotatePieces();
          }
          updates += gameName + "rotatePieces";
          break;
        case DOWN:
          if (executeActions) {
            this.movePieces(Direction.DIRECTION_DOWN, false);
          }
          updates += gameName + "movePieces=DOWN, false";
          break;
        case LEFT:
          if (executeActions) {
            this.movePieces(Direction.DIRECTION_LEFT, false);
          }
          updates += gameName + "movePieces=LEFT, false";
          break;
        case RIGHT:
          if (executeActions) {
            this.movePieces(Direction.DIRECTION_RIGHT, false);
          }
          updates += gameName + "movePieces=RIGHT, false";
          break;
      }
    }
    else {
      switch(key) {
        case ' ':
          if (executeActions) {
            this.board.dropPiece();
          }
          updates += gameName + "dropPieces";
          break;
        case 'c':
          if (executeActions) {
            this.savePiece();
          }
          updates += gameName + "savePiece";
          break;
      }
    }
    if (executeActions) {
      this.drawBoard();
    }
    updates += gameName + "drawBoard";
    return updates;
  }
  
  // Returns whether message was executed
  public boolean executeMessage(String message) {
    if (message.equals("")) {
      return false;
    }
    String[] messageSplit = split(message, "=");
    if (this.gameOver) {
      if (!trim(messageSplit[0]).equals("addGameOverMessage")) {
        return false;
      }
    }
    switch(trim(messageSplit[0])) {
      case "checkFilledRows":
        this.checkFilledRows();
        break;
      case "addPiece":
        if (messageSplit.length < 2) {
          return false;
        }
        String shapeName = trim(messageSplit[1]);
        Piece newPiece = new Piece(this.board.spaces.length, shapeName);
        this.addPiece(newPiece);
        break;
      case "drawBoard":
        this.drawBoard();
        break;
      case "drawPanel":
        this.drawPanel();
        break;
      case "movePieces":
        if (messageSplit.length > 1) {
          String[] parameters = split(trim(messageSplit[1]), ',');
          if (parameters.length != 2) {
            return false;
          }
          Direction direction = Direction.DIRECTION_DOWN;
          switch(trim(parameters[0])) {
            case "DOWN":
              direction = Direction.DIRECTION_DOWN;
              break;
            case "RIGHT":
              direction = Direction.DIRECTION_RIGHT;
              break;
            case "LEFT":
              direction = Direction.DIRECTION_LEFT;
              break;
            case "UP":
              direction = Direction.DIRECTION_UP;
              break;
            default:
              return false;
          }
          boolean stopFalling = false;
          switch(trim(parameters[1])) {
            case "true":
              stopFalling = true;
              break;
            case "false":
              stopFalling = false;
              break;
            default:
              return false;
          }
          this.movePieces(direction, stopFalling);
        }
        else {
          this.movePieces();
        }
        break;
      case "rotatePieces":
        this.rotatePieces();
        break;
      case "dropPieces":
        this.board.dropPiece();
        break;
      case "savePiece":
        this.savePiece();
        break;
      case "gameOver":
        this.gameOver = true;
        this.showStats();
        break;
      case "addGameOverMessage":
        if (messageSplit.length > 1) {
          String[] parameters = split(trim(messageSplit[1]), ",");
          if (parameters.length != 2) {
            return false;
          }
          this.addGameOverMessage(trim(parameters[0]), trim(parameters[1]));
          this.drawBoard();
        }
        break;
      case "tick":
        this.incrementStatistic("Ticks");
        this.increaseStatistic("Points", constants.scoreTick);
        break;
      default:
        return false;
    }
    return true;
  }
}
class Joinee {
  private Client client;
  private String id;
  private String writeHeader = "";
  private String name = "";
  private int port;
  private int ping = -1;
  private int lastPingRequest = 0;
  private boolean waitingForResponse = false;
  private boolean receivedInitialResponse = false;
  private int pingRequestsMissed = 0;
  
  Joinee(Client newClient, int port, String header) {
    this.client = newClient;
    this.port = port;
    this.writeHeader = header;
    if (newClient != null) {
      this.id = newClient.ip() + ", " + port + ", " + millis();
      this.initialRequest();
    }
    println("New joinee made with id: " + this.id);
  }
  Joinee(Client newClient, int port, String header, String id) {
    this.client = newClient;
    this.port = port;
    this.writeHeader = header;
    this.id = id;
    println("New joinee made with set id to: " + this.id);
  }
  
  public boolean messageForMe(String message) {
    return this.messageForMe(split(message, ":"));
  }
  public boolean messageForMe(String[] splitMessage) {
    if (splitMessage.length >= 3) {
      if (this.id.equals(trim(splitMessage[2]))) {
        return true;
      }
    }
    return false;
  }
  
  public void setNewName(String newName, String s1) {
    this.name = "";
    for (char i : newName.toCharArray()) {
      if (textWidth(this.name + i) < textWidth(s1 + " ")) {
        this.name += i;
      }
      else {
        break;
      }
    }
  }
  
  public void initialRequest() {
    if ((this.client != null) && (this.client.active())) {
      this.lastPingRequest = millis();
      this.write("Initial Request");
      this.waitingForResponse = true;
    }
  }
  public void resolveInitialRequest() {
    this.resolveInitialRequest("", "");
  }
  public void resolveInitialRequest(String newName, String s1) {
    if ((this.client != null) && (this.client.active())) {
      if (waitingForResponse && !receivedInitialResponse) {
        this.ping = millis() - this.lastPingRequest;
        this.lastPingRequest = millis();
        this.waitingForResponse = false;
        this.receivedInitialResponse = true;
        this.setNewName(newName, s1);
      }
    }
  }
  
  public void pingRequest() {
    if ((this.client != null) && (this.client.active())) {
      this.lastPingRequest = millis();
      this.write("Ping Request");
      this.waitingForResponse = true;
    }
  }
  public void resolvePingRequest() {
    if ((this.client != null) && (this.client.active())) {
      if (waitingForResponse) {
        this.ping = millis() - this.lastPingRequest;
        this.lastPingRequest = millis();
        this.waitingForResponse = false;
        this.pingRequestsMissed = 0;
      }
    }
  }
  public void missedPingRequest() {
    this.waitingForResponse = false;
    this.ping = millis() - this.lastPingRequest + this.pingRequestsMissed * constants.defaultPingTimeout;
    this.pingRequestsMissed++;
  }
  
  public void write(String message) {
    if ((this.client != null) && (this.client.active())) {
      this.client.write("|" + this.writeHeader + message + ": " + this.id);
    }
  }
}
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

public int dynamicColorChanger(int c) {
  float c_r = c >> 16 & 0xFF;
  float c_g = c >> 8 & 0xFF;
  float c_b = c & 0xFF;
  float time = millis() / 3.0f;
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
  int returnColor = color(c_r, c_g, c_b);
  return returnColor;
}

public int ColorToColor(Color c) {
  return stringToColor(c.getColorName());
}
public int stringToColor(String colorName) {
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

public User createNewUser(ArrayList<String> existingUsernames) {
  String userName = showInputDialog(null, "What should we call you?", "Tetris", PLAIN_MESSAGE);
  while (true) {
    if (userName == null) {
      userName = showInputDialog(null, "Please enter a name", "Tetris", PLAIN_MESSAGE);
      continue;
    }
    if (userName == "") {
      userName = showInputDialog(null, "Please enter a name", "Tetris", PLAIN_MESSAGE);
      continue;
    }
    for (String name : existingUsernames) {
      if (userName.equals(name)) {
        userName = showInputDialog(null, "Username already in use", "Tetris", PLAIN_MESSAGE);
        continue;
      }
    }
    break;
  }
  return new User(userName);
}
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
  private String defaultUsername;
  private ArrayList<String> usernames = new ArrayList<String>();
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
        case "defaultUsername":
          this.defaultUsername = option;
          break;
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
  
  public User chooseDefaultUsername() {
    User user;
    while(true) {
      if (this.usernames.size() == 0) {
        user = createNewUser(this.usernames);
        this.defaultUsername = user.name;
        break;
      }
      else {
        String response = null;
        while (response == null) {
          response = (String)showInputDialog(null, "Choose a user", "Tetris", PLAIN_MESSAGE, null, this.usernames.toArray(), this.usernames.get(0));
        }
        try {
          user = new User(this.usernames.get(this.usernames.indexOf(response)));
          break;
        } catch (Exception e) {
          continue;
        }
      }
    }
    this.saveOptions();
    return user;
  }
  
  public void saveOptions() {
    PrintWriter optionsFile = createWriter("options.tetris");
    if (this.defaultUsername != null) {
      optionsFile.println("defaultUsername: " + this.defaultUsername);
    }
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
public enum Shape {
  I_BLOCK, J_BLOCK, L_BLOCK, O_BLOCK, S_BLOCK, T_BLOCK, Z_BLOCK;
  private static final List<Shape> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
  private static final int SIZE = VALUES.size();
  private static final Random RANDOM = new Random();
  public static Shape randomShape() {
    return VALUES.get(RANDOM.nextInt(SIZE));
  }
}

public enum Direction {
  DIRECTION_UP, DIRECTION_RIGHT, DIRECTION_DOWN, DIRECTION_LEFT;
}

public class Piece {
  private Shape shape;
  private int xLocation;
  private int yLocation;
  private Direction rotation = Direction.DIRECTION_UP;
  public Color pieceColor;
  
  Piece() {}
  Piece(int boardSizeX) {
    // get random shape
    this.shape = Shape.randomShape();
    // place shape in middle right above the board
    this.xLocation = boardSizeX / 2 - 1;
    this.yLocation = -2;
    if (this.shape == Shape.I_BLOCK) {
      this.yLocation = -3;
    }
    this.setPieceColor();
  }
  Piece(int boardSizeX, String shapeName) {
    // get random shape
    this.shape = Shape.valueOf(shapeName);
    // place shape in middle right above the board
    this.xLocation = boardSizeX / 2 - 1;
    this.yLocation = -2;
    if (this.shape == Shape.I_BLOCK) {
      this.yLocation = -3;
    }
    this.setPieceColor();
  }
  Piece(Piece piece) {
    this.shape = piece.shape;
    this.xLocation = piece.xLocation;
    this.yLocation = piece.yLocation;
    this.rotation = piece.rotation;
    this.pieceColor = piece.pieceColor;
  }
  
  public Shape getShape() {
    return this.shape;
  }
  public String getShapeName() {
    return this.shape.name();
  }
  public int getXLocation() {
    return this.xLocation;
  }
  public int getYLocation() {
    return this.yLocation;
  }
  public Direction getRotation() {
    return this.rotation;
  }
  public Color getPieceColor() {
    return this.pieceColor;
  }
  
  public void setPieceColor() {
    switch(this.shape) {
      case I_BLOCK:
        this.pieceColor = options.IFill;
        break;
      case J_BLOCK:
        this.pieceColor = options.JFill;
        break;
      case L_BLOCK:
        this.pieceColor = options.LFill;
        break;
      case O_BLOCK:
        this.pieceColor = options.OFill;
        break;
      case S_BLOCK:
        this.pieceColor = options.SFill;
        break;
      case T_BLOCK:
        this.pieceColor = options.TFill;
        break;
      case Z_BLOCK:
        this.pieceColor = options.ZFill;
        break;
    }
  }
  
  public void movePiece(Direction dir) {
    switch(dir) {
      case DIRECTION_UP:
        this.yLocation -= 1;
        break;
      case DIRECTION_RIGHT:
        this.xLocation += 1;
        break;
      case DIRECTION_DOWN:
        this.yLocation += 1;
        break;
      case DIRECTION_LEFT:
        this.xLocation -= 1;
        break;
    }
  }
  public void setXLocation(int x) {
    this.xLocation = x;
  }
  public void setYLocation(int x) {
    this.yLocation = x;
  }
  
  public void rotatePiece() {
    this.rotatePiece(true);
  }
  public void rotatePiece(boolean clockwise) {
    if (this.shape == Shape.O_BLOCK) {
      return;
    }
    switch(this.rotation) {
      case DIRECTION_UP:
        if (clockwise) {
          this.rotation = Direction.DIRECTION_RIGHT;
        }
        else {
          this.rotation = Direction.DIRECTION_LEFT;
        }
        break;
      case DIRECTION_RIGHT:
        if (clockwise) {
          this.rotation = Direction.DIRECTION_DOWN;
        }
        else {
          this.rotation = Direction.DIRECTION_UP;
        }
        break;
      case DIRECTION_DOWN:
        if (clockwise) {
          this.rotation = Direction.DIRECTION_LEFT;
        }
        else {
          this.rotation = Direction.DIRECTION_RIGHT;
        }
        break;
      case DIRECTION_LEFT:
        if (clockwise) {
          this.rotation = Direction.DIRECTION_UP;
        }
        else {
          this.rotation = Direction.DIRECTION_DOWN;
        }
        break;
    }
  }
  
  public ArrayList<Pair<Integer, Integer>> getPieceSpace() {
    ArrayList<Pair<Integer, Integer>> spaces = new ArrayList<Pair<Integer, Integer>>();
    // set square to rotate around
    spaces.add(new Pair(0, 0));
    // add other squares
    switch(this.shape) {
      case I_BLOCK:
        spaces.add(new Pair(0, -1));
        spaces.add(new Pair(0, 1));
        spaces.add(new Pair(0, 2));
        break;
      case J_BLOCK:
        spaces.add(new Pair(0, -1));
        spaces.add(new Pair(0, 1));
        spaces.add(new Pair(-1, 1));
        break;
      case L_BLOCK:
        spaces.add(new Pair(0, 1));
        spaces.add(new Pair(0, -1));
        spaces.add(new Pair(1, 1));
        break;
      case O_BLOCK:
        spaces.add(new Pair(1, 0));
        spaces.add(new Pair(0, 1));
        spaces.add(new Pair(1, 1));
        break;
      case S_BLOCK:
        spaces.add(new Pair(1, 0));
        spaces.add(new Pair(0, 1));
        spaces.add(new Pair(-1, 1));
        break;
      case T_BLOCK:
        spaces.add(new Pair(-1, 0));
        spaces.add(new Pair(0, 1));
        spaces.add(new Pair(1, 0));
        break;
      case Z_BLOCK:
        spaces.add(new Pair(-1, 0));
        spaces.add(new Pair(0, 1));
        spaces.add(new Pair(1, 1));
        break;
    }
    // account for rotation
    switch(this.rotation) {
      case DIRECTION_UP:
        break;
      case DIRECTION_RIGHT:
        for (int i = 0; i < spaces.size(); i++) {
          Pair<Integer, Integer> oldPair = spaces.get(i);
          spaces.set(i, new Pair(-oldPair.getValue(), oldPair.getKey()));
        }
        break;
      case DIRECTION_DOWN:
        for (int i = 0; i < spaces.size(); i++) {
          Pair<Integer, Integer> oldPair = spaces.get(i);
          spaces.set(i, new Pair(-oldPair.getKey(), -oldPair.getValue()));
        }
        break;
      case DIRECTION_LEFT:
        for (int i = 0; i < spaces.size(); i++) {
          Pair<Integer, Integer> oldPair = spaces.get(i);
          spaces.set(i, new Pair(oldPair.getValue(), -oldPair.getKey()));
        }
        break;
      default:
        println("ERROR: Piece rotation " + this.rotation + " invalid.");
        break;
    }
    // account for position
    for (int i = 0; i < spaces.size(); i++) {
      Pair<Integer, Integer> oldPair = spaces.get(i);
      spaces.set(i, new Pair(oldPair.getKey() + this.xLocation, oldPair.getValue() + this.yLocation));
    }
    return spaces;
  }
  
  public void drawPiece(float xi, float yi, float xf, float yf) {
    // get piece spaces
    ArrayList<Pair<Integer, Integer>> spaces = this.getPieceSpace();
    int minX = spaces.get(0).getKey();
    int maxX = minX;
    int minY = spaces.get(0).getValue();
    int maxY = minY;
    // find min/max x/y
    for (Pair<Integer, Integer> i : spaces) {
      int x = i.getKey();
      int y = i.getValue();
      if (x < minX) {
        minX = x;
      }
      else if (x > maxX) {
        maxX = x;
      }
      if (y < minY) {
        minY = y;
      }
      else if (y > maxY) {
        maxY = y;
      }
    }
    // find sidelength
    int xDif = max(constants.minPieceDisplayGridSize, maxX - minX);
    int yDif = max(constants.minPieceDisplayGridSize, maxY - minY);
    float sideLength = min((xf - xi) / xDif, (yf - yi) / yDif);
    // draw squares
    switch(options.pieceStyle) {
      case FLAT_NORMAL:
        fill(stringToColor(this.pieceColor.getColorName()));
        stroke(constants.defaultPieceStroke);
        rectMode(CORNER);
        for (Pair<Integer, Integer> i : spaces) {
          square(xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength);
        }
        break;
      case FLAT_SMOOTH:
        fill(stringToColor(this.pieceColor.getColorName()));
        stroke(stringToColor(this.pieceColor.getColorName()));
        rectMode(CORNER);
        for (Pair<Integer, Integer> i : spaces) {
          square(xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength);
        }
        break;
      case FLAT_DYNAMIC:
        fill(dynamicColorChanger(stringToColor(this.pieceColor.getColorName())));
        stroke(constants.defaultPieceStroke);
        rectMode(CORNER);
        for (Pair<Integer, Integer> i : spaces) {
          square(xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength);
        }
        break;
      case FLAT_FADE:
        imageMode(CORNER);
        PImage image_2d_fade = null;
        switch(this.pieceColor) {
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
          tint(255);
          for (Pair<Integer, Integer> i : spaces) {
            image(image_2d_fade, xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength, sideLength);
          }
        }
        break;
      case RAISED_FADE:
        imageMode(CORNER);
        PImage image_3d_fade_soft = null;
        switch(this.pieceColor) {
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
          tint(255);
          for (Pair<Integer, Integer> i : spaces) {
            image(image_3d_fade_soft, xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength, sideLength);
          }
        }
        break;
      case RAISED_SHARP:
        imageMode(CORNER);
        PImage image_3d_fade_sharp = null;
        switch(this.pieceColor) {
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
          tint(255);
          for (Pair<Integer, Integer> i : spaces) {
            image(image_3d_fade_sharp, xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength, sideLength);
          }
        }
        break;
      case RAISED_NORMAL:
        imageMode(CORNER);
        PImage image_3d_normal = null;
        switch(this.pieceColor) {
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
          tint(255);
          for (Pair<Integer, Integer> i : spaces) {
            image(image_3d_normal, xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength, sideLength);
          }
        }
        break;
      case RAISED_FAT:
        imageMode(CORNER);
        PImage image_3d_fat = null;
        switch(this.pieceColor) {
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
          tint(255);
          for (Pair<Integer, Integer> i : spaces) {
            image(image_3d_fat, xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength, sideLength);
          }
        }
        break;
      default:
        println("ERROR: piecetype not recognized.");
        break;
    }
  }
  
  public void printShapeInfo() {
    println(this.shape.toString());
    println(this.xLocation + " "  + this.yLocation);
  }
}
class tableListBar {
  private listBar listB;
  private float headerHeight;
  private ArrayList<Pair<String, Float>> headers = new ArrayList<Pair<String, Float>>();
  tableListBar(float xi, float yi, float xf, float yf, float headerHeight) {
    if (headerHeight > yf - yi) {
      println("ERROR: headerSize for tableListBar larger than space given");
    }
    this.listB = new listBar(xi, yi + headerHeight, xf, yf);
    this.headerHeight = headerHeight;
  }
  
  public void update(float x, float y) {
    // Draw outer box
    rectMode(CORNERS);
    stroke(0);
    fill(200);
    rect(this.listB.getXI(), this.listB.getYI() - this.headerHeight, this.listB.getXF(), this.listB.getYF());
    // Update listBar
    this.listB.update(x, y);
    // Write header text
    textSize(this.headerHeight - 2);
    textAlign(LEFT, BOTTOM);
    fill(0);
    String headerText = "";
  }
}

class listBar extends scrollBar {
  private int selected = -1; // item selected on list
  listBar(float xi, float yi, float xf, float yf) {
    super(xi, yi, xf, yf);
    this.setDBLCLK(true);
  }
  public void setSEL(int i) {
    this.selected = i;
  } public int getSEL() {
    return this.selected;
  } public void select() {
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
  public void click() { // only purpose is to set yPress
    this.yPress = mouseY;
  }
  public void setLOC(int hidden, int curr, float yi, float yf, float bSize, int tSize) {
    float sizeMax = yf - yi - 2*bSize; // max size it can be
    float actSize = sizeMax - (hidden*tSize); // actual size decreases with each unseen line
    if (actSize < bSize) { // give it minimum size
      actSize = bSize;
    }
    float fractAbove = (float)curr / hidden; // fraction of hidden lines above scroll bar
    float yInit = yi + bSize + (sizeMax - actSize)*fractAbove;
    this.yPress += yInit + actSize/2.0f - getY(); // move yPress how much bar is moving
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
  public int resetDEL(int curr, int max) {
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
  } public void clearSTRS() {
    this.strings = new String[0];
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
  
  public abstract void select();
  
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
  
  public boolean getOccupied() {
    return this.occupied;
  }
  
  public void setShadow(Color c) {
    this.shadow = true;
    this.spaceColor = c;
  }
  public void removeShadow() {
    this.shadow = false;
    this.spaceColor = constants.defaultSpaceColor;
  }
  public void setColor(Color c) {
    this.occupied = true;
    this.shadow = false;
    this.spaceColor = c;
  }
  public void removeColor() {
    this.occupied = false;
    this.spaceColor = constants.defaultSpaceColor;
  }
  
  public void drawSpace(float xi, float yi, float sideLength) {
    int fillColor = stringToColor(this.spaceColor.getColorName());
    if (!occupied && !shadow) {
      strokeWeight(0.5f);
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
class User {
  private String name;
  
  User(String name) {
    this.name = name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public void saveUser() {
    PrintWriter userFile = createWriter("data/users/" + this.name + ".user.tetris");
    userFile.flush();
    userFile.close();
  }
}
public enum VisualEffectType {
  ROW_CLEARED, PIECE_DROP
}

class VisualEffect {
  private VisualEffectType type;
  private int maxLength; // in ms
  private float startTime;
  private int integer1;
  private Piece piece;
  
  VisualEffect(VisualEffectType type, int integer1) {
    this(type, integer1, new Piece());
  }
  VisualEffect(VisualEffectType type, Piece piece) {
    this(type, 0, piece);
  }
  VisualEffect(VisualEffectType type, int integer1, Piece piece) {
    this.type = type;
    switch(this.type) {
      case ROW_CLEARED:
        this.maxLength = constants.effectMaxLength_RowCleared;
        this.integer1 = integer1;
        break;
      case PIECE_DROP:
        this.maxLength = constants.effectMaxth_PieceDrop;
        this.piece = piece;
        break;
      default:
        println("ERROR: visual effect not found");
        break;
    }
    this.startTime = millis();
  }
  
  public void setInteger1(int i) {
    this.integer1 = i;
  }
  
  public boolean drawVisualEffect(Board board) {
    float effectProgress = (float)(millis() - startTime) / this.maxLength;
    if (effectProgress > 1) {
      return true;
    }
    float squareSize = min((board.xf - board.xi) / (board.spaces.length + 2), (board.yf - board.yi) / (board.spaces[0].length + 2));
    switch(this.type) {
      case ROW_CLEARED:
        fill(color(255), effectProgress * 255);
        stroke(color(255), effectProgress * 255);
        ellipseMode(CENTER);
        ellipse(board.xi + 0.5f * (board.xf - board.xi), board.yi + squareSize * (1.5f + this.integer1), board.xf - board.xi - 2 * squareSize, squareSize);
        /*
        imageMode(CENTER);
        tint(255, effectProgress * 255);
        image(constants.lightning, board.xi + 0.5 * (board.xf - board.xi), board.yi + squareSize * (1.5 + this.integer1), board.xf - board.xi, squareSize);*/
        break;
      case PIECE_DROP:
        ArrayList<Pair<Integer, Integer>> pieceSpaces = this.piece.getPieceSpace();
        ArrayList<Pair<Integer, Integer>> pieceSpacesDrawn = new ArrayList<Pair<Integer, Integer>>();
        strokeWeight(1);
        // first loop to remove unnecessary squares
        for (Pair<Integer, Integer> i : pieceSpaces) {
          int x = i.getKey();
          int y = i.getValue();
          boolean noneOfColumn = true;
          for (int j = 0; j < pieceSpacesDrawn.size(); j++) {
            if (x == pieceSpacesDrawn.get(j).getKey()) {
              noneOfColumn = false;
              if (y < pieceSpacesDrawn.get(j).getValue()) {
                pieceSpacesDrawn.remove(j);
                pieceSpacesDrawn.add(i);
              }
              break;
            }
          }
          if (noneOfColumn) {
            pieceSpacesDrawn.add(new Pair(x, y));
          }
        }
        // then loop to draw lines
        for (Pair<Integer, Integer> i : pieceSpacesDrawn) {
          int x = i.getKey();
          int y = i.getValue();
          for (int j = 0; j < squareSize * this.integer1; j++) {
            if (squareSize * y + j < 0) {
              continue;
            }
            stroke(stringToColor(this.piece.pieceColor.getColorName()), 255 * (j / (squareSize * this.integer1)));
            line(board.xi + squareSize * (x + 1), board.yi + squareSize * (y + 1) + j, board.xi + squareSize * (x + 2), board.yi + squareSize * (y + 1) + j);
          }
        }
        break;
      default:
        println("ERROR: visual effect not found");
        break;
    }
    return false;
  }
}
  public void settings() {  size(1330, 800); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Tetris" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
