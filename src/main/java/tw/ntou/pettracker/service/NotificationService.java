package tw.ntou.pettracker.service;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.util.Duration;
// 注意：如果沒有 ControlsFX，可以用 JavaFX 的 Alert 代替
// import org.controlsfx.control.Notifications;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.model.Pet;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;// 需要的額外 imports
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

public class NotificationService {
    private static NotificationService instance;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private boolean systemTraySupported = false;

    // 通知類型
    public enum NotificationType {
        TASK_DUE("任務到期", "⏰"),
        TASK_OVERDUE("任務逾期", "❗"),
        PET_HUNGRY("寵物飢餓", "🍖"),
        PET_UNHAPPY("寵物不開心", "😿"),
        ACHIEVEMENT_UNLOCKED("成就解鎖", "🏆"),
        DAILY_GOAL_REACHED("達成每日目標", "🎯"),
        REMINDER("提醒", "🔔"),
        STREAK_MILESTONE("連續達成", "🔥");

        private final String title;
        private final String emoji;

        NotificationType(String title, String emoji) {
            this.title = title;
            this.emoji = emoji;
        }

        public String getTitle() { return title; }
        public String getEmoji() { return emoji; }
    }

    private NotificationService() {
        initializeSystemTray();
    }

    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    private void initializeSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("系統托盤不支援");
            return;
        }

        try {
            systemTray = SystemTray.getSystemTray();

            // 使用預設圖標或替代方案
            java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(
                    getClass().getResource("/tw/ntou/pettracker/icon/cat.png"));

            trayIcon = new TrayIcon(awtImage, "Pet Habit Tracker");
            trayIcon.setImageAutoSize(true);

            // 添加右鍵選單
            PopupMenu popup = new PopupMenu();
            MenuItem showItem = new MenuItem("顯示主視窗");
            MenuItem exitItem = new MenuItem("退出");

            popup.add(showItem);
            popup.addSeparator();
            popup.add(exitItem);

            trayIcon.setPopupMenu(popup);
            systemTray.add(trayIcon);
            systemTraySupported = true;

        } catch (Exception e) {
            System.err.println("無法初始化系統托盤: " + e.getMessage());
        }
    }

    // 顯示應用內通知（使用 JavaFX Alert 替代 ControlsFX）
    public void showNotification(NotificationType type, String message) {
        Platform.runLater(() -> {
            // 使用 JavaFX Alert 替代 ControlsFX Notifications
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(type.getEmoji() + " " + type.getTitle());
            alert.setHeaderText(null);
            alert.setContentText(message);

            // 自動關閉
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(5),
                    ae -> alert.close()
            ));
            timeline.play();

            alert.show();
        });

        // 同時顯示系統托盤通知
        if (systemTraySupported && trayIcon != null) {
            trayIcon.displayMessage(
                    type.getTitle(),
                    message,
                    MessageType.INFO
            );
        }
    }

    // 顯示成就解鎖通知（特殊動畫）
    public void showAchievementNotification(String achievementName, String description, int points) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("🏆 成就解鎖！");
            alert.setHeaderText(achievementName);
            alert.setContentText(description + "\n+" + points + " 點");
            alert.show();
        });
    }

    // 安排任務提醒
    public void scheduleTaskReminder(Task task) {
        if (!task.isRemind() || task.getRemindAt() == null) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime remindTime = task.getRemindAt();

        if (remindTime.isBefore(now)) return;

        long delay = ChronoUnit.MILLIS.between(now, remindTime);

        scheduler.schedule(() -> {
            showNotification(
                    NotificationType.REMINDER,
                    "任務提醒: " + task.getDescription()
            );
        }, delay, TimeUnit.MILLISECONDS);
    }

    // 檢查並發送任務相關通知
    public void checkTaskNotifications(ObservableList<Task> tasks) {
        LocalDate today = LocalDate.now();

        for (Task task : tasks) {
            if (task.isDone()) continue;

            // 檢查逾期任務
            if (task.getDueDate().isBefore(today)) {
                showNotification(
                        NotificationType.TASK_OVERDUE,
                        "任務已逾期: " + task.getDescription()
                );
            }
            // 檢查今日到期任務
            else if (task.getDueDate().equals(today)) {
                showNotification(
                        NotificationType.TASK_DUE,
                        "任務今日到期: " + task.getDescription()
                );
            }
        }
    }

    // 檢查寵物狀態並發送通知
    public void checkPetNotifications(Pet pet) {
        if (pet.getFullness() < 20) {
            showNotification(
                    NotificationType.PET_HUNGRY,
                    "你的寵物很餓了！快去餵食吧"
            );
        }

        if (pet.getSatisfaction() < 20) {
            showNotification(
                    NotificationType.PET_UNHAPPY,
                    "你的寵物不太開心，需要你的關愛"
            );
        }
    }

    // 開始定期檢查
    public void startPeriodicChecks(ObservableList<Task> tasks, Pet pet) {
        // 每小時檢查一次
        scheduler.scheduleAtFixedRate(() -> {
            checkTaskNotifications(tasks);
            checkPetNotifications(pet);
        }, 0, 1, TimeUnit.HOURS);
    }

    // 關閉服務
    public void shutdown() {
        scheduler.shutdown();
        if (systemTraySupported && systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
        }
    }
}

