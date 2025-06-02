package tw.ntou.pettracker.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Achievement {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty icon = new SimpleStringProperty();
    private final IntegerProperty points = new SimpleIntegerProperty();
    private final BooleanProperty unlocked = new SimpleBooleanProperty(false);
    private final ObjectProperty<LocalDateTime> unlockedAt = new SimpleObjectProperty<>();
    private final ObjectProperty<AchievementType> type = new SimpleObjectProperty<>();
    private final IntegerProperty progress = new SimpleIntegerProperty(0);
    private final IntegerProperty maxProgress = new SimpleIntegerProperty(1);
    private final BooleanProperty notificationShown = new SimpleBooleanProperty(false);
    // 成就類型
    public enum AchievementType {
        TASK_COMPLETION("任務完成", "✅"),
        STREAK("連續達成", "🔥"),
        PET_CARE("寵物照顧", "🐱"),
        PRODUCTIVITY("生產力", "📈"),
        SPECIAL("特殊成就", "⭐");

        private final String displayName;
        private final String emoji;

        AchievementType(String displayName, String emoji) {
            this.displayName = displayName;
            this.emoji = emoji;
        }

        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
    }

    public Achievement(String id, String name, String description, String icon,
                       int points, AchievementType type, int maxProgress) {
        this.id.set(id);
        this.name.set(name);
        this.description.set(description);
        this.icon.set(icon);
        this.points.set(points);
        this.type.set(type);
        this.maxProgress.set(maxProgress);
    }

    // 檢查是否達成
    public boolean checkUnlock(int currentProgress) {
        if (!isUnlocked() && currentProgress >= getMaxProgress()) {
            setUnlocked(true);
            setUnlockedAt(LocalDateTime.now());
            setProgress(getMaxProgress());
            return true;
        }
        setProgress(Math.min(currentProgress, getMaxProgress()));
        return false;
    }

    // 獲取進度百分比
    public double getProgressPercentage() {
        return (double) getProgress() / getMaxProgress() * 100;
    }

    // Getters and Setters
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    public String getIcon() { return icon.get(); }
    public void setIcon(String value) { icon.set(value); }
    public StringProperty iconProperty() { return icon; }

    public int getPoints() { return points.get(); }
    public void setPoints(int value) { points.set(value); }
    public IntegerProperty pointsProperty() { return points; }

    public boolean isUnlocked() { return unlocked.get(); }
    public void setUnlocked(boolean value) { unlocked.set(value); }
    public BooleanProperty unlockedProperty() { return unlocked; }

    public LocalDateTime getUnlockedAt() { return unlockedAt.get(); }
    public void setUnlockedAt(LocalDateTime value) { unlockedAt.set(value); }
    public ObjectProperty<LocalDateTime> unlockedAtProperty() { return unlockedAt; }

    public AchievementType getType() { return type.get(); }
    public void setType(AchievementType value) { type.set(value); }
    public ObjectProperty<AchievementType> typeProperty() { return type; }

    public int getProgress() { return progress.get(); }
    public void setProgress(int value) { progress.set(value); }
    public IntegerProperty progressProperty() { return progress; }

    public int getMaxProgress() { return maxProgress.get(); }
    public void setMaxProgress(int value) { maxProgress.set(value); }
    public IntegerProperty maxProgressProperty() { return maxProgress; }

    public boolean isNotificationShown() { return notificationShown.get(); }
    public void setNotificationShown(boolean value) { notificationShown.set(value); }
    public BooleanProperty notificationShownProperty() { return notificationShown; }

}

