package tw.ntou.pettracker.util;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import tw.ntou.pettracker.model.Task;

import java.util.function.BiConsumer;

/**
 * 拖放功能管理器
 */
public class DragDropManager {

    public static void setupDragDrop(TableView<Task> table, BiConsumer<Integer, Integer> moveAction) {
        table.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(String.valueOf(row.getIndex()));
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    int dropIndex = row.isEmpty() ? table.getItems().size() : row.getIndex();

                    // 執行移動操作
                    moveAction.accept(draggedIndex, dropIndex);

                    event.setDropCompleted(true);
                    event.consume();
                }
            });

            return row;
        });
    }
}