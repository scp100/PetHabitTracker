package tw.ntou.pettracker.controller;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import tw.ntou.pettracker.model.Pet;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.service.NotificationService;
import tw.ntou.pettracker.util.MessageUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 處理所有任務相關的業務邏輯
 */
public class TaskController {
    private final ObservableList<Task> tasks;
    private final Consumer<String> saveStateCallback;
    private AnimationController animationController;
    private NotificationService notificationService;

    public TaskController(ObservableList<Task> tasks, Consumer<String> saveStateCallback) {
        this.tasks = tasks;
        this.saveStateCallback = saveStateCallback;
    }

    public void setAnimationController(AnimationController animationController) {
        this.animationController = animationController;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 創建新任務
     */
    public Task createTask(String description, LocalDate dueDate, int priority, Task.TaskCategory category) {
        Task newTask = new Task(description, dueDate, priority);
        newTask.setCategory(category);
        return newTask;
    }

    /**
     * 批量完成任務
     */
    public void batchComplete(ObservableList<Task> selectedTasks, Pet pet) {
        if (selectedTasks.isEmpty()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("批量完成");
        alert.setHeaderText("確定要標記這些任務為已完成嗎？");
        alert.setContentText(String.format("將完成 %d 個任務", selectedTasks.size()));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                saveStateCallback.accept("批量完成任務");

                List<Task> tasksToComplete = new ArrayList<>(selectedTasks);
                int completedCount = 0;

                for (Task task : tasksToComplete) {
                    if (!task.isDone()) {
                        task.setDone(true);
                        if (pet != null) {
                            pet.reactToTaskCompletion(task.getPriority());
                        }
                        completedCount++;
                    }
                }

                if (animationController != null) {
                    animationController.playTaskCompletionAnimation();
                }

                MessageUtil.showCelebration(String.format("批量完成了 %d 個任務！", completedCount));
            }
        });
    }

    /**
     * 批量刪除任務
     */
    public void batchDelete(ObservableList<Task> selectedTasks, ObservableList<Task> allTasks) {
        if (selectedTasks.isEmpty()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("批量刪除");
        alert.setHeaderText("確定要刪除這些任務嗎？");
        alert.setContentText(String.format("將刪除 %d 個任務，此操作無法復原", selectedTasks.size()));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                saveStateCallback.accept("批量刪除任務");

                List<Task> tasksToDelete = new ArrayList<>(selectedTasks);
                allTasks.removeAll(tasksToDelete);

                MessageUtil.showMessage(String.format("已刪除 %d 個任務", tasksToDelete.size()));
            }
        });
    }

    /**
     * 移動任務位置（用於拖放）
     */
    public void moveTask(ObservableList<Task> tasks, int fromIndex, int toIndex) {
        Task task = tasks.remove(fromIndex);
        if (toIndex > fromIndex) {
            tasks.add(toIndex - 1, task);
        } else {
            tasks.add(toIndex, task);
        }
    }

    /**
     * 快速重命名任務
     */
    public void quickRenameTask(Task task, Consumer<String> callback) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(task.getDescription());
        dialog.setTitle("快速重命名");
        dialog.setHeaderText("修改任務名稱");
        dialog.setContentText("新名稱:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                saveStateCallback.accept("重命名任務");
                task.setDescription(newName.trim());
                callback.accept(newName);
            }
        });
    }

    /**
     * 檢查並處理重複任務
     */
    public Task processRepeatTask(Task completedTask) {
        if (completedTask.getRepeatPattern() != null) {
            return completedTask.createNextRepeatInstance();
        }
        return null;
    }

    /**
     * 驗證任務資料
     */
    public boolean validateTask(String description, LocalDate dueDate, Integer priority) {
        if (description == null || description.trim().isEmpty()) {
            MessageUtil.showError("任務描述不能為空");
            return false;
        }
        if (dueDate == null) {
            MessageUtil.showError("請選擇到期日期");
            return false;
        }
        if (priority == null || priority < 1 || priority > 5) {
            MessageUtil.showError("請選擇有效的優先級");
            return false;
        }
        return true;
    }

    /**
     * 計算任務統計信息
     */
    public TaskStatistics calculateStatistics() {
        int total = tasks.size();
        long completed = tasks.stream().filter(Task::isDone).count();
        long pending = tasks.stream().filter(t -> !t.isDone()).count();
        long overdue = tasks.stream()
                .filter(t -> !t.isDone() && t.getDueDate().isBefore(LocalDate.now()))
                .count();

        return new TaskStatistics(total, completed, pending, overdue);
    }

    public static class TaskStatistics {
        public final int total;
        public final long completed;
        public final long pending;
        public final long overdue;

        public TaskStatistics(int total, long completed, long pending, long overdue) {
            this.total = total;
            this.completed = completed;
            this.pending = pending;
            this.overdue = overdue;
        }

        public double getCompletionRate() {
            return total > 0 ? (double) completed / total * 100 : 0;
        }
    }
}