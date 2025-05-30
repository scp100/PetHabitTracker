package tw.ntou.pettracker.controller;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.service.StatisticsService;
import tw.ntou.pettracker.util.MessageUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.YearMonth;
import javafx.stage.FileChooser;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

/**
 * 處理統計相關功能
 */
public class StatisticsController {
    private final ObservableList<Task> tasks;
    private Label totalTasksLabel;
    private Label completedTasksLabel;
    private Label pendingTasksLabel;
    private Label todayTasksLabel;
    private Label overdueTasksLabel;
    private Label completionRateLabel;
    private ProgressBar completionRateBar;
    private Label dailyProgressLabel;
    private Label monthlyProgressLabel;
    private Button statsButton;

    private static final int DAILY_GOAL = 5;
    private static final int MONTHLY_GOAL = 30;

    public StatisticsController(ObservableList<Task> tasks) {
        this.tasks = tasks;
    }

    public void setLabels(Label totalTasksLabel, Label completedTasksLabel,
                          Label pendingTasksLabel, Label todayTasksLabel,
                          Label overdueTasksLabel, Label completionRateLabel) {
        this.totalTasksLabel = totalTasksLabel;
        this.completedTasksLabel = completedTasksLabel;
        this.pendingTasksLabel = pendingTasksLabel;
        this.todayTasksLabel = todayTasksLabel;
        this.overdueTasksLabel = overdueTasksLabel;
        this.completionRateLabel = completionRateLabel;
    }

    public void setProgressBars(ProgressBar completionRateBar, Label dailyProgressLabel,
                                Label monthlyProgressLabel) {
        this.completionRateBar = completionRateBar;
        this.dailyProgressLabel = dailyProgressLabel;
        this.monthlyProgressLabel = monthlyProgressLabel;
    }

    public void setStatsButton(Button statsButton) {
        this.statsButton = statsButton;
        if (statsButton != null) {
            statsButton.setOnAction(e -> showStatisticsDialog());
        }
    }

    /**
     * 更新所有統計數據
     */
    public void updateStatistics() {
        updateBasicStats();
        updateProgress();
    }

    /**
     * 更新基本統計
     */
    private void updateBasicStats() {
        StatisticsService.TaskStatistics stats = StatisticsService.calculateStatistics(tasks);

        if (totalTasksLabel != null) {
            totalTasksLabel.setText(String.valueOf(stats.totalTasks));
        }
        if (completedTasksLabel != null) {
            completedTasksLabel.setText(String.valueOf(stats.completedTasks));
        }
        if (pendingTasksLabel != null) {
            pendingTasksLabel.setText(String.valueOf(stats.pendingTasks));
        }
        if (todayTasksLabel != null) {
            LocalDate today = LocalDate.now();
            long todayTasks = tasks.stream()
                    .filter(task -> task.getDueDate().equals(today) && !task.isDone())
                    .count();
            todayTasksLabel.setText(String.valueOf(todayTasks));
        }
        if (overdueTasksLabel != null) {
            overdueTasksLabel.setText(String.valueOf(stats.overdueTasks));
            // 逾期任務用紅色顯示
            if (stats.overdueTasks > 0) {
                overdueTasksLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            } else {
                overdueTasksLabel.setStyle("-fx-text-fill: #28a745;");
            }
        }

        if (completionRateLabel != null) {
            completionRateLabel.setText(String.format("%.1f%%", stats.completionRate));
        }
        if (completionRateBar != null) {
            completionRateBar.setProgress(stats.completionRate / 100.0);
        }
    }

    /**
     * 更新進度統計
     */
    private void updateProgress() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        // 計算今日完成任務數
        long dailyCompleted = tasks.stream()
                .filter(Task::isDone)
                .filter(t -> t.getCompletedAt() != null &&
                        t.getCompletedAt().toLocalDate().equals(today))
                .count();

        // 計算本月完成任務數
        long monthlyCompleted = tasks.stream()
                .filter(Task::isDone)
                .filter(t -> t.getCompletedAt() != null &&
                        YearMonth.from(t.getCompletedAt().toLocalDate()).equals(currentMonth))
                .count();

        if (dailyProgressLabel != null) {
            dailyProgressLabel.setText(dailyCompleted + "/" + DAILY_GOAL);
            // 根據進度設定顏色
            if (dailyCompleted >= DAILY_GOAL) {
                dailyProgressLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            } else if (dailyCompleted >= DAILY_GOAL * 0.6) {
                dailyProgressLabel.setStyle("-fx-text-fill: #ffc107;");
            } else {
                dailyProgressLabel.setStyle("-fx-text-fill: #dc3545;");
            }
        }

        if (monthlyProgressLabel != null) {
            monthlyProgressLabel.setText(monthlyCompleted + "/" + MONTHLY_GOAL);
            // 根據進度設定顏色
            if (monthlyCompleted >= MONTHLY_GOAL) {
                monthlyProgressLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            }
        }
    }

    /**
     * 顯示詳細統計對話框
     */
    private void showStatisticsDialog() {
        StatisticsService.TaskStatistics stats = StatisticsService.calculateStatistics(tasks);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📊 詳細統計報告");
        dialog.setHeaderText(null);

        // 創建報告內容
        TextArea reportArea = new TextArea(StatisticsService.generateReport(stats));
        reportArea.setEditable(false);
        reportArea.setPrefRowCount(20);
        reportArea.setPrefColumnCount(50);
        reportArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace;");

        VBox content = new VBox(10);
        content.getChildren().add(reportArea);

        // 添加導出按鈕
        Button exportButton = new Button("📤 導出報告");
        exportButton.setOnAction(e -> exportReport(stats));
        content.getChildren().add(exportButton);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    /**
     * 導出統計報告
     */
    private void exportReport(StatisticsService.TaskStatistics stats) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存統計報告");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("文字檔案", "*.txt"),
                new FileChooser.ExtensionFilter("CSV檔案", "*.csv")
        );
        fileChooser.setInitialFileName("任務統計報告_" + LocalDate.now() + ".txt");

        File file = fileChooser.showSaveDialog(statsButton.getScene().getWindow());
        if (file != null) {
            try {
                String content;
                if (file.getName().endsWith(".csv")) {
                    content = generateCSVReport(stats);
                } else {
                    content = StatisticsService.generateReport(stats);
                }
                Files.write(file.toPath(), content.getBytes("UTF-8"));
                MessageUtil.showMessage("📊 報告已成功導出到: " + file.getName());
            } catch (IOException e) {
                MessageUtil.showError("無法保存檔案: " + e.getMessage());
            }
        }
    }

    /**
     * 生成CSV格式報告
     */
    private String generateCSVReport(StatisticsService.TaskStatistics stats) {
        StringBuilder csv = new StringBuilder();

        // 標題行
        csv.append("統計項目,數值\n");

        // 基本統計
        csv.append("總任務數,").append(stats.totalTasks).append("\n");
        csv.append("已完成,").append(stats.completedTasks).append("\n");
        csv.append("進行中,").append(stats.pendingTasks).append("\n");
        csv.append("已逾期,").append(stats.overdueTasks).append("\n");
        csv.append("完成率,").append(String.format("%.1f%%", stats.completionRate)).append("\n");
        csv.append("平均完成時間(小時),").append(String.format("%.1f", stats.averageCompletionTime)).append("\n");
        csv.append("當前連續天數,").append(stats.currentStreak).append("\n");
        csv.append("最長連續天數,").append(stats.longestStreak).append("\n");

        // 優先級分布
        csv.append("\n優先級分布\n");
        csv.append("優先級,任務數\n");
        for (int i = 1; i <= 5; i++) {
            csv.append("優先級").append(i).append(",")
                    .append(stats.tasksByPriority.getOrDefault(i, 0)).append("\n");
        }

        // 類別分布
        csv.append("\n類別分布\n");
        csv.append("類別,任務數\n");
        stats.tasksByCategory.forEach((category, count) ->
                csv.append(category.getDisplayName()).append(",").append(count).append("\n")
        );

        // 生產力時段
        csv.append("\n生產力時段分析\n");
        csv.append("時段,完成任務數,效率分數\n");
        stats.productivityByHour.forEach(hour ->
                csv.append(String.format("%02d:00", hour.hour)).append(",")
                        .append(hour.tasksCompleted).append(",")
                        .append(String.format("%.1f", hour.efficiency)).append("\n")
        );

        return csv.toString();
    }

    /**
     * 獲取當前統計摘要
     */
    public String getStatisticsSummary() {
        StatisticsService.TaskStatistics stats = StatisticsService.calculateStatistics(tasks);
        return String.format("任務統計 - 總計:%d, 完成:%d (%.1f%%), 逾期:%d",
                stats.totalTasks, stats.completedTasks,
                stats.completionRate, stats.overdueTasks);
    }

    /**
     * 檢查是否達成每日目標
     */
    public boolean isDailyGoalReached() {
        LocalDate today = LocalDate.now();
        long dailyCompleted = tasks.stream()
                .filter(Task::isDone)
                .filter(t -> t.getCompletedAt() != null &&
                        t.getCompletedAt().toLocalDate().equals(today))
                .count();
        return dailyCompleted >= DAILY_GOAL;
    }

    /**
     * 檢查是否達成每月目標
     */
    public boolean isMonthlyGoalReached() {
        YearMonth currentMonth = YearMonth.now();
        long monthlyCompleted = tasks.stream()
                .filter(Task::isDone)
                .filter(t -> t.getCompletedAt() != null &&
                        YearMonth.from(t.getCompletedAt().toLocalDate()).equals(currentMonth))
                .count();
        return monthlyCompleted >= MONTHLY_GOAL;
    }

    /**
     * 獲取今日剩餘任務數
     */
    public int getRemainingTodayTasks() {
        LocalDate today = LocalDate.now();
        return (int) tasks.stream()
                .filter(task -> task.getDueDate().equals(today) && !task.isDone())
                .count();
    }
}