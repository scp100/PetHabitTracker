package tw.ntou.pettracker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setLeft(new Label("【虛擬寵物區】"));
        root.setRight(new Label("【任務管理區】"));
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("虛擬寵物伴你養成好習慣");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}

