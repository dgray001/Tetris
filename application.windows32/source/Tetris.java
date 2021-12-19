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

// Tetris
// v0.1.6a
// 20211218











Constants constants = new Constants();
CurrGame currGame = new CurrGame(this);
int frameTimer = millis();
int frameCounter = frameCount;
float lastFPS = constants.maxFPS;

public void setup() {
  
  frameRate(constants.maxFPS);
  background(constants.defaultBackgroundColor);
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
      this.spaces[x][y].setShadow(true);
      this.spaces[x][y].setShadowFill(this.piece.getPieceFill());
      this.spaces[x][y].setShadowStroke(this.piece.getPieceStroke());
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
      this.spaces[x][y].setOccupied(true);
      this.spaces[x][y].setSpaceFill(this.piece.getPieceFill());
      this.spaces[x][y].setSpaceStroke(this.piece.getPieceStroke());
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
      this.spaces[x][y].setOccupied(false);
      this.spaces[x][y].setSpaceFill(constants.defaultSpaceFill);
      this.spaces[x][y].setSpaceStroke(constants.defaultSpaceStroke);
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
      this.spaces[x][y].setShadow(false);
      this.spaces[x][y].setShadowFill(constants.defaultSpaceFill);
      this.spaces[x][y].setShadowStroke(constants.defaultSpaceStroke);
    }
  }
  public boolean aPieceFalling() {
    if (this.piece == null) {
      return false;
    }
    return true;
  }
  public void dropPiece() {
    while(this.aPieceFalling()) {
      this.movePiece(Direction.DIRECTION_DOWN, true);
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
        rowsFilled++;
        // remove current row
        for (int i = 0; i < this.spaces.length; i++) {
          this.spaces[i][j].setOccupied(false);
          this.spaces[i][j].setSpaceFill(constants.defaultSpaceFill);
          this.spaces[i][j].setSpaceStroke(constants.defaultSpaceStroke);
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
  
  public void setMES(String mes) {
    this.message = mes;
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
    if (this.mouseOn) {
      this.setCLK(true);
      if (!(this.releaseButton)) {
        this.click();
        this.setMS(millis()); // start timer for repeated clicks
      }
    }
  }
  
  public void mouseRelease() {
    this.setACT(false);
    if ((this.releaseButton)&&(this.mouseOn)&&(this.clicked)) {
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
    if (super.mouseOn) {
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
    text(super.message, this.xInitial + (this.xFinal-this.xInitial)/2.0f, this.yInitial + (this.yFinal-this.yInitial)/2.0f);
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
    if (super.mouseOn) {
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
    text(super.message, this.xCenter, this.yCenter);
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
  private leaveLobbyButton llB = new leaveLobbyButton();
  
  private hostGameButton hgB = new hostGameButton();
  private kickPlayerButton kpB = new kickPlayerButton();
  
  private findGameButton fgB = new findGameButton();
  private startGameButton sgB = new startGameButton();
  
  private listBar cSB = new listBar(10, 740, 260, 798);
  
  private joinLobbyButton jlB = new joinLobbyButton();
  private showInfoButton siB = new showInfoButton();
  
  private findIpButton fiB = new findIpButton();
  
  AllButtons() {
  }
  
  public void update(GameState state) {
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rectMode(CORNERS);
    rect(0, 685, 600, 720);
    qB.update(mouseX, mouseY);
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
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.ngB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.update(mouseX, mouseY);
        if (this.cSB.getHIGH() != -1) {
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
        this.siB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        break;
      case MULTIPLAYER_JOINED:
        this.siB.update(mouseX, mouseY);
        this.cSB.update(mouseX, mouseY);
        break;
    }
  }
  public void mousePress(GameState state) {
    qB.mousePress();
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
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.ngB.mousePress();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.mousePress();
        if (this.cSB.getSTRS().length == 2) {
          this.sgB.mousePress();
        }
        this.siB.mousePress();
        if (this.cSB.getHIGH() != -1) {
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
        this.siB.mousePress();
        this.cSB.mousePress();
        break;
      case MULTIPLAYER_JOINED:
        this.siB.mousePress();
        this.cSB.mousePress();
        break;
    }
  }
  public void mouseRelease(GameState state) {
    qB.mouseRelease();
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
      case CONNECTING_TO_LOBBY:
        break;
      case SINGLEPLAYER:
        this.ngB.mouseRelease();
        break;
      case MULTIPLAYER_LOBBY_HOSTING:
        this.llB.mouseRelease();
        if (this.cSB.getHIGH() != -1) {
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
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        break;
      case MULTIPLAYER_JOINED:
        this.siB.mouseRelease();
        this.cSB.mouseRelease();
        break;
    }
  }
  public void scroll(int count) {
    this.cSB.scroll(count);
  }
  
  public void clearButton(int id) {
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rectMode(CORNERS);
    switch(id) {
      case 0:
        rect(10, 740, 260, 800);
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
    exit();
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
class Constants {
  // Tetris
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
  public final int defaultBoardColumns = 10;
  public final int defaultBoardRows = 20;
  public final int boardBackground = color(0);
  public final int boardBorderFill = color(200, 200, 200);
  public final int boardBorderStroke = color(100, 100, 100);
  
  // Space
  public final int defaultSpaceFill = color(0);
  public final int defaultSpaceStroke = color(255);
  public final float shadowOpacity = 100.0f;
  
  // Piece
  public final int defaultPieceFill = color(255);
  public final int defaultPieceStroke = color(0);
  public final int IFill = color(0, 255, 255);
  public final int JFill = color(255, 192, 203);
  public final int LFill = color(255, 165, 0);
  public final int OFill = color(255, 255, 0);
  public final int SFill = color(255, 0, 0);
  public final int TFill = color(128, 0, 128);
  public final int ZFill = color(0, 255, 0);
  public final int minPieceDisplayGridSize = 3;
  
  // Game
  public final int scoreTick = 1;
  public final int scorePiece= 10;
  public final int scoreRow = 30;
  public final int scoreDouble = 30;
  public final int scoreTriple = 90;
  public final int scoreQuadruple = 180;
  
  Constants() {
  }
}
public enum GameState {
  MAIN_MENU, CONNECTING_TO_LOBBY, SINGLEPLAYER, MULTIPLAYER_LOBBY_HOSTING, MULTIPLAYER_LOBBY_JOINED, MULTIPLAYER_HOSTING, MULTIPLAYER_JOINED;
}

class CurrGame {
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
  
  CurrGame(Tetris thisInstance) {
    this.thisInstance = thisInstance;
  }
  
  public void startSinglePlayerGame() {
    this.myGame = new Game(constants.game1Borders);
    this.state = GameState.SINGLEPLAYER;
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
    rectMode(CORNERS);
    fill(constants.defaultBackgroundColor);
    stroke(constants.defaultBackgroundColor);
    rect(10, 720, 265, 738);
    ArrayList<String> IPs = this.findAddressesOnLAN();
    this.lobbyClients = this.findHosts(IPs);
    if (this.lobbyClients.size() == 0) {
      textSize(13);
      textAlign(LEFT, TOP);
      fill(0);
      text("No games found. Maybe try \"Find IP\"", 10, 723);
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
    Thread localhostThread = new Thread(new Runnable() {
      public void run() {
        try {
          if (InetAddress.getByName("localhost").isReachable(constants.defaultPingTimeout)) {
            IPs.add("localhost");
          }
        } catch (Exception e) {
          //e.printStackTrace();
        }
      }
    });
    threads.add(localhostThread);
    localhostThread.start();
    for(Thread thread : threads) {
      try {
        thread.join();
      } catch(InterruptedException e) {
        //e.printStackTrace();
      }
    }
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
    delay(constants.defaultPingTimeout);
    for(Thread thread : threads) {
      try {
        thread.interrupt();
      } catch(Exception e) {
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
    this.otherPlayer = this.lobbyClients.get(this.buttons.cSB.getHIGH());
    this.otherPlayer.write("Join Lobby");
  }
  
  public void clientConnects(Client someClient) {
    println("Client connected with IP address: " + someClient.ip());
    if (this.state == GameState.MULTIPLAYER_LOBBY_HOSTING) {
      this.lobbyClients.add(new Joinee(someClient, this.portHosting, "LOBBY: "));
    }
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
    if (this.state == GameState.MULTIPLAYER_LOBBY_JOINED) {
      this.otherPlayer.write("Quit Lobby");
      this.otherPlayer.client.stop();
    }
    if (this.state == GameState.MULTIPLAYER_LOBBY_HOSTING) {
      this.server.write("LOBBY: Quit Lobby|");
      this.server.stop();
    }
    this.state = GameState.MAIN_MENU;
  }
  
  public void kickPlayer() {
    if (this.otherPlayer == null) {
      println("No client to kick.");
    }
    else {
      println("Kicked client with ID: " + this.otherPlayer.id);
      this.server.write("LOBBY: Kick Player|");
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
    this.server.write("LOBBY: Start Game|");
  }
  
  public void clientEvent(Client someClient) {
    String[] messages = split(someClient.readString(), '|');
    for(String message : messages) {
      message = trim(message);
      if (message.equals("")) {
        continue;
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
                  removeClient = true;
                }
                j.ping = millis() - j.lastPingRequest;
                if (j.ping > constants.defaultPingTimeout * 2) {
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
            int secondGap = round((width2 - textWidth(j.name + " " + multiplyString(" ", firstGap) + j.id + ": ")) / textWidth(" "));
            lbStrings = append(lbStrings, j.name + " " + multiplyString(" ", firstGap) + j.id + " " + multiplyString(" ", secondGap) + j.ping + " ms");
          }
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      case CONNECTING_TO_LOBBY:
        if (!this.otherPlayer.client.active()) {
          this.otherPlayer.client.stop();
          this.otherPlayer = null;
          this.state = GameState.MAIN_MENU;
          break;
        }
        if (this.otherPlayer.receivedInitialResponse && !this.otherPlayer.waitingForResponse) {
          this.otherPlayer.write("Join Lobby");
          this.otherPlayer.waitingForResponse = true;
          break;
        }
        break;
      case SINGLEPLAYER:
        if (this.myGame.isOver()) {
          this.myGame = null;
          this.state = GameState.MAIN_MENU;
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
        text("Connection to Lobby", 10, 725);
        lbStrings = new String[0];
        boolean leaveLobby = false;
        if (this.otherPlayer != null) {
          if (this.otherPlayer.client.active()) {
            lbStrings = append(lbStrings, this.otherPlayer.name + "  (" + this.otherPlayer.ping + " ms)");
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
            this.state = GameState.MAIN_MENU;
          }
        }
        else {
          showMessageDialog(null, "There was an error connecting to the lobby", "", PLAIN_MESSAGE);
          this.state = GameState.MAIN_MENU;
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      case MULTIPLAYER_HOSTING:
        boolean hostGameOver = this.myGame.gameOver;
        boolean joineeGameOver = this.otherGame.gameOver;
        String myGameChanges = this.myGame.update("| HOST_GAME: ", false);
        String otherGameChanges = this.otherGame.update("| JOINEE_GAME: ", false);
        // check for game over messages
        if ((!this.myGame.gameOver) && (this.otherGame.gameOver)) {
          this.myGame.gameOverMessage("You", "Won");
          myGameChanges += "| HOST_GAME: gameOverMessage=You, Won";
        }
        if ((this.myGame.gameOver) && (!this.otherGame.gameOver)) {
          this.otherGame.gameOverMessage("You", "Won");
          otherGameChanges += "| JOINEE_GAME: gameOverMessage=You, Won";
        }
        if ((!hostGameOver) && (this.myGame.gameOver)) {
          if (joineeGameOver) {
            this.myGame.gameOverMessage("You", "Won");
            myGameChanges += "| HOST_GAME: gameOverMessage=You, Won";
          }
          else {
            this.myGame.gameOverMessage("You", "Lost");
            myGameChanges += "| HOST_GAME: gameOverMessage=You, Lost";
          }
        }
        if ((!joineeGameOver) && (this.otherGame.gameOver)) {
          if (hostGameOver) {
            this.otherGame.gameOverMessage("You", "Won");
            otherGameChanges += "| JOINEE_GAME: gameOverMessage=You, Won";
          }
          else {
            this.otherGame.gameOverMessage("You", "Lost");
            otherGameChanges += "| JOINEE_GAME: gameOverMessage=You, Lost";
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
            this.state = GameState.MAIN_MENU;
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
            lbStrings = append(lbStrings, this.otherPlayer.name + " (" + this.otherPlayer.ping + " ms)");
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
            this.state = GameState.MAIN_MENU;
          }
        }
        else {
          showMessageDialog(null, "There was an error connecting to the game", "", PLAIN_MESSAGE);
          this.state = GameState.MAIN_MENU;
        }
        this.buttons.cSB.setSTR(lbStrings);
        break;
      default:
        break;
    }
    while(this.messageQ.peek() != null) {
      String message = this.messageQ.remove();
      println(message);
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
                    this.lobbyClients.get(index).resolveInitialRequest(trim(splitMessage[3]), splitMessage[4]);
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
                  this.server.write("LOBBY: Ping Resolve: " + trim(splitMessage[2]) + "|");
                  break;
                case "Ping Resolve":
                  if (this.otherPlayer.messageForMe(splitMessage)) {
                    this.otherPlayer.resolvePingRequest();
                  }
                  break;
                case "Initial Request":
                  if (splitMessage.length < 3) {
                    println("ERROR: No IP address for initial request");
                    break;
                  }
                  this.server.write("LOBBY: Initial Resolve: " + trim(splitMessage[2]) + ": " + this.lobbyName + ":Game             :|");
                  break;
                case "Initial Resolve":
                  if (splitMessage.length < 3) {
                    println("ERROR: initial resolve message invalid");
                    break;
                  }
                  try {
                    if (this.lobbyClients.get(index).messageForMe(splitMessage)) {
                      this.lobbyClients.get(index).resolveInitialRequest();
                    }
                  } catch(Exception e) {}
                  try {
                    if (this.otherPlayer.messageForMe(splitMessage)) {
                      this.otherPlayer.resolveInitialRequest();
                    }
                  } catch(Exception e) {}
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
                    this.server.write("LOBBY: Join Lobby: " + trim(splitMessage[2]) + "|");
                    this.otherPlayer = this.lobbyClients.get(index);
                    this.lobbyClients.remove(index);
                  }
                  else {
                    this.server.write("LOBBY: Lobby Full: " + trim(splitMessage[2]) + "|");
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
                  this.state = GameState.MAIN_MENU;
                  break;
                case "Quit Lobby":
                  println("The host quit the lobby");
                  this.otherPlayer = null;
                  this.messageQ.clear();
                  this.state = GameState.MAIN_MENU;
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
                  this.server.write("LOBBY: Ping Resolve: " + trim(splitMessage[2]) + "|");
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
            case "HOST_GAME":
              if (this.myGame.executeMessage(trim(splitMessage[1]))) {
                this.server.write(message);
              }
              else {
                println("ERROR: HOST_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            case "JOINEE_GAME":
              if (this.otherGame.executeMessage(trim(splitMessage[1]))) {
                this.server.write(message);
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
                  this.state = GameState.MAIN_MENU;
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
                default:
                  println("ERROR: LOBBY message not recognized -> " + trim(splitMessage[1]));
                  break;
              }
              break;
            case "HOST_GAME":
              if (!this.otherGame.executeMessage(trim(splitMessage[1]))) {
                println("ERROR: HOST_GAME message not recognized -> " + trim(splitMessage[1]));
              }
              break;
            case "JOINEE_GAME":
              if (!this.myGame.executeMessage(trim(splitMessage[1]))) {
                println("ERROR: JOINEE_GAME message not recognized -> " + trim(splitMessage[1]));
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
          this.otherPlayer.write(gameChanges);
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
  private int tickLenth = constants.defaultTickLength;
  private int lastTick;
  private float xi = 0;
  private float yi = 0;
  private float xf = 0;
  private float yf = 0;
  private boolean gameOver = false;
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
  public String update(String gameName, boolean gameOverMessage) {
    if (this.gameOver) {
      return "";
    }
    String updates = "";
    if (millis() - this.lastTick > tickLenth) {
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
          if (gameOverMessage) {
            this.gameOverMessage();
          }
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
      this.board.drawBoard();
      this.drawPanel();
      updates += gameName + "drawBoard";
      updates += gameName + "drawPanel";
    }
    return updates;
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
  
  public void gameOverMessage() {
    this.gameOverMessage("Game", "Over");
  }
  public void gameOverMessage(String s1, String s2) {
    fill(color(0), 150);
    rectMode(CORNERS);
    rect(this.board.xi, this.board.yi, this.board.xf, this.board.yf);
    fill(255);
    textSize(60);
    textAlign(CENTER, BOTTOM);
    text(s1, this.board.xi + 0.5f * (this.board.xf - this.board.xi), this.board.yi + 0.5f * (this.board.yf - this.board.yi));
    textAlign(CENTER, TOP);
    text(s2, this.board.xi + 0.5f * (this.board.xf - this.board.xi), this.board.yi + 0.5f * (this.board.yf - this.board.yi));
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
      this.board.drawBoard();
    }
    updates += gameName + "drawBoard";
    return updates;
  }
  
  // Returns whether message was executed
  public boolean executeMessage(String message) {
    if (this.gameOver) {
      return false;
    }
    if (message.equals("")) {
      return false;
    }
    String[] messageSplit = split(message, '=');
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
        this.board.drawBoard();
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
        this.gameOverMessage();
        this.showStats();
        break;
      case "gameOverMessage":
        if (messageSplit.length > 1) {
          String[] parameters = split(trim(messageSplit[1]), ',');
          if (parameters.length != 2) {
            return false;
          }
          this.gameOverMessage(parameters[0], parameters[1]);
        }
        else {
          this.gameOverMessage();
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
      this.id = newClient.ip();
      this.initialRequest();
    }
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
      this.ping = millis() - this.lastPingRequest;
      this.lastPingRequest = millis();
      this.waitingForResponse = false;
      this.receivedInitialResponse = true;
      this.setNewName(newName, s1);
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
      this.ping = millis() - this.lastPingRequest;
      this.lastPingRequest = millis();
      this.waitingForResponse = false;
      this.pingRequestsMissed = 0;
    }
  }
  public void missedPingRequest() {
    this.waitingForResponse = false;
    this.ping = millis() - this.lastPingRequest + this.pingRequestsMissed * constants.defaultPingTimeout;
    this.pingRequestsMissed++;
  }
  
  public void write(String message) {
    if ((this.client != null) && (this.client.active())) {
      this.client.write(this.writeHeader + message + ": " + this.id + "|");
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
  public int pieceFill = constants.defaultPieceFill;
  public int pieceStroke = constants.defaultPieceStroke;
  
  Piece(int boardSizeX) {
    // get random shape
    this.shape = Shape.randomShape();
    // place shape in middle right above the board
    this.xLocation = boardSizeX / 2 - 1;
    this.yLocation = -2;
    if (this.shape == Shape.I_BLOCK) {
      this.yLocation = -3;
    }
    // set piece color
    switch(this.shape) {
      case I_BLOCK:
        this.pieceFill = constants.IFill;
        break;
      case J_BLOCK:
        this.pieceFill = constants.JFill;
        break;
      case L_BLOCK:
        this.pieceFill = constants.LFill;
        break;
      case O_BLOCK:
        this.pieceFill = constants.OFill;
        break;
      case S_BLOCK:
        this.pieceFill = constants.SFill;
        break;
      case T_BLOCK:
        this.pieceFill = constants.TFill;
        break;
      case Z_BLOCK:
        this.pieceFill = constants.ZFill;
        break;
    }
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
    // set piece color
    switch(this.shape) {
      case I_BLOCK:
        this.pieceFill = constants.IFill;
        break;
      case J_BLOCK:
        this.pieceFill = constants.JFill;
        break;
      case L_BLOCK:
        this.pieceFill = constants.LFill;
        break;
      case O_BLOCK:
        this.pieceFill = constants.OFill;
        break;
      case S_BLOCK:
        this.pieceFill = constants.SFill;
        break;
      case T_BLOCK:
        this.pieceFill = constants.TFill;
        break;
      case Z_BLOCK:
        this.pieceFill = constants.ZFill;
        break;
    }
  }
  Piece(Piece piece) {
    this.shape = piece.shape;
    this.xLocation = piece.xLocation;
    this.yLocation = piece.yLocation;
    this.rotation = piece.rotation;
    this.pieceFill = piece.pieceFill;
    this.pieceStroke = piece.pieceStroke;
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
  public int getPieceFill() {
    return this.pieceFill;
  }
  public int getPieceStroke() {
    return this.pieceStroke;
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
    fill(this.pieceFill);
    stroke(this.pieceStroke);
    rectMode(CORNER);
    for (Pair<Integer, Integer> i : spaces) {
      square(xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength);
    }
  }
  
  public void printShapeInfo() {
    println(this.shape.toString());
    println(this.xLocation + " "  + this.yLocation);
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
  public int spaceFill = constants.defaultSpaceFill;
  public int spaceStroke = constants.defaultSpaceStroke;
  public boolean shadow = false;
  public int shadowFill = constants.defaultSpaceFill;
  public int shadowStroke = constants.defaultSpaceStroke;
  
  Space() {
  }
  Space(Space space) {
    this.occupied = space.occupied;
    this.spaceFill = space.spaceFill;
    this.spaceStroke = space.spaceStroke;
  }
  
  public boolean getOccupied() {
    return this.occupied;
  }
  
  public void setOccupied(boolean x) {
    this.occupied = x;
  }
  public void setSpaceFill(int x) {
    this.spaceFill = x;
  }
  public void setSpaceStroke(int x) {
    this.spaceStroke = x;
  }
  public void setShadow(boolean x) {
    this.shadow = x;
  }
  public void setShadowFill(int x) {
    this.shadowFill = x;
  }
  public void setShadowStroke(int x) {
    this.shadowStroke = x;
  }
  
  public void drawSpace(float xi, float yi, float sideLength) {
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
