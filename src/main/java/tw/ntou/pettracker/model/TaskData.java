package tw.ntou.pettracker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskData {
    public String description;
    public boolean done;
    public LocalDate dueDate;
    public int priority;
    public boolean remind;
    public LocalDateTime remindAt;
    public String tags;

    public TaskData() {}
}