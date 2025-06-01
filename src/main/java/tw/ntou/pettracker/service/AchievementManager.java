package tw.ntou.pettracker.service;

import tw.ntou.pettracker.model.Achievement;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 成就管理器 - 定義和管理所有成就
 */
public class AchievementManager {

    // 成就定義 - 所有可用的成就
    private static final List<Achievement> ALL_ACHIEVEMENTS = Arrays.asList(
            // ===== 任務完成成就 =====
            new Achievement(
                    "first_task",
                    "初出茅廬",
                    "完成你的第一個任務",
                    "🎯",
                    10,
                    Achievement.AchievementType.TASK_COMPLETION,
                    1
            ),

            new Achievement(
                    "task_10",
                    "任務達人",
                    "完成10個任務",
                    "🏆",
                    50,
                    Achievement.AchievementType.TASK_COMPLETION,
                    10
            ),

            new Achievement(
                    "task_50",
                    "任務大師",
                    "完成50個任務",
                    "👑",
                    100,
                    Achievement.AchievementType.TASK_COMPLETION,
                    50
            ),

            new Achievement(
                    "task_100",
                    "任務傳奇",
                    "完成100個任務",
                    "🌟",
                    200,
                    Achievement.AchievementType.TASK_COMPLETION,
                    100
            ),

            new Achievement(
                    "task_500",
                    "任務之神",
                    "完成500個任務",
                    " ⚡ ",
                    500,
                    Achievement.AchievementType.TASK_COMPLETION,
                    500
            ),

            // ===== 連續達成成就 =====
            new Achievement(
                    "streak_3",
                    "三日不懈",
                    "連續3天完成每日目標",
                    "🔥",
                    30,
                    Achievement.AchievementType.STREAK,
                    3
            ),

            new Achievement(
                    "streak_7",
                    "週週向上",
                    "連續7天完成每日目標",
                    "💪",
                    70,
                    Achievement.AchievementType.STREAK,
                    7
            ),

            new Achievement(
                    "streak_30",
                    "月度達人",
                    "連續30天完成每日目標",
                    "🏅",
                    300,
                    Achievement.AchievementType.STREAK,
                    30
            ),

            new Achievement(
                    "streak_100",
                    "百日堅持",
                    "連續100天完成每日目標",
                    "💎",
                    1000,
                    Achievement.AchievementType.STREAK,
                    100
            ),

            // ===== 寵物照顧成就 =====
            new Achievement(
                    "pet_happy",
                    "快樂夥伴",
                    "寵物滿意度達到100%",
                    "😸",
                    50,
                    Achievement.AchievementType.PET_CARE,
                    1
            ),

            new Achievement(
                    "pet_full",
                    "美食家",
                    "寵物飽食度達到100%",
                    "🍖",
                    30,
                    Achievement.AchievementType.PET_CARE,
                    1
            ),

            new Achievement(
                    "pet_play_10",
                    "玩伴",
                    "與寵物玩耍10次",
                    "🎾",
                    40,
                    Achievement.AchievementType.PET_CARE,
                    10
            ),

            new Achievement(
                    "pet_feed_20",
                    "飼養員",
                    "餵食寵物20次",
                    "🍎",
                    40,
                    Achievement.AchievementType.PET_CARE,
                    20
            ),

            new Achievement(
                    "pet_perfect_week",
                    "完美照顧",
                    "連續7天保持寵物雙項指標80%以上",
                    "🌈",
                    100,
                    Achievement.AchievementType.PET_CARE,
                    7
            ),

            // ===== 生產力成就 =====
            new Achievement(
                    "priority_master",
                    "優先級大師",
                    "完成20個高優先級任務",
                    " ⚡ ",
                    80,
                    Achievement.AchievementType.PRODUCTIVITY,
                    20
            ),

            new Achievement(
                    "early_bird",
                    "早起鳥兒",
                    "在早上8點前完成5個任務",
                    "🌅",
                    60,
                    Achievement.AchievementType.PRODUCTIVITY,
                    5
            ),

            new Achievement(
                    "night_owl",
                    "夜貓子",
                    "在晚上10點後完成5個任務",
                    "🦉",
                    60,
                    Achievement.AchievementType.PRODUCTIVITY,
                    5
            ),

            new Achievement(
                    "speed_demon",
                    "極速達人",
                    "一小時內完成5個任務",
                    " ⚡ ",
                    80,
                    Achievement.AchievementType.PRODUCTIVITY,
                    5
            ),

            new Achievement(
                    "planner",
                    "計劃大師",
                    "提前完成30個任務",
                    "📅",
                    100,
                    Achievement.AchievementType.PRODUCTIVITY,
                    30
            ),

            // ===== 特殊成就 =====
            new Achievement(
                    "perfect_week",
                    "完美一週",
                    "一週內完成所有計劃任務",
                    "💎",
                    150,
                    Achievement.AchievementType.SPECIAL,
                    1
            ),

            new Achievement(
                    "comeback",
                    "王者歸來",
                    "中斷後重新開始使用",
                    "👑",
                    50,
                    Achievement.AchievementType.SPECIAL,
                    1
            ),

            new Achievement(
                    "multitasker",
                    "多工達人",
                    "一天內完成5個不同類別的任務",
                    "🎪",
                    100,
                    Achievement.AchievementType.SPECIAL,
                    1
            ),

            new Achievement(
                    "zero_overdue",
                    "零逾期",
                    "連續30天沒有逾期任務",
                    "✨",
                    200,
                    Achievement.AchievementType.SPECIAL,
                    30
            ),

            new Achievement(
                    "all_categories",
                    "全能王",
                    "在所有類別中都完成至少10個任務",
                    "🎯",
                    150,
                    Achievement.AchievementType.SPECIAL,
                    1
            ),

            new Achievement(
                    "weekend_warrior",
                    "週末戰士",
                    "連續4個週末都完成任務",
                    "⚔ ",
                    80,
                    Achievement.AchievementType.SPECIAL,
                    4
            ),

            new Achievement(
                    "subtask_master",
                    "細節大師",
                    "完成50個包含子任務的任務",
                    "🔍",
                    100,
                    Achievement.AchievementType.SPECIAL,
                    50
            )
    );

    // 成就類別描述
    public static final String[] ACHIEVEMENT_CATEGORY_DESCRIPTIONS = {
            "任務完成 - 完成指定數量的任務",
            "連續達成 - 保持連續完成任務的紀錄",
            "寵物照顧 - 照顧好你的虛擬寵物",
            "生產力 - 展現你的工作效率",
            "特殊成就 - 完成特殊挑戰"
    };

    /**
     * 獲取所有成就列表
     */
    public static List<Achievement> getAllAchievements() {
        return new ArrayList<>(ALL_ACHIEVEMENTS);
    }

    /**
     * 根據ID獲取成就
     */
    public static Achievement getAchievementById(String id) {
        return ALL_ACHIEVEMENTS.stream()
                .filter(achievement -> achievement.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根據類型獲取成就列表
     */
    public static List<Achievement> getAchievementsByType(Achievement.AchievementType type) {
        return ALL_ACHIEVEMENTS.stream()
                .filter(achievement -> achievement.getType() == type)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 獲取總成就數量
     */
    public static int getTotalAchievementCount() {
        return ALL_ACHIEVEMENTS.size();
    }

    /**
     * 獲取總成就點數
     */
    public static int getTotalPossiblePoints() {
        return ALL_ACHIEVEMENTS.stream()
                .mapToInt(Achievement::getPoints)
                .sum();
    }

    /**
     * 檢查成就解鎖條件
     * 這個方法可以被擴展來支援更複雜的解鎖邏輯
     */
    public static boolean checkAchievementCondition(String achievementId, Object... params) {
        Achievement achievement = getAchievementById(achievementId);
        if (achievement == null) {
            return false;
        }

        // 特殊成就的複雜條件檢查
        switch (achievementId) {
            case "perfect_week":
                // 檢查是否一週內完成所有任務
                // 需要傳入任務列表和日期範圍
                return checkPerfectWeek(params);

            case "all_categories":
                // 檢查是否在所有類別都有完成任務
                return checkAllCategories(params);

            case "weekend_warrior":
                // 檢查連續週末完成任務
                return checkWeekendWarrior(params);

            default:
                // 其他成就使用簡單的數量檢查
                return true;
        }
    }

    /**
     * 檢查完美一週成就
     */
    private static boolean checkPerfectWeek(Object... params) {

        // 實現檢查邏輯
        // 這裡需要檢查一週內是否所有計劃的任務都完成了
        return true; // 簡化實現
    }

    /**
     * 檢查全能王成就
     */
    private static boolean checkAllCategories(Object... params) {
        // 實現檢查邏輯
        // 檢查是否在每個類別都完成了至少10個任務
        return true; // 簡化實現
    }

    /**
     * 檢查週末戰士成就
     */
    private static boolean checkWeekendWarrior(Object... params) {
        // 實現檢查邏輯
        // 檢查連續4個週末是否都有完成任務
        return true; // 簡化實現
    }

    /**
     * 獲取成就統計信息
     */
    public static class AchievementStats {
        public final int totalAchievements;
        public final int unlockedAchievements;
        public final int totalPoints;
        public final int earnedPoints;
        public final double completionPercentage;

        public AchievementStats(List<Achievement> achievements) {
            this.totalAchievements = achievements.size();
            this.unlockedAchievements = (int) achievements.stream()
                    .filter(Achievement::isUnlocked)
                    .count();
            this.totalPoints = achievements.stream()
                    .mapToInt(Achievement::getPoints)
                    .sum();
            this.earnedPoints = achievements.stream()
                    .filter(Achievement::isUnlocked)
                    .mapToInt(Achievement::getPoints)
                    .sum();
            this.completionPercentage = totalAchievements > 0 ?
                    (double) unlockedAchievements / totalAchievements * 100 : 0;
        }
    }

    /**
     * 生成成就報告
     */
    public static String generateAchievementReport(List<Achievement> achievements) {
        AchievementStats stats = new AchievementStats(achievements);
        StringBuilder report = new StringBuilder();

        report.append("🏆 成就系統報告\n");
        report.append("================\n\n");

        report.append(String.format("解鎖進度: %d/%d (%.1f%%)\n",
                stats.unlockedAchievements, stats.totalAchievements, stats.completionPercentage));
        report.append(String.format("獲得點數: %d/%d\n\n",
                stats.earnedPoints, stats.totalPoints));

        // 按類型分組顯示
        for (Achievement.AchievementType type : Achievement.AchievementType.values()) {
            report.append(String.format("\n%s %s\n", type.getEmoji(), type.getDisplayName()));
            report.append("-".repeat(20)).append("\n");

            achievements.stream()
                    .filter(a -> a.getType() == type)
                    .forEach(a -> {
                        String status = a.isUnlocked() ? "✅" : "⬜";
                        report.append(String.format("%s %s - %s (%d點)\n",
                                status, a.getName(), a.getDescription(), a.getPoints()));
                    });
        }

        return report.toString();
    }
}