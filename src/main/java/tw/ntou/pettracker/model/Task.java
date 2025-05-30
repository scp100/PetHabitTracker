package tw.ntou.pettracker.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    private final StringProperty description = new SimpleStringProperty(this, "description", "");
    private final BooleanProperty done = new SimpleBooleanProperty(this, "done", false);
    private final ObjectProperty<LocalDate> dueDate = new SimpleObjectProperty<>(this, "dueDate", LocalDate.now());
    private final IntegerProperty priority = new SimpleIntegerProperty(this, "priority", 3);
    private final BooleanProperty remind = new SimpleBooleanProperty(this, "remind", false);
    private final ObjectProperty<LocalDateTime> remindAt = new SimpleObjectProperty<>(this, "remindAt", null);
    private final StringProperty tags = new SimpleStringProperty(this, "tags", "");

    // 新增：任務分類
    private final ObjectProperty<TaskCategory> category = new SimpleObjectProperty<>(this, "category", TaskCategory.PERSONAL);

    // 新增：子任務列表
    private final ObservableList<SubTask> subTasks = FXCollections.observableArrayList();

    // 新增：重複任務設置
    private final ObjectProperty<RepeatPattern> repeatPattern = new SimpleObjectProperty<>(this, "repeatPattern", null);

    // 新增：任務顏色標記
    private final StringProperty colorTag = new SimpleStringProperty(this, "colorTag", "#007bff");

    // 新增：預估時間（分鐘）
    private final IntegerProperty estimatedMinutes = new SimpleIntegerProperty(this, "estimatedMinutes", 0);

    // 新增：實際花費時間（分鐘）
    private final IntegerProperty actualMinutes = new SimpleIntegerProperty(this, "actualMinutes", 0);

    // 新增：完成時間記錄
    private final ObjectProperty<LocalDateTime> completedAt = new SimpleObjectProperty<>(this, "completedAt", null);

    // 任務分類枚舉
    public enum TaskCategory {
        WORK("工作", "💼", "#2196F3"),
        STUDY("學習", "📚", "#4CAF50"),
        PERSONAL("個人", "🏠", "#FF9800"),
        HEALTH("健康", "💪", "#F44336"),
        SHOPPING("購物", "🛒", "#9C27B0"),
        OTHER("其他", "📌", "#607D8B");

        private final String displayName;
        private final String emoji;
        private final String defaultColor;

        TaskCategory(String displayName, String emoji, String defaultColor) {
            this.displayName = displayName;
            this.emoji = emoji;
            this.defaultColor = defaultColor;
        }

        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
        public String getDefaultColor() { return defaultColor; }

        @Override
        public String toString() {
            return emoji + " " + displayName;
        }
    }

    // 重複模式枚舉
    public enum RepeatPattern {
        DAILY("每日"),
        WEEKLY("每週"),
        MONTHLY("每月"),
        YEARLY("每年"),
        WEEKDAYS("工作日"),
        CUSTOM("自定義");

        private final String displayName;

        RepeatPattern(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // 子任務類
    public static class SubTask {
        private final StringProperty description = new SimpleStringProperty();
        private final BooleanProperty done = new SimpleBooleanProperty(false);

        public SubTask(String description) {
            this.description.set(description);
        }

        // Getters and setters
        public String getDescription() { return description.get(); }
        public void setDescription(String value) { description.set(value); }
        public StringProperty descriptionProperty() { return description; }

        public boolean isDone() { return done.get(); }
        public void setDone(boolean value) { done.set(value); }
        public BooleanProperty doneProperty() { return done; }
    }

    // 建構子
    public Task(String desc, LocalDate due, int prio) {
        setDescription(desc);
        setDueDate(due);
        setPriority(prio);
    }

    // 原有的 getters 和 setters
    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v); }
    public StringProperty descriptionProperty() { return description; }

    public boolean isDone() { return done.get(); }
    public void setDone(boolean v) {
        done.set(v);
        if (v) {
            setCompletedAt(LocalDateTime.now());
        }
    }
    public BooleanProperty doneProperty() { return done; }

    public LocalDate getDueDate() { return dueDate.get(); }
    public void setDueDate(LocalDate v) { dueDate.set(v); }
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }

    public int getPriority() { return priority.get(); }
    public void setPriority(int v) { priority.set(v); }
    public IntegerProperty priorityProperty() { return priority; }

    public boolean isRemind() { return remind.get(); }
    public void setRemind(boolean v) { remind.set(v); }
    public BooleanProperty remindProperty() { return remind; }

    public LocalDateTime getRemindAt() { return remindAt.get(); }
    public void setRemindAt(LocalDateTime v) { remindAt.set(v); }
    public ObjectProperty<LocalDateTime> remindAtProperty() { return remindAt; }

    public String getTags() { return tags.get(); }
    public void setTags(String v) { tags.set(v); }
    public StringProperty tagsProperty() { return tags; }

    // 新增屬性的 getters 和 setters
    public TaskCategory getCategory() { return category.get(); }
    public void setCategory(TaskCategory v) { category.set(v); }
    public ObjectProperty<TaskCategory> categoryProperty() { return category; }

    public ObservableList<SubTask> getSubTasks() { return subTasks; }

    public RepeatPattern getRepeatPattern() { return repeatPattern.get(); }
    public void setRepeatPattern(RepeatPattern v) { repeatPattern.set(v); }
    public ObjectProperty<RepeatPattern> repeatPatternProperty() { return repeatPattern; }

    public String getColorTag() { return colorTag.get(); }
    public void setColorTag(String v) { colorTag.set(v); }
    public StringProperty colorTagProperty() { return colorTag; }

    public int getEstimatedMinutes() { return estimatedMinutes.get(); }
    public void setEstimatedMinutes(int v) { estimatedMinutes.set(v); }
    public IntegerProperty estimatedMinutesProperty() { return estimatedMinutes; }

    public int getActualMinutes() { return actualMinutes.get(); }
    public void setActualMinutes(int v) { actualMinutes.set(v); }
    public IntegerProperty actualMinutesProperty() { return actualMinutes; }

    public LocalDateTime getCompletedAt() { return completedAt.get(); }
    public void setCompletedAt(LocalDateTime v) { completedAt.set(v); }
    public ObjectProperty<LocalDateTime> completedAtProperty() { return completedAt; }

    // 便利方法
    public double getSubTaskProgress() {
        if (subTasks.isEmpty()) return 0;
        long completed = subTasks.stream().filter(SubTask::isDone).count();
        return (double) completed / subTasks.size();
    }

    public boolean isOverdue() {
        return !isDone() && getDueDate().isBefore(LocalDate.now());
    }

    public boolean isDueToday() {
        return getDueDate().equals(LocalDate.now());
    }

    public boolean isDueSoon(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        return !isDone() && getDueDate().isBefore(deadline);
    }

    // 為重複任務生成下一個實例
    public Task createNextRepeatInstance() {
        if (repeatPattern.get() == null) return null;

        Task nextTask = new Task(getDescription(), calculateNextDueDate(), getPriority());
        nextTask.setCategory(getCategory());
        nextTask.setColorTag(getColorTag());
        nextTask.setRepeatPattern(getRepeatPattern());
        nextTask.setEstimatedMinutes(getEstimatedMinutes());
        nextTask.setTags(getTags());

        return nextTask;
    }

    private LocalDate calculateNextDueDate() {
        LocalDate current = getDueDate();
        switch (repeatPattern.get()) {
            case DAILY:
                return current.plusDays(1);
            case WEEKLY:
                return current.plusWeeks(1);
            case MONTHLY:
                return current.plusMonths(1);
            case YEARLY:
                return current.plusYears(1);
            case WEEKDAYS:
                LocalDate next = current.plusDays(1);
                while (next.getDayOfWeek().getValue() > 5) {
                    next = next.plusDays(1);
                }
                return next;
            default:
                return current.plusDays(1);
        }
    }
}