package tw.ntou.pettracker.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Pet {
    private final IntegerProperty satisfaction = new SimpleIntegerProperty(this, "satisfaction", 80);
    private final IntegerProperty fullness     = new SimpleIntegerProperty(this, "fullness", 50);

    public int getSatisfaction() { return satisfaction.get(); }
    public void setSatisfaction(int v) { satisfaction.set(v); }
    public IntegerProperty satisfactionProperty() { return satisfaction; }

    public int getFullness() { return fullness.get(); }
    public void setFullness(int v) { fullness.set(v); }
    public IntegerProperty fullnessProperty() { return fullness; }
}
