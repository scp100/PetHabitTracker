package tw.ntou.pettracker;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.ScheduledService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.DateTimeStringConverter;
import tw.ntou.pettracker.model.Task;  // ← 正常导入你的 Task model

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // --- 数据与表格排序 ---
        ObservableList<Task> tasks = FXCollections.observableArrayList();
        SortedList<Task> sortedTasks = new SortedList<>(tasks,
            Comparator.comparing(Task::getDueDateTime)
        );

        TableView<Task> table = new TableView<>(sortedTasks);
        table.setEditable(true);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeStringConverter dtc = new DateTimeStringConverter(dtf, dtf);

        // 完成
        TableColumn<Task, Boolean> doneCol = new TableColumn<>("完成");
        doneCol.setCellValueFactory(new PropertyValueFactory<>("done"));
        doneCol.setCellFactory(CheckBoxTableCell.forTableColumn(doneCol));
        doneCol.setPrefWidth(60);

        // 描述
        TableColumn<Task, String> descCol = new TableColumn<>("描述");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descCol.setOnEditCommit(evt -> evt.getRowValue().setDescription(evt.getNewValue()));
        descCol.setPrefWidth(200);

        // 截止（日期+时间）
        TableColumn<Task, LocalDateTime> dueCol = new TableColumn<>("截止");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("dueDateTime"));
        dueCol.setCellFactory(TextFieldTableCell.forTableColumn(dtc));
        dueCol.setOnEditCommit(evt -> evt.getRowValue().setDueDateTime(evt.getNewValue()));
        dueCol.setPrefWidth(140);

        // 优先级
        TableColumn<Task, Integer> prioCol = new TableColumn<>("优先级");
        prioCol.setCellValueFactory(cell -> cell.getValue().priorityProperty().asObject());
        prioCol.setCellFactory(ComboBoxTableCell.forTableColumn(1,2,3,4,5));
        prioCol.setOnEditCommit(evt -> evt.getRowValue().setPriority(evt.getNewValue()));
        prioCol.setPrefWidth(80);

        // 提醒
        TableColumn<Task, Boolean> remindCol = new TableColumn<>("提醒");
        remindCol.setCellValueFactory(new PropertyValueFactory<>("remind"));
        remindCol.setCellFactory(CheckBoxTableCell.forTableColumn(remindCol));
        remindCol.setPrefWidth(60);

        // 提醒时间
        TableColumn<Task, LocalDateTime> remindAtCol = new TableColumn<>("提醒时间");
        remindAtCol.setCellValueFactory(new PropertyValueFactory<>("remindAt"));
        remindAtCol.setCellFactory(TextFieldTableCell.forTableColumn(dtc));
        remindAtCol.setOnEditCommit(evt -> evt.getRowValue().setRemindAt(evt.getNewValue()));
        remindAtCol.setPrefWidth(140);

        // 标签
        TableColumn<Task, String> tagsCol = new TableColumn<>("标签");
        tagsCol.setCellValueFactory(new PropertyValueFactory<>("tags"));
        tagsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        tagsCol.setOnEditCommit(evt -> evt.getRowValue().setTags(evt.getNewValue()));
        tagsCol.setPrefWidth(120);

        table.getColumns().addAll(
            doneCol, descCol, dueCol, prioCol, remindCol, remindAtCol, tagsCol
        );

        // --- 提醒服务 （每分钟检查一次） ---
        ScheduledService<Void> remindSvc = new ScheduledService<>() {
            @Override
            protected javafx.concurrent.Task<Void> createTask() {
                return new javafx.concurrent.Task<>() {
                    @Override
                    protected Void call() {
                        Platform.runLater(() -> {
                            LocalDateTime now = LocalDateTime.now();
                            for (Task t : tasks) {
                                if (t.isRemind()
                                    && t.getRemindAt() != null
                                    && !t.getRemindAt().isAfter(now))
                                {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setHeaderText("任务提醒");
                                    alert.setContentText("「" + t.getDescription() + "」已到提醒时间！");
                                    alert.show();
                                    t.setRemind(false);
                                }
                            }
                        });
                        return null;
                    }
                };
            }
        };
        remindSvc.setPeriod(Duration.minutes(1));
        remindSvc.start();

        // --- 浮动按钮与弹出新增表单 ---
        BorderPane mainPane = new BorderPane(table);
        StackPane stack = new StackPane(mainPane);

        // 遮罩
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        overlay.setVisible(false);

        // 表单控件
        TextField txtDesc    = new TextField();
        txtDesc.setPromptText("描述…");
        TextField txtDue     = new TextField(LocalDateTime.now().format(dtf));
        ComboBox<Integer> cbPrio = new ComboBox<>(FXCollections.observableArrayList(1,2,3,4,5));
        cbPrio.getSelectionModel().select(Integer.valueOf(3));
        CheckBox cbRemind    = new CheckBox("提醒");
        TextField txtRemindAt= new TextField(LocalDateTime.now().format(dtf));
        txtRemindAt.setDisable(true);
        cbRemind.selectedProperty().addListener((o,ov,nv)-> txtRemindAt.setDisable(!nv));
        TextField txtTags    = new TextField();
        txtTags.setPromptText("标签…");

        Button btnSubmit     = new Button("提交");
        btnSubmit.setOnAction(e -> {
            Task t = new Task(
                txtDesc.getText(),
                LocalDateTime.parse(txtDue.getText(), dtf),
                cbPrio.getValue()
            );
            if (cbRemind.isSelected()) {
                t.setRemind(true);
                t.setRemindAt(LocalDateTime.parse(txtRemindAt.getText(), dtf));
            }
            t.setTags(txtTags.getText());
            tasks.add(t);
            overlay.setVisible(false);
        });

        VBox form = new VBox(10,
            txtDesc, txtDue, cbPrio, cbRemind, txtRemindAt, txtTags, btnSubmit
        );
        form.getStyleClass().add("add-form");
        form.setVisible(false);
        overlay.getChildren().add(form);
        StackPane.setAlignment(form, Pos.CENTER);

        // 浮动 + 按钮
        Button fab = new Button("+");
        fab.getStyleClass().add("fab");
        fab.setOnAction(e -> {
            overlay.setVisible(true);
            form.setVisible(true);
        });
        StackPane.setAlignment(fab, Pos.BOTTOM_LEFT);
        StackPane.setMargin(fab, new Insets(0,0,20,20));

        stack.getChildren().addAll(overlay, fab);

        // Scene + CSS
        Scene scene = new Scene(stack, 900, 600);
        scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("PetHabitTracker – To-Do List");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
