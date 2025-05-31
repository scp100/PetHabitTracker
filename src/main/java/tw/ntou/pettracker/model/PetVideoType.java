package tw.ntou.pettracker.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 寵物影片類型枚舉 - 根據實際影片檔案更新
 */
public enum PetVideoType {
    // 基本行為影片
    PLAY("逗貓", "玩耍互動", VideoCategory.BASIC_ACTION, 0),
    EAT("吃飯", "進食", VideoCategory.BASIC_ACTION, 0),
    SLEEP("睡覺", "休息中", VideoCategory.BASIC_ACTION, 0),

    // 情緒表現影片
    ANGRY("生氣", "滿意度極低", VideoCategory.EMOTION, 0),
    UNHAPPY("不爽貓", "滿意度低", VideoCategory.EMOTION, 0),
    DISDAIN("不屑一顧貓", "高傲表情", VideoCategory.EMOTION, 1),
    UPSET("沮喪", "飽食度低", VideoCategory.EMOTION, 0),
    HUNGRY("餓貓", "飽食度極低", VideoCategory.EMOTION, 0),

    // 特殊行為影片
    SERIOUS("正經貓", "正經模式", VideoCategory.SPECIAL, 1),
    BITE("愛咬不咬貓", "假裝咬人", VideoCategory.SPECIAL, 2),
    LASER_EYE("雷射眼", "特殊技能", VideoCategory.SPECIAL, 3),
    PLAYFUL("調皮貓", "調皮搗蛋", VideoCategory.SPECIAL, 1),
    STUNNED("癡呆貓", "發呆狀態", VideoCategory.SPECIAL, 2),

    // 服裝系統影片
    COSTUME("穿衣", "服裝系統", VideoCategory.ACHIEVEMENT, 1),
    COSTUME_ANGRY("穿衣-生氣", "生氣穿衣", VideoCategory.ACHIEVEMENT, 2),
    COSTUME_UNHAPPY("穿衣-不爽貓", "不爽穿衣", VideoCategory.ACHIEVEMENT, 2),
    COSTUME_UPSET("穿衣-沮喪貓", "沮喪穿衣", VideoCategory.ACHIEVEMENT, 2),

    // 互動反應影片
    PLAY_UNHAPPY("逗貓-不爽貓", "不想玩", VideoCategory.INTERACTION, 1),
    PLAY_SURPRISED("逗貓-驚訝貓", "驚訝反應", VideoCategory.INTERACTION, 1),

    // 預留影片
    NULL("Null", "未使用", VideoCategory.RESERVED, 99),
    YET("yet", "未實裝", VideoCategory.RESERVED, 99);

    private final String displayName;
    private final String description;
    private final VideoCategory category;
    private final int requiredLevel;

    PetVideoType(String displayName, String description, VideoCategory category, int requiredLevel) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.requiredLevel = requiredLevel;
    }

    public enum VideoCategory {
        BASIC_ACTION("基本動作"),
        EMOTION("情緒表現"),
        SPECIAL("特殊行為"),
        ACHIEVEMENT("成就解鎖"),
        INTERACTION("互動反應"),
        RESERVED("預留");

        private final String name;

        VideoCategory(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    }

    // Getters
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public VideoCategory getCategory() { return category; }
    public int getRequiredLevel() { return requiredLevel; }
}
