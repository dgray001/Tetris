public enum VisualEffectType {
  ROW_CLEARED
}

class VisualEffect {
  private VisualEffectType type;
  private int maxLength; // in ms
  private int integer1;
  
  VisualEffect(VisualEffectType type, int integer1) {
    this.type = type;
    switch(this.type) {
      case ROW_CLEARED:
        this.maxLength = constants.effectMaxLength_RowCleared;
        this.integer1 = integer1;
        break;
      default:
        println("ERROR: visual effect not found");
        break;
    }
  }
  
  void drawVisualEffect(Board board, int tickProgress, int tickLength) {
    float effectProgress = float(tickProgress) / min(tickLength, this.maxLength);
    if (effectProgress > 1) {
      return;
    }
    float squareSize = min((board.xf - board.xi) / (board.spaces.length + 2), (board.yf - board.yi) / (board.spaces[0].length + 2));
    switch(this.type) {
      case ROW_CLEARED:
        /*
        fill(color(255), effectProgress * 255);
        stroke(color(255), effectProgress * 255);
        ellipseMode(CENTER);
        ellipse(board.xi + 0.5 * (board.xf - board.xi), board.yi + squareSize * (1.5 + this.integer1), board.xf - board.xi, squareSize);
        */
        imageMode(CENTER);
        tint(255, effectProgress * 255);
        image(constants.lightning, board.xi + 0.5 * (board.xf - board.xi), board.yi + squareSize * (1.5 + this.integer1), board.xf - board.xi, squareSize);
        break;
      default:
        println("ERROR: visual effect not found");
        break;
    }
  }
}
