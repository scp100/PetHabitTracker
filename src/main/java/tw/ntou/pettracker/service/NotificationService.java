package tw.ntou.pettracker.service;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.util.Duration;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.model.Pet;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

public class NotificationService {
    private static NotificationService instance;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private boolean systemTraySupported = false;
    private Stage primaryStage;
    private volatile boolean isProcessingAction = false;

    public interface DataSaveCallback {
        void saveData();
    }

    private DataSaveCallback dataSaveCallback;

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

    public void initialize(Stage primaryStage, DataSaveCallback dataSaveCallback) {
        this.primaryStage = primaryStage;
        this.dataSaveCallback = dataSaveCallback;

        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            minimizeToTray();
        });
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

            // 右鍵選單
            PopupMenu popup = new PopupMenu();
            MenuItem openItem = new MenuItem("open main window");
            MenuItem exitItem = new MenuItem("exit");

            openItem.addActionListener(e -> {
                Platform.runLater(this::restoreFromTray);
            });


            exitItem.addActionListener(e -> {
                exitApplication();
            });


            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // 左鍵點擊行為
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (!isProcessingAction) {
                            isProcessingAction = true;
                            Platform.runLater(() -> {
                                try {
                                    restoreFromTray();
                                } finally {
                                    isProcessingAction = false;
                                }
                            });
                        }
                    }
                }
            });

            if (systemTray != null) {
                systemTray.add(trayIcon);
            }
            systemTraySupported = true;
            System.out.println("系統托盤初始化成功");

        } catch (Exception e) {
            systemTraySupported = false;
            System.err.println("無法初始化系統托盤: " + e.getMessage());
            e.printStackTrace();
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

        scheduler.schedule(() -> showNotification(
                NotificationType.REMINDER,
                "任務提醒: " + task.getDescription()
        ), delay, TimeUnit.MILLISECONDS);
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

    // 最小化到系統托盤
    public void minimizeToTray() {
        if (primaryStage == null) {
            return;
        }

        // 保存數據
        if (dataSaveCallback != null) {
            dataSaveCallback.saveData();
            System.out.println("💾 資料已保存");
        }

        // 隱藏主畫面
        primaryStage.hide();

        System.out.println("應用已最小化到系統托盤");
    }

    // 新增方法：從系統托盤恢復窗口
    public void restoreFromTray() {
        if (primaryStage == null) {
            System.err.println("主窗口未設置，無法從托盤恢復");
            return;
        }

        // 顯示主窗口
        primaryStage.show();

        // 取消最小化狀態，並將窗口帶到前台
        primaryStage.setIconified(false);
        primaryStage.toFront();
        primaryStage.requestFocus();

        System.out.println("從系統托盤恢復視窗");
    }

    public void exitApplication() {
        // 保存數據
        if (dataSaveCallback != null) {
            try {
                dataSaveCallback.saveData();
                System.out.println("💾 退出前資料已保存");
            } catch (Exception e) {
                System.err.println("保存數據時出錯: " + e.getMessage());
            }
        }

        // 關閉定時任務
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        // 清理系統托盤資源
        if (systemTraySupported && systemTray != null && trayIcon != null) {
            try {
                systemTray.remove(trayIcon);
            } catch (Exception e) {
                System.err.println("移除系統托盤圖標時出錯: " + e.getMessage());
            }
        }

        // 直接使用System.exit退出
        new Thread(() -> {
            try {
                Thread.sleep(500); // 給資源清理一些時間
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.exit(0);
        }).start();
    }

}

