package tw.ntou.pettracker.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import tw.ntou.pettracker.model.Pet;
import tw.ntou.pettracker.model.PetVideoType;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 寵物影片管理服務 - 更新版
 */
public class PetVideoService {
    private static PetVideoService instance;
    private final Map<String, List<PetVideo>> videoMap = new HashMap<>();
    private final Set<String> unlockedVideos = new HashSet<>();
    private MediaPlayer currentPlayer;

    // 影片路徑（放在 resources/tw/ntou/pettracker/video/ 底下）
    private static final String VIDEO_PATH = "/tw/ntou/pettracker/video/";

    // 完整的影片檔案列表
    private static final String[] VIDEO_FILES = {
            "Null.mp4", "null2.mp4", "yet1.mp4", "yet2.mp4",
            "不屑一顧貓.mp4", "不爽貓.mp4", "不爽貓2.mp4", "不爽貓3.mp4", "不爽貓4.mp4", "不爽貓5.mp4",
            "正經貓.mp4", "生氣1.mp4", "生氣2.mp4", "生氣3.mp4",
            "吃飯1.mp4", "吃飯2.mp4", "吃飯3.mp4",
            "沮喪1.mp4", "沮喪2.mp4", "沮喪3.mp4", "沮喪4.mp4", "沮喪5.mp4", "沮喪6.mp4",
            "穿衣1.mp4", "穿衣2.mp4", "穿衣3.mp4", "穿衣4.mp4", "穿衣5.mp4",
            "穿衣-不爽貓.mp4", "穿衣-不爽貓2.mp4", "穿衣-不爽貓3.mp4", "穿衣-不爽貓4.mp4", "穿衣-不爽貓5.mp4",
            "穿衣-生氣.mp4", "穿衣-沮喪貓.mp4",
            "逗貓5.mp4", "逗貓6.mp4", "逗貓7.mp4", "逗貓9.mp4", "逗貓10.mp4",
            "逗貓11.mp4", "逗貓12.mp4", "逗貓13.mp4", "逗貓14.mp4", "逗貓15.mp4", "逗貓16.mp4",
            "逗貓-不爽貓.mp4", "逗貓-驚訝貓.mp4",
            "愛咬不咬貓.mp4", "雷射眼.mp4",
            "睡覺1.mp4", "睡覺2.mp4", "睡覺3.mp4", "睡覺4.mp4", "睡覺5.mp4", "睡覺6.mp4",
            "調皮貓.mp4", "餓貓.mp4", "餓貓2.mp4", "癡呆貓.mp4"
    };

    private PetVideoService() {
        initializeVideos();
        loadUnlockedVideos();
    }

    public static PetVideoService getInstance() {
        if (instance == null) {
            instance = new PetVideoService();
        }
        return instance;
    }

    /**
     * 影片資料類
     */
    public static class PetVideo {
        private final String filename;
        private final PetVideoType type;
        private final int number;
        private final boolean isLocked;
        private final String unlockCondition;

        public PetVideo(String filename, PetVideoType type, int number) {
            this.filename = filename;
            this.type = type;
            this.number = number;
            this.isLocked = type.getRequiredLevel() > 0;
            this.unlockCondition = generateUnlockCondition();
        }

        private String generateUnlockCondition() {
            switch (type) {
                case COSTUME:
                case COSTUME_ANGRY:
                case COSTUME_UNHAPPY:
                case COSTUME_UPSET:
                    return "完成「時尚達人」成就";
                case SERIOUS:
                    return "連續完成任務7天";
                case DISDAIN:
                    return "拒絕寵物需求10次";
                case BITE:
                    return "與寵物玩耍50次";
                case LASER_EYE:
                    return "達到10級";
                case PLAYFUL:
                    return "完成「調皮搗蛋」成就";
                case STUNNED:
                    return "讓寵物挨餓3次";
                case PLAY_UNHAPPY:
                case PLAY_SURPRISED:
                    return "多次在寵物心情不好時玩耍";
                default:
                    if (type.getRequiredLevel() > 0) {
                        return "達到等級 " + type.getRequiredLevel();
                    }
                    return "基礎影片";
            }
        }

        public String getDisplayName() {
            return filename.replace(".mp4", "");
        }

        // Getters
        public String getFilename() {
            return filename;
        }

        public PetVideoType getType() {
            return type;
        }

        public int getNumber() {
            return number;
        }

        public boolean isLocked() {
            return isLocked;
        }

        public String getUnlockCondition() {
            return unlockCondition;
        }
    }

    /**
     * 初始化影片資源
     */
    private void initializeVideos() {
        for (String filename : VIDEO_FILES) {
            PetVideo video = parseVideoFile(filename);
            if (video != null) {
                String key = video.getType().name();
                videoMap.computeIfAbsent(key, k -> new ArrayList<>()).add(video);
            }
        }

        // 輸出統計資訊
        System.out.println("影片載入完成:");
        videoMap.forEach((type, videos) ->
                System.out.println(type + ": " + videos.size() + " 個影片")
        );
    }

    /**
     * 解析影片檔名 - 更精確的解析邏輯
     */
    private PetVideo parseVideoFile(String filename) {
        String name = filename.replace(".mp4", "");

        // 特殊檔名對應
        Map<String, PetVideoType> specialNames = new HashMap<>();
        specialNames.put("Null", PetVideoType.NULL);
        specialNames.put("null2", PetVideoType.NULL);
        specialNames.put("yet1", PetVideoType.YET);
        specialNames.put("yet2", PetVideoType.YET);
        specialNames.put("不屑一顧貓", PetVideoType.DISDAIN);
        specialNames.put("正經貓", PetVideoType.SERIOUS);
        specialNames.put("愛咬不咬貓", PetVideoType.BITE);
        specialNames.put("雷射眼", PetVideoType.LASER_EYE);
        specialNames.put("調皮貓", PetVideoType.PLAYFUL);
        specialNames.put("癡呆貓", PetVideoType.STUNNED);

        // 檢查特殊名稱
        if (specialNames.containsKey(name)) {
            return new PetVideo(filename, specialNames.get(name), 1);
        }

        // 解析帶編號的影片
        if (name.startsWith("不爽貓")) {
            int num = extractNumber(name, "不爽貓");
            return new PetVideo(filename, PetVideoType.UNHAPPY, num);
        }

        if (name.startsWith("生氣")) {
            int num = extractNumber(name, "生氣");
            return new PetVideo(filename, PetVideoType.ANGRY, num);
        }

        if (name.startsWith("吃飯")) {
            int num = extractNumber(name, "吃飯");
            return new PetVideo(filename, PetVideoType.EAT, num);
        }

        if (name.startsWith("沮喪")) {
            int num = extractNumber(name, "沮喪");
            return new PetVideo(filename, PetVideoType.UPSET, num);
        }

        if (name.startsWith("穿衣-生氣")) {
            return new PetVideo(filename, PetVideoType.COSTUME_ANGRY, 1);
        }

        if (name.startsWith("穿衣-不爽貓")) {
            int num = extractNumber(name, "穿衣-不爽貓");
            return new PetVideo(filename, PetVideoType.COSTUME_UNHAPPY, num);
        }

        if (name.startsWith("穿衣-沮喪貓")) {
            return new PetVideo(filename, PetVideoType.COSTUME_UPSET, 1);
        }

        if (name.startsWith("穿衣")) {
            int num = extractNumber(name, "穿衣");
            return new PetVideo(filename, PetVideoType.COSTUME, num);
        }

        if (name.equals("逗貓-不爽貓")) {
            return new PetVideo(filename, PetVideoType.PLAY_UNHAPPY, 1);
        }

        if (name.equals("逗貓-驚訝貓")) {
            return new PetVideo(filename, PetVideoType.PLAY_SURPRISED, 1);
        }

        if (name.startsWith("逗貓")) {
            int num = extractNumber(name, "逗貓");
            return new PetVideo(filename, PetVideoType.PLAY, num);
        }

        if (name.startsWith("睡覺")) {
            int num = extractNumber(name, "睡覺");
            return new PetVideo(filename, PetVideoType.SLEEP, num);
        }

        if (name.startsWith("餓貓")) {
            int num = extractNumber(name, "餓貓");
            return new PetVideo(filename, PetVideoType.HUNGRY, num);
        }

        // 無法識別的影片
        System.out.println("無法識別的影片: " + filename);
        return null;
    }

    /**
     * 從檔名中提取編號
     */
    private int extractNumber(String filename, String prefix) {
        String suffix = filename.substring(prefix.length());
        if (suffix.isEmpty()) {
            return 1;
        }
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * 根據寵物狀態獲取合適的影片
     */
    public PetVideo getVideoForPetState(Pet pet) {
        int satisfaction = pet.getSatisfaction();
        int fullness = pet.getFullness();

        // 根據狀態選擇影片
        if (satisfaction < 10) {
            return getRandomVideo(PetVideoType.ANGRY);
        } else if (satisfaction < 30) {
            return getRandomVideo(PetVideoType.UNHAPPY);
        } else if (fullness < 10) {
            return getRandomVideo(PetVideoType.HUNGRY);
        } else if (fullness < 30) {
            return getRandomVideo(PetVideoType.UPSET);
        } else if (satisfaction > 80 && fullness > 80) {
            // 狀態良好時隨機播放正面影片
            PetVideoType[] happyTypes = {
                    PetVideoType.PLAY, PetVideoType.SLEEP, PetVideoType.PLAYFUL
            };
            return getRandomVideo(happyTypes[(int)(Math.random() * happyTypes.length)]);
        }

        // 預設播放睡覺影片
        return getRandomVideo(PetVideoType.SLEEP);
    }

    /**
     * 獲取特定類型的隨機影片
     */
    public PetVideo getRandomVideo(PetVideoType type) {
        List<PetVideo> videos = videoMap.get(type.name());
        if (videos == null || videos.isEmpty()) return null;

        // 只選擇已解鎖的影片
        List<PetVideo> unlockedList = videos.stream()
                .filter(v -> !v.isLocked() || unlockedVideos.contains(v.getFilename()))
                .collect(Collectors.toList());

        if (unlockedList.isEmpty()) {
            // 如果沒有解鎖的影片，返回第一個基礎影片
            return videos.stream()
                    .filter(v -> !v.isLocked())
                    .findFirst()
                    .orElse(null);
        }

        return unlockedList.get((int)(Math.random() * unlockedList.size()));
    }

    /**
     * 播放影片
     */
    public void playVideo(PetVideo video) {
        if (video == null) return;

        try {
            // 停止當前播放
            if (currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.dispose();
            }

            // 載入新影片
            URL videoUrl = getClass().getResource(VIDEO_PATH + video.getFilename());
            if (videoUrl != null) {
                Media media = new Media(videoUrl.toExternalForm());
                currentPlayer = new MediaPlayer(media);
                currentPlayer.setCycleCount(MediaPlayer.INDEFINITE); // 循環播放
                currentPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("無法播放影片 " + video.getFilename() + ": " + e.getMessage());
        }
    }

    /**
     * 停止當前播放
     */
    public void stopCurrentVideo() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
        }
    }

    /**
     * 解鎖影片
     */
    public void unlockVideo(String filename) {
        unlockedVideos.add(filename);
        saveUnlockedVideos();
    }

    /**
     * 解鎖特定成就的所有影片
     */
    public void unlockVideosForAchievement(String achievementId) {
        switch (achievementId) {
            case "fashion_master":
                // 解鎖所有穿衣系列
                unlockAllVideosOfType(PetVideoType.COSTUME);
                unlockAllVideosOfType(PetVideoType.COSTUME_ANGRY);
                unlockAllVideosOfType(PetVideoType.COSTUME_UNHAPPY);
                unlockAllVideosOfType(PetVideoType.COSTUME_UPSET);
                break;
            case "streak_7":
                // 解鎖正經貓
                unlockAllVideosOfType(PetVideoType.SERIOUS);
                break;
            case "pet_play_10":
                // 解鎖愛咬不咬貓
                unlockAllVideosOfType(PetVideoType.BITE);
                break;
            case "level_10":
                // 解鎖雷射眼
                unlockAllVideosOfType(PetVideoType.LASER_EYE);
                break;
            case "playful_achievement":
                // 解鎖調皮貓
                unlockAllVideosOfType(PetVideoType.PLAYFUL);
                break;
            case "neglect_pet":
                // 解鎖癡呆貓和不屑一顧貓
                unlockAllVideosOfType(PetVideoType.STUNNED);
                unlockAllVideosOfType(PetVideoType.DISDAIN);
                break;
        }
    }

    /**
     * 解鎖特定類型的所有影片
     */
    private void unlockAllVideosOfType(PetVideoType type) {
        List<PetVideo> videos = videoMap.get(type.name());
        if (videos != null) {
            videos.forEach(v -> unlockVideo(v.getFilename()));
        }
    }

    /**
     * 解鎖等級以下的所有影片
     */
    public void unlockVideosUpToLevel(int level) {
        videoMap.values().stream()
                .flatMap(List::stream)
                .filter(v -> v.getType().getRequiredLevel() <= level)
                .forEach(v -> unlockVideo(v.getFilename()));
    }

    /**
     * 獲取所有已解鎖的影片（用於相簿）
     */
    public List<PetVideo> getUnlockedVideos() {
        return videoMap.values().stream()
                .flatMap(List::stream)
                .filter(v -> !v.isLocked() || unlockedVideos.contains(v.getFilename()))
                .sorted(Comparator.comparing(PetVideo::getFilename))
                .collect(Collectors.toList());
    }

    /**
     * 獲取按類別分組的已解鎖影片
     */
    public Map<PetVideoType.VideoCategory, List<PetVideo>> getUnlockedVideosByCategory() {
        return getUnlockedVideos().stream()
                .collect(Collectors.groupingBy(v -> v.getType().getCategory()));
    }

    /**
     * 檢查影片是否已解鎖
     */
    public boolean isVideoUnlocked(String filename) {
        PetVideo video = videoMap.values().stream()
                .flatMap(List::stream)
                .filter(v -> v.getFilename().equals(filename))
                .findFirst()
                .orElse(null);

        if (video == null) return false;
        return !video.isLocked() || unlockedVideos.contains(filename);
    }

    /**
     * 獲取影片解鎖進度
     */
    public VideoProgress getVideoProgress() {
        long totalVideos = videoMap.values().stream()
                .flatMap(List::stream)
                .filter(v -> v.getType().getCategory() != PetVideoType.VideoCategory.RESERVED)
                .count();

        long unlockedCount = videoMap.values().stream()
                .flatMap(List::stream)
                .filter(v -> v.getType().getCategory() != PetVideoType.VideoCategory.RESERVED)
                .filter(v -> !v.isLocked() || unlockedVideos.contains(v.getFilename()))
                .count();

        return new VideoProgress(totalVideos, unlockedCount);
    }

    /**
     * 影片進度類
     */
    public static class VideoProgress {
        public final long total;
        public final long unlocked;
        public final double percentage;

        public VideoProgress(long total, long unlocked) {
            this.total = total;
            this.unlocked = unlocked;
            this.percentage = total > 0 ? (double) unlocked / total * 100 : 0;
        }
    }

    /**
     * 保存/載入已解鎖影片列表
     */
    private void saveUnlockedVideos() {
        // TODO: 保存到本地檔案或偏好設定
        // 可用 JSON 或 Java Preferences API 實作
    }

    private void loadUnlockedVideos() {
        // TODO: 從本地載入已解鎖的影片列表
        // 預設解鎖基本動作影片、情緒影片（requiredLevel == 0）
        for (PetVideoType type : PetVideoType.values()) {
            if ((type.getCategory() == PetVideoType.VideoCategory.BASIC_ACTION
                    || type.getCategory() == PetVideoType.VideoCategory.EMOTION)
                    && type.getRequiredLevel() == 0) {
                unlockAllVideosOfType(type);
            }
        }
    }
}
