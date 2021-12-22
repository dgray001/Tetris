public enum Shape {
  I_BLOCK, J_BLOCK, L_BLOCK, O_BLOCK, S_BLOCK, T_BLOCK, Z_BLOCK;
  private static final List<Shape> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
  private static final int SIZE = VALUES.size();
  private static final Random RANDOM = new Random();
  public static Shape randomShape() {
    return VALUES.get(RANDOM.nextInt(SIZE));
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
  public color pieceFill = constants.defaultPieceFill;
  public color pieceStroke = constants.defaultPieceStroke;
  
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
    // set piece color
    switch(this.shape) {
      case I_BLOCK:
        this.pieceFill = options.IFill;
        break;
      case J_BLOCK:
        this.pieceFill = options.JFill;
        break;
      case L_BLOCK:
        this.pieceFill = options.LFill;
        break;
      case O_BLOCK:
        this.pieceFill = options.OFill;
        break;
      case S_BLOCK:
        this.pieceFill = options.SFill;
        break;
      case T_BLOCK:
        this.pieceFill = options.TFill;
        break;
      case Z_BLOCK:
        this.pieceFill = options.ZFill;
        break;
    }
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
    // set piece color
    switch(this.shape) {
      case I_BLOCK:
        this.pieceFill = options.IFill;
        break;
      case J_BLOCK:
        this.pieceFill = options.JFill;
        break;
      case L_BLOCK:
        this.pieceFill = options.LFill;
        break;
      case O_BLOCK:
        this.pieceFill = options.OFill;
        break;
      case S_BLOCK:
        this.pieceFill = options.SFill;
        break;
      case T_BLOCK:
        this.pieceFill = options.TFill;
        break;
      case Z_BLOCK:
        this.pieceFill = options.ZFill;
        break;
    }
  }
  Piece(Piece piece) {
    this.shape = piece.shape;
    this.xLocation = piece.xLocation;
    this.yLocation = piece.yLocation;
    this.rotation = piece.rotation;
    this.pieceFill = piece.pieceFill;
    this.pieceStroke = piece.pieceStroke;
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
  color getPieceFill() {
    return this.pieceFill;
  }
  color getPieceStroke() {
    return this.pieceStroke;
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
    fill(this.pieceFill);
    stroke(this.pieceStroke);
    rectMode(CORNER);
    for (Pair<Integer, Integer> i : spaces) {
      square(xi + sideLength * (i.getKey() - minX), yi + sideLength * (i.getValue() - minY), sideLength);
    }
  }
  
  void printShapeInfo() {
    println(this.shape.toString());
    println(this.xLocation + " "  + this.yLocation);
  }
}
