package tw.ntou.pettracker.controller;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import tw.ntou.pettracker.util.ThemeManager;

/**
 * 主題控制器
 */
public class ThemeController {
    private ThemeManager themeManager;
    private Button themeButton;
    private Scene scene;

    public ThemeController() {
        this.themeManager = ThemeManager.getInstance();
    }

    public void setThemeButton(Button themeButton) {
        this.themeButton = themeButton;
        if (themeButton != null) {
            themeButton.setOnAction(e -> showThemeDialog());
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        if (scene != null) {
            themeManager.currentThemeProperty().addListener((obs, oldTheme, newTheme) -> {
                themeManager.applyTheme(scene);
            });
            themeManager.applyTheme(scene);
        }
    }

    public void showThemeDialog() {
        // 使用 ThemeManager 的對話框
        themeManager.showThemeDialog(scene);
    }
}
