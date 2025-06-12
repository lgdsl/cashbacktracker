package com.example.cashbacktracker.controller;

import com.example.cashbacktracker.model.Card;
import com.example.cashbacktracker.model.CardHistory;
import com.example.cashbacktracker.service.CardService;
import com.example.cashbacktracker.dao.CardDaoFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер главного окна приложения.
 * Управляет отображением списка карт, фильтрацией и основными операциями с картами.
 */
public class MainController {
    private static final String ALL_BANKS = "Все банки";
    private static final String ALL_CATEGORIES = "Все категории";
    private static final String ALL_STATUSES = "Все статусы";
    
    @FXML
    TableView<Card> cardsTable;
    @FXML
    private TableColumn<Card, String> bankNameColumn;
    @FXML
    private TableColumn<Card, String> cardNameColumn;
    @FXML
    private TableColumn<Card, String> categoryColumn;
    @FXML
    private TableColumn<Card, Double> cashbackColumn;
    @FXML
    private TableColumn<Card, LocalDate> changeDateColumn;
    @FXML
    private TableColumn<Card, Card.CardStatus> statusColumn;
    
    @FXML
    ComboBox<String> bankFilter;
    @FXML
    ComboBox<String> categoryFilter;
    @FXML
    ComboBox<Card.CardStatus> statusFilter;
    @FXML
    ComboBox<String> storageTypeCombo;
    @FXML
    ComboBox<String> searchCategoryCombo;
    
    private CardService cardService;
    private final ObservableList<Card> cardList = FXCollections.observableArrayList();
    
    /**
     * Конструктор контроллера.
     */
    public MainController() {
    }
    
    /**
     * Устанавливает сервис для работы с картами и инициализирует необходимые компоненты.
     *
     * @param cardService сервис для работы с картами
     */
    public void setCardService(CardService cardService) {
        this.cardService = cardService;
        setupStorageTypeCombo();
        loadCards();
        checkExpiringCards();
    }
    
    /**
     * Инициализирует компоненты интерфейса при загрузке FXML.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
    }
    
    /**
     * Настраивает колонки таблицы карт.
     */
    private void setupTableColumns() {
        bankNameColumn.setCellValueFactory(new PropertyValueFactory<>("bankName"));
        cardNameColumn.setCellValueFactory(new PropertyValueFactory<>("cardName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        cashbackColumn.setCellValueFactory(new PropertyValueFactory<>("cashback"));
        changeDateColumn.setCellValueFactory(new PropertyValueFactory<>("categoryChangeDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Card.CardStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(status.getDisplayName());
                }
            }
        });
        
        cardsTable.setItems(cardList);
    }
    
    /**
     * Настраивает фильтры для списка карт.
     */
    private void setupFilters() {
        bankFilter.getItems().add(ALL_BANKS);
        categoryFilter.getItems().add(ALL_CATEGORIES);
        statusFilter.getItems().addAll(null, Card.CardStatus.ACTIVE, Card.CardStatus.EXPIRED);
        
        statusFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(Card.CardStatus status) {
                return status == null ? ALL_STATUSES : status.getDisplayName();
            }

            @Override
            public Card.CardStatus fromString(String string) {
                if (string == null || string.equals(ALL_STATUSES)) {
                    return null;
                }
                for (Card.CardStatus status : Card.CardStatus.values()) {
                    if (status.getDisplayName().equals(string)) {
                        return status;
                    }
                }
                return null;
            }
        });
        
        bankFilter.setValue(ALL_BANKS);
        categoryFilter.setValue(ALL_CATEGORIES);
        statusFilter.setValue(null);

        searchCategoryCombo.getItems().add(ALL_CATEGORIES);
        searchCategoryCombo.setValue(ALL_CATEGORIES);

        bankFilter.setOnAction(e -> applyFilters());
        categoryFilter.setOnAction(e -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
    }
    
    /**
     * Настраивает комбобокс для выбора типа хранилища данных.
     */
    private void setupStorageTypeCombo() {
        storageTypeCombo.getItems().addAll("SQLite", "JSON", "XML");
        storageTypeCombo.setValue("SQLite");
        
        storageTypeCombo.setOnAction(e -> {
            String selectedStorage = storageTypeCombo.getValue();
            if (selectedStorage != null) {
                try {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Подтверждение смены хранилища");
                    alert.setHeaderText("Внимание!");
                    alert.setContentText("При смене хранилища все данные в текущем хранилище будут недоступны. " +
                            "Данные в новом хранилище будут независимы. Продолжить?");
                    
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                CardDaoFactory.StorageType type = CardDaoFactory.StorageType.valueOf(selectedStorage.toUpperCase());
                                cardService.switchStorage(type);
                                loadCards();
                                showInfo("Информация", "Хранилище данных успешно изменено на " + selectedStorage);
                            } catch (Exception ex) {
                                showError("Ошибка", "Не удалось переключить хранилище данных: " + ex.getMessage());
                                storageTypeCombo.setValue(storageTypeCombo.getItems().get(0));
                            }
                        } else {
                            storageTypeCombo.setValue(storageTypeCombo.getItems().get(0));
                        }
                    });
                } catch (Exception ex) {
                    showError("Ошибка", "Не удалось переключить хранилище данных: " + ex.getMessage());
                    storageTypeCombo.setValue(storageTypeCombo.getItems().get(0));
                }
            }
        });
    }
    
    /**
     * Загружает список карт из хранилища.
     */
    protected void loadCards() {
        cardList.clear();
        cardList.addAll(cardService.getAllCards());
        updateFilterOptions();
    }
    
    /**
     * Обновляет опции фильтров на основе текущего списка карт.
     */
    private void updateFilterOptions() {
        List<String> banks = cardList.stream()
                .map(Card::getBankName)
                .distinct()
                .toList();
        List<String> categories = cardList.stream()
                .map(Card::getCategory)
                .distinct()
                .toList();
        
        String currentBank = bankFilter.getValue();
        String currentCategory = categoryFilter.getValue();
        String currentSearchCategory = searchCategoryCombo.getValue();
        
        bankFilter.getItems().setAll(ALL_BANKS);
        bankFilter.getItems().addAll(banks);
        
        categoryFilter.getItems().setAll(ALL_CATEGORIES);
        categoryFilter.getItems().addAll(categories);
        
        searchCategoryCombo.getItems().setAll(ALL_CATEGORIES);
        searchCategoryCombo.getItems().addAll(categories);
        
        if (currentBank != null && bankFilter.getItems().contains(currentBank)) {
            bankFilter.setValue(currentBank);
        }
        if (currentCategory != null && categoryFilter.getItems().contains(currentCategory)) {
            categoryFilter.setValue(currentCategory);
        }
        if (currentSearchCategory != null && searchCategoryCombo.getItems().contains(currentSearchCategory)) {
            searchCategoryCombo.setValue(currentSearchCategory);
        }
    }
    
    /**
     * Применяет выбранные фильтры к списку карт.
     */
    protected void applyFilters() {
        String selectedBank = bankFilter.getValue();
        String selectedCategory = categoryFilter.getValue();
        Card.CardStatus selectedStatus = statusFilter.getValue();
        
        List<Card> filteredCards = cardService.getAllCards();

        if (selectedBank != null && !selectedBank.equals(ALL_BANKS)) {
            filteredCards = filteredCards.stream()
                    .filter(card -> card.getBankName().equals(selectedBank))
                    .toList();
        }

        if (selectedCategory != null && !selectedCategory.equals(ALL_CATEGORIES)) {
            filteredCards = filteredCards.stream()
                    .filter(card -> card.getCategory().equals(selectedCategory))
                    .toList();
        }

        if (selectedStatus != null) {
            filteredCards = filteredCards.stream()
                    .filter(card -> card.getStatus() == selectedStatus)
                    .toList();
        }
        
        cardList.setAll(filteredCards);
    }
    
    /**
     * Обработчик нажатия кнопки добавления новой карты.
     */
    @FXML
    void handleAddCard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-card-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            AddCardDialogController controller = loader.getController();
            controller.setDialogPane(dialogPane);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Добавить карту");
            dialog.setResizable(false);

            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Card newCard = controller.getCard();
                    if (newCard != null) {
                        cardService.addCard(newCard);
                        loadCards();
                    }
                }
            });
        } catch (Exception e) {
            showError("Ошибка", "Не удалось открыть окно добавления карты");
        }
    }
    
    /**
     * Обработчик нажатия кнопки редактирования карты.
     */
    @FXML
    void handleEditCard() {
        Card selectedCard = cardsTable.getSelectionModel().getSelectedItem();
        if (selectedCard == null) {
            showError("Ошибка", "Пожалуйста, выберите карту для редактирования");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-card-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            AddCardDialogController controller = loader.getController();
            controller.setCard(selectedCard);
            controller.setDialogPane(dialogPane);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Редактировать карту");
            dialog.setResizable(false);

            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Card updatedCard = controller.getCard();
                    if (updatedCard != null) {
                        updatedCard.setId(selectedCard.getId());
                        cardService.updateCard(updatedCard);
                        loadCards();
                    }
                }
            });
        } catch (Exception e) {
            showError("Ошибка", "Не удалось открыть окно редактирования карты");
        }
    }
    
    /**
     * Обработчик нажатия кнопки удаления карты.
     */
    @FXML
    void handleDeleteCard() {
        Card selectedCard = cardsTable.getSelectionModel().getSelectedItem();
        if (selectedCard == null) {
            showError("Ошибка", "Пожалуйста, выберите карту для удаления");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление карты");
        alert.setContentText("Вы уверены, что хотите удалить карту " + selectedCard.getCardName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cardService.deleteCard(selectedCard.getId());
                loadCards();
            }
        });
    }
    
    /**
     * Обработчик нажатия кнопки просмотра истории карты.
     */
    @FXML
    void handleShowHistory() {
        Card selectedCard = cardsTable.getSelectionModel().getSelectedItem();
        if (selectedCard == null) {
            showError("Ошибка", "Пожалуйста, выберите карту для просмотра истории");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/card-history-view.fxml"));
            VBox root = loader.load();
            CardHistoryController controller = loader.getController();

            List<CardHistory> history = cardService.getCardHistory(selectedCard.getId());
            controller.setHistory(history);
            controller.setTitle("История изменений");

            Stage stage = new Stage();
            stage.initOwner(cardsTable.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setTitle("История изменений карты " + selectedCard.getCardName());
            stage.show();
        } catch (Exception e) {
            showError("Ошибка", "Не удалось открыть окно истории изменений");
        }
    }
    
    /**
     * Обработчик нажатия кнопки сброса фильтров.
     */
    @FXML
    private void handleResetFilters() {
        resetFilters();
        applyFilters();
    }
    
    /**
     * Сбрасывает все фильтры к значениям по умолчанию.
     */
    private void resetFilters() {
        bankFilter.setValue(ALL_BANKS);
        categoryFilter.setValue(ALL_CATEGORIES);
        statusFilter.setValue(null);
    }
    
    /**
     * Проверяет наличие карт с истекающей категорией кэшбэка.
     */
    public void checkExpiringCards() {
        List<Card> expiringCards = cardService.getExpiringCards(LocalDate.now());
        if (!expiringCards.isEmpty()) {
            StringBuilder message = new StringBuilder("Следующие карты требуют обновления категории кэшбэка:\n\n");
            for (Card card : expiringCards) {
                message.append(String.format("%s %s: %s (%.1f%%)\n",
                        card.getBankName(),
                        card.getCardName(),
                        card.getCategory(),
                        card.getCashback()));
                
                card.setStatus(Card.CardStatus.EXPIRED);
                cardService.updateCard(card);
            }
            showInfo("Внимание", message.toString());
            loadCards();
        }
    }
    
    /**
     * Обработчик нажатия кнопки поиска лучшей карты для категории.
     */
    @FXML
    private void handleFindBestCard() {
        String selectedCategory = searchCategoryCombo.getValue();
        if (selectedCategory == null || selectedCategory.equals(ALL_CATEGORIES)) {
            showError("Ошибка", "Пожалуйста, выберите конкретную категорию");
            return;
        }
        
        Optional<Card> bestCard = cardService.findBestCardForCategory(selectedCategory);
        if (bestCard.isPresent()) {
            Card card = bestCard.get();
            String message = String.format("""
                    Найдена карта с наибольшим кэшбэком для категории "%s":
                    
                    Банк: %s
                    Название карты: %s
                    Кэшбэк: %.1f%%
                    Дата изменения категории: %s
                    """,
                    selectedCategory,
                    card.getBankName(),
                    card.getCardName(),
                    card.getCashback(),
                    card.getCategoryChangeDate());
            
            showInfo("Лучшая карта", message);
        } else {
            showInfo("Информация", "Для выбранной категории не найдено карт с кэшбэком");
        }
    }
    
    /**
     * Показывает диалог с сообщением об ошибке.
     *
     * @param title заголовок диалога
     * @param content текст сообщения
     */
    private void showError(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, content);
    }
    
    /**
     * Показывает диалог с информационным сообщением.
     *
     * @param title заголовок диалога
     * @param content текст сообщения
     */
    private void showInfo(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }
    
    /**
     * Возвращает список карт для отображения в таблице.
     *
     * @return список карт
     */
    ObservableList<Card> getCardList() {
        return cardList;
    }
    
    //Методы для тестирования

    /**
     * Добавляет новую карту в список.
     *
     * @param card карта для добавления
     */
    public void addCard(Card card) {
        cardService.addCard(card);
        loadCards();
    }
    
    /**
     * Удаляет карту из списка.
     *
     * @param id идентификатор карты для удаления
     */
    public void deleteCard(Long id) {
        cardService.deleteCard(id);
        loadCards();
    }
    
    /**
     * Применяет фильтры к списку карт.
     *
     * @param bank фильтр по банку
     * @param category фильтр по категории
     * @param status фильтр по статусу
     * @return отфильтрованный список карт
     */
    public List<Card> applyFilters(String bank, String category, Card.CardStatus status) {
        List<Card> filteredCards = cardService.getAllCards();
        
        if (bank != null && !bank.equals(ALL_BANKS)) {
            filteredCards = filteredCards.stream()
                    .filter(card -> card.getBankName().equals(bank))
                    .toList();
        }
        
        if (category != null && !category.equals(ALL_CATEGORIES)) {
            filteredCards = filteredCards.stream()
                    .filter(card -> card.getCategory().equals(category))
                    .toList();
        }
        
        if (status != null) {
            filteredCards = filteredCards.stream()
                    .filter(card -> card.getStatus() == status)
                    .toList();
        }
        
        return filteredCards;
    }
    
    /**
     * Получает историю изменений для карты.
     *
     * @param cardId идентификатор карты
     * @return список записей истории
     */
    public List<CardHistory> getCardHistory(Long cardId) {
        return cardService.getCardHistory(cardId);
    }
    
    /**
     * Находит лучшую карту для указанной категории.
     *
     * @param category категория кэшбэка
     * @return Optional с лучшей картой, если такая найдена
     */
    public Optional<Card> findBestCardForCategory(String category) {
        return cardService.findBestCardForCategory(category);
    }
    
    /**
     * Обновляет информацию о карте.
     *
     * @param card карта с обновленными данными
     */
    public void updateCard(Card card) {
        cardService.updateCard(card);
        loadCards();
    }
    
    /**
     * Показывает диалог с указанным типом, заголовком и содержимым.
     *
     * @param type тип диалога
     * @param title заголовок диалога
     * @param content содержимое диалога
     */
    protected void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 