package tetris.core;

import tetris.model.Board;
import tetris.model.NextPieceManager;
import tetris.model.ScoreManager;
import tetris.model.Tetromino;

/**
 * Core game engine that manages the game state and logic.
 * Separates game logic from UI rendering.
 */
public class GameEngine {
    private final Board board;
    private final ScoreManager scoreManager;
    private final NextPieceManager nextPieceManager;

    private Tetromino currentPiece;
    private int currentX;
    private int currentY;

    private Tetromino nextPiece;

    private boolean isStarted;
    private boolean isPaused;
    private boolean isGameOver;
    // Hold piece feature
    private Tetromino heldPiece;
    private boolean holdUsedThisTurn;

    public GameEngine() {
        this.board = new Board();
        this.scoreManager = new ScoreManager();
        this.nextPieceManager = new NextPieceManager();
        this.isStarted = false;
        this.isPaused = false;
        this.isGameOver = false;

        // Initialize with a valid piece
        this.nextPiece = nextPieceManager.pop();
    }

    public void start() {
        board.clear();
        scoreManager.reset();
        isStarted = true;
        isPaused = false;
        isGameOver = false;
        spawnNewPiece();
    }

    public void pause() {
        if (!isStarted || isGameOver) return;
        isPaused = !isPaused;
    }

    public void spawnNewPiece() {
        // Set the new piece as current
        currentPiece = nextPiece;
        currentX = board.getWidth() / 2;
        currentY = 0;

        // Get the next piece from the queue
        nextPiece = nextPieceManager.pop();

        // Check if game over (new piece cannot spawn at the start position)
        if (!board.isValidPosition(currentPiece, currentX, currentY)) {
            isGameOver = true;
            isStarted = false;
        }
        // Reset hold usage for the new piece
        holdUsedThisTurn = false;
    }

    public void dropDown() {
        if (!isStarted || isPaused || isGameOver) return;

        int droppedCells = 0;
        while (board.isValidPosition(currentPiece, currentX, currentY + 1)) {
            currentY++;
            droppedCells++;
        }

        if (droppedCells > 0) {
            scoreManager.addHardDropPoints(droppedCells);
        }

        lockPiece();
    }

    public void moveLeft() {
        if (!isStarted || isPaused || isGameOver) return;
        tryMove(currentPiece, currentX - 1, currentY);
    }

    public void moveRight() {
        if (!isStarted || isPaused || isGameOver) return;
        tryMove(currentPiece, currentX + 1, currentY);
    }

    public void moveDown() {
        if (!isStarted || isPaused || isGameOver) return;

        if (board.isValidPosition(currentPiece, currentX, currentY + 1)) {
            currentY++;
            scoreManager.addSoftDropPoints(1);
        } else {
            lockPiece();
        }
    }

    public void rotateLeft() {
        if (!isStarted || isPaused || isGameOver) return;
        rotatePiece(-1);
    }

    public void rotateRight() {
        if (!isStarted || isPaused || isGameOver) return;
        rotatePiece(1);
    }

    private void rotatePiece(int direction) {
        Tetromino rotated = (direction > 0) ? currentPiece.rotateRight() : currentPiece.rotateLeft();

        // Simple rotation without wall kicks for this basic version
        if (board.isValidPosition(rotated, currentX, currentY)) {
            currentPiece = rotated;
        }
    }

    // Hold piece feature
    private Tetromino heldPiece;
    private boolean holdUsedThisTurn;

    public void holdSwap() {
        if (holdUsedThisTurn) return; // only once per drop
        Tetromino temp = heldPiece;
        heldPiece = currentPiece;
        if (temp == null) {
            // No piece held yet, just spawn new
            spawnNewPiece();
        } else {
            currentPiece = temp;
            currentX = board.getWidth() / 2;
            currentY = 0;
        }
        holdUsedThisTurn = true;
    }

    public Tetromino getHeldPiece() {
        return heldPiece;
    }

    // Soft-drop acceleration (UI can call to speed up)
    private boolean softDropActive = false;

    public void startSoftDrop() {
        softDropActive = true;
    }

    public void stopSoftDrop() {
        softDropActive = false;
    }

    public boolean isSoftDropActive() {
        return softDropActive;
    }

    private boolean tryMove(Tetromino piece, int newX, int newY) {
        if (board.isValidPosition(piece, newX, newY)) {
            currentPiece = piece;
            currentX = newX;
            currentY = newY;
            return true;
        }
        return false;
    }

    private void lockPiece() {
        board.lockPiece(currentPiece, currentX, currentY);
        int linesCleared = board.clearLines();
        if (linesCleared > 0) {
            scoreManager.addLineClears(linesCleared);
        }
        spawnNewPiece();
    }

    public void tick() {
        if (!isStarted || isPaused || isGameOver) return;
        moveDown();
    }

    // Getters for rendering and UI
    public Board getBoard() {
        return board;
    }

    public Tetromino getCurrentPiece() {
        return currentPiece;
    }

    public int getCurrentX() {
        return currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public Tetromino getNextPiece() {
        return nextPiece;
    }

    public int getGhostY() {
        if (!isStarted || isGameOver) return currentY;
        int ghostY = currentY;
        while (board.isValidPosition(currentPiece, currentX, ghostY + 1)) {
            ghostY++;
        }
        return ghostY;
    }
}