class Constants {
  // Tetris
  public final int defaultTickLength = 400;
  public final int maxFPS = 60;
  public final int frameUpdateTime = 100;
  public final int frameAverageCache = 5;
  public final color defaultBackgroundColor = color(200);
  
  // CurrGame
  public final int portRangeFirst = 48620;
  public final int portRangeLast = 48650; // 49150 is max for future reference
  public final int nextPieceQueueLength = 3;
  public final float[] game1Borders = { 10, 30, 660, 680 };
  public final float[] game2Borders = { 670, 30, 1320, 680 };
  public final int defaultPingTimeout = 3000;
  public final int pingRequestFrequency = 100;
  
  // Board
  public final int defaultBoardColumns = 10;
  public final int defaultBoardRows = 20;
  public final color boardBackground = color(0);
  public final color boardBorderFill = color(200, 200, 200);
  public final color boardBorderStroke = color(100, 100, 100);
  
  // Space
  public final color defaultSpaceFill = color(0);
  public final color defaultSpaceStroke = color(255);
  public final float shadowOpacity = 100.0;
  
  // Piece
  public final color defaultPieceFill = color(255);
  public final color defaultPieceStroke = color(0);
  public final color IFill = color(0, 255, 255);
  public final color JFill = color(255, 192, 203);
  public final color LFill = color(255, 165, 0);
  public final color OFill = color(255, 255, 0);
  public final color SFill = color(255, 0, 0);
  public final color TFill = color(128, 0, 128);
  public final color ZFill = color(0, 255, 0);
  public final int minPieceDisplayGridSize = 3;
  
  Constants() {
  }
}
