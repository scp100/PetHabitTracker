package tw.ntou.pettracker.util;

import tw.ntou.pettracker.model.Task;
import java.util.ArrayList;
import java.util.List;

/**
 * 任務備忘錄（用於撤銷/重做）
 */
public class TaskMemento {
    private final List<Task> tasks;
    private final String description;

    public TaskMemento(List<Task> tasks, String description) {
        this.tasks = new ArrayList<>(tasks);
        this.description = description;
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }

    public String getDescription() {
        return description;
    }
}
