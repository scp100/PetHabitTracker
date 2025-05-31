package tw.ntou.pettracker.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import tw.ntou.pettracker.model.ViewMode;
import tw.ntou.pettracker.model.Task;
import tw.ntou.pettracker.util.MessageUtil;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * 處理任務的篩選、搜尋與排序
 */
public class FilterController {
    private final ObservableList<Task> tasks;
    private final FilteredList<Task> filteredTasks;

    // 由 MainController 注入的 UI 元件
    private TextField searchField;
    private ComboBox<String> filterPriorityBox;
    private ComboBox<String> filterStatusBox;
    private ComboBox<String> sortBox;
    private Button clearFiltersBtn;
    private TableView<Task> tableView;

    // 當前視圖模式
    private ViewMode currentViewMode = ViewMode.ALL;

    public FilterController(ObservableList<Task> tasks) {
        this.tasks = tasks;
        this.filteredTasks = new FilteredList<>(tasks);
    }

    /** 注入搜尋框 */
    public void setSearchField(TextField searchField) {
        this.searchField = searchField;
        if (searchField != null) {
            searchField.textProperty()
                    .addListener((obs, oldV, newV) -> applyFilters());
        }
    }

    /** 注入優先級篩選下拉 */
    public void setFilterPriorityBox(ComboBox<String> filterPriorityBox) {
        this.filterPriorityBox = filterPriorityBox;
        if (filterPriorityBox != null) {
            filterPriorityBox.setItems(FXCollections.observableArrayList(
                    "全部優先級", "(1) 高", "(2) 中高",
                    "(3) 中", "(4) 中低 ", "(5) 低 "
            ));
            filterPriorityBox.getSelectionModel().select(0);
            filterPriorityBox.setOnAction(e -> applyFilters());
        }
    }

    /** 注入狀態篩選下拉 */
    public void setFilterStatusBox(ComboBox<String> filterStatusBox) {
        this.filterStatusBox = filterStatusBox;
        if (filterStatusBox != null) {
            filterStatusBox.setItems(FXCollections.observableArrayList(
                    "全部狀態", "進行中", "已完成", "今日到期", "逾期"
            ));
            filterStatusBox.getSelectionModel().select(0);
            filterStatusBox.setOnAction(e -> applyFilters());
        }
    }

    /** 注入排序下拉 */
    public void setSortBox(ComboBox<String> sortBox) {
        this.sortBox = sortBox;
        if (sortBox != null) {
            sortBox.setItems(FXCollections.observableArrayList(
                    "按日期排序",
                    "按優先級排序",
                    "按名稱排序",
                    "按完成狀態排序",
                    "按類別排序"
            ));
            sortBox.getSelectionModel().select(0);
            sortBox.setOnAction(e -> applySorting(sortBox.getValue()));
        }
    }

    /** 注入「清除篩選」按鈕 */
    public void setClearFiltersBtn(Button clearFiltersBtn) {
        this.clearFiltersBtn = clearFiltersBtn;
        if (clearFiltersBtn != null) {
            clearFiltersBtn.setOnAction(e -> clearAllFilters());
        }
    }

    /** 注入主畫面的 TableView，供排序時呼叫 setItems() */
    public void setTableView(TableView<Task> tableView) {
        this.tableView = tableView;
    }

    /** 變更視圖模式 (TODAY, UPCOMING, ALL) */
    public void setViewMode(ViewMode viewMode) {
        this.currentViewMode = viewMode;
        applyFilters();
    }

    /** 取得篩選後清單，供 MainController 初始時建立 SortedList */
    public FilteredList<Task> getFilteredTasks() {
        return filteredTasks;
    }

    /** 取得篩選後清單的 ObservableList 版本 */
    public ObservableList<Task> getFilteredTasksAsList() {
        return filteredTasks;
    }

    /** 清除所有篩選條件並重新套用 */
    public void clearAllFilters() {
        if (searchField != null)       searchField.clear();
        if (filterPriorityBox != null) filterPriorityBox.getSelectionModel().select(0);
        if (filterStatusBox != null)   filterStatusBox.getSelectionModel().select(0);
        if (sortBox != null)           sortBox.getSelectionModel().select(0);

        applyFilters();
        applySorting(sortBox != null ? sortBox.getValue() : null);

        MessageUtil.showMessage("已清除所有篩選條件");
    }

    /** 套用搜尋、優先級、狀態、視圖模式篩選 */
    public void applyFilters() {
        Predicate<Task> combined = getViewFilter()
                .and(getSearchFilter())
                .and(getPriorityFilter())
                .and(getStatusFilter());
        filteredTasks.setPredicate(combined);
    }

    /** 根據傳入的排序文字套用排序並更新 TableView */
    public void applySorting(String sortOption) {
        if (tableView == null) return;

        Comparator<Task> comp;
        switch (sortOption) {
            case "按優先級排序":
                comp = Comparator.comparing(Task::getPriority)
                        .thenComparing(Task::getDueDate);
                break;
            case "按名稱排序":
                comp = Comparator.comparing(
                        Task::getDescription, String.CASE_INSENSITIVE_ORDER
                );
                break;
            case "按完成狀態排序":
                comp = Comparator.comparing(Task::isDone)
                        .thenComparing(Task::getDueDate);
                break;
            case "按類別排序":
                comp = Comparator
                        .<Task,String>comparing(t ->
                                t.getCategory() != null
                                        ? t.getCategory().getDisplayName()
                                        : ""
                        )
                        .thenComparing(Task::getDueDate);
                break;
            case "按日期排序":
            default:
                comp = Comparator.comparing(Task::getDueDate)
                        .thenComparing(Task::getPriority);
                break;
        }

        // 用 FilteredList 建構 SortedList，再綁定回 TableView
        SortedList<Task> sorted = new SortedList<>(filteredTasks);
        sorted.setComparator(comp);
        tableView.setItems(sorted);
    }

    // === 以下為各種子篩選邏輯 ===

    private Predicate<Task> getViewFilter() {
        LocalDate today = LocalDate.now();
        switch (currentViewMode) {
            case TODAY:
                return t -> t.getDueDate().equals(today);
            case UPCOMING:
                return t -> {
                    LocalDate d = t.getDueDate();
                    return d.isAfter(today) && d.isBefore(today.plusDays(7));
                };
            case ALL:
            default:
                return t -> true;
        }
    }

    private Predicate<Task> getSearchFilter() {
        if (searchField == null || searchField.getText().trim().isEmpty())
            return t -> true;
        String txt = searchField.getText().toLowerCase().trim();
        return t -> {
            if (t.getDescription().toLowerCase().contains(txt)) return true;
            if (t.getTags() != null &&
                    t.getTags().toLowerCase().contains(txt)) return true;
            if (t.getCategory() != null &&
                    t.getCategory().getDisplayName().toLowerCase().contains(txt))
                return true;
            return false;
        };
    }

    private Predicate<Task> getPriorityFilter() {
        if (filterPriorityBox == null ||
                filterPriorityBox.getSelectionModel().getSelectedIndex() <= 0)
            return t -> true;
        int idx = filterPriorityBox.getSelectionModel().getSelectedIndex();
        return t -> t.getPriority() == idx;
    }

    private Predicate<Task> getStatusFilter() {
        if (filterStatusBox == null ||
                filterStatusBox.getSelectionModel().getSelectedIndex() <= 0)
            return t -> true;
        String sel = filterStatusBox.getSelectionModel().getSelectedItem();
        LocalDate today = LocalDate.now();
        switch (sel) {
            case "進行中":   return t -> !t.isDone();
            case "已完成":   return Task::isDone;
            case "今日到期": return t -> t.getDueDate().equals(today) && !t.isDone();
            case "逾期":     return t -> t.getDueDate().isBefore(today) && !t.isDone();
            default:           return t -> true;
        }
    }
}
