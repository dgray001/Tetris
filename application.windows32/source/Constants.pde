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
  public final int maxPingRequestsMissed = 3;
  
  // Board
  public final boolean defaultGridlines = false;
  public final int defaultBoardColumns = 10;
  public final int defaultBoardRows = 20;
  public final color boardBackground = color(0);
  public final color boardBorderFill = color(200, 200, 200);
  public final color boardBorderStroke = color(100, 100, 100);
  
  // Space
  public final Color defaultSpaceColor = Color.DEEP_BLACK;
  public final color defaultSpaceStroke = color(255);
  public final float shadowOpacity = 100.0;
  
  // Piece
  public final String defaultPieceType = "2d_smooth";
  public final color defaultPieceFill = color(255);
  public final color defaultPieceStroke = color(0);
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
  
  Constants() {
  }
  
  void loadImages() {
    this.lightning = loadImage(sketchPath("") + "data/lightning.png");
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
  }
}
