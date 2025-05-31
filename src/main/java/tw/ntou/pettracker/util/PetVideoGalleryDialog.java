package tw.ntou.pettracker.util;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import tw.ntou.pettracker.model.PetVideoType;
import tw.ntou.pettracker.service.PetVideoService;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 寵物影片相簿對話框
 */
public class PetVideoGalleryDialog extends Dialog<PetVideoService.PetVideo> {
    private final PetVideoService videoService = PetVideoService.getInstance();
    private MediaPlayer currentPlayer;
    private MediaView mediaView;
    private VBox videoInfoBox;
    private GridPane videoGrid;
    private Label statsLabel;
    private PetVideoType.VideoCategory currentCategory = null;

    public PetVideoGalleryDialog() {
        setTitle("🎬 寵物影片相簿");
        setHeaderText("瀏覽已解鎖的寵物影片");

        // 設定對話框大小
        getDialogPane().setPrefSize(1000, 700);

        // 主容器
        BorderPane mainPane = new BorderPane();
        mainPane.setPrefSize(980, 650);

        // 左側類別選單
        VBox categoryPane = createCategoryPane();

        // 中間影片網格
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        // 下面這一行不要再做 refreshVideoGrid()，只回傳新的 GridPane
        videoGrid = createVideoGrid();
        scrollPane.setContent(videoGrid);

        // 右側播放器
        VBox playerPane = createPlayerPane();

        // 組裝布局
        mainPane.setLeft(categoryPane);
        mainPane.setCenter(scrollPane);
        mainPane.setRight(playerPane);

        getDialogPane().setContent(mainPane);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // 套用樣式
        getDialogPane().getStyleClass().add("video-gallery-dialog");

        // ---- 建立完所有元件後，才正式呼叫一次 refreshVideoGrid() ----
        refreshVideoGrid();

        // 清理資源
        setOnCloseRequest(e -> {
            if (currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.dispose();
            }
        });
    }

    /**
     * 創建類別選單
     */
    private VBox createCategoryPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));
        pane.setPrefWidth(200);
        pane.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; " +
                "-fx-border-width: 0 1 0 0;");

        Label title = new Label("📁 影片類別");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        pane.getChildren().add(title);

        Separator separator1 = new Separator();
        pane.getChildren().add(separator1);

        ToggleGroup categoryGroup = new ToggleGroup();

        // 全部類別按鈕
        RadioButton allBtn = createCategoryButton("全部影片", null, categoryGroup);
        allBtn.setSelected(true);
        pane.getChildren().add(allBtn);

        // 各個類別按鈕
        for (PetVideoType.VideoCategory category : PetVideoType.VideoCategory.values()) {
            if (category != PetVideoType.VideoCategory.RESERVED) {
                String icon = getCategoryIcon(category);
                RadioButton btn = createCategoryButton(icon + " " + category.getName(), category, categoryGroup);
                pane.getChildren().add(btn);

                // 顯示該類別的影片數量
                long count = videoService.getUnlockedVideos().stream()
                        .filter(v -> v.getType().getCategory() == category)
                        .count();
                if (count > 0) {
                    btn.setText(btn.getText() + " (" + count + ")");
                }
            }
        }

        // 統計資訊
        Separator separator2 = new Separator();
        separator2.setPadding(new Insets(10, 0, 10, 0));
        pane.getChildren().add(separator2);

        // 進度資訊
        PetVideoService.VideoProgress progress = videoService.getVideoProgress();

        Label progressTitle = new Label("📊 收集進度");
        progressTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ProgressBar progressBar = new ProgressBar(progress.percentage / 100);
        progressBar.setPrefWidth(180);
        progressBar.setStyle("-fx-accent: #28a745;");

        statsLabel = new Label(String.format("已解鎖: %d/%d (%.1f%%)",
                progress.unlocked, progress.total, progress.percentage));
        statsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        pane.getChildren().addAll(progressTitle, progressBar, statsLabel);

        // 監聽類別選擇變化
        categoryGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentCategory = (PetVideoType.VideoCategory) newVal.getUserData();
                refreshVideoGrid();
            }
        });

        return pane;
    }

    /**
     * 創建類別按鈕
     */
    private RadioButton createCategoryButton(String text, PetVideoType.VideoCategory category, ToggleGroup group) {
        RadioButton btn = new RadioButton(text);
        btn.setToggleGroup(group);
        btn.setUserData(category);
        btn.setStyle("-fx-font-size: 13px; -fx-padding: 5 0;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }

    /**
     * 獲取類別圖標
     */
    private String getCategoryIcon(PetVideoType.VideoCategory category) {
        switch (category) {
            case BASIC_ACTION: return "🎮";
            case EMOTION: return "😊";
            case SPECIAL: return "⭐";
            case ACHIEVEMENT: return "🏆";
            case INTERACTION: return "🤝";
            default: return "📁";
        }
    }

    /**
     * 創建影片網格（**此處不再呼叫 refreshVideoGrid()**）
     */
    private GridPane createVideoGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        // 注意：不要在這裡呼叫 refreshVideoGrid()，因為 videoGrid 還沒指派
        return grid;
    }

    /**
     * 刷新影片網格
     */
    private void refreshVideoGrid() {
        // 確保 videoGrid 已經被 createVideoGrid() 賦值
        if (videoGrid == null) return;

        videoGrid.getChildren().clear();

        List<PetVideoService.PetVideo> videos;

        if (currentCategory == null) {
            // 顯示全部影片
            videos = videoService.getUnlockedVideos();
        } else {
            // 顯示特定類別
            videos = videoService.getUnlockedVideos().stream()
                    .filter(v -> v.getType().getCategory() == currentCategory)
                    .collect(Collectors.toList());
        }

        if (videos.isEmpty()) {
            Label emptyLabel = new Label("此類別尚無解鎖的影片");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
            videoGrid.add(emptyLabel, 0, 0);
            return;
        }

        int col = 0;
        int row = 0;
        for (PetVideoService.PetVideo video : videos) {
            VBox videoCard = createVideoCard(video);
            videoGrid.add(videoCard, col, row);

            col++;
            if (col > 3) { // 每行 4 個影片
                col = 0;
                row++;
            }
        }
    }

    /**
     * 創建影片卡片
     */
    private VBox createVideoCard(PetVideoService.PetVideo video) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("video-card");
        card.setPrefWidth(160);
        card.setCursor(javafx.scene.Cursor.HAND);

        // 影片縮圖（使用類型圖標和背景色）
        StackPane thumbnail = new StackPane();
        thumbnail.setPrefSize(140, 90);
        thumbnail.setStyle("-fx-background-color: " + getCategoryColor(video.getType().getCategory()) +
                "; -fx-background-radius: 8;");

        // 類型圖標
        Label iconLabel = new Label(getVideoIcon(video.getType()));
        iconLabel.setStyle("-fx-font-size: 36px;");

        // 播放圖標疊加
        Label playIcon = new Label("▶");
        playIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: white; " +
                "-fx-background-color: rgba(0,0,0,0.5); " +
                "-fx-background-radius: 20; -fx-padding: 8 12;");
        playIcon.setVisible(false);

        thumbnail.getChildren().addAll(iconLabel, playIcon);

        // 影片名稱
        Label nameLabel = new Label(video.getDisplayName());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(140);
        nameLabel.setAlignment(Pos.CENTER);

        // 類別標籤
        Label categoryLabel = new Label(video.getType().getDescription());
        categoryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        categoryLabel.setWrapText(true);
        categoryLabel.setMaxWidth(140);
        categoryLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(thumbnail, nameLabel, categoryLabel);

        // 滑鼠事件
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #007bff; " +
                    "-fx-border-width: 2; -fx-border-radius: 8; " +
                    "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,123,255,0.3), 10, 0, 0, 2);");
            playIcon.setVisible(true);
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; " +
                    "-fx-border-width: 1; -fx-border-radius: 8; " +
                    "-fx-background-radius: 8;");
            playIcon.setVisible(false);
        });

        // 點擊播放
        card.setOnMouseClicked(e -> playVideo(video));

        return card;
    }

    /**
     * 獲取類別顏色
     */
    private String getCategoryColor(PetVideoType.VideoCategory category) {
        switch (category) {
            case BASIC_ACTION: return "#e3f2fd";
            case EMOTION: return "#fff3e0";
            case SPECIAL: return "#f3e5f5";
            case ACHIEVEMENT: return "#e8f5e9";
            case INTERACTION: return "#fce4ec";
            default: return "#f5f5f5";
        }
    }

    /**
     * 獲取影片圖標
     */
    private String getVideoIcon(PetVideoType type) {
        switch (type) {
            case PLAY: return "🎾";
            case EAT: return "🍖";
            case SLEEP: return "😴";
            case ANGRY: return "😠";
            case UNHAPPY: return "😾";
            case UPSET: return "😿";
            case HUNGRY: return "😋";
            case DISDAIN: return "🙄";
            case SERIOUS: return "🧐";
            case BITE: return "😼";
            case LASER_EYE: return "👁";
            case PLAYFUL: return "😈";
            case STUNNED: return "😵";
            case COSTUME: return "👔";
            default: return "🐱";
        }
    }

    /**
     * 創建播放器面板
     */
    private VBox createPlayerPane() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));
        pane.setPrefWidth(320);
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; " +
                "-fx-border-width: 0 0 0 1;");

        Label title = new Label("🎥 影片播放器");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // 影片播放區域
        mediaView = new MediaView();
        mediaView.setFitWidth(280);
        mediaView.setFitHeight(210);
        mediaView.setPreserveRatio(true);

        StackPane videoContainer = new StackPane();
        videoContainer.setStyle("-fx-background-color: black; -fx-background-radius: 8;");
        videoContainer.setPrefSize(280, 210);
        videoContainer.setMaxSize(280, 210);

        // 預設顯示提示
        Label placeholderLabel = new Label("點擊影片開始播放");
        placeholderLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        videoContainer.getChildren().addAll(placeholderLabel, mediaView);

        // 播放控制按鈕
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10, 0, 10, 0));

        Button playBtn = new Button("▶");
        Button pauseBtn = new Button("⏸");
        Button stopBtn = new Button("⏹");
        Button replayBtn = new Button("🔄");

        // 設定按鈕樣式
        String buttonStyle = "-fx-background-color: #007bff; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-cursor: hand; " +
                "-fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40;";

        playBtn.setStyle(buttonStyle);
        pauseBtn.setStyle(buttonStyle);
        stopBtn.setStyle(buttonStyle);
        replayBtn.setStyle(buttonStyle);

        controls.getChildren().addAll(playBtn, pauseBtn, stopBtn, replayBtn);

        // 音量控制
        HBox volumeBox = new HBox(10);
        volumeBox.setAlignment(Pos.CENTER);
        Label volumeLabel = new Label("🔊");
        Slider volumeSlider = new Slider(0, 100, 50);
        volumeSlider.setPrefWidth(150);
        volumeSlider.setShowTickLabels(false);
        volumeSlider.setShowTickMarks(false);
        volumeBox.getChildren().addAll(volumeLabel, volumeSlider);

        // 影片資訊
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        Label infoTitle = new Label("📋 影片資訊");
        infoTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        videoInfoBox = new VBox(8);
        videoInfoBox.setPadding(new Insets(10));
        videoInfoBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e9ecef; -fx-border-radius: 8;");

        // 預設資訊
        Label defaultInfo = new Label("選擇一個影片以查看詳細資訊");
        defaultInfo.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
        videoInfoBox.getChildren().add(defaultInfo);

        pane.getChildren().addAll(title, videoContainer, controls, volumeBox,
                separator, infoTitle, videoInfoBox);

        // 綁定控制按鈕
        playBtn.setOnAction(e -> {
            if (currentPlayer != null) {
                currentPlayer.play();
                placeholderLabel.setVisible(false);
            }
        });

        pauseBtn.setOnAction(e -> {
            if (currentPlayer != null) currentPlayer.pause();
        });

        stopBtn.setOnAction(e -> {
            if (currentPlayer != null) {
                currentPlayer.stop();
                placeholderLabel.setVisible(true);
            }
        });

        replayBtn.setOnAction(e -> {
            if (currentPlayer != null) {
                currentPlayer.seek(currentPlayer.getStartTime());
                currentPlayer.play();
                placeholderLabel.setVisible(false);
            }
        });

        // 音量控制
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentPlayer != null) {
                currentPlayer.setVolume(newVal.doubleValue() / 100);
            }
        });

        return pane;
    }

    /**
     * 播放選中的影片
     */
    private void playVideo(PetVideoService.PetVideo video) {
        // 停止當前播放
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
        }

        try {
            // 載入新影片
            String videoPath = "/tw/ntou/pettracker/video/" + video.getFilename();
            URL videoUrl = getClass().getResource(videoPath);
            if (videoUrl == null) {
                throw new RuntimeException("無法找到影片檔案: " + videoPath);
            }
            Media media = new Media(videoUrl.toExternalForm());
            currentPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(currentPlayer);

            // 設定循環播放
            currentPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            // 自動開始播放
            currentPlayer.play();

            // 更新影片資訊
            updateVideoInfo(video);

            // 發生錯誤時的處理
            currentPlayer.setOnError(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("播放錯誤");
                alert.setHeaderText("無法播放影片");
                alert.setContentText("影片格式可能不支援或檔案損壞");
                alert.showAndWait();
            });

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("載入錯誤");
            alert.setHeaderText("無法載入影片");
            alert.setContentText("檔案路徑: " + video.getFilename() + "\n錯誤: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * 更新影片資訊顯示
     */
    private void updateVideoInfo(PetVideoService.PetVideo video) {
        videoInfoBox.getChildren().clear();

        // 檔名
        HBox fileBox = new HBox(8);
        Label fileLabel = new Label("檔名:");
        fileLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label fileName = new Label(video.getFilename());
        fileName.setStyle("-fx-font-size: 12px;");
        fileBox.getChildren().addAll(fileLabel, fileName);

        // 類型
        HBox typeBox = new HBox(8);
        Label typeLabel = new Label("類型:");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label typeName = new Label(getVideoIcon(video.getType()) + " " + video.getType().getDisplayName());
        typeName.setStyle("-fx-font-size: 12px;");
        typeBox.getChildren().addAll(typeLabel, typeName);

        // 類別
        HBox categoryBox = new HBox(8);
        Label categoryLabel = new Label("類別:");
        categoryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label categoryName = new Label(video.getType().getCategory().getName());
        categoryName.setStyle("-fx-font-size: 12px;");
        categoryBox.getChildren().addAll(categoryLabel, categoryName);

        // 說明
        Label descLabel = new Label("說明:");
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label descText = new Label(video.getType().getDescription());
        descText.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        descText.setWrapText(true);

        videoInfoBox.getChildren().addAll(fileBox, typeBox, categoryBox, descLabel, descText);

        // 如果是鎖定的影片，顯示解鎖條件
        if (video.isLocked() && !videoService.isVideoUnlocked(video.getFilename())) {
            Separator sep = new Separator();
            sep.setPadding(new Insets(5, 0, 5, 0));

            Label lockLabel = new Label("🔒 解鎖條件:");
            lockLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #dc3545;");

            Label conditionLabel = new Label(video.getUnlockCondition());
            conditionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc3545;");
            conditionLabel.setWrapText(true);

            videoInfoBox.getChildren().addAll(sep, lockLabel, conditionLabel);
        }
    }
}
