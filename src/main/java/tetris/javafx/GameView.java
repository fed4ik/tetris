package tetris.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import tetris.core.GameEngine;
import tetris.model.Tetromino;
import tetris.model.TetrominoType;
import java.util.HashMap;
import java.util.Map;

public class GameView extends Pane {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 22;
    private static final int SQUARE_SIZE = 30; // pixel size of each block
    private static final int SIDE_PANEL_WIDTH = 150;

    private final Canvas canvas = new Canvas(BOARD_WIDTH * SQUARE_SIZE + SIDE_PANEL_WIDTH, BOARD_HEIGHT * SQUARE_SIZE);
    private final GameEngine engine = new GameEngine();
    private Timeline gameLoop;
    private final Map<String, KeyCode> keyMap = new HashMap<>();

    public GameView() {
        getChildren().add(canvas);
        setFocusTraversable(true);
        loadKeyMap();
        initKeyHandlers();
        engine.start();
    }

    private void loadKeyMap() {
        // Default bindings – can be overridden by controls.properties (not loaded here for brevity)
        keyMap.put("moveLeft", KeyCode.LEFT);
        keyMap.put("moveRight", KeyCode.RIGHT);
        keyMap.put("rotateLeft", KeyCode.UP);
        keyMap.put("rotateRight", KeyCode.Z);
        keyMap.put("softDrop", KeyCode.DOWN);
        keyMap.put("hardDrop", KeyCode.SPACE);
        keyMap.put("hold", KeyCode.C);
        keyMap.put("pause", KeyCode.P);
    }

    private void initKeyHandlers() {
        setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            if (code == keyMap.get("moveLeft")) {
                engine.moveLeft();
            } else if (code == keyMap.get("moveRight")) {
                engine.moveRight();
            } else if (code == keyMap.get("rotateLeft")) {
                engine.rotateLeft();
            } else if (code == keyMap.get("rotateRight")) {
                engine.rotateRight();
            } else if (code == keyMap.get("softDrop")) {
                engine.startSoftDrop();
            } else if (code == keyMap.get("hardDrop")) {
                engine.dropDown();
            } else if (code == keyMap.get("hold")) {
                engine.holdSwap();
            } else if (code == keyMap.get("pause")) {
                engine.pause();
            }
        });
        setOnKeyReleased(e -> {
            if (e.getCode() == keyMap.get("softDrop")) {
                engine.stopSoftDrop();
            }
        });
    }

    public void startGameLoop() {
        int delay = engine.getScoreManager().getDropSpeed(); // milliseconds per tick
        gameLoop = new Timeline(new KeyFrame(Duration.millis(delay), ev -> {
            if (engine.isStarted() && !engine.isPaused() && !engine.isGameOver()) {
                engine.tick();
                draw();
            }
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // Clear background
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw board blocks
        int[] cells = engine.getBoard().getCells();
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                int val = cells[y * BOARD_WIDTH + x];
                if (val > 0) {
                    TetrominoType type = TetrominoType.values()[val - 1];
                    gc.setFill(type.getColor());
                    gc.fillRect(x * SQUARE_SIZE, y * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
                }
            }
        }

        // Draw ghost piece
        if (!engine.isGameOver()) {
            drawPiece(gc, engine.getCurrentPiece(), engine.getCurrentX(), engine.getGhostY(), true);
        }
        // Draw current piece
        drawPiece(gc, engine.getCurrentPiece(), engine.getCurrentX(), engine.getCurrentY(), false);
        // Draw hold piece in side panel
        drawHoldPiece(gc);
        // Draw next piece preview
        drawNextPiece(gc);
        // Draw score/level info
        drawInfo(gc);
    }

    private void drawPiece(GraphicsContext gc, Tetromino piece, int offsetX, int offsetY, boolean ghost) {
        if (piece == null) return;
        for (int i = 0; i < 4; i++) {
            int x = offsetX + piece.getBlockX(i);
            int y = offsetY + piece.getBlockY(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) continue;
            gc.setFill(piece.getColor().deriveColor(0, 1, ghost ? 0.4 : 1.0, ghost ? 0.5 : 1.0));
            gc.fillRect(x * SQUARE_SIZE, y * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
        }
    }

    private void drawHoldPiece(GraphicsContext gc) {
        gc.setFill(javafx.scene.paint.Color.DARKGRAY);
        gc.fillRect(BOARD_WIDTH * SQUARE_SIZE + 20, 20, SIDE_PANEL_WIDTH - 40, 80);
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillText("HOLD", BOARD_WIDTH * SQUARE_SIZE + 30, 40);
        Tetromino held = engine.getHeldPiece();
        if (held != null) {
            drawMiniPiece(gc, held, BOARD_WIDTH * SQUARE_SIZE + 30, 50);
        }
    }

    private void drawNextPiece(GraphicsContext gc) {
        gc.setFill(javafx.scene.paint.Color.DARKGRAY);
        gc.fillRect(BOARD_WIDTH * SQUARE_SIZE + 20, 120, SIDE_PANEL_WIDTH - 40, 80);
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillText("NEXT", BOARD_WIDTH * SQUARE_SIZE + 30, 140);
        Tetromino next = engine.getNextPiece();
        if (next != null) {
            drawMiniPiece(gc, next, BOARD_WIDTH * SQUARE_SIZE + 30, 150);
        }
    }

    private void drawMiniPiece(GraphicsContext gc, Tetromino piece, int baseX, int baseY) {
        int previewSize = SQUARE_SIZE / 2;
        // Center the piece
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            minX = Math.min(minX, piece.getBlockX(i));
            minY = Math.min(minY, piece.getBlockY(i));
        }
        for (int i = 0; i < 4; i++) {
            int x = piece.getBlockX(i) - minX;
            int y = piece.getBlockY(i) - minY;
            gc.setFill(piece.getColor());
            gc.fillRect(baseX + x * previewSize, baseY + y * previewSize, previewSize - 1, previewSize - 1);
        }
    }

    private void drawInfo(GraphicsContext gc) {
        int infoX = BOARD_WIDTH * SQUARE_SIZE + 20;
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillText("Score: " + engine.getScoreManager().getScore(), infoX, 260);
        gc.fillText("Level: " + engine.getScoreManager().getLevel(), infoX, 280);
        gc.fillText("Lines: " + engine.getScoreManager().getLinesCleared(), infoX, 300);
    }
}
