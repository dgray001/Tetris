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
  
  void setInteger1(int i) {
    this.integer1 = i;
  }
  
  boolean drawVisualEffect(Board board) {
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
        ellipse(board.xi + 0.5 * (board.xf - board.xi), board.yi + squareSize * (1.5 + this.integer1), board.xf - board.xi, squareSize);
        /*
        imageMode(CENTER);
        tint(255, effectProgress * 255);
        image(constants.lightning, board.xi + 0.5 * (board.xf - board.xi), board.yi + squareSize * (1.5 + this.integer1), board.xf - board.xi, squareSize);*/
        break;
      case PIECE_DROP:
        ArrayList<Pair<Integer, Integer>> pieceSpaces = this.piece.getPieceSpace();
        int highestRow = board.spaces[0].length;
        int lowestRow = 0;
        int highestFilledRow = 0;
        int leftColumn = board.spaces.length;
        int rightColumn = 0;
        // first loop to calculate values
        for (Pair<Integer, Integer> i : pieceSpaces) {
          int x = i.getKey();
          int y = i.getValue();
          if (x < leftColumn) {
            leftColumn = x;
            if (y > highestFilledRow) {
              highestFilledRow = y;
            }
          }
          if (x > rightColumn) {
            rightColumn = x;
            if (y > highestFilledRow) {
              highestFilledRow = y;
            }
          }
          if (y < highestRow) {
            highestRow = y;
          }
          if (y > lowestRow) {
            lowestRow = y;
          }
        }
        lowestRow += this.integer1;
        float totalHeight = squareSize * (lowestRow - highestRow + 1);
        strokeWeight(1);
        // then loop to draw lines
        for (Pair<Integer, Integer> i : pieceSpaces) {
          int x = i.getKey();
          int y = i.getValue();
          if (y < 0) {
            continue;
          }
          if (y < highestFilledRow) {
            for (int j = 0; j < squareSize + 1; j++) {
              float currHeight = squareSize * (y - highestRow) + j;
              stroke(color(this.piece.pieceFill, 255 * (currHeight / totalHeight)));
              stroke(155);
              line(board.xi + squareSize * (x + 1), board.yi + squareSize * (y + 1) + j, board.xi + squareSize * (x + 2), board.yi + squareSize * (y + 1) + j);
            }
          }
        }
        for (int j = 0; j < squareSize * (lowestRow + this.integer1 - highestFilledRow + 1) + 1; j++) {
          println(j);
          float currHeight = squareSize * (highestFilledRow - highestRow) + j;
          stroke(color(this.piece.pieceFill, 255 * (currHeight / totalHeight)));
          stroke(255);
          line(board.xi + squareSize * (leftColumn + 1), board.yi + squareSize * (highestFilledRow + 1) + j, board.xi + squareSize * (rightColumn + 1), board.yi + squareSize * (highestFilledRow + 1) + j);
        }
        break;
      default:
        println("ERROR: visual effect not found");
        break;
    }
    return false;
  }
}
