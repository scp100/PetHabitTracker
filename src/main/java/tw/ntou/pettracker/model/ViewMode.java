package tw.ntou.pettracker.model;

public enum ViewMode {
    TODAY("今日"),
    UPCOMING("即將到來"),
    ALL("全部");

    private final String displayName;

    ViewMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}