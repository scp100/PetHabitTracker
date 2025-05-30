package tw.ntou.pettracker.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static ThemeManager instance;
    private final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>();
    private final Map<String, Theme> themes = new HashMap<>();
    private final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);

    // 主題定義
    public static class Theme {
        private final String id;
        private final String name;
        private final String cssFile;

        public Theme(String id, String name, String cssFile) {
            this.id = id;
            this.name = name;
            this.cssFile = cssFile;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCssFile() { return cssFile; }
    }

    private ThemeManager() {
        initializeThemes();
        loadSavedTheme();
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private void initializeThemes() {
        // 只有兩個主題：淺色和深色
        Theme lightTheme = new Theme("light", "淺色主題", "light-theme.css");
        Theme darkTheme = new Theme("dark", "深色主題", "dark-theme.css");

        themes.put(lightTheme.getId(), lightTheme);
        themes.put(darkTheme.getId(), darkTheme);
    }

    private void loadSavedTheme() {
        String savedThemeId = prefs.get("theme", "light");
        Theme savedTheme = themes.get(savedThemeId);
        if (savedTheme != null) {
            currentTheme.set(savedTheme);
        } else {
            currentTheme.set(themes.get("light"));
        }
    }

    public void applyTheme(Scene scene) {
        Theme theme = currentTheme.get();
        if (theme == null || scene == null) return;

        // 清除所有主題樣式
        scene.getStylesheets().removeIf(stylesheet ->
                stylesheet.contains("-theme.css"));

        // 應用新主題
        try {
            String themePath = getClass().getResource("/tw/ntou/pettracker/themes/" + theme.getCssFile()).toExternalForm();
            scene.getStylesheets().add(themePath);
        } catch (Exception e) {
            System.err.println("無法載入主題: " + theme.getName());
        }
    }

    public void setTheme(String themeId) {
        Theme theme = themes.get(themeId);
        if (theme != null) {
            currentTheme.set(theme);
            prefs.put("theme", themeId);
        }
    }

    public Theme getCurrentTheme() {
        return currentTheme.get();
    }

    public ObjectProperty<Theme> currentThemeProperty() {
        return currentTheme;
    }

    public Map<String, Theme> getAllThemes() {
        return new HashMap<>(themes);
    }

    // 根據時間自動切換主題
    public void enableAutoThemeSwitch(int lightThemeHour, int darkThemeHour) {
        int currentHour = java.time.LocalTime.now().getHour();

        if (currentHour >= lightThemeHour && currentHour < darkThemeHour) {
            setTheme("light");
        } else {
            setTheme("dark");
        }
    }

    // 顯示主題選擇對話框
    public void showThemeDialog(Scene scene) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("選擇主題");
        dialog.setHeaderText("選擇您喜歡的界面主題");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ToggleGroup themeGroup = new ToggleGroup();

        // 只顯示淺色和深色主題
        RadioButton lightRadio = new RadioButton("☀️ 淺色主題");
        lightRadio.setUserData("light");
        lightRadio.setToggleGroup(themeGroup);

        RadioButton darkRadio = new RadioButton("🌙 深色主題");
        darkRadio.setUserData("dark");
        darkRadio.setToggleGroup(themeGroup);

        // 選中當前主題
        if (getCurrentTheme().getId().equals("light")) {
            lightRadio.setSelected(true);
        } else {
            darkRadio.setSelected(true);
        }

        content.getChildren().addAll(lightRadio, darkRadio);

        CheckBox autoSwitch = new CheckBox("自動切換主題（白天淺色/夜晚深色）");
        content.getChildren().add(autoSwitch);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                RadioButton selected = (RadioButton) themeGroup.getSelectedToggle();
                return selected != null ? (String) selected.getUserData() : null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(themeId -> {
            setTheme(themeId);
            if (scene != null) {
                applyTheme(scene);
            }
            if (autoSwitch.isSelected()) {
                enableAutoThemeSwitch(6, 18); // 6AM-6PM 使用淺色主題
            }
        });
    }

    // 快速切換主題
    public void toggleTheme() {
        if (getCurrentTheme().getId().equals("light")) {
            setTheme("dark");
        } else {
            setTheme("light");
        }
    }
}