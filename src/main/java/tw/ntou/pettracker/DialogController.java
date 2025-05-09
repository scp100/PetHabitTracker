package tw.ntou.pettracker;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tw.ntou.pettracker.model.Task;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DialogController implements Initializable {
    @FXML private TextField descField;          //  fx:id="descField"
    @FXML private DatePicker datePicker;        //  fx:id="datePicker"
    @FXML private ComboBox<Integer> prioBox;    // fx:id="prioBox"
    @FXML private CheckBox remindCheck;         // fx:id="remindCheck"
    @FXML private DatePicker remindPicker;      // fx:id="remindPicker"
    @FXML private TextField tagsField;          // fx:id="tagsField"
    @FXML private Button submitBtn;             // fx:id="submitBtn"
    @FXML private Button cancelBtn;             //  onAction="#onCancel"

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        prioBox.setItems(FXCollections.observableArrayList(1,2,3,4,5));
        prioBox.getSelectionModel().select(2);


        remindPicker.setDisable(true);
        remindCheck.selectedProperty().addListener((obs, oldV, newV) ->
            remindPicker.setDisable(!newV)
        );


        submitBtn.setOnAction(e -> handleSubmit());
        cancelBtn.setOnAction(e -> handleCancel());
    }


    private void handleSubmit() {
        String desc = descField.getText().trim();
        LocalDate date = datePicker.getValue();
        Integer prio = prioBox.getValue();

        if (desc.isEmpty() || date == null || prio == null) {

            return;
        }


        Task t = new Task(desc, date, prio);

        t.setRemind(remindCheck.isSelected());
        if (remindCheck.isSelected()) {
            t.setRemindAt(remindPicker.getValue().atStartOfDay());
        }


        t.setTags(tagsField.getText().trim());

        // TODO: 把 t 加回 MainController.tasks（可用 callback 或靜態列表）
        // e.g. MainController.getTasks().add(t);


        Stage stg = (Stage) submitBtn.getScene().getWindow();
        stg.close();
    }

    private void handleCancel() {
        Stage stg = (Stage) cancelBtn.getScene().getWindow();
        stg.close();
    }
}
