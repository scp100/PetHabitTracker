package tw.ntou.pettracker.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


public class MessageUtil {

    public static void showMessage(String message) {
        System.out.println(message);
        // 可以擴展為顯示在狀態欄或通知
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("錯誤");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("警告");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean confirmDelete(String itemName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("確認刪除");
        alert.setHeaderText("確定要刪除這個項目嗎？");
        alert.setContentText(itemName);

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public static void showCelebration(String message) {
        // 可以擴展為更華麗的慶祝效果
        showMessage(message);
    }
}