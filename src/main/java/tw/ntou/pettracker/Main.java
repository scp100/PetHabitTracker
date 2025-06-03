package tw.ntou.pettracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import tw.ntou.pettracker.service.AchievementManager;
import tw.ntou.pettracker.model.WindowSetting;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // 載入現代化界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = loader.load();

            // 創建場景並設定尺寸 (增大窗口以容納新布局)
            ScrollPane scrollPane = new ScrollPane(root);

            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(false);


            Scene scene = new Scene(scrollPane);

            // 載入樣式表 (使用更新後的樣式)
            scene.getStylesheets().add(
                    getClass().getResource("app.css").toExternalForm());
            scene.getStylesheets().add(
                    getClass().getResource("style.css").toExternalForm());

            // 獲取控制器並設定關閉事件
            MainController controller = loader.getController();

            Persistence.loadAchievementsStatus(AchievementManager.getAllAchievements());
            System.out.println("成就載入完畢");

            primaryStage.setOnCloseRequest(e -> {
                Persistence.saveTasks(controller.getTaskList());
                Persistence.saveAchievementsStatus(AchievementManager.getAllAchievements());
                System.out.println("💾 資料已保存");

                javafx.application.Platform.exit();
                System.exit(0);
            });

            // 設定窗口屬性
            primaryStage.setScene(scene);
            primaryStage.setTitle("🐱 Pet Habit Tracker");
            primaryStage.setMaximized(true);

            WindowSetting settings = Persistence.loadWindowSettings();
            System.out.println("讀取設定檔: maximized=" + settings.isMaximized());

            //讀取設定大小
            if (!settings.isMaximized()) {
                String[] dims = settings.getResolution().split("x");
                int width = Integer.parseInt(dims[0]);
                int height = Integer.parseInt(dims[1]);
                primaryStage.setWidth(width);
                primaryStage.setHeight(height);
                primaryStage.centerOnScreen();
            }
            primaryStage.setMaximized(settings.isMaximized());//決定是否全螢幕，如果顛倒會判斷錯誤。

            // 設定窗口圖標（如果有的話）
            try {
                primaryStage.getIcons().add(new javafx.scene.image.Image(
                        getClass().getResource("/tw/ntou/pettracker/icon/cat.png").toExternalForm()));
            } catch (Exception e) {
                System.out.println("無法載入應用程式圖標");
            }

            primaryStage.show();

            System.out.println("Pet Habit Tracker 現代化版本啟動成功！");
            System.out.println("窗口大小: 全螢幕");
            System.out.println("現代化界面已載入");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("應用程式啟動失敗: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("正在啟動 Pet Habit Tracker...");
        launch(args);
    }
}
