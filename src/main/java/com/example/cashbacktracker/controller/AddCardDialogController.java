package com.example.cashbacktracker.controller;

import com.example.cashbacktracker.model.Card;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

/**
 * Контроллер диалогового окна добавления/редактирования карты.
 * Управляет формой ввода данных карты и их валидацией.
 */
public class AddCardDialogController {
    @FXML
    private TextField bankNameField;
    @FXML
    private TextField cardNameField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField cashbackField;
    @FXML
    private DatePicker changeDatePicker;
    @FXML
    private CheckBox activeCheckBox;
    @FXML
    private DialogPane dialogPane;
    
    @FXML
    private Label bankNameError;
    @FXML
    private Label cardNameError;
    @FXML
    private Label categoryError;
    @FXML
    private Label cashbackError;
    @FXML
    private Label dateError;

    private Card card;
    private boolean isEditMode = false;

    /**
     * Инициализирует контроллер и настраивает обработчики событий для полей ввода.
     */
    @FXML
    public void initialize() {

        bankNameField.textProperty().addListener((obs, oldVal, newVal) -> bankNameError.setText(""));
        cardNameField.textProperty().addListener((obs, oldVal, newVal) -> cardNameError.setText(""));
        categoryField.textProperty().addListener((obs, oldVal, newVal) -> categoryError.setText(""));
        cashbackField.textProperty().addListener((obs, oldVal, newVal) -> cashbackError.setText(""));
        changeDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> dateError.setText(""));
    }

    /**
     * Устанавливает диалоговую панель и настраивает обработку кнопки OK.
     *
     * @param dialogPane диалоговая панель
     */
    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);

        okButton.setOnAction(event -> {
            if (!validateInput()) {
                event.consume();

                dialogPane.getScene().getWindow().requestFocus();
            }
        });

        dialogPane.getButtonTypes().stream()
                .filter(buttonType -> buttonType == ButtonType.OK)
                .findFirst()
                .ifPresent(buttonType -> {
                    Button button = (Button) dialogPane.lookupButton(buttonType);
                    button.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                        if (!validateInput()) {
                            event.consume();
                        }
                    });
                });
    }

    /**
     * Устанавливает карту для редактирования.
     * Если карта не null, заполняет поля формы её данными.
     *
     * @param card карта для редактирования
     */
    public void setCard(Card card) {
        this.card = card;
        this.isEditMode = card != null;
        if (isEditMode) {
            bankNameField.setText(card.getBankName());
            cardNameField.setText(card.getCardName());
            categoryField.setText(card.getCategory());
            cashbackField.setText(String.valueOf(card.getCashback()));
            changeDatePicker.setValue(card.getCategoryChangeDate());
            activeCheckBox.setSelected(card.isActive());
        }
    }

    /**
     * Получает данные карты из формы.
     * Перед возвратом данных выполняет их валидацию.
     *
     * @return объект Card с данными из формы или null, если данные невалидны
     */
    public Card getCard() {
        if (!validateInput()) {
            return null;
        }

        String bankName = bankNameField.getText().trim();
        String cardName = cardNameField.getText().trim();
        String category = categoryField.getText().trim();
        double cashback = Double.parseDouble(cashbackField.getText().trim());
        LocalDate changeDate = changeDatePicker.getValue();
        boolean isActive = activeCheckBox.isSelected();

        Card newCard = new Card(bankName, cardName, category, cashback, changeDate, isActive);
        if (isEditMode) {
            newCard.setId(card.getId());
        }
        return newCard;
    }

    /**
     * Проверяет корректность введенных данных.
     * Проверяет заполнение всех полей и корректность их значений.
     *
     * @return true если все данные валидны, false в противном случае
     */
    private boolean validateInput() {
        boolean isValid = true;

        bankNameError.setText("");
        cardNameError.setText("");
        categoryError.setText("");
        cashbackError.setText("");
        dateError.setText("");

        String bankName = bankNameField.getText().trim();
        if (bankName.isEmpty()) {
            bankNameError.setText("Необходимо заполнить");
            isValid = false;
        } else if (Character.isDigit(bankName.charAt(0))) {
            bankNameError.setText("Название банка не должно начинаться с цифры");
            isValid = false;
        }

        String cardName = cardNameField.getText().trim();
        if (cardName.isEmpty()) {
            cardNameError.setText("Необходимо заполнить");
            isValid = false;
        } else if (Character.isDigit(cardName.charAt(0))) {
            cardNameError.setText("Название карты не должно начинаться с цифры");
            isValid = false;
        }

        String category = categoryField.getText().trim();
        if (category.isEmpty()) {
            categoryError.setText("Необходимо заполнить");
            isValid = false;
        } else if (Character.isDigit(category.charAt(0))) {
            categoryError.setText("Название категории не должно начинаться с цифры");
            isValid = false;
        }

        String cashbackText = cashbackField.getText().trim();
        if (cashbackText.isEmpty()) {
            cashbackError.setText("Необходимо заполнить");
            isValid = false;
        } else {
            try {
                double cashback = Double.parseDouble(cashbackText);
                if (cashback < 0 || cashback > 100) {
                    cashbackError.setText("Кэшбэк должен быть от 0 до 100%");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                cashbackError.setText("Введите корректное число");
                isValid = false;
            }
        }

        LocalDate changeDate = changeDatePicker.getValue();
        if (changeDate == null) {
            dateError.setText("Необходимо заполнить");
            isValid = false;
        } else if (!changeDate.isAfter(LocalDate.now())) {
            dateError.setText("Дата должна быть позже текущей");
            isValid = false;
        }
        
        return isValid;
    }
} 