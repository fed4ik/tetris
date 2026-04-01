package tetris;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class Board extends Canvas {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;

    private final int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private Piece currentPiece;
    private Timeline timeline;

    public Board() {
        setWidth(BOARD_WIDTH * BLOCK_SIZE);
        setHeight(BOARD_HEIGHT * BLOCK_SIZE);
        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> gameLoop()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        spawnPiece();
        timeline.play();
    }

    private void spawnPiece() {
        currentPiece = new Piece();
    }

    private void gameLoop() {
        // simple gravity
        if (!movePiece(0, 1)) {
            lockPiece();
            clearLines();
            spawnPiece();
        }
        draw();
    }

    private boolean movePiece(int dx, int dy) {
        // placeholder: always allow move
        currentPiece.x += dx;
        currentPiece.y += dy;
        return true;
    }

    private void lockPiece() {
        // naive lock: fill board cells
        for (int[] p : currentPiece.shape) {
            int px = currentPiece.x + p[0];
            int py = currentPiece.y + p[1];
            if (py >= 0 && py < BOARD_HEIGHT && px >= 0 && px < BOARD_WIDTH) {
                board[py][px] = 1;
            }
        }
    }

    private void clearLines() {
        // simple line clear (no scoring)
        for (int y = BOARD_HEIGHT - 1; y >= 0; y--) {
            boolean full = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] == 0) { full = false; break; }
            }
            if (full) {
                // move all rows above down
                for (int ty = y; ty > 0; ty--) {
                    System.arraycopy(board[ty - 1], 0, board[ty], 0, BOARD_WIDTH);
                }
                // clear top row
                for (int x = 0; x < BOARD_WIDTH; x++) board[0][x] = 0;
                y++; // re‑check same row
            }
        }
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());
        // draw board
        gc.setFill(Color.BLUE);
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] != 0) {
                    gc.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
                }
            }
        }
        // draw current piece
        gc.setFill(Color.RED);
        for (int[] p : currentPiece.shape) {
            int px = currentPiece.x + p[0];
            int py = currentPiece.y + p[1];
            if (py >= 0) {
                gc.fillRect(px * BLOCK_SIZE, py * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
            }
        }
    }
}
