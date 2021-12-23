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
        ellipse(board.xi + 0.5 * (board.xf - board.xi), board.yi + squareSize * (1.5 + this.integer1), board.xf - board.xi - 2 * squareSize, squareSize);
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
