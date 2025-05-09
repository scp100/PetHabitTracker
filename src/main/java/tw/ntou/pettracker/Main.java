package tw.ntou.pettracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(
                getClass().getResource("app.css").toExternalForm());
            scene.getStylesheets().add(
                getClass().getResource("style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("PetHabitTracker – To-Do List");
            primaryStage.show();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
