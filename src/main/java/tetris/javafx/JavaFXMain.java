package tetris.javafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXMain extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameView view = new GameView();
        Scene scene = new Scene(view);
        primaryStage.setTitle("Tetris");
        primaryStage.setScene(scene);
        primaryStage.show();
        view.startGameLoop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
