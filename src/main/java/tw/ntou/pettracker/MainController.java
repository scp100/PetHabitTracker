package tw.ntou.pettracker;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.model.Pet;

import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private HBox inputBar;
    @FXML private TextField descField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> prioBox;
    @FXML private Button plusBtn;
    @FXML private TableView<Task> table;
    @FXML private TableColumn<Task, Boolean> doneCol;
    @FXML private TableColumn<Task, String> descCol;
    @FXML private TableColumn<Task, LocalDate> dateCol;
    @FXML private TableColumn<Task, Integer> prioCol;
    @FXML private ImageView     petImage;
    @FXML private ProgressBar    satisfactionBar;
    @FXML private ProgressBar    fullnessBar;

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private Pet pet;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        //pet model
        pet = new Pet();

        petImage.setImage(new Image(
            getClass().getResource("icon/cat.png").toExternalForm()));

        satisfactionBar.progressProperty()
            .bind(pet.satisfactionProperty().divide(100.0));
        fullnessBar.progressProperty()
            .bind(pet.fullnessProperty().divide(100.0));

        tasks.addAll(Persistence.loadTasks()); // 載入 json

        inputBar.setVisible(false);
        inputBar.setManaged(false);
        datePicker.setValue(LocalDate.now());
        prioBox.setItems(FXCollections.observableArrayList(1,2,3,4,5));
        prioBox.getSelectionModel().select(2);

        SortedList<Task> sorted = new SortedList<>(tasks,
            Comparator.comparing(Task::getDueDate));
        table.setItems(sorted);

        doneCol.setCellValueFactory(cell -> cell.getValue().doneProperty());
        doneCol.setCellFactory(CheckBoxTableCell.forTableColumn(doneCol));

        descCol.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        descCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descCol.setOnEditCommit(evt -> 
            evt.getRowValue().setDescription(evt.getNewValue())
        );

        dateCol.setCellValueFactory(cell -> 
            new SimpleObjectProperty<>(cell.getValue().getDueDate())
        );
        dateCol.setCellFactory(col -> new DatePickerTableCell<>());
        dateCol.setOnEditCommit(evt -> 
            evt.getRowValue().setDueDate(evt.getNewValue())
        );

        prioCol.setCellValueFactory(cell -> 
            cell.getValue().priorityProperty().asObject()
        );
        prioCol.setCellFactory(ComboBoxTableCell.forTableColumn(1,2,3,4,5));
        prioCol.setOnEditCommit(evt -> 
            evt.getRowValue().setPriority(evt.getNewValue())
        );


        ImageView iv = (ImageView) plusBtn.getGraphic();
        if (iv != null) {
            iv.setImage(new Image(getClass()
                .getResource("icons/plus.png")
                .toExternalForm()));
        }
    }

    @FXML private void onShowInput() {
        boolean show = !inputBar.isVisible();
        inputBar.setVisible(show);
        inputBar.setManaged(show);
    }

    @FXML private void onAddTask() {
        String desc = descField.getText().trim();
        LocalDate due = datePicker.getValue();
        Integer prio = prioBox.getValue();
        if (!desc.isEmpty() && due != null && prio != null) {
            tasks.add(new Task(desc, due, prio));
            descField.clear();
            datePicker.setValue(LocalDate.now());
            prioBox.getSelectionModel().select(2);
            onShowInput();
        }
    }

    public ObservableList<Task> getTaskList() {
        return tasks;
    }
}
