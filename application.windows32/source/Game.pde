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
    float xStart = borders[0] + (float(constants.defaultBoardColumns + 2) / (constants.defaultBoardRows + 2)) * (borders[2] - borders[0]);
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
  
  void initializeStatistics() {
    this.statistics.put("Points", 0);
    this.statistics.put("Ticks", 0);
    this.statistics.put("Pieces", 0);
    this.statistics.put("Rows Cleared", 0);
    this.statistics.put("Double Combos", 0);
    this.statistics.put("Triple Combos", 0);
    this.statistics.put("Quadruple Combos", 0);
  }
  
  void incrementStatistic(String statistic) {
    this.increaseStatistic(statistic, 1);
  }
  void increaseStatistic(String statistic, int amount) {
    Integer stat = this.statistics.get(statistic);
    if (stat == null) {
      println("ERROR: statistic " + statistic + " is not defined.");
      return;
    }
    this.statistics.put(statistic, stat + amount);
  }
  
  boolean isOver() {
    return this.gameOver;
  }
  
  // Update returns string with all game changes
  String update() {
    return this.update("", true);
  }
  String update(String gameName, boolean singlePlayer) {
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
  
  void drawBoard() {
    this.board.drawBoard();
    this.drawPanel();
    if (this.displayGameOverMessage) {
      this.drawGameOverMessage();
    }
  }
  
  void drawPanel() {
    // background
    fill(0);
    stroke(0);
    rectMode(CORNERS);
    rect(this.xi, this.yi, this.xf, this.yf);
    // Points
    fill(255);
    textSize(26);
    textAlign(CENTER, TOP);
    text("Points", this.xi + 0.5 * (this.xf - this.xi), this.yi);
    textSize(20);
    text(this.statistics.get("Points"), this.xi + 0.5 * (this.xf - this.xi), this.yi + 30);
    textSize(26);
    textAlign(CENTER, BOTTOM);
    // next pieces
    float gapSize = 0.3 * (this.xf - this.xi);
    /*
    float pieceLength = (this.xf - this.xi) / constants.nextPieceQueueLength;
    for(int i = 0; i < this.nextPieces.size(); i++) {
      this.nextPieces.get(i).drawPiece(xi + gapSize + 0.9 * i * pieceLength, yi + gapSize, xi + gapSize + 0.9 * (i + 1) * pieceLength, yi + 0.25 * (yf - yi));
    }
    */
    text("Next Piece", this.xi + 0.5 * (this.xf - this.xi), this.yi + 0.34 * (this.yf - this.yi));
    if (this.nextPieces.size() > 0) {
      this.nextPieces.get(0).drawPiece(this.xi + gapSize, this.yi + 0.35 * (this.yf - this.yi) + 0.2 * gapSize, this.xf - gapSize, this.yi + 0.6 * (this.yf - this.yi));
    }
    // saved piece
    fill(255);
    text("Saved Piece", this.xi + 0.5 * (this.xf - this.xi), this.yi + 0.74 * (this.yf - this.yi));
    if (this.savedPiece != null) {
      this.savedPiece.drawPiece(this.xi + 1.2 * gapSize, this.yi + 0.75 * (this.yf - this.yi) + 0.2 * gapSize, this.xf - 1.2 * gapSize, this.yi + 0.95 * (this.yf - this.yi));
    }
  }
  
  void showStats() {
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
    text("Points", this.xi + 0.5 * (this.xf - this.xi), this.yi);
    textSize(20);
    text(this.statistics.get("Points"), this.xi + 0.5 * (this.xf - this.xi), this.yi + textHeight);
    // Other stat headers
    textSize(24);
    textAlign(LEFT, TOP);
    text("Time Survived", this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 3);
    text("Pieces Used", this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 6);
    text("Rows Cleared", this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 9);
    text("Doubles", this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 12);
    text("Triples", this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 15);
    text("Quadrupels", this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 18);
    // Other stats
    textSize(18);
    text(this.statistics.get("Ticks") + " ticks", this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 4);
    text(this.statistics.get("Pieces"), this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 7);
    text(this.statistics.get("Rows Cleared"), this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 10);
    text(this.statistics.get("Double Combos"), this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 13);
    text(this.statistics.get("Triple Combos"), this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 16);
    text(this.statistics.get("Quadruple Combos"), this.xi + 0.1 * (this.xf - this.xi), this.yi + textHeight * 19);
  }
  
  void addGameOverMessage(String s1, String s2) {
    this.displayGameOverMessage = true;
    this.gameOverMessage[0] = s1;
    this.gameOverMessage[1] = s2;
  }
  void clearGameOverMessage() {
    this.displayGameOverMessage = false;
  }
  void drawGameOverMessage() {
    fill(color(0), 150);
    stroke(color(0), 150);
    rectMode(CORNERS);
    rect(this.board.xi, this.board.yi, this.board.xf, this.board.yf);
    fill(255);
    textSize(60);
    textAlign(CENTER, BOTTOM);
    text(this.gameOverMessage[0], this.board.xi + 0.5 * (this.board.xf - this.board.xi), this.board.yi + 0.5 * (this.board.yf - this.board.yi));
    textAlign(CENTER, TOP);
    text(this.gameOverMessage[1], this.board.xi + 0.5 * (this.board.xf - this.board.xi), this.board.yi + 0.5 * (this.board.yf - this.board.yi));
  }
  
  void addPiece(Piece p) {
    if (this.nextPieces.size() == constants.nextPieceQueueLength) {
      this.board.addPiece(this.nextPieces.get(0));
      this.incrementStatistic("Pieces");
      this.increaseStatistic("Points", constants.scorePiece);
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
  
  void checkFilledRows() {
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
  
  String pressedKey() {
    return this.pressedKey("", true);
  }
  String pressedKey(String gameName) {
    return this.pressedKey(gameName, true);
  }
  String pressedKey(String gameName, boolean executeActions) {
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
  boolean executeMessage(String message) {
    if (message.equals("")) {
      return false;
    }
    String[] messageSplit = split(message, '=');
    if (this.gameOver) {
      if (!trim(messageSplit[0]).equals("gameOverMessage")) {
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
          String[] parameters = split(trim(messageSplit[1]), ',');
          if (parameters.length != 2) {
            return false;
          }
          this.addGameOverMessage(trim(parameters[0]), trim(parameters[1]));
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
