package tw.ntou.pettracker.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tw.ntou.pettracker.model.Achievement;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.service.NotificationService;
import tw.ntou.pettracker.service.StatisticsService;
import tw.ntou.pettracker.service.AchievementManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理成就系統
 */
public class AchievementController {
    private final ObservableList<Task> tasks;
    private final ObservableList<Achievement> achievements = FXCollections.observableArrayList();
    private final Map<String, Integer> achievementProgress = new HashMap<>();
    private Button achievementButton;
    private Label streakLabel;
    private NotificationService notificationService;

    // 追蹤特定成就的進度
    private int playWithPetCount = 0;
    private int feedPetCount = 0;

    public AchievementController(ObservableList<Task> tasks) {
        this.tasks = tasks;
        initializeAchievements();
    }

    public void setAchievementButton(Button achievementButton) {
        this.achievementButton = achievementButton;
        if (achievementButton != null) {
            achievementButton.setOnAction(e -> showAchievementsDialog());
        }
    }

    public void setStreakLabel(Label streakLabel) {
        this.streakLabel = streakLabel;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 初始化所有成就
     */
    private void initializeAchievements() {

        achievements.addAll(AchievementManager.getAllAchievements());


        achievements.forEach(achievement ->
                achievementProgress.put(achievement.getId(), 0)
        );
    }

    /**
     * 檢查所有成就
     */
    public void checkAchievements() {
        checkTaskCompletionAchievements();
        checkStreakAchievements();
        checkProductivityAchievements();
        updateStreakLabel();
    }

    /**
     * 檢查任務完成相關成就
     */
    private void checkTaskCompletionAchievements() {
        long completedCount = tasks.stream().filter(Task::isDone).count();

        checkAndUnlockAchievement("first_task", (int) completedCount);
        checkAndUnlockAchievement("task_10", (int) completedCount);
        checkAndUnlockAchievement("task_50", (int) completedCount);
        checkAndUnlockAchievement("task_100", (int) completedCount);


        long highPriorityCompleted = tasks.stream()
                .filter(Task::isDone)
                .filter(t -> t.getPriority() == 1)
                .count();
        checkAndUnlockAchievement("priority_master", (int) highPriorityCompleted);
    }

    /**
     * 檢查連續達成成就
     */
    private void checkStreakAchievements() {
        StatisticsService.TaskStatistics stats = StatisticsService.calculateStatistics(tasks);
        int currentStreak = stats.currentStreak;

        checkAndUnlockAchievement("streak_3", currentStreak);
        checkAndUnlockAchievement("streak_7", currentStreak);
        checkAndUnlockAchievement("streak_30", currentStreak);
    }

    /**
     * 檢查生產力相關成就
     */
    private void checkProductivityAchievements() {
        LocalDate today = LocalDate.now();

        long earlyBirdCount = tasks.stream()
                .filter(Task::isDone)
                .filter(t -> t.getCompletedAt() != null &&
                        t.getCompletedAt().getHour() < 8)
                .count();
        checkAndUnlockAchievement("early_bird", (int) earlyBirdCount);


        long nightOwlCount = tasks.stream()
                .filter(Task::isDone)
                .filter(t -> t.getCompletedAt() != null &&
                        t.getCompletedAt().getHour() >= 22)
                .count();
        checkAndUnlockAchievement("night_owl", (int) nightOwlCount);


        long todayCategories = tasks.stream()
                .filter(Task::isDone)
                .filter(t -> t.getCompletedAt() != null &&
                        t.getCompletedAt().toLocalDate().equals(today))
                .map(Task::getCategory)
                .distinct()
                .count();
        if (todayCategories >= 5) {
            checkAndUnlockAchievement("multitasker", 1);
        }
    }

    /**
     * 檢查寵物相關成就
     */
    public void checkPetAchievements(int satisfaction, int fullness) {
        if (satisfaction >= 100) {
            checkAndUnlockAchievement("pet_happy", 1);
        }
        if (fullness >= 100) {
            checkAndUnlockAchievement("pet_full", 1);
        }
    }

    /**
     * 增加玩耍次數
     */
    public void incrementPlayCount() {
        playWithPetCount++;
        checkAndUnlockAchievement("pet_play_10", playWithPetCount);
    }

    /**
     * 增加餵食次數
     */
    public void incrementFeedCount() {
        feedPetCount++;
        // 可以添加餵食相關成就
    }

    /**
     * 檢查並解鎖成就
     */
    private void checkAndUnlockAchievement(String achievementId, int progress) {
        Achievement achievement = achievements.stream()
                .filter(a -> a.getId().equals(achievementId))
                .findFirst()
                .orElse(null);

        if (achievement != null && !achievement.isUnlocked()) {
            achievementProgress.put(achievementId, progress);

            if (achievement.checkUnlock(progress)) {
                // 顯示成就解鎖通知
                if (notificationService != null) {
                    notificationService.showAchievementNotification(
                            achievement.getName(),
                            achievement.getDescription(),
                            achievement.getPoints()
                    );
                }
            }
        }
    }

    /**
     * 更新連續達成標籤
     */
    private void updateStreakLabel() {
        if (streakLabel != null) {
            StatisticsService.TaskStatistics stats = StatisticsService.calculateStatistics(tasks);
            int currentStreak = stats.currentStreak;

            streakLabel.setText("🔥 " + currentStreak + " 天");


            if (currentStreak >= 30) {
                streakLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 16px;");
            } else if (currentStreak >= 7) {
                streakLabel.setStyle("-fx-text-fill: #ffa500; -fx-font-weight: bold; -fx-font-size: 15px;");
            } else if (currentStreak >= 3) {
                streakLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");
            } else {
                streakLabel.setStyle("-fx-text-fill: #6c757d;");
            }
        }
    }

    /**
     * 顯示成就對話框
     */
    private void showAchievementsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("🏆 成就系統");
        dialog.setHeaderText("您的成就進度");


        int totalPoints = achievements.stream()
                .filter(Achievement::isUnlocked)
                .mapToInt(Achievement::getPoints)
                .sum();


        long unlockedCount = achievements.stream().filter(Achievement::isUnlocked).count();
        int totalCount = achievements.size();


        VBox topStats = new VBox(5);
        Label pointsLabel = new Label("🏆 總成就點數: " + totalPoints);
        pointsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label progressLabel = new Label(String.format("📊 解鎖進度: %d/%d (%.1f%%)",
                unlockedCount, totalCount, (double) unlockedCount / totalCount * 100));
        progressLabel.setStyle("-fx-font-size: 14px;");

        ProgressBar progressBar = new ProgressBar((double) unlockedCount / totalCount);
        progressBar.setPrefWidth(400);

        topStats.getChildren().addAll(pointsLabel, progressLabel, progressBar);

        // 成就列表
        ListView<Achievement> achievementList = new ListView<>(achievements);
        achievementList.setCellFactory(lv -> new AchievementListCell());
        achievementList.setPrefHeight(400);
        achievementList.setPrefWidth(500);


        HBox filterBox = new HBox(10);
        ToggleGroup filterGroup = new ToggleGroup();

        RadioButton allBtn = new RadioButton("全部");
        allBtn.setToggleGroup(filterGroup);
        allBtn.setSelected(true);

        RadioButton unlockedBtn = new RadioButton("已解鎖");
        unlockedBtn.setToggleGroup(filterGroup);

        RadioButton lockedBtn = new RadioButton("未解鎖");
        lockedBtn.setToggleGroup(filterGroup);

        filterBox.getChildren().addAll(allBtn, unlockedBtn, lockedBtn);


        allBtn.setOnAction(e -> achievementList.setItems(achievements));
        unlockedBtn.setOnAction(e -> {
            ObservableList<Achievement> filtered = achievements.filtered(Achievement::isUnlocked);
            achievementList.setItems(filtered);
        });
        lockedBtn.setOnAction(e -> {
            ObservableList<Achievement> filtered = achievements.filtered(a -> !a.isUnlocked());
            achievementList.setItems(filtered);
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(topStats, new Separator(), filterBox, achievementList);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    /**
     * 自定義成就列表單元格
     */
    private class AchievementListCell extends ListCell<Achievement> {
        @Override
        protected void updateItem(Achievement achievement, boolean empty) {
            super.updateItem(achievement, empty);

            if (empty || achievement == null) {
                setGraphic(null);
            } else {
                HBox box = new HBox(10);
                box.setAlignment(Pos.CENTER_LEFT);
                box.setPadding(new javafx.geometry.Insets(5));


                Label iconLabel = new Label(achievement.getIcon());
                iconLabel.setStyle("-fx-font-size: 28px;");


                VBox info = new VBox(4);


                HBox titleBox = new HBox(10);
                Label nameLabel = new Label(achievement.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                Label pointsLabel = new Label("+" + achievement.getPoints() + " 點");
                pointsLabel.setStyle("-fx-text-fill: #ffa500; -fx-font-weight: bold;");

                titleBox.getChildren().addAll(nameLabel, pointsLabel);


                Label descLabel = new Label(achievement.getDescription());
                descLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");


                ProgressBar progressBar = new ProgressBar(achievement.getProgressPercentage() / 100);
                progressBar.setPrefWidth(200);


                Label progressLabel = new Label(String.format("%d/%d",
                        achievement.getProgress(), achievement.getMaxProgress()));
                progressLabel.setStyle("-fx-font-size: 11px;");

                info.getChildren().addAll(titleBox, descLabel, progressBar, progressLabel);


                if (achievement.isUnlocked()) {
                    Label unlockedLabel = new Label("✅ 已解鎖");
                    unlockedLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");

                    if (achievement.getUnlockedAt() != null) {
                        Label dateLabel = new Label(achievement.getUnlockedAt().toLocalDate().toString());
                        dateLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
                        info.getChildren().addAll(unlockedLabel, dateLabel);
                    } else {
                        info.getChildren().add(unlockedLabel);
                    }

                    box.setStyle("-fx-background-color: #f0f8ff; -fx-background-radius: 5;");
                } else {
                    box.setOpacity(0.7);
                }

                box.getChildren().addAll(iconLabel, info);
                setGraphic(box);
            }
        }
    }

    /**
     * 獲取總成就點數
     */
    public int getTotalPoints() {
        return achievements.stream()
                .filter(Achievement::isUnlocked)
                .mapToInt(Achievement::getPoints)
                .sum();
    }

    /**
     * 獲取成就解鎖百分比
     */
    public double getUnlockPercentage() {
        long unlockedCount = achievements.stream().filter(Achievement::isUnlocked).count();
        return achievements.isEmpty() ? 0 : (double) unlockedCount / achievements.size() * 100;
    }
}