package tw.ntou.pettracker.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * 寵物 Model，包含滿意度、飽食度，以及根據狀態決定要播放哪段影片
 */
public class Pet {

    // ===== 滿意度屬性 =====
    private final IntegerProperty satisfaction = new SimpleIntegerProperty(this, "satisfaction", 80);

    public int getSatisfaction() {
        return satisfaction.get();
    }

    public void setSatisfaction(int v) {
        satisfaction.set(Math.max(0, Math.min(100, v)));
    }

    public IntegerProperty satisfactionProperty() {
        return satisfaction;
    }

    // ===== 飽食度屬性 =====
    private final IntegerProperty fullness = new SimpleIntegerProperty(this, "fullness", 50);

    public int getFullness() {
        return fullness.get();
    }

    public void setFullness(int v) {
        fullness.set(Math.max(0, Math.min(100, v)));
    }

    public IntegerProperty fullnessProperty() {
        return fullness;
    }

    // ===== 當前影片類型屬性 =====
    private final ObjectProperty<PetVideoType> currentVideoType = new SimpleObjectProperty<>(this, "currentVideoType");

    public PetVideoType getCurrentVideoType() {
        return currentVideoType.get();
    }

    public void setCurrentVideoType(PetVideoType type) {
        currentVideoType.set(type);
    }

    public ObjectProperty<PetVideoType> currentVideoTypeProperty() {
        return currentVideoType;
    }

    // ===== 建構子 =====
    public Pet() {
        // 啟動時先初始化 currentVideoType，再綁定監聽
        PetVideoType initialType = getCurrentVideoTypeByState();
        currentVideoType.set(initialType);
        setupVideoListeners();
    }

    // ===== 遊戲邏輯方法 =====

    /**
     * 根據任務完成情況更新寵物狀態
     */
    public void reactToTaskCompletion(int priority) {
        int satisfactionGain = 0;
        int fullnessGain = 0;

        // 根據任務優先級給予不同獎勵
        switch (priority) {
            case 1: // 最高優先級
                satisfactionGain = 15;
                fullnessGain = 10;
                break;
            case 2:
                satisfactionGain = 10;
                fullnessGain = 8;
                break;
            case 3:
                satisfactionGain = 8;
                fullnessGain = 5;
                break;
            case 4:
                satisfactionGain = 5;
                fullnessGain = 3;
                break;
            case 5: // 最低優先級
                satisfactionGain = 3;
                fullnessGain = 2;
                break;
        }

        setSatisfaction(getSatisfaction() + satisfactionGain);
        setFullness(getFullness() + fullnessGain);
    }

    /**
     * 餵食寵物
     */
    public void feed() {
        setFullness(getFullness() + 20);
        setSatisfaction(getSatisfaction() + 5);
    }

    /**
     * 和寵物玩耍
     */
    public void play() {
        setSatisfaction(getSatisfaction() + 15);
        setFullness(Math.max(0, getFullness() - 5)); // 玩耍會稍微消耗飽食度
    }

    /**
     * 時間流逝的狀態衰減
     */
    public void timePass() {
        // 隨時間緩慢減少狀態值
        setSatisfaction(Math.max(0, getSatisfaction() - 1));
        setFullness(Math.max(0, getFullness() - 2));
    }

    /**
     * 獲取寵物當前狀態描述（文字版）
     */
    public String getStatusDescription() {
        if (getSatisfaction() > 80 && getFullness() > 80) {
            return "非常開心！";
        } else if (getSatisfaction() > 60 && getFullness() > 60) {
            return "心情不錯";
        } else if (getSatisfaction() > 40 && getFullness() > 40) {
            return "還可以";
        } else if (getSatisfaction() > 20 && getFullness() > 20) {
            return "有點不開心";
        } else {
            return "需要關心";
        }
    }

    /**
     * 獲取寵物情緒表情符號（Emoji 版）
     */
    public String getEmoji() {
        if (getSatisfaction() > 80) {
            return "😸"; // 非常開心的貓
        } else if (getSatisfaction() > 60) {
            return "😺"; // 開心的貓
        } else if (getSatisfaction() > 40) {
            return "😐"; // 普通表情
        } else if (getSatisfaction() > 20) {
            return "😿"; // 有點難過
        } else {
            return "😾"; // 生氣的貓
        }
    }

    @Override
    public String toString() {
        return String.format("寵物狀態 - 滿意度:%d%%, 飽食度:%d%%",
                getSatisfaction(), getFullness());
    }

    // ===== 影片狀態決策與監聽 =====

    /**
     * 根據目前 satisfaction & fullness 計算出對應的 PetVideoType
     */
    private PetVideoType getCurrentVideoTypeByState() {
        int sat = getSatisfaction();
        int full = getFullness();

        // 當狀態極低時顯示憤怒
        if (sat < 10) {
            return PetVideoType.ANGRY;
        }
        // 當滿意度低但不至於極低
        if (sat < 30) {
            return PetVideoType.UNHAPPY;
        }
        // 當飽食度極低時顯示飢餓
        if (full < 10) {
            return PetVideoType.HUNGRY;
        }
        // 當飽食度低但滿意度沒那麼糟
        if (full < 30) {
            return PetVideoType.UPSET;
        }
        // 滿意度和飽食度都很高時顯示玩耍動畫
        if (sat > 80 && full > 80) {
            return PetVideoType.PLAY;
        }
        // 其餘情況顯示休息／睡覺
        return PetVideoType.SLEEP;
    }

    /**
     * 為 satisfactionProperty 和 fullnessProperty 註冊監聽器：
     * 一旦任一屬性改變，重新計算並更新 currentVideoType
     */
    private void setupVideoListeners() {
        satisfaction.addListener((obs, oldVal, newVal) -> {
            PetVideoType newType = getCurrentVideoTypeByState();
            if (newType != currentVideoType.get()) {
                currentVideoType.set(newType);
            }
        });

        fullness.addListener((obs, oldVal, newVal) -> {
            PetVideoType newType = getCurrentVideoTypeByState();
            if (newType != currentVideoType.get()) {
                currentVideoType.set(newType);
            }
        });
    }
}
