package tw.ntou.pettracker.model;

public class WindowSetting {
    private boolean maximized;//是否全螢幕
    private String resolution;//最後解析度
    private boolean undecorated; // 是否無邊框

    public boolean isMaximized() {return maximized;}
    public void setMaximized(boolean maximized) {this.maximized = maximized;}

    public String getResolution() {return resolution;}
    public void setResolution(String resolution) {this.resolution = resolution;}

    public boolean isUndecorated() { return undecorated; }
    public void setUndecorated(boolean undecorated) { this.undecorated = undecorated; }
}
