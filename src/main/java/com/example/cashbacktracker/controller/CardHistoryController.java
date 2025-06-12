package com.example.cashbacktracker.controller;

import com.example.cashbacktracker.model.CardHistory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Контроллер окна истории изменений карты.
 * Управляет отображением таблицы с историей изменений кэшбэка по карте.
 */
public class CardHistoryController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    
    @FXML
    private TableView<CardHistory> historyTable;
    @FXML
    private TableColumn<CardHistory, String> categoryColumn;
    @FXML
    private TableColumn<CardHistory, Double> cashbackColumn;
    @FXML
    private TableColumn<CardHistory, LocalDate> dateColumn;
    @FXML
    private TableColumn<CardHistory, String> changeDateColumn;

    private String title;

    /**
     * Инициализирует контроллер и настраивает колонки таблицы.
     */
    @FXML
    public void initialize() {
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        cashbackColumn.setCellValueFactory(new PropertyValueFactory<>("cashbackPercentage"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("changeDate"));
        
        changeDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime recordDate = cellData.getValue().getRecordDate();
            return new SimpleStringProperty(
                recordDate != null ? recordDate.format(DATE_TIME_FORMATTER) : ""
            );
        });

        if (title != null) {
            updateTitleLabel();
            Stage stage = (Stage) historyTable.getScene().getWindow();
            if (stage != null) {
                stage.setTitle(title);
            }
        }
    }

    /**
     * Устанавливает список записей истории для отображения в таблице.
     *
     * @param history список записей истории
     */
    public void setHistory(List<CardHistory> history) {
        historyTable.setItems(FXCollections.observableArrayList(history));
    }

    /**
     * Устанавливает заголовок окна истории.
     *
     * @param title заголовок окна
     */
    public void setTitle(String title) {
        this.title = title;
        if (historyTable != null && historyTable.getScene() != null) {
            Stage stage = (Stage) historyTable.getScene().getWindow();
            if (stage != null) {
                stage.setTitle(title);
            }
            updateTitleLabel();
        }
    }

    /**
     * Обновляет текст заголовка в интерфейсе.
     */
    private void updateTitleLabel() {
        Platform.runLater(() -> {
            javafx.scene.control.Label titleLabel = (javafx.scene.control.Label) historyTable.getScene().lookup("#titleLabel");
            if (titleLabel != null) {
                titleLabel.setText(title);
            }
        });
    }

    /**
     * Обработчик нажатия кнопки закрытия окна.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) historyTable.getScene().getWindow();
        stage.close();
    }
} 