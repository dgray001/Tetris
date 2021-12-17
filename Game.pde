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
  
  Game(float[] borders) {
    float xStart = borders[0] + (float(constants.defaultBoardColumns + 2) / (constants.defaultBoardRows + 2)) * (borders[2] - borders[0]);
    this.board = new Board(borders[0], borders[1], xStart, borders[3]);
    this.xi = xStart;
    this.yi = borders[1];
    this.xf = borders[2];
    this.yf = borders[3];
    this.lastTick = millis();
    this.board.drawBoard();
    this.drawPanel();
  }
  
  boolean isOver() {
    return this.gameOver;
  }
  
  // Update returns string with all game changes
  String update() {
    return this.update("");
  }
  String update(String gameName) {
    String updates = "";
    if (millis() - this.lastTick > tickLenth) {
      if (this.board.aPieceFalling()) {
        this.movePieces(Direction.DIRECTION_DOWN, true);
        updates += gameName + "movePieces=DOWN, true";
      }
      else {
        this.gameOver = this.board.getPieceOverflow();
        if (this.gameOver) {
          updates += gameName + "gameOver";
          return updates;
        }
        this.board.checkFilledRows();
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
  
  void drawPanel() {
    // background
    fill(0);
    stroke(0);
    rectMode(CORNERS);
    rect(this.xi, this.yi, this.xf, this.yf);
    // next pieces
    float gapSize = 0.3 * (this.xf - this.xi);
    /*
    float pieceLength = (this.xf - this.xi) / constants.nextPieceQueueLength;
    for(int i = 0; i < this.nextPieces.size(); i++) {
      this.nextPieces.get(i).drawPiece(xi + gapSize + 0.9 * i * pieceLength, yi + gapSize, xi + gapSize + 0.9 * (i + 1) * pieceLength, yi + 0.25 * (yf - yi));
    }
    */
    if (this.nextPieces.size() > 0) {
      this.nextPieces.get(0).drawPiece(this.xi + gapSize, this.yi + 0.2 * gapSize, this.xf - gapSize, this.yi + 0.25 * (this.yf - this.yi));
    }
    // saved piece
    if (this.savedPiece != null) {
      this.savedPiece.drawPiece(this.xi + 1.2 * gapSize, this.yi + 0.4 * (this.yf - this.yi) + 0.2 * gapSize, this.xf - 1.2 * gapSize, this.yi + 0.6 * (this.yf - this.yi));
    }
  }
  
  void addPiece(Piece p) {
    if (this.nextPieces.size() == constants.nextPieceQueueLength) {
      this.board.addPiece(this.nextPieces.get(0));
      this.nextPieces.remove(0);
    }
    this.nextPieces.add(p);
  }
  void movePieces() {
    this.board.movePiece();
  }
  void movePieces(Direction dir, boolean stopFalling) {
    this.board.movePiece(dir, stopFalling);
  }
  void rotatePieces() {
    this.board.rotatePiece();
  }
  void rotatePieces(boolean clockwise) {
    this.board.rotatePiece(clockwise);
  }
  void savePiece() {
    this.savedPiece = this.board.replaceSavedPiece(this.savedPiece);
    this.drawPanel();
  }
  
  String pressedKey() {
    return this.pressedKey("", true);
  }
  String pressedKey(String gameName) {
    return this.pressedKey(gameName, true);
  }
  String pressedKey(String gameName, boolean executeActions) {
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
  boolean executeMessage(String message) {
    if (message.equals("")) {
      return false;
    }
    String[] messageSplit = split(message, '=');
    switch(trim(messageSplit[0])) {
      case "checkFilledRows":
        this.board.checkFilledRows();
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
      default:
        return false;
    }
    return true;
  }
}
