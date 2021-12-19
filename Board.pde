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
  
  Piece getPiece() {
    return this.piece;
  }
  boolean getPieceOverflow() {
    return this.pieceOverflow;
  }
  void setBoardLocation(float xi, float yi, float xf, float yf) {
    this.xi = xi;
    this.yi = yi;
    this.xf = xf;
    this.yf = yf;
  }
  
  void drawBoard() {
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
  
  void drawVisualEffects() {
    this.drawBoard();
    for (int i = 0; i < this.visualEffects.size(); i++) {
      if (this.visualEffects.get(i).drawVisualEffect(this)) {
        this.visualEffects.remove(i);
        i--;
      }
    }
  }
  void clearVisualEffects() {
    this.visualEffects.clear();
  }
  
  void addPiece() {
    this.addPiece(new Piece(this.spaces.length));
  }
  void addPiece(Piece piece) {
    piece.setXLocation(this.spaces.length / 2 - 1);
    piece.setYLocation(-2);
    // add to piece list
    this.piece = piece;
    // resolve space logic
    this.addPieceLogic();
  }
  void addPieceLogic() {
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
  void removePieceLogic() {
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
  boolean aPieceFalling() {
    if (this.piece == null) {
      return false;
    }
    return true;
  }
  void dropPiece() {
    if (this.aPieceFalling()) {
      VisualEffect ve = new VisualEffect(VisualEffectType.PIECE_DROP, new Piece(this.piece));
      while(this.aPieceFalling()) {
        this.movePiece(Direction.DIRECTION_DOWN, true);
        ve.setInteger1(ve.integer1++);
      }
      this.visualEffects.add(ve);
    }
  }
  boolean movePiece() {
    return this.movePiece(Direction.DIRECTION_DOWN, true);
  }
  boolean movePiece(Direction dir, boolean stopFalling) {
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
  boolean canMove(Piece p) {
    return this.canMove(p, Direction.DIRECTION_DOWN);
  }
  boolean canMove(Piece p, Direction dir) {
    if (p == null) {
      return false;
    }
    Piece pieceCopy = new Piece(p);
    pieceCopy.movePiece(dir);
    return this.canBe(pieceCopy);
  }
  boolean rotatePiece() {
    return this.rotatePiece(true);
  }
  boolean rotatePiece(boolean clockwise) {
    boolean rotatedPiece = false;
    this.removePieceLogic();
    if (this.canRotate(this.piece, clockwise)) {
      rotatedPiece = true;
      this.piece.rotatePiece(clockwise);
    }
    this.addPieceLogic();
    return rotatedPiece;
  }
  boolean canRotate(Piece p) {
    return this.canRotate(p, true);
  }
  boolean canRotate(Piece p, boolean clockwise) {
    if (p == null) {
      return false;
    }
    Piece pieceCopy = new Piece(p);
    pieceCopy.rotatePiece(clockwise);
    return this.canBe(pieceCopy);
  }
  boolean canBe(Piece p) {
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
  
  Piece replaceSavedPiece(Piece p) {
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
  
  int checkFilledRows() {
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
        this.visualEffects.add(new VisualEffect(VisualEffectType.ROW_CLEARED, j - rowsFilled));
        rowsFilled++;
        j++; // have to adjust row
      }
    }
    return rowsFilled;
  }
}
