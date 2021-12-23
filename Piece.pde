public enum Shape {
  I_BLOCK, J_BLOCK, L_BLOCK, O_BLOCK, S_BLOCK, T_BLOCK, Z_BLOCK;
  private static final List<Shape> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
  private static final int SIZE = VALUES.size();
  private static final Random RANDOM = new Random();
  public static Shape randomShape() {
    return VALUES.get(RANDOM.nextInt(SIZE));
  }
}

public enum Color {
  BLUE("blue"), RED("red"), GREEN("green"), YELLOW("yellow"), CYAN("cyan"), FUCHSIA("fuchsia"), PURPLE("purple"),
  ORANGE("orange"), PINK("pink"), TAN("tan"), GRAY("gray"), BROWN("brown"), BLACK("black"), WHITE("white"), DEEP_BLACK("deep_black");
  private String name;
  private Color(String name) {
    this.name = name;
  }
  public String getColorName() {
    return this.name;
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
  public Color pieceColor;
  
  Piece() {}
  Piece(int boardSizeX) {
    // get random shape
    this.shape = Shape.randomShape();
    // place shape in middle right above the board
    this.xLocation = boardSizeX / 2 - 1;
    this.yLocation = -2;
    if (this.shape == Shape.I_BLOCK) {
      this.yLocation = -3;
    }
    this.setPieceColor();
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
    this.setPieceColor();
  }
  Piece(Piece piece) {
    this.shape = piece.shape;
    this.xLocation = piece.xLocation;
    this.yLocation = piece.yLocation;
    this.rotation = piece.rotation;
    this.pieceColor = piece.pieceColor;
  }
  
  Shape getShape() {
    return this.shape;
  }
  String getShapeName() {
    return this.shape.name();
  }
  int getXLocation() {
    return this.xLocation;
  }
  int getYLocation() {
    return this.yLocation;
  }
  Direction getRotation() {
    return this.rotation;
  }
  Color getPieceColor() {
    return this.pieceColor;
  }
  
  void setPieceColor() {
    switch(this.shape) {
      case I_BLOCK:
        this.pieceColor = options.IFill;
        break;
      case J_BLOCK:
        this.pieceColor = options.JFill;
        break;
      case L_BLOCK:
        this.pieceColor = options.LFill;
        break;
      case O_BLOCK:
        this.pieceColor = options.OFill;
        break;
      case S_BLOCK:
        this.pieceColor = options.SFill;
        break;
      case T_BLOCK:
        this.pieceColor = options.TFill;
        break;
      case Z_BLOCK:
        this.pieceColor = options.ZFill;
        break;
    }
  }
  
  void movePiece(Direction dir) {
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
  void setXLocation(int x) {
    this.xLocation = x;
  }
  void setYLocation(int x) {
    this.yLocation = x;
  }
  
  void rotatePiece() {
    this.rotatePiece(true);
  }
  void rotatePiece(boolean clockwise) {
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
  
  ArrayList<Pair<Integer, Integer>> getPieceSpace() {
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
  
  void drawPiece(float xi, float yi, float xf, float yf) {
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
    switch(options.pieceType) {
      case "2d_normal":
        fill(stringToColor(this.pieceColor.getColorName()));
        stroke(constants.defaultPieceStroke);
        rectMode(CORNER);
        for (Pair<Integer, Integer> i : spaces) {
          square(xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength);
        }
        break;
      case "2d_smooth":
        fill(stringToColor(this.pieceColor.getColorName()));
        stroke(stringToColor(this.pieceColor.getColorName()));
        rectMode(CORNER);
        for (Pair<Integer, Integer> i : spaces) {
          square(xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength);
        }
        break;
      case "3d_normal":
        imageMode(CORNER);
        PImage image_3d_normal = null;
        switch(this.pieceColor) {
          case BLUE:
            image_3d_normal = constants.normal_3D_blue;
            break;
          case RED:
            image_3d_normal = constants.normal_3D_red;
            break;
          case GREEN:
            image_3d_normal = constants.normal_3D_green;
            break;
          case YELLOW:
            image_3d_normal = constants.normal_3D_yellow;
            break;
          case CYAN:
            image_3d_normal = constants.normal_3D_cyan;
            break;
          case FUCHSIA:
            image_3d_normal = constants.normal_3D_fuchsia;
            break;
          case PURPLE:
            image_3d_normal = constants.normal_3D_purple;
            break;
          case ORANGE:
            image_3d_normal = constants.normal_3D_orange;
            break;
          case TAN:
            image_3d_normal = constants.normal_3D_tan;
            break;
          case PINK:
            image_3d_normal = constants.normal_3D_pink;
            break;
          case GRAY:
            image_3d_normal = constants.normal_3D_gray;
            break;
          case BROWN:
            //image_3d_normal = constants.normal_3D_brown;
            break;
          case BLACK:
            image_3d_normal = constants.normal_3D_black;
            break;
          case WHITE:
            //image_3d_normal = constants.normal_3D_white;
            break;
          default:
            println("ERROR: piece color not recognized");
            break;
        }
        if (image_3d_normal != null) {
          for (Pair<Integer, Integer> i : spaces) {
            tint(255);
            image(image_3d_normal, xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength, sideLength);
          }
        }
        break;
      case "3d_fat":
        imageMode(CORNER);
        PImage image_3d_fat = null;
        switch(this.pieceColor) {
          case BLUE:
            image_3d_fat = constants.fat_3D_blue;
            break;
          case RED:
            image_3d_fat = constants.fat_3D_red;
            break;
          case GREEN:
            image_3d_fat = constants.fat_3D_green;
            break;
          case YELLOW:
            image_3d_fat = constants.fat_3D_yellow;
            break;
          case CYAN:
            image_3d_fat = constants.fat_3D_cyan;
            break;
          case FUCHSIA:
            image_3d_fat = constants.fat_3D_fuchsia;
            break;
          case PURPLE:
            image_3d_fat = constants.fat_3D_purple;
            break;
          case ORANGE:
            image_3d_fat = constants.fat_3D_orange;
            break;
          case TAN:
            image_3d_fat = constants.fat_3D_tan;
            break;
          case PINK:
            image_3d_fat = constants.fat_3D_pink;
            break;
          case GRAY:
            image_3d_fat = constants.fat_3D_gray;
            break;
          case BROWN:
            //image_3d_fat = constants.fat_3D_brown;
            break;
          case BLACK:
            image_3d_fat = constants.fat_3D_black;
            break;
          case WHITE:
            //image_3d_fat = constants.fat_3D_white;
            break;
          default:
            println("ERROR: piece color not recognized");
            break;
        }
        if (image_3d_fat != null) {
          for (Pair<Integer, Integer> i : spaces) {
            tint(255);
            image(image_3d_fat, xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength, sideLength);
          }
        }
        break;
      default:
        println("ERROR: piecetype not recognized.");
        break;
    }
  }
  
  void printShapeInfo() {
    println(this.shape.toString());
    println(this.xLocation + " "  + this.yLocation);
  }
}
