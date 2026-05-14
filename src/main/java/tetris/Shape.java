package tetris;

public class Shape {
    public enum Tetrominoes { ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape }
    private Tetrominoes pieceShape;
    private int[] coords = new int[4 * 2]; // x,y for 4 blocks

    private static final int[][][] coordsTable = {
        {{0,-1}, {0,0}, {-1,0}, {-1,1}}, // ZShape
        {{0,-1}, {0,0}, {1,0}, {1,1}}, // SShape
        {{0,-1}, {0,0}, {0,1}, {0,2}}, // LineShape
        {{-1,0}, {0,0}, {1,0}, {0,1}}, // TShape
        {{0,0}, {1,0}, {0,1}, {1,1}}, // SquareShape
        {{-1,-1}, {0,-1}, {0,0}, {0,1}}, // LShape
        {{1,-1}, {0,-1}, {0,0}, {0,1}} // MirroredLShape
    };

    public Shape() {
        // Initialize to a random shape for proper game start
        setRandomShape();
    }

    public void setShape(Tetrominoes shape) {
        for (int i = 0; i < 4; i++) {
            coords[2*i] = coordsTable[shape.ordinal()][i][0];
            coords[2*i+1] = coordsTable[shape.ordinal()][i][1];
        }
        pieceShape = shape;
    }

    public void setRandomShape() {
        // Generate a random index for valid shapes (0-6)
        int randomIndex = (int)(Math.random() * 7);
        Tetrominoes[] values = Tetrominoes.values();
        setShape(values[randomIndex]);
    }

    public int x(int index) { return coords[2*index]; }
    public int y(int index) { return coords[2*index+1]; }
    public Tetrominoes getShape() { return pieceShape; }

    public Shape rotateLeft() {
        if (pieceShape == Tetrominoes.SquareShape) return this;
        Shape result = new Shape();
        result.pieceShape = pieceShape;
        for (int i = 0; i < 4; ++i) {
            int x = y(i);
            int y = -x(i);
            result.coords[2*i] = x;
            result.coords[2*i+1] = y;
        }
        return result;
    }

    public Shape rotateRight() {
        if (pieceShape == Tetrominoes.SquareShape) return this;
        Shape result = new Shape();
        result.pieceShape = pieceShape;
        for (int i = 0; i < 4; ++i) {
            int x = -y(i);
            int y = x(i);
            result.coords[2*i] = x;
            result.coords[2*i+1] = y;
        }
        return result;
    }

    // This method is used for debugging or specific initialization needs
    public static Tetrominoes[] getValidShapes() {
        return Tetrominoes.values();
    }
}