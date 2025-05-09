package tw.ntou.pettracker;

import javafx.scene.control.*;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import java.time.LocalDate;

public class DatePickerTableCell<S> extends TableCell<S, LocalDate> {
    private final DatePicker picker = new DatePicker();

    public DatePickerTableCell() {
        picker.setOnAction(evt -> commitEdit(picker.getValue()));
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (getItem() != null) {
            picker.setValue(getItem());
        }
        setGraphic(picker);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
    }

    @Override
    protected void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else if (isEditing()) {
            picker.setValue(item);
            setGraphic(picker);
        } else {
            setText(item != null ? item.toString() : "");
            setGraphic(null);
        }
    }
}
