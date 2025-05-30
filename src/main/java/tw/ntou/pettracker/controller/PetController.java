package tw.ntou.pettracker.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import tw.ntou.pettracker.model.Pet;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.util.MessageUtil;

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

    public PetController(Pet pet, ProgressBar satisfactionBar, ProgressBar fullnessBar,
                         Label satisfactionLabel, Label fullnessLabel) {
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

    public void setPlayButton(Button playButton) {
        this.playButton = playButton;
        if (playButton != null) {
            playButton.setOnAction(e -> playWithPet());
        }
    }

    /**
     * 綁定寵物狀態到UI
     */
    private void bindPetStats() {
        // 綁定進度條
        satisfactionBar.progressProperty().bind(pet.satisfactionProperty().divide(100.0));
        fullnessBar.progressProperty().bind(pet.fullnessProperty().divide(100.0));

        // 綁定文字標籤
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

        // 監聽寵物狀態變化
        pet.satisfactionProperty().addListener((obs, oldVal, newVal) -> {
            checkPetMood(newVal.intValue());
        });

        pet.fullnessProperty().addListener((obs, oldVal, newVal) -> {
            checkPetHunger(newVal.intValue());
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

        // 檢查成就
        checkFeedingAchievement();
    }

    /**
     * 和寵物玩耍
     */
    private void playWithPet() {
        pet.play();
        if (animationController != null) {
            animationController.playPetAnimation();
        }
        MessageUtil.showMessage("🎾 你和寵物玩耍，它很興奮！");

        // 檢查玩耍成就
        checkPlayAchievement();
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
        } else if (completedToday == 0) {
            // 沒有完成任務，寵物狀態下降
            pet.setSatisfaction(Math.max(0, pet.getSatisfaction() - 5));
            pet.setFullness(Math.max(0, pet.getFullness() - 10));
        }
    }

    /**
     * 更新進度顯示
     */
    public void updateProgress() {
        // 進度條會自動更新（因為已經綁定）
        // 這裡可以添加其他需要更新的內容
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
     * 檢查寵物飢餓狀態
     */
    private void checkPetHunger(int fullness) {
        if (fullness < 20) {
            MessageUtil.showWarning("🍖 你的寵物很餓了！快去餵食吧！");
        }
    }

    /**
     * 檢查餵食成就
     */
    private void checkFeedingAchievement() {
        // 這裡可以觸發相關成就
        if (pet.getFullness() >= 100) {
            // 觸發"美食家"成就
        }
    }

    /**
     * 檢查玩耍成就
     */
    private void checkPlayAchievement() {
        // 這裡可以記錄玩耍次數並觸發相關成就
        if (pet.getSatisfaction() >= 100) {
            // 觸發"快樂夥伴"成就
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
    }

    public Pet getPet() {
        return pet;
    }
}