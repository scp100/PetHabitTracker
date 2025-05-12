package tw.ntou.pettracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.model.TaskData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Persistence {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final File FILE = new File("tasks.json");// 在 Persistence 类的顶部添加导入

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
}
