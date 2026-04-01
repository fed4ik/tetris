package tetris;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TetrisGame extends Application {
    @Override
    public void start(Stage primaryStage) {
        Board board = new Board();
        StackPane root = new StackPane(board);
        Scene scene = new Scene(root, 300, 600);
        primaryStage.setTitle("Tetris");
        primaryStage.setScene(scene);
        primaryStage.show();
        board.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
