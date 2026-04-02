package tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Random;

/**
 * Main Tetris game class implementing the core game logic
 */
public class Tetris extends JPanel implements ActionListener {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 22;

    private final Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;

    private int curX = 0;
    private int curY = 0;
    private Shape curPiece;

    private final int[] board;
    private final Color[] colors = {
        new Color(0, 0, 0),           // empty (black)
        new Color(0, 255, 255),       // I - cyan
        new Color(0, 0, 255),         // J - blue
        new Color(255, 165, 0),       // L - orange
        new Color(255, 255, 0),       // O - yellow
        new Color(0, 255, 0),         // S - green
        new Color(255, 0, 0),         // Z - red
        new Color(128, 0, 128)        // T - purple
    };

    /**
     * Constructor initializes the game components
     */
    public Tetris() {
        setFocusable(true);
        curPiece = new Shape();
        timer = new Timer(400, this);
        timer.start();
        board = new int[BOARD_HEIGHT * BOARD_WIDTH];
        addKeyListener(new TAdapter());
        clearBoard();
    }

    /**
     * Clears the game board
     */
    private void clearBoard() {
        Arrays.fill(board, 0);
    }

    /**
     * Main game loop handler
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) return;
        if (isStarted) {
            if (isFallingFinished) {
                isFallingFinished = false;
                newPiece();
            } else {
                oneLineDown();
            }
        }
        repaint();
    }

    /**
     * Moves piece down one line
     */
    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY + 1)) {
            pieceDropped();
        }
    }

    /**
     * Handles piece dropping
     */
    private void pieceDropped() {
        for (int i = 0; i < 4; ++i) {
            int x = curX + curPiece.x(i);
            int y = curY + curPiece.y(i);
            board[y * BOARD_WIDTH + x] = curPiece.getShape().ordinal() + 1;
        }
        removeFullLines();
        isFallingFinished = true;
    }

    /**
     * Creates a new piece
     */
    private void newPiece() {
        curPiece.setRandomShape();
        curX = BOARD_WIDTH / 2 + 1;
        curY = 0;
        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Shape.Tetrominoes.NoShape);
            isStarted = false;
            timer.stop();
        }
        isFallingFinished = false;
    }

    /**
     * Tries to move a piece
     */
    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY + newPiece.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }
            if (board[y * BOARD_WIDTH + x] != 0) {
                return false;
            }
        }
        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    /**
     * Removes completed lines
     */
    private void removeFullLines() {
        int numFullLines = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineFull = true;
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (board[i * BOARD_WIDTH + j] == 0) {
                    lineFull = false;
                    break;
                }
            }
            if (lineFull) {
                ++numFullLines;
                for (int k = i; k > 0; --k) {
                    System.arraycopy(board, (k - 1) * BOARD_WIDTH, board, k * BOARD_WIDTH, BOARD_WIDTH);
                }
                Arrays.fill(board, 0, BOARD_WIDTH, 0);
                ++i; // recheck same line
            }
        }
        if (numFullLines > 0) {
            // Could add score handling here
        }
    }

    /**
     * Paints the game components
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    /**
     * Draws the game elements
     */
    private void doDrawing(Graphics g) {
        var size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();
        int squareW = squareWidth();
        int squareH = squareHeight();

        // Prevent division by zero
        if (squareW <= 0 || squareH <= 0) {
            return;
        }

        // draw board
        for (int i = 0; i < BOARD_HEIGHT; ++i) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                int shape = board[i * BOARD_WIDTH + j];
                if (shape != 0) {
                    drawSquare(g, j * squareW, boardTop + i * squareH, colors[shape]);
                }
            }
        }

        // draw current piece
        if (curPiece.getShape() != Shape.Tetrominoes.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = curY + curPiece.y(i);
                // Only draw if within board bounds
                if (x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT) {
                    drawSquare(g, x * squareW, boardTop + y * squareH, colors[curPiece.getShape().ordinal()]);
                }
            }
        }
    }

    /**
     * Draws a single square
     */
    private void drawSquare(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + 1);
    }

    /**
     * Calculates square width
     */
    private int squareWidth() {
        int width = (int) getSize().getWidth() / BOARD_WIDTH;
        return Math.max(width, 15); // Ensure minimum size
    }

    /**
     * Calculates square height
     */
    private int squareHeight() {
        int height = (int) getSize().getHeight() / BOARD_HEIGHT;
        return Math.max(height, 15); // Ensure minimum size
    }

    /**
     * Key listener adapter
     */
    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted || curPiece.getShape() == Shape.Tetrominoes.NoShape) {
                return;
            }
            int keycode = e.getKeyCode();
            switch (keycode) {
                case KeyEvent.VK_P:
                    pause();
                    break;
                case KeyEvent.VK_LEFT:
                    tryMove(curPiece, curX - 1, curY);
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(curPiece, curX + 1, curY);
                    break;
                case KeyEvent.VK_DOWN:
                    tryMove(curPiece, curX, curY + 1);
                    break;
                case KeyEvent.VK_UP:
                    // Rotate piece
                    Shape rotatedPiece = curPiece.rotateRight();
                    tryMove(rotatedPiece, curX, curY);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
                case KeyEvent.VK_D:
                    oneLineDown();
                    break;
            }
        }
    }

    /**
     * Drops piece to bottom
     */
    private void dropDown() {
        int newY = curY;
        while (newY < BOARD_HEIGHT) {
            if (!tryMove(curPiece, curX, newY + 1)) {
                break;
            }
            ++newY;
        }
        pieceDropped();
    }

    /**
     * Toggles game pause
     */
    private void pause() {
        if (!isStarted) {
            return;
        }
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }
        repaint();
    }

    /**
     * Starts the game
     */
    public void start() {
        isStarted = true;
        isFallingFinished = false;
        clearBoard();
        newPiece();
        timer.start();
    }

    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Java Tetris");
            Tetris game = new Tetris();
            frame.add(game);
            frame.setSize(400, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.start();
        });
    }
}