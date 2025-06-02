package tw.ntou.pettracker;

import tw.ntou.pettracker.model.Achievement;
import tw.ntou.pettracker.model.AchievementData;

public class AchievementConverter {
    public static AchievementData toData(Achievement achievement) {
        AchievementData data = new AchievementData();
        data.id = achievement.getId();
        data.unlocked = achievement.isUnlocked();
        return data;
    }

    public static Achievement fromData(AchievementData data, Achievement achievement) {
        achievement.setUnlocked(data.unlocked);
        return achievement;
    }
}
