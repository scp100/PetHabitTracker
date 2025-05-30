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
            // 載入現代化界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = loader.load();

            // 創建場景並設定尺寸 (增大窗口以容納新布局)
            Scene scene = new Scene(root, 1200, 800);

            // 載入樣式表 (使用更新後的樣式)
            scene.getStylesheets().add(
                    getClass().getResource("app.css").toExternalForm());
            scene.getStylesheets().add(
                    getClass().getResource("style.css").toExternalForm());

            // 獲取控制器並設定關閉事件
            MainController controller = loader.getController();
            primaryStage.setOnCloseRequest(e -> {
                Persistence.saveTasks(controller.getTaskList());
                System.out.println("💾 資料已保存");
            });

            // 設定窗口屬性
            primaryStage.setScene(scene);
            primaryStage.setTitle("🐱 Pet Habit Tracker - 現代化版本");
            primaryStage.setMinWidth(1000);  // 設定最小寬度
            primaryStage.setMinHeight(600);   // 設定最小高度

            // 設定窗口圖標（如果有的話）
            try {
                primaryStage.getIcons().add(new javafx.scene.image.Image(
                        getClass().getResource("/tw/ntou/pettracker/icon/cat.png").toExternalForm()));
            } catch (Exception e) {
                System.out.println("⚠️ 無法載入應用程式圖標");
            }

            primaryStage.show();

            System.out.println("🚀 Pet Habit Tracker 現代化版本啟動成功！");
            System.out.println("📱 窗口大小: 1200x800");
            System.out.println("🎨 現代化界面已載入");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ 應用程式啟動失敗: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("🐾 正在啟動 Pet Habit Tracker...");
        launch(args);
    }
}