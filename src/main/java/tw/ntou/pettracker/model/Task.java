package tw.ntou.pettracker.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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

    public Task(String desc, LocalDate due, int prio) {
        setDescription(desc);
        setDueDate(due);
        setPriority(prio);
    }

    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v); }
    public StringProperty descriptionProperty() { return description; }

    public boolean isDone() { return done.get(); }
    public void setDone(boolean v) { done.set(v); }
    public BooleanProperty doneProperty() { return done; }

    public LocalDate getDueDate() { return dueDate.get(); }
    public void setDueDate(LocalDate v) { dueDate.set(v); }
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }

    public int getPriority() { return priority.get(); }
    public void setPriority(int v) { priority.set(v); }
    public IntegerProperty priorityProperty() { return priority; }

    // 提醒功能
    public boolean isRemind() { return remind.get(); }
    public void setRemind(boolean v) { remind.set(v); }
    public BooleanProperty remindProperty() { return remind; }

    public LocalDateTime getRemindAt() { return remindAt.get(); }
    public void setRemindAt(LocalDateTime v) { remindAt.set(v); }
    public ObjectProperty<LocalDateTime> remindAtProperty() { return remindAt; }

    // 標籤功能
    public String getTags() { return tags.get(); }
    public void setTags(String v) { tags.set(v); }
    public StringProperty tagsProperty() { return tags; }
}
