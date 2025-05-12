package tw.ntou.pettracker;

import tw.ntou.pettracker.model.TaskData;
import tw.ntou.pettracker.model.Task;

public class TaskConverter {
    public static TaskData toData(Task task) {
        TaskData data = new TaskData();
        data.description = task.getDescription();
        data.done = task.isDone();
        data.dueDate = task.getDueDate();
        data.priority = task.getPriority();
        data.remind = task.isRemind();
        data.remindAt = task.getRemindAt();
        data.tags = task.getTags();
        return data;
    }

    public static Task fromData(TaskData data) {
        Task task = new Task(data.description, data.dueDate, data.priority);
        task.setDone(data.done);
        task.setRemind(data.remind);
        task.setRemindAt(data.remindAt);
        task.setTags(data.tags);
        return task;
    }
}
