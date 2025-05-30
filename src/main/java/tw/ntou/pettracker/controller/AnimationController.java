package tw.ntou.pettracker.controller;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * 管理所有動畫效果
 */
public class AnimationController {
    private final ImageView petImage;
    private final TableView<?> table;
    private final VBox petPanel;

    public AnimationController(ImageView petImage, TableView<?> table, VBox petPanel) {
        this.petImage = petImage;
        this.table = table;
        this.petPanel = petPanel;
    }

    /**
     * 播放任務完成動畫
     */
    public void playTaskCompletionAnimation() {
        if (petImage != null) {
            // 寵物旋轉慶祝
            RotateTransition rotate = new RotateTransition(Duration.millis(500), petImage);
            rotate.setByAngle(15);
            rotate.setCycleCount(4);
            rotate.setAutoReverse(true);

            // 放大效果
            ScaleTransition scale = new ScaleTransition(Duration.millis(300), petImage);
            scale.setToX(1.2);
            scale.setToY(1.2);
            scale.setCycleCount(2);
            scale.setAutoReverse(true);

            // 並行播放
            ParallelTransition parallel = new ParallelTransition(rotate, scale);
            parallel.play();
        }
    }

    /**
     * 播放任務新增動畫
     */
    public void playTaskAddAnimation() {
        if (table != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), table);
            fade.setFromValue(0.7);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    /**
     * 播放寵物互動動畫
     */
    public void playPetAnimation() {
        if (petImage != null) {
            // 跳躍效果
            TranslateTransition jump = new TranslateTransition(Duration.millis(200), petImage);
            jump.setByY(-20);
            jump.setCycleCount(2);
            jump.setAutoReverse(true);

            // 放大效果
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), petImage);
            scale.setToX(1.1);
            scale.setToY(1.1);
            scale.setCycleCount(2);
            scale.setAutoReverse(true);

            ParallelTransition parallel = new ParallelTransition(jump, scale);
            parallel.play();
        }
    }

    /**
     * 播放慶祝動畫
     */
    public void playCelebrationAnimation() {
        if (petPanel != null) {
            // 整個寵物面板的慶祝效果
            ScaleTransition celebration = new ScaleTransition(Duration.millis(300), petPanel);
            celebration.setToX(1.05);
            celebration.setToY(1.05);
            celebration.setCycleCount(2);
            celebration.setAutoReverse(true);

            // 顏色閃爍效果
            FadeTransition flash = new FadeTransition(Duration.millis(150), petPanel);
            flash.setFromValue(1.0);
            flash.setToValue(0.8);
            flash.setCycleCount(4);
            flash.setAutoReverse(true);

            ParallelTransition parallel = new ParallelTransition(celebration, flash);
            parallel.play();
        }

        // 寵物特殊動畫
        if (petImage != null) {
            playHappyDance();
        }
    }

    /**
     * 播放聚焦動畫
     */
    public void playFocusAnimation(Node node) {
        if (node != null) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.setCycleCount(2);
            scale.setAutoReverse(true);
            scale.play();
        }
    }

    /**
     * 播放錯誤動畫
     */
    public void playErrorAnimation(Node node) {
        if (node != null) {
            // 震動效果
            TranslateTransition shake = new TranslateTransition(Duration.millis(100), node);
            shake.setByX(5);
            shake.setCycleCount(4);
            shake.setAutoReverse(true);
            shake.play();
        }
    }

    /**
     * 播放成功動畫
     */
    public void playSuccessAnimation(Node node) {
        if (node != null) {
            // 淡入效果
            FadeTransition fade = new FadeTransition(Duration.millis(500), node);
            fade.setFromValue(0.5);
            fade.setToValue(1.0);

            // 放大效果
            ScaleTransition scale = new ScaleTransition(Duration.millis(300), node);
            scale.setFromX(0.8);
            scale.setFromY(0.8);
            scale.setToX(1.0);
            scale.setToY(1.0);

            ParallelTransition parallel = new ParallelTransition(fade, scale);
            parallel.play();
        }
    }

    /**
     * 播放載入動畫
     */
    public void playLoadingAnimation(Node loadingIndicator, boolean start) {
        if (loadingIndicator != null) {
            if (start) {
                loadingIndicator.setVisible(true);
                RotateTransition rotate = new RotateTransition(Duration.seconds(1), loadingIndicator);
                rotate.setByAngle(360);
                rotate.setCycleCount(Timeline.INDEFINITE);
                rotate.play();
            } else {
                loadingIndicator.setVisible(false);
            }
        }
    }

    /**
     * 播放寵物快樂舞蹈
     */
    private void playHappyDance() {
        // 複雜的組合動畫
        RotateTransition rotate1 = new RotateTransition(Duration.millis(200), petImage);
        rotate1.setByAngle(10);

        RotateTransition rotate2 = new RotateTransition(Duration.millis(200), petImage);
        rotate2.setByAngle(-20);

        RotateTransition rotate3 = new RotateTransition(Duration.millis(200), petImage);
        rotate3.setByAngle(10);

        TranslateTransition jump = new TranslateTransition(Duration.millis(300), petImage);
        jump.setByY(-30);
        jump.setAutoReverse(true);
        jump.setCycleCount(2);

        SequentialTransition dance = new SequentialTransition(
                rotate1, rotate2, rotate3, jump
        );
        dance.play();
    }

    /**
     * 播放拖放動畫
     */
    public void playDragAnimation(Node node, boolean isDragging) {
        if (node != null) {
            if (isDragging) {
                node.setOpacity(0.5);
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), node);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.play();
            } else {
                node.setOpacity(1.0);
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), node);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            }
        }
    }

    /**
     * 播放心情變化動畫
     */
    public void playMoodChangeAnimation(boolean isHappy) {
        if (petImage != null) {
            if (isHappy) {
                // 開心動畫
                ScaleTransition grow = new ScaleTransition(Duration.millis(500), petImage);
                grow.setToX(1.2);
                grow.setToY(1.2);
                grow.setAutoReverse(true);
                grow.setCycleCount(2);
                grow.play();
            } else {
                // 不開心動畫
                TranslateTransition droop = new TranslateTransition(Duration.millis(500), petImage);
                droop.setByY(10);
                droop.setAutoReverse(true);
                droop.setCycleCount(2);

                FadeTransition fade = new FadeTransition(Duration.millis(500), petImage);
                fade.setToValue(0.8);
                fade.setAutoReverse(true);
                fade.setCycleCount(2);

                ParallelTransition sad = new ParallelTransition(droop, fade);
                sad.play();
            }
        }
    }
}