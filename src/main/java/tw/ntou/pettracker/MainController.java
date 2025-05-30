package tw.ntou.pettracker;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// Model imports
import tw.ntou.pettracker.model.*;

// Controller imports
import tw.ntou.pettracker.controller.*;

// Service imports
import tw.ntou.pettracker.service.*;

// Util imports
import tw.ntou.pettracker.util.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 主控制器 - 負責協調各個子控制器和服務
 */
public class MainController implements Initializable {

    // ===== UI 元件 =====
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label todayTasksLabel;
    @FXML private Label overdueTasksLabel;
    @FXML private Label completionRateLabel;
    @FXML private ProgressBar completionRateBar;
    @FXML private Button batchCompleteBtn;
    @FXML private Button batchDeleteBtn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterPriorityBox;
    @FXML private ComboBox<String> filterStatusBox;
    @FXML private ComboBox<String> sortBox;
    @FXML private Button clearFiltersBtn;
    @FXML private Label dailyProgressLabel;
    @FXML private Label monthlyProgressLabel;
    @FXML private Button settingsBtn;
    @FXML private ToggleButton todayTab;
    @FXML private ToggleButton upcomingTab;
    @FXML private ToggleButton allTab;
    @FXML private Button feedPetBtn;
    @FXML private Button playWithPetBtn;
    @FXML private Label satisfactionLabel;
    @FXML private Label fullnessLabel;
    @FXML private VBox petPanel;
    @FXML private HBox inputBar;
    @FXML private TextField descField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> prioBox;
    @FXML private Button plusBtn;
    @FXML private TableView<Task> table;
    @FXML private TableColumn<Task, Boolean> doneCol;
    @FXML private TableColumn<Task, String> descCol;
    @FXML private TableColumn<Task, LocalDate> dateCol;
    @FXML private TableColumn<Task, Integer> prioCol;
    @FXML private TableColumn<Task, Void> deleteCol;
    @FXML private ImageView petImage;
    @FXML private ProgressBar satisfactionBar;
    @FXML private ProgressBar fullnessBar;

    // 新增UI元件
    @FXML private ComboBox<Task.TaskCategory> categoryFilter;
    @FXML private Button themeButton;
    @FXML private Button statsButton;
    @FXML private Button achievementButton;
    @FXML private Label streakLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private TableColumn<Task, Task.TaskCategory> categoryCol;
    @FXML private void onClearFilters(){
        filterController.clearAllFilters();
        filterController.applyFilters();
        filterController.applySorting("📅 按到期日排序");
    }
    // ===== 資料模型 =====
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private FilteredList<Task> filteredTasks;
    private Pet pet;
    private static final int DAILY_GOAL = 5;
    private static final int MONTHLY_GOAL = 30;

    // ===== 子控制器和服務 =====
    private TaskController taskController;
    private PetController petController;
    private FilterController filterController;
    private StatisticsController statisticsController;
    private AchievementController achievementController;
    private ThemeController themeController;
    private AnimationController animationController;

    private NotificationService notificationService;
    private ViewMode currentView = ViewMode.TODAY;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化寵物
        initializePet();

        // 初始化服務
        initializeServices();

        // 初始化子控制器
        initializeControllers();

        // 初始化UI
        initializeUI();

        // 載入資料
        loadDataAsync();

        // 設定初始視圖
        switchToTodayView();

        // 開始定期更新
        startPeriodicUpdates();
    }

    private void initializePet() {
        pet = new Pet();

        // 載入寵物圖片
        try {
            petImage.setImage(new Image(
                    getClass().getResource("/tw/ntou/pettracker/icon/cat.png").toExternalForm()));
        } catch (Exception e) {
            System.out.println("警告: 無法載入寵物圖片");
        }
    }

    private void initializeServices() {
        notificationService = NotificationService.getInstance();
    }

    private void initializeControllers() {
        // 初始化動畫控制器
        animationController = new AnimationController(petImage, table, petPanel);

        // 初始化任務控制器
        taskController = new TaskController(tasks, this::saveState);
        taskController.setAnimationController(animationController);
        taskController.setNotificationService(notificationService);

        // 初始化寵物控制器
        petController = new PetController(pet, satisfactionBar, fullnessBar,
                satisfactionLabel, fullnessLabel);
        petController.setAnimationController(animationController);
        petController.setFeedButton(feedPetBtn);
        petController.setPlayButton(playWithPetBtn);

        // 初始化篩選控制器
        filterController = new FilterController(tasks);
        filterController.setSearchField(searchField);
        filterController.setFilterPriorityBox(filterPriorityBox);
        filterController.setFilterStatusBox(filterStatusBox);
        filterController.setSortBox(sortBox);
        filterController.setClearFiltersBtn(clearFiltersBtn);

        // 初始化統計控制器
        statisticsController = new StatisticsController(tasks);
        statisticsController.setLabels(totalTasksLabel, completedTasksLabel,
                pendingTasksLabel, todayTasksLabel,
                overdueTasksLabel, completionRateLabel);
        statisticsController.setProgressBars(completionRateBar, dailyProgressLabel,
                monthlyProgressLabel);
        statisticsController.setStatsButton(statsButton);

        // 初始化成就控制器
        achievementController = new AchievementController(tasks);
        achievementController.setAchievementButton(achievementButton);
        achievementController.setStreakLabel(streakLabel);
        achievementController.setNotificationService(notificationService);

        // 初始化主題控制器
        themeController = new ThemeController();
        themeController.setThemeButton(themeButton);
        Platform.runLater(() -> {
            if (table.getScene() != null) {
                themeController.setScene(table.getScene());
            }
        });
    }

    private void initializeUI() {
        // 設定視圖切換
        setupViewTabs();

        // 設定快速輸入欄
        setupQuickInput();

        // 設定表格
        setupTable();

        // 初始化篩選列表
        filteredTasks = filterController.getFilteredTasks();
        SortedList<Task> sortedTasks = new SortedList<>(filterController.getFilteredTasksAsList());
        table.setItems(sortedTasks);

        // 設定任務監聽器
        tasks.addListener((ListChangeListener<Task>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Task t : c.getAddedSubList()) {
                        setupTaskListeners(t);
                        notificationService.scheduleTaskReminder(t);
                    }
                }
            }
            updateAllControllers();
        });

        // 初始化鍵盤快捷鍵
        KeyboardShortcutManager.setupShortcuts(table, descField, searchField,
                this::undo, this::redo,
                () -> themeController.showThemeDialog());

        // 初始化拖放
        DragDropManager.setupDragDrop(table, this::moveTask);
    }

    private void setupViewTabs() {
        ToggleGroup viewGroup = new ToggleGroup();
        if (todayTab != null) {
            todayTab.setToggleGroup(viewGroup);
            todayTab.setOnAction(e -> switchToTodayView());
        }
        if (upcomingTab != null) {
            upcomingTab.setToggleGroup(viewGroup);
            upcomingTab.setOnAction(e -> switchToUpcomingView());
        }
        if (allTab != null) {
            allTab.setToggleGroup(viewGroup);
            allTab.setOnAction(e -> switchToAllView());
        }
    }

    private void setupQuickInput() {
        if (inputBar != null) {
            inputBar.setVisible(true);
            inputBar.setManaged(true);
        }

        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }

        if (prioBox != null) {
            prioBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
            prioBox.getSelectionModel().select(2);
        }

        if (categoryFilter != null) {
            categoryFilter.setItems(FXCollections.observableArrayList(Task.TaskCategory.values()));
            categoryFilter.setPromptText("選擇類別");
        }

        if (descField != null) {
            descField.setOnAction(e -> onAddTask());
            updatePromptText();
        }

        if (plusBtn != null) {
            plusBtn.setOnAction(e -> onFocusInput());
        }
    }

    private void setupTable() {
        // 設定表格列
        TableColumnSetup.setupColumns(table, doneCol, descCol, dateCol,
                prioCol, categoryCol, deleteCol,
                this::saveState, this::editTask,
                this::deleteTask);

        // 設定多選和批量操作
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Task>) c -> {
            boolean hasSelection = !table.getSelectionModel().getSelectedItems().isEmpty();
            if (batchCompleteBtn != null) batchCompleteBtn.setVisible(hasSelection);
            if (batchDeleteBtn != null) batchDeleteBtn.setVisible(hasSelection);
        });

        if (batchCompleteBtn != null) {
            batchCompleteBtn.setOnAction(e -> onBatchComplete());
        }
        if (batchDeleteBtn != null) {
            batchDeleteBtn.setOnAction(e -> onBatchDelete());
        }
    }

    private void setupTaskListeners(Task task) {
        task.doneProperty().addListener((o, ov, nv) -> {
            updateAllControllers();
            if (nv) {
                handleTaskCompletion(task);
            }
        });
    }

    private void handleTaskCompletion(Task task) {
        animationController.playTaskCompletionAnimation();
        pet.reactToTaskCompletion(task.getPriority());
        notificationService.showNotification(
                NotificationService.NotificationType.TASK_DUE,
                "完成任務: " + task.getDescription()
        );
        checkDailyGoal();

        // 處理重複任務
        if (task.getRepeatPattern() != null) {
            Task nextTask = task.createNextRepeatInstance();
            if (nextTask != null) {
                Platform.runLater(() -> tasks.add(nextTask));
            }
        }
    }

    private void loadDataAsync() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }

        CompletableFuture.supplyAsync(() ->
                Persistence.loadTasks()
        ).thenAcceptAsync(loadedTasks -> {
            tasks.addAll(loadedTasks);
            updateAllControllers();
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
        }, Platform::runLater);
    }

    // ===== 視圖切換方法 =====

    private void switchToTodayView() {
        currentView = ViewMode.TODAY;
        filterController.setViewMode(currentView);
        if (todayTab != null) todayTab.setSelected(true);
    }

    private void switchToUpcomingView() {
        currentView = ViewMode.UPCOMING;
        filterController.setViewMode(currentView);
        if (upcomingTab != null) upcomingTab.setSelected(true);
    }

    private void switchToAllView() {
        currentView = ViewMode.ALL;
        filterController.setViewMode(currentView);
        if (allTab != null) allTab.setSelected(true);
    }

    // ===== 任務操作方法 =====

    @FXML
    private void onAddTask() {
        String desc = descField != null ? descField.getText().trim() : "";
        LocalDate due = datePicker != null ? datePicker.getValue() : LocalDate.now();
        Integer prio = prioBox != null ? prioBox.getValue() : 3;
        Task.TaskCategory category = categoryFilter != null ? categoryFilter.getValue() : Task.TaskCategory.PERSONAL;

        if (!desc.isEmpty() && due != null && prio != null) {
            Task newTask = taskController.createTask(desc, due, prio, category);
            tasks.add(newTask);

            // 清空輸入
            clearInput();

            // 動畫和更新
            animationController.playTaskAddAnimation();
            petController.updatePetStatus(tasks, DAILY_GOAL);
            updatePromptText();
        } else {
            showInputError();
        }
    }

    @FXML
    private void onFocusInput() {
        if (descField != null) {
            descField.requestFocus();
            animationController.playFocusAnimation(inputBar);
        }
    }

    @FXML
    private void onBatchComplete() {
        taskController.batchComplete(table.getSelectionModel().getSelectedItems(), pet);
        table.getSelectionModel().clearSelection();
        updateAllControllers();
    }

    @FXML
    private void onBatchDelete() {
        taskController.batchDelete(table.getSelectionModel().getSelectedItems(), tasks);
        table.getSelectionModel().clearSelection();
    }

    private void editTask(Task task) {
        TaskEditDialog dialog = new TaskEditDialog(task);
        dialog.showAndWait().ifPresent(result -> {
            saveState("編輯任務");
            table.refresh();
            updateAllControllers();
            MessageUtil.showMessage("✏️ 任務已更新: " + result.getDescription());
        });
    }

    private void deleteTask(Task task) {
        if (MessageUtil.confirmDelete(task.getDescription())) {
            saveState("刪除任務");
            tasks.remove(task);
            MessageUtil.showMessage("🗑️ 已刪除任務: " + task.getDescription());
        }
    }

    private void moveTask(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) return;
        saveState("移動任務");
        taskController.moveTask(tasks, fromIndex, toIndex);
    }

    // ===== 輔助方法 =====

    private void updateAllControllers() {
        statisticsController.updateStatistics();
        petController.updateProgress();
        achievementController.checkAchievements();
    }

    private void clearInput() {
        if (descField != null) {
            descField.clear();
            descField.requestFocus();
        }
        if (datePicker != null) datePicker.setValue(LocalDate.now());
        if (prioBox != null) prioBox.getSelectionModel().select(2);
        if (categoryFilter != null) categoryFilter.setValue(null);
    }

    private void updatePromptText() {
        if (descField == null) return;
        String[] prompts = {
                "➕ 新增今日任務...",
                "📝 輸入要完成的事情...",
                "🎯 設定新目標...",
                "⭐ 今天要做什麼？",
                "🚀 開始新任務..."
        };
        int randomIndex = (int) (Math.random() * prompts.length);
        descField.setPromptText(prompts[randomIndex]);
    }

    private void showInputError() {
        if (descField != null && descField.getText().trim().isEmpty()) {
            descField.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
            descField.setPromptText("⚠️ 請輸入任務描述");

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(2), e -> {
                        descField.setStyle("");
                        updatePromptText();
                    })
            );
            timeline.play();
        }
    }

    private void checkDailyGoal() {
        LocalDate today = LocalDate.now();
        long todayCompleted = tasks.stream()
                .filter(Task::isDone)
                .filter(t -> t.getCompletedAt() != null &&
                        t.getCompletedAt().toLocalDate().equals(today))
                .count();

        if (todayCompleted == DAILY_GOAL) {
            notificationService.showNotification(
                    NotificationService.NotificationType.DAILY_GOAL_REACHED,
                    "恭喜！您已達成今日目標！"
            );
            petController.celebrateDailyGoal();
        }
    }

    private void startPeriodicUpdates() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    if (pet != null) {
                        pet.timePass();
                    }
                    Persistence.saveTasks(tasks);
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        notificationService.startPeriodicChecks(tasks, pet);
    }

    // ===== 撤銷/重做系統 =====

    private final Stack<TaskMemento> undoStack = new Stack<>();
    private final Stack<TaskMemento> redoStack = new Stack<>();

    private void saveState(String description) {
        undoStack.push(new TaskMemento(tasks, description));
        redoStack.clear();
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(new TaskMemento(tasks, "Current"));
            TaskMemento previousState = undoStack.pop();
            tasks.clear();
            tasks.addAll(previousState.getTasks());
            MessageUtil.showMessage("↩️ 撤銷: " + previousState.getDescription());
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(new TaskMemento(tasks, "Current"));
            TaskMemento nextState = redoStack.pop();
            tasks.clear();
            tasks.addAll(nextState.getTasks());
            MessageUtil.showMessage("↪️ 重做: " + nextState.getDescription());
        }
    }

    // ===== 公開方法 =====

    public ObservableList<Task> getTaskList() {
        return tasks;
    }

    public Pet getPet() {
        return pet;
    }

    public void shutdown() {
        Persistence.saveTasks(tasks);
        notificationService.shutdown();
    }
}