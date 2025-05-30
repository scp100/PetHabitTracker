package tw.ntou.pettracker.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Pet {
    private final IntegerProperty satisfaction = new SimpleIntegerProperty(this, "satisfaction", 80);
    private final IntegerProperty fullness = new SimpleIntegerProperty(this, "fullness", 50);

    // ===== 滿意度屬性 =====
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
    public int getFullness() {
        return fullness.get();
    }

    public void setFullness(int v) {
        fullness.set(Math.max(0, Math.min(100, v)));
    }

    public IntegerProperty fullnessProperty() {
        return fullness;
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
     * 獲取寵物當前狀態描述
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
     * 獲取寵物情緒表情符號
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
        setFullness(Math.max(0, getFullness() - 5)); // 玩耍會消耗一點體力
    }

    /**
     * 時間流逝的狀態衰減
     */
    public void timePass() {
        // 隨時間緩慢減少狀態值
        setSatisfaction(Math.max(0, getSatisfaction() - 1));
        setFullness(Math.max(0, getFullness() - 2));
    }

    @Override
    public String toString() {
        return String.format("寵物狀態 - 滿意度:%d%%, 飽食度:%d%%",
                getSatisfaction(), getFullness());
    }
}