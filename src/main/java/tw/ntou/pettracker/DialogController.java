package tw.ntou.pettracker;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tw.ntou.pettracker.model.Task;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DialogController implements Initializable {
    @FXML private TextField      descField;     // fx:id="descField"
    @FXML private DatePicker     datePicker;    // fx:id="datePicker"
    @FXML private ComboBox<Integer> prioBox;     // fx:id="prioBox"
    @FXML private CheckBox       remindCheck;   // fx:id="remindCheck"
    @FXML private DatePicker     remindPicker;  // fx:id="remindPicker"
    @FXML private TextField      tagsField;     // fx:id="tagsField"
    @FXML private Button         submitBtn;     // fx:id="submitBtn"
    @FXML private Button         cancelBtn;     // fx:id="cancelBtn"

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 優先級下拉
        prioBox.setItems(FXCollections.observableArrayList(1,2,3,4,5));
        prioBox.getSelectionModel().select(2);

        // 提醒日期啟用邏輯
        remindPicker.setDisable(true);
        remindCheck.selectedProperty().addListener((obs, oldV, newV) ->
                remindPicker.setDisable(!newV)
        );

        // 按鈕行為
        submitBtn.setOnAction(e -> handleSubmit());
        cancelBtn.setOnAction(e -> onCancel());
    }

    /** 對應 FXML onAction="#onCancel" */
    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    /** 提交表單 */
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


        Stage stg = (Stage) submitBtn.getScene().getWindow();
        stg.close();
    }
}
