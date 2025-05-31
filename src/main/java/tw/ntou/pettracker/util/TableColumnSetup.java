package tw.ntou.pettracker.util;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleObjectProperty;
import tw.ntou.pettracker.DatePickerTableCell;
import tw.ntou.pettracker.model.Task;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * 表格列設定工具
 */
public class TableColumnSetup {

    public static void setupColumns(TableView<Task> table,
                                    TableColumn<Task, Boolean> doneCol,
                                    TableColumn<Task, String> descCol,
                                    TableColumn<Task, LocalDate> dateCol,
                                    TableColumn<Task, Integer> prioCol,
                                    TableColumn<Task, Task.TaskCategory> categoryCol,
                                    TableColumn<Task, Void> deleteCol,
                                    Consumer<String> saveStateCallback,
                                    Consumer<Task> editTaskCallback,
                                    Consumer<Task> deleteTaskCallback) {

        // 完成狀態列
        doneCol.setCellValueFactory(cell -> cell.getValue().doneProperty());
        doneCol.setCellFactory(CheckBoxTableCell.forTableColumn(doneCol));

        // 描述列
        descCol.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        descCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descCol.setOnEditCommit(evt -> {
            saveStateCallback.accept("編輯任務描述");
            evt.getRowValue().setDescription(evt.getNewValue());
        });

        // 日期列
        dateCol.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getDueDate())
        );
        dateCol.setCellFactory(col -> new DatePickerTableCell<>());
        dateCol.setOnEditCommit(evt -> {
            saveStateCallback.accept("編輯任務日期");
            evt.getRowValue().setDueDate(evt.getNewValue());
        });

        // 優先級列
        prioCol.setCellValueFactory(cell ->
                cell.getValue().priorityProperty().asObject()
        );
        prioCol.setCellFactory(ComboBoxTableCell.forTableColumn(1, 2, 3, 4, 5));
        prioCol.setOnEditCommit(evt -> {
            saveStateCallback.accept("編輯任務優先級");
            evt.getRowValue().setPriority(evt.getNewValue());
        });

        // 類別列
        if (categoryCol != null) {
            categoryCol.setCellValueFactory(cell -> cell.getValue().categoryProperty());
            categoryCol.setCellFactory(ComboBoxTableCell.forTableColumn(Task.TaskCategory.values()));
            categoryCol.setOnEditCommit(evt -> {
                saveStateCallback.accept("編輯任務類別");
                evt.getRowValue().setCategory(evt.getNewValue());
            });
        }

        // 操作按鈕列
        deleteCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("刪除");
            private final Button editButton = new Button("編輯");
            private final HBox buttonBox = new HBox(4);

            {
                deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc3545; " +
                        "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 4;");
                editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #007bff; " +
                        "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 4;");

                buttonBox.getChildren().addAll(editButton, deleteButton);
                buttonBox.setAlignment(Pos.CENTER);

                deleteButton.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    deleteTaskCallback.accept(task);
                });

                editButton.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    editTaskCallback.accept(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
    }
}