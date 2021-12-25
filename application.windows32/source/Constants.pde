class Constants {
  // Tetris
  public final String version = "Tetris v0.3.8b";
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
  public final PieceStyle defaultPieceStyle = PieceStyle.RAISED_FADE;
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
  
  void loadImages() {
    this.lightning = loadImage(sketchPath("") + "data/lightning.png");
    /*
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
    */
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
    /*
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
    */
  }
  
  HashMap<Color, PImage> generateImages(PImage inputImage) {
    HashMap<Color, PImage> returnMap = new HashMap<Color, PImage>();
    for (Color c : Color.VALUES) {
      color c_color = ColorToColor(c);
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
