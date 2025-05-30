package tw.ntou.pettracker.util;

import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * 鍵盤快捷鍵管理器
 */
public class KeyboardShortcutManager {

    public static void setupShortcuts(TableView<?> table, TextField descField,
                                      TextField searchField, Runnable undoAction,
                                      Runnable redoAction, Runnable themeAction) {
        if (table != null && table.getScene() != null) {
            // Ctrl+N - 新增任務
            table.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                    () -> {
                        if (descField != null) descField.requestFocus();
                    }
            );

            // Ctrl+F - 搜索
            table.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
                    () -> {
                        if (searchField != null) searchField.requestFocus();
                    }
            );

            // Ctrl+Z - 撤銷
            table.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
                    undoAction
            );

            // Ctrl+Y - 重做
            table.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
                    redoAction
            );

            // Ctrl+T - 主題
            table.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN),
                    themeAction
            );
        }
    }
}
