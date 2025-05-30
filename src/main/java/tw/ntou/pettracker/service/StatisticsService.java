package tw.ntou.pettracker.service;

import javafx.collections.ObservableList;
import tw.ntou.pettracker.model.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsService {

    // 任務統計數據類
    public static class TaskStatistics {
        public final int totalTasks;
        public final int completedTasks;
        public final int pendingTasks;
        public final int overdueTasks;
        public final double completionRate;
        public final double averageCompletionTime;
        public final Map<Integer, Integer> tasksByPriority;
        public final Map<Task.TaskCategory, Integer> tasksByCategory;
        public final Map<LocalDate, Integer> completionTrend;
        public final List<ProductivityHour> productivityByHour;
        public final int currentStreak;
        public final int longestStreak;

        public TaskStatistics(int totalTasks, int completedTasks, int pendingTasks,
                              int overdueTasks, double completionRate, double averageCompletionTime,
                              Map<Integer, Integer> tasksByPriority,
                              Map<Task.TaskCategory, Integer> tasksByCategory,
                              Map<LocalDate, Integer> completionTrend,
                              List<ProductivityHour> productivityByHour,
                              int currentStreak, int longestStreak) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.pendingTasks = pendingTasks;
            this.overdueTasks = overdueTasks;
            this.completionRate = completionRate;
            this.averageCompletionTime = averageCompletionTime;
            this.tasksByPriority = tasksByPriority;
            this.tasksByCategory = tasksByCategory;
            this.completionTrend = completionTrend;
            this.productivityByHour = productivityByHour;
            this.currentStreak = currentStreak;
            this.longestStreak = longestStreak;
        }
    }

    // 生產力時段類
    public static class ProductivityHour {
        public final int hour;
        public final int tasksCompleted;
        public final double efficiency;

        public ProductivityHour(int hour, int tasksCompleted, double efficiency) {
            this.hour = hour;
            this.tasksCompleted = tasksCompleted;
            this.efficiency = efficiency;
        }
    }

    // 計算完整統計數據
    public static TaskStatistics calculateStatistics(ObservableList<Task> tasks) {
        LocalDate today = LocalDate.now();

        // 基本統計
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(Task::isDone).count();
        int pendingTasks = (int) tasks.stream().filter(t -> !t.isDone()).count();
        int overdueTasks = (int) tasks.stream()
                .filter(t -> !t.isDone() && t.getDueDate().isBefore(today))
                .count();
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

        // 平均完成時間（小時）
        double averageCompletionTime = calculateAverageCompletionTime(tasks);

        // 按優先級分組
        Map<Integer, Integer> tasksByPriority = tasks.stream()
                .collect(Collectors.groupingBy(
                        Task::getPriority,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // 按類別分組
        Map<Task.TaskCategory, Integer> tasksByCategory = tasks.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Task::getCategory,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // 完成趨勢（最近30天）
        Map<LocalDate, Integer> completionTrend = calculateCompletionTrend(tasks, 30);

        // 生產力時段分析
        List<ProductivityHour> productivityByHour = calculateProductivityByHour(tasks);

        // 連續達成統計
        int currentStreak = calculateCurrentStreak(tasks);
        int longestStreak = calculateLongestStreak(tasks);

        return new TaskStatistics(
                totalTasks, completedTasks, pendingTasks, overdueTasks,
                completionRate, averageCompletionTime, tasksByPriority,
                tasksByCategory, completionTrend, productivityByHour,
                currentStreak, longestStreak
        );
    }

    // 計算平均完成時間
    private static double calculateAverageCompletionTime(ObservableList<Task> tasks) {
        List<Double> completionTimes = tasks.stream()
                .filter(t -> t.isDone() && t.getCompletedAt() != null)
                .map(t -> {
                    LocalDateTime created = t.getDueDate().atStartOfDay();
                    LocalDateTime completed = t.getCompletedAt();
                    return (double) ChronoUnit.HOURS.between(created, completed);
                })
                .collect(Collectors.toList());

        return completionTimes.isEmpty() ? 0 :
                completionTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    // 計算完成趨勢
    private static Map<LocalDate, Integer> calculateCompletionTrend(ObservableList<Task> tasks, int days) {
        Map<LocalDate, Integer> trend = new TreeMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 初始化所有日期為0
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            trend.put(date, 0);
        }

        // 統計每日完成數
        tasks.stream()
                .filter(t -> t.isDone() && t.getCompletedAt() != null)
                .forEach(t -> {
                    LocalDate completedDate = t.getCompletedAt().toLocalDate();
                    if (!completedDate.isBefore(startDate) && !completedDate.isAfter(endDate)) {
                        trend.merge(completedDate, 1, Integer::sum);
                    }
                });

        return trend;
    }

    // 計算生產力時段
    private static List<ProductivityHour> calculateProductivityByHour(ObservableList<Task> tasks) {
        Map<Integer, Integer> hourlyCount = new HashMap<>();
        Map<Integer, Integer> hourlyTotal = new HashMap<>();

        // 初始化24小時
        for (int i = 0; i < 24; i++) {
            hourlyCount.put(i, 0);
            hourlyTotal.put(i, 0);
        }

        // 統計每小時完成的任務
        tasks.stream()
                .filter(t -> t.isDone() && t.getCompletedAt() != null)
                .forEach(t -> {
                    int hour = t.getCompletedAt().getHour();
                    hourlyCount.merge(hour, 1, Integer::sum);
                    // 根據優先級計算效率分數
                    int score = 6 - t.getPriority(); // 優先級1得5分，優先級5得1分
                    hourlyTotal.merge(hour, score, Integer::sum);
                });

        // 轉換為ProductivityHour列表
        List<ProductivityHour> productivity = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            int count = hourlyCount.get(hour);
            double efficiency = count > 0 ? (double) hourlyTotal.get(hour) / count : 0;
            productivity.add(new ProductivityHour(hour, count, efficiency));
        }

        return productivity;
    }

    // 計算當前連續達成天數
    private static int calculateCurrentStreak(ObservableList<Task> tasks) {
        Map<LocalDate, Long> dailyCompletions = tasks.stream()
                .filter(Task::isDone)
                .collect(Collectors.groupingBy(
                        t -> t.getCompletedAt() != null ? t.getCompletedAt().toLocalDate() : t.getDueDate(),
                        Collectors.counting()
                ));

        int streak = 0;
        LocalDate currentDate = LocalDate.now();

        // 從今天開始往前數
        while (dailyCompletions.getOrDefault(currentDate, 0L) >= 5) { // 假設每日目標是5個
            streak++;
            currentDate = currentDate.minusDays(1);
        }

        return streak;
    }

    // 計算最長連續達成天數
    private static int calculateLongestStreak(ObservableList<Task> tasks) {
        Map<LocalDate, Long> dailyCompletions = tasks.stream()
                .filter(Task::isDone)
                .collect(Collectors.groupingBy(
                        t -> t.getCompletedAt() != null ? t.getCompletedAt().toLocalDate() : t.getDueDate(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        if (dailyCompletions.isEmpty()) return 0;

        int maxStreak = 0;
        int currentStreak = 0;
        LocalDate prevDate = null;

        for (Map.Entry<LocalDate, Long> entry : dailyCompletions.entrySet()) {
            LocalDate date = entry.getKey();
            Long count = entry.getValue();

            if (count >= 5) { // 假設每日目標是5個
                if (prevDate == null || date.equals(prevDate.plusDays(1))) {
                    currentStreak++;
                } else {
                    currentStreak = 1;
                }
                maxStreak = Math.max(maxStreak, currentStreak);
                prevDate = date;
            } else {
                currentStreak = 0;
                prevDate = null;
            }
        }

        return maxStreak;
    }

    // 生成報告
    public static String generateReport(TaskStatistics stats) {
        StringBuilder report = new StringBuilder();

        report.append("📊 任務統計報告\n");
        report.append("================\n\n");

        report.append("📝 基本統計\n");
        report.append(String.format("總任務數: %d\n", stats.totalTasks));
        report.append(String.format("已完成: %d (%.1f%%)\n", stats.completedTasks, stats.completionRate));
        report.append(String.format("進行中: %d\n", stats.pendingTasks));
        report.append(String.format("已逾期: %d\n", stats.overdueTasks));
        report.append(String.format("平均完成時間: %.1f 小時\n\n", stats.averageCompletionTime));

        report.append("⭐ 優先級分布\n");
        for (int i = 1; i <= 5; i++) {
            int count = stats.tasksByPriority.getOrDefault(i, 0);
            report.append(String.format("優先級 %d: %d 個任務\n", i, count));
        }
        report.append("\n");

        report.append("📂 類別分布\n");
        stats.tasksByCategory.forEach((category, count) ->
                report.append(String.format("%s: %d 個任務\n", category, count))
        );
        report.append("\n");

        report.append("🔥 連續達成\n");
        report.append(String.format("當前連續: %d 天\n", stats.currentStreak));
        report.append(String.format("最長紀錄: %d 天\n\n", stats.longestStreak));

        report.append("⏰ 最佳工作時段 (前5名)\n");
        stats.productivityByHour.stream()
                .sorted((a, b) -> Integer.compare(b.tasksCompleted, a.tasksCompleted))
                .limit(5)
                .forEach(hour ->
                        report.append(String.format("%02d:00 - %d 個任務 (效率: %.1f)\n",
                                hour.hour, hour.tasksCompleted, hour.efficiency))
                );

        return report.toString();
    }
}