package tw.ntou.pettracker.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import java.net.URL;
import tw.ntou.pettracker.model.Pet;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.model.PetVideoType;
import tw.ntou.pettracker.service.PetVideoService;
import tw.ntou.pettracker.service.PetVideoService.PetVideo;
import tw.ntou.pettracker.util.MessageUtil;
import tw.ntou.pettracker.util.PetVideoGalleryDialog;

import java.io.File;
import java.time.LocalDate;

/**
 * 處理寵物相關的所有邏輯
 */
public class PetController {
    private final Pet pet;
    private final ProgressBar satisfactionBar;
    private final ProgressBar fullnessBar;
    private final Label satisfactionLabel;
    private final Label fullnessLabel;
    private AnimationController animationController;
    private Button feedButton;
    private Button playButton;
    private Label playChanceLabel;

    // 處理影片播放的服務
    private final PetVideoService videoService = PetVideoService.getInstance();
    private MediaView petMediaView; // 從 FXML 注入，用於播放影片

    public PetController(Pet pet,
                         ProgressBar satisfactionBar,
                         ProgressBar fullnessBar,
                         Label satisfactionLabel,
                         Label fullnessLabel) {
        this.pet = pet;
        this.satisfactionBar = satisfactionBar;
        this.fullnessBar = fullnessBar;
        this.satisfactionLabel = satisfactionLabel;
        this.fullnessLabel = fullnessLabel;

        bindPetStats();
    }

    public void setAnimationController(AnimationController animationController) {
        this.animationController = animationController;
    }

    public void setFeedButton(Button feedButton) {
        this.feedButton = feedButton;
        if (feedButton != null) {
            feedButton.setOnAction(e -> feedPet());
        }
    }

    public void setPlayButton(Button playButton, Label playChanceLabel) {
        this.playButton = playButton;
        if (playButton != null) {
            playButton.setOnAction(e -> playWithPet());
            updatePlayChanceLabel();
        }
    }

    /**
     * 綁定寵物狀態到 UI
     */
    private void bindPetStats() {
        // 綁定進度條值（將 0-100 的屬性綁到 0.0-1.0）
        satisfactionBar.progressProperty().bind(pet.satisfactionProperty().divide(100.0));
        fullnessBar.progressProperty().bind(pet.fullnessProperty().divide(100.0));

        // 綁定顯示文字
        if (satisfactionLabel != null) {
            satisfactionLabel.textProperty().bind(
                    Bindings.concat(pet.satisfactionProperty().asString(), "%")
            );
        }
        if (fullnessLabel != null) {
            fullnessLabel.textProperty().bind(
                    Bindings.concat(pet.fullnessProperty().asString(), "%")
            );
        }

        // 監聽滿意度變化，更新心情提示和影片
        pet.satisfactionProperty().addListener((obs, oldVal, newVal) -> {
            checkPetMood(newVal.intValue());
            updatePetVideo();
        });

        // 監聽飽食度變化，更新飢餓提示和影片
        pet.fullnessProperty().addListener((obs, oldVal, newVal) -> {
            checkPetHunger(newVal.intValue());
            updatePetVideo();
        });
    }

    /**
     * 餵食寵物
     */
    private void feedPet() {
        pet.feed();
        if (animationController != null) {
            animationController.playPetAnimation();
        }
        MessageUtil.showMessage("🍎 你餵了寵物，它很開心！");

        // 播放「吃飯」類型影片 (EAT)
        PetVideo video = videoService.getRandomVideo(PetVideoType.EAT);
        if (video != null && petMediaView != null) {
            playVideoOnMediaView(video);
        }

        checkFeedingAchievement();
    }

    /**
     * 和寵物玩耍
     */
    private void playWithPet() {
        System.out.print(pet.getPlayChances()+"\n");
        if (pet.getPlayChances()> 0) {
            pet.play(); // 呼叫 model（Pet.java）邏輯
            pet.usePlayChance();
            updatePlayChanceLabel();
            if (animationController != null) {
                animationController.playPetAnimation();
            }
            MessageUtil.showMessage("🎾 你和寵物玩耍，它很興奮！");

            // 播放「玩耍」類型影片 (PLAY)
            PetVideo video = videoService.getRandomVideo(PetVideoType.PLAY);
            if (video != null && petMediaView != null) {
                playVideoOnMediaView(video);
            }

            checkPlayAchievement();
        } else {
            MessageUtil.showMessage("沒有可用的玩耍次數！");
        }
    }

    /**
     * 更新寵物狀態（根據任務完成情況）
     */
    public void updatePetStatus(ObservableList<Task> tasks, int dailyGoal) {
        LocalDate today = LocalDate.now();
        long completedToday = tasks.stream()
                .filter(Task::isDone)
                .filter(t -> t.getCompletedAt() != null &&
                        t.getCompletedAt().toLocalDate().equals(today))
                .count();

        if (completedToday >= dailyGoal) {
            pet.setSatisfaction(Math.min(100, pet.getSatisfaction() + 10));
            pet.setFullness(Math.min(100, pet.getFullness() + 15));

            PetVideo video = videoService.getRandomVideo(PetVideoType.COSTUME);
            if (video != null && petMediaView != null) {
                playVideoOnMediaView(video);
            }
        } else if (completedToday == 0) {
            // 沒有完成任何任務，寵物狀態下降
            pet.setSatisfaction(Math.max(0, pet.getSatisfaction() - 5));
            pet.setFullness(Math.max(0, pet.getFullness() - 10));
        }
    }

    /**
     * 更新進度顯示（已綁定，通常不需動作）
     */
    public void updateProgress() {
        // 進度條自動更新
    }

    /**
     * 慶祝達成每日目標
     */
    public void celebrateDailyGoal() {
        pet.setSatisfaction(Math.min(100, pet.getSatisfaction() + 20));
        pet.setFullness(Math.min(100, pet.getFullness() + 15));

        if (animationController != null) {
            animationController.playCelebrationAnimation();
        }

        // ► 同樣改為 COSTUME 類型影片當作慶祝
        PetVideo video = videoService.getRandomVideo(PetVideoType.COSTUME);
        if (video != null && petMediaView != null) {
            playVideoOnMediaView(video);
        }
    }

    /**
     * 檢查寵物心情
     */
    private void checkPetMood(int satisfaction) {
        if (satisfaction < 20) {
            MessageUtil.showWarning("😿 你的寵物不太開心，需要你的關愛！");
        } else if (satisfaction >= 100) {
            MessageUtil.showMessage("😸 你的寵物非常開心！");
        }
    }

    /**
     * 檢查寵物飢餓
     */
    private void checkPetHunger(int fullness) {
        if (fullness < 20) {
            MessageUtil.showWarning("🍖 你的寵物很餓了！快去餵食吧！");
        }
    }

    private void checkFeedingAchievement() {
        if (pet.getFullness() >= 100) {
            // 觸發「美食家」成就

        }
    }

    private void checkPlayAchievement() {
        if (pet.getSatisfaction() >= 100) {
            // 觸發「快樂夥伴」成就

        }
    }

    /**
     * 獲取寵物狀態摘要
     */
    public String getPetStatusSummary() {
        return String.format("%s - 滿意度: %d%%, 飽食度: %d%%",
                pet.getStatusDescription(),
                pet.getSatisfaction(),
                pet.getFullness());
    }

    /**
     * 時間流逝效果
     */
    public void timePasses() {
        pet.timePass();
        updatePetVideo();
    }

    public Pet getPet() {
        return pet;
    }


    /**
     * 更新寵物影片，根據 currentVideoType 播放對應影片
     */
    public void updatePetVideo() {
        PetVideoType type = pet.getCurrentVideoType();
        // ► 改用 getRandomVideo
        PetVideo video = videoService.getRandomVideo(type);
        if (video != null && petMediaView != null) {
            playVideoOnMediaView(video);
        }
    }

    /**
     * 顯示影片相簿讓使用者選擇播放
     */
    public void showVideoGallery() {
        PetVideoGalleryDialog dialog = new PetVideoGalleryDialog();
        dialog.showAndWait().ifPresent(selectedVideo -> {
            if (petMediaView != null) {
                playVideoOnMediaView(selectedVideo);
            }
        });
    }

    /**
     * 注入來自 FXML 的 MediaView
     */
    public void setPetMediaView(MediaView mediaView) {
        this.petMediaView = mediaView;
    }

    /**
     * 私有方法：在 Controller 端將 PetVideo 綁定到 MediaView 播放
     */
    private void playVideoOnMediaView(PetVideo video) {
        String filename = video.getFilename();
        // 假設 filename 例如 "逗貓14.mp4"

        //  用 getResource 從 Classpath 內讀取影片 URL
        //    路徑要跟 src/main/resources 底下的結構對應
        URL videoUrl = getClass().getResource("/tw/ntou/pettracker/video/" + filename);
        if (videoUrl == null) {
            // 如果 resource 路徑找不到，就跳警告
            MessageUtil.showWarning("影片檔案不存在: " + filename);
            return;
        }

        // 停掉並釋放舊的 MediaPlayer
        MediaPlayer oldPlayer = petMediaView.getMediaPlayer();
        if (oldPlayer != null) {
            oldPlayer.stop();
            oldPlayer.dispose();
        }

        // 建立新的 MediaPlayer，並綁定到 MediaView
        Media media = new Media(videoUrl.toExternalForm());
        MediaPlayer newPlayer = new MediaPlayer(media);
        // 如果想要一直循環播放，可以打開下一行；否則可拿掉
        newPlayer.setCycleCount(MediaPlayer.INDEFINITE);

        petMediaView.setMediaPlayer(newPlayer);
        petMediaView.setVisible(true);  // 確保 MediaView 可見
        newPlayer.play();
    }

    public void setPlayChanceLabel(Label label) {
        this.playChanceLabel = label;
        updatePlayChanceLabel();
    }
    private void updatePlayChanceLabel() {
        if (playChanceLabel != null) {
            playChanceLabel.setText("剩餘玩耍次數：" + pet.getPlayChances());
        }
    }
}
