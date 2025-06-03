package tw.ntou.pettracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import tw.ntou.pettracker.model.Achievement;
import tw.ntou.pettracker.model.AchievementData;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.model.TaskData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import tw.ntou.pettracker.model.WindowSetting;

public class Persistence {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final File FILE = new File("tasks.json");
    private static final File ACHIEVEMENTS_FILE = new File("achievements.json");
    private static final File SETTINGS_FILE = new File("settings.json"); //設定控制文件

    // 時間格式化的 eg. 2025-05-12
    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // task -> json
    public static void saveTasks(List<Task> taskList) {
        List<TaskData> dataList = new ArrayList<>();
        for (Task task : taskList) {
            dataList.add(TaskConverter.toData(task));
        }

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(FILE, dataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // json -> task
    public static List<Task> loadTasks() {
        if (!FILE.exists()) {
            return new ArrayList<>();
        }

        try {
            CollectionType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, TaskData.class);
            List<TaskData> dataList = mapper.readValue(FILE, listType);

            List<Task> taskList = new ArrayList<>();
            for (TaskData data : dataList) {
                taskList.add(TaskConverter.fromData(data));
            }
            return taskList;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveAchievementsStatus(List<Achievement> achievements) {
        List<AchievementData> dataList = new ArrayList<>();
        for (Achievement achievement : achievements) {
            dataList.add(AchievementConverter.toData(achievement));
        }

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(ACHIEVEMENTS_FILE, dataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAchievementsStatus(List<Achievement> achievements) {
        if (!ACHIEVEMENTS_FILE.exists()) {
            return;
        }

        try {
            CollectionType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, AchievementData.class);
            List<AchievementData> dataList = mapper.readValue(ACHIEVEMENTS_FILE, listType);

            for (AchievementData data : dataList) {
                for (Achievement achievement : achievements) {
                    if (achievement.getId().equals(data.id)) {
                        AchievementConverter.fromData(data, achievement);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //儲存視窗設定
    public static void saveWindowSettings(WindowSetting settings) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(SETTINGS_FILE, settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 讀取視窗設定
    public static WindowSetting loadWindowSettings() {
        if (!SETTINGS_FILE.exists()) {
            WindowSetting defaultSettings = new WindowSetting();
            defaultSettings.setMaximized(true);
            defaultSettings.setResolution("1200x800"); // 備用解析度
            defaultSettings.setUndecorated(false);    // 預設無邊框關閉
            return defaultSettings;
        }

        try {
            return mapper.readValue(SETTINGS_FILE, WindowSetting.class);
        } catch (IOException e) {
            e.printStackTrace();
            WindowSetting defaultSettings = new WindowSetting();
            defaultSettings.setUndecorated(false);
            return defaultSettings; // 預設空白設定
        }
    }



}
