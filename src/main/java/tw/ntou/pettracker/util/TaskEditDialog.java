package tw.ntou.pettracker.util;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tw.ntou.pettracker.model.Task;

/**
 * 任務編輯對話框
 */
public class TaskEditDialog extends Dialog<Task> {

    public TaskEditDialog(Task task) {
        setTitle("編輯任務");
        setHeaderText("修改任務詳細信息");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox form = new VBox();
        form.setSpacing(10);
        form.setPadding(new Insets(20));

        TextField descField = new TextField(task.getDescription());
        descField.setPromptText("任務描述");

        DatePicker datePicker = new DatePicker(task.getDueDate());

        ComboBox<Integer> priorityBox = new ComboBox<>();
        priorityBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        priorityBox.setValue(task.getPriority());

        ComboBox<Task.TaskCategory> categoryBox = new ComboBox<>();
        categoryBox.setItems(FXCollections.observableArrayList(Task.TaskCategory.values()));
        categoryBox.setValue(task.getCategory());

        TextField tagsField = new TextField(task.getTags() != null ? task.getTags() : "");
        tagsField.setPromptText("標籤 (用逗號分隔)");

        CheckBox remindBox = new CheckBox("設定提醒");
        remindBox.setSelected(task.isRemind());

        ComboBox<Task.RepeatPattern> repeatBox = new ComboBox<>();
        repeatBox.setItems(FXCollections.observableArrayList(Task.RepeatPattern.values()));
        repeatBox.setValue(task.getRepeatPattern());
        repeatBox.setPromptText("重複模式");

        Spinner<Integer> estimatedSpinner = new Spinner<>(0, 480, task.getEstimatedMinutes(), 15);
        estimatedSpinner.setEditable(true);

        // 子任務區域
        VBox subTasksBox = createSubTasksSection(task);

        form.getChildren().addAll(
                new Label("任務描述:"), descField,
                new Label("到期日:"), datePicker,
                new Label("優先級:"), priorityBox,
                new Label("類別:"), categoryBox,
                new Label("標籤:"), tagsField,
                new Label("預估時間 (分鐘):"), estimatedSpinner,
                new Label("重複:"), repeatBox,
                remindBox,
                new Separator(),
                subTasksBox
        );

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        getDialogPane().setContent(scrollPane);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                task.setDescription(descField.getText());
                task.setDueDate(datePicker.getValue());
                task.setPriority(priorityBox.getValue());
                task.setCategory(categoryBox.getValue());
                task.setTags(tagsField.getText());
                task.setRemind(remindBox.isSelected());
                task.setRepeatPattern(repeatBox.getValue());
                task.setEstimatedMinutes(estimatedSpinner.getValue());
                return task;
            }
            return null;
        });
    }

    private VBox createSubTasksSection(Task task) {
        VBox subTasksBox = new VBox(5);
        Label subTasksLabel = new Label("子任務:");
        ListView<Task.SubTask> subTasksList = new ListView<>(task.getSubTasks());
        subTasksList.setCellFactory(lv -> new SubTaskCell());
        subTasksList.setPrefHeight(100);

        HBox addSubTaskBox = new HBox(5);
        TextField newSubTaskField = new TextField();
        newSubTaskField.setPromptText("新增子任務");
        Button addSubTaskBtn = new Button("➕");
        addSubTaskBtn.setOnAction(e -> {
            String subDesc = newSubTaskField.getText().trim();
            if (!subDesc.isEmpty()) {
                task.getSubTasks().add(new Task.SubTask(subDesc));
                newSubTaskField.clear();
            }
        });
        addSubTaskBox.getChildren().addAll(newSubTaskField, addSubTaskBtn);

        subTasksBox.getChildren().addAll(subTasksLabel, subTasksList, addSubTaskBox);
        return subTasksBox;
    }

    // 子任務單元格
    private static class SubTaskCell extends ListCell<Task.SubTask> {
        @Override
        protected void updateItem(Task.SubTask subTask, boolean empty) {
            super.updateItem(subTask, empty);

            if (empty || subTask == null) {
                setGraphic(null);
            } else {
                HBox box = new HBox(5);
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(subTask.isDone());
                checkBox.setOnAction(e -> subTask.setDone(checkBox.isSelected()));

                Label label = new Label(subTask.getDescription());
                if (subTask.isDone()) {
                    label.setStyle("-fx-text-fill: gray; -fx-strikethrough: true;");
                }

                box.getChildren().addAll(checkBox, label);
                setGraphic(box);
            }
        }
    }
}