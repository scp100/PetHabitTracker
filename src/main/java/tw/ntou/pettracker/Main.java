package tw.ntou.pettracker;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
//
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();


        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(new Menu("File"), new Menu("Help"));
        root.setTop(menuBar);

        ListView<String> petList = new ListView<>();
        petList.getItems().addAll("小狗", "小猫", "仓鼠");
        petList.setPrefWidth(150);
        root.setLeft(petList);
）
        VBox detailBox = new VBox(10);
        detailBox.setAlignment(Pos.CENTER);
        Label placeholder = new Label("请选择左侧的宠物");
        detailBox.getChildren().add(placeholder);
        root.setCenter(detailBox);

        Label status = new Label("就绪");
        BorderPane.setAlignment(status, Pos.CENTER_LEFT);
        root.setBottom(status);


        petList.getSelectionModel().selectedItemProperty().addListener((obs, oldPet, newPet) -> {
            detailBox.getChildren().clear();
            if (newPet != null) {
                Label title = new Label("当前选中：" + newPet);

                detailBox.getChildren().addAll(title);
                status.setText("已选：" + newPet);
            } else {
                detailBox.getChildren().add(placeholder);
                status.setText("就绪");
            }
        });

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("PetHabitTracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
