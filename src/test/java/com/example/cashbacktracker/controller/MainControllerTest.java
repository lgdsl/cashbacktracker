package com.example.cashbacktracker.controller;

import com.example.cashbacktracker.model.Card;
import com.example.cashbacktracker.model.CardHistory;
import com.example.cashbacktracker.service.CardService;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {
    @Mock
    private CardService cardService;

    private ComboBox<String> bankFilter;
    private ComboBox<String> categoryFilter;
    private ComboBox<Card.CardStatus> statusFilter;
    private ComboBox<String> storageTypeCombo;
    private ComboBox<String> searchCategoryCombo;
    private TableView<Card> cardsTable;

    private MainController controller;

    @BeforeAll
    static void initJfx() throws Exception {
        // Инициализация JavaFX Toolkit
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    @BeforeEach
    void setUp() {
        controller = Mockito.spy(new MainController());
        lenient().doNothing().when(controller).showAlert(any(), anyString(), anyString());
        // Реальные JavaFX-компоненты
        bankFilter = new ComboBox<>();
        categoryFilter = new ComboBox<>();
        statusFilter = new ComboBox<>();
        storageTypeCombo = new ComboBox<>();
        searchCategoryCombo = new ComboBox<>();
        cardsTable = new TableView<>();

        bankFilter.setValue("Все банки");
        categoryFilter.setValue("Все категории");
        statusFilter.setValue(null);
        storageTypeCombo.setValue("SQLite");
        searchCategoryCombo.setValue("Все категории");

        // Установка компонентов через рефлексию
        try {
            java.lang.reflect.Field bankFilterField = MainController.class.getDeclaredField("bankFilter");
            bankFilterField.setAccessible(true);
            bankFilterField.set(controller, bankFilter);

            java.lang.reflect.Field categoryFilterField = MainController.class.getDeclaredField("categoryFilter");
            categoryFilterField.setAccessible(true);
            categoryFilterField.set(controller, categoryFilter);

            java.lang.reflect.Field statusFilterField = MainController.class.getDeclaredField("statusFilter");
            statusFilterField.setAccessible(true);
            statusFilterField.set(controller, statusFilter);

            java.lang.reflect.Field storageTypeComboField = MainController.class.getDeclaredField("storageTypeCombo");
            storageTypeComboField.setAccessible(true);
            storageTypeComboField.set(controller, storageTypeCombo);

            java.lang.reflect.Field searchCategoryComboField = MainController.class.getDeclaredField("searchCategoryCombo");
            searchCategoryComboField.setAccessible(true);
            searchCategoryComboField.set(controller, searchCategoryCombo);

            java.lang.reflect.Field cardsTableField = MainController.class.getDeclaredField("cardsTable");
            cardsTableField.setAccessible(true);
            cardsTableField.set(controller, cardsTable);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        controller.setCardService(cardService);
        // Сброс взаимодействий с моками после инициализации
        clearInvocations(cardService);
    }

    @Test
    void testAddCard() {
        // Arrange
        Card card = new Card("Test Bank", "Test Card", "Test Category", 5.0, LocalDate.now(), true);
        
        // Act
        controller.addCard(card);
        
        // Assert
        verify(cardService).addCard(card);
        verify(cardService).getAllCards();
    }
    
    @Test
    void testDeleteCard() {
        // Arrange
        Long cardId = 1L;
        
        // Act
        controller.deleteCard(cardId);
        
        // Assert
        verify(cardService).deleteCard(cardId);
        verify(cardService).getAllCards();
    }
    
    @Test
    void testApplyFilters() {
        // Arrange
        List<Card> allCards = Arrays.asList(
            new Card("Bank1", "Card1", "Category1", 5.0, LocalDate.now(), true),
            new Card("Bank2", "Card2", "Category2", 10.0, LocalDate.now(), true)
        );
        when(cardService.getAllCards()).thenReturn(allCards);
        
        // Act
        List<Card> filteredCards = controller.applyFilters("Bank1", "Category1", Card.CardStatus.ACTIVE);
        
        // Assert
        assertEquals(1, filteredCards.size());
        assertEquals("Bank1", filteredCards.get(0).getBankName());
        assertEquals("Category1", filteredCards.get(0).getCategory());
        assertEquals(Card.CardStatus.ACTIVE, filteredCards.get(0).getStatus());
    }
    
    @Test
    void testGetCardHistory() {
        // Arrange
        Long cardId = 1L;
        List<CardHistory> expectedHistory = Arrays.asList(
            new CardHistory(),
            new CardHistory()
        );
        when(cardService.getCardHistory(cardId)).thenReturn(expectedHistory);
        
        // Act
        List<CardHistory> actualHistory = controller.getCardHistory(cardId);
        
        // Assert
        assertEquals(expectedHistory, actualHistory);
        verify(cardService).getCardHistory(cardId);
    }
    
    @Test
    void testFindBestCardForCategory() {
        // Arrange
        String category = "Test Category";
        Card bestCard = new Card("Bank1", "Card1", category, 15.0, LocalDate.now(), true);
        when(cardService.findBestCardForCategory(category)).thenReturn(Optional.of(bestCard));
        
        // Act
        Optional<Card> result = controller.findBestCardForCategory(category);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(bestCard, result.get());
        verify(cardService).findBestCardForCategory(category);
    }
    
    @Test
    void testCheckExpiringCards() throws Exception {
        // Arrange
        LocalDate today = LocalDate.now();
        List<Card> expiringCards = Arrays.asList(
            new Card("Bank1", "Card1", "Category1", 5.0, today.minusDays(1), true),
            new Card("Bank2", "Card2", "Category2", 10.0, today.minusDays(2), true)
        );
        when(cardService.getExpiringCards(today)).thenReturn(expiringCards);

        // Act (на FX Application Thread)
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.checkExpiringCards();
            latch.countDown();
        });
        latch.await();

        // Assert
        verify(cardService).getExpiringCards(today);
        verify(cardService, times(2)).updateCard(any());
        verify(cardService).getAllCards();
    }
    
    @Test
    void testAddCardScenario() {
        // Arrange
        Card newCard = new Card("Test Bank", "Test Card", "Test Category", 5.0, LocalDate.now(), true);
        List<Card> expectedCards = Arrays.asList(newCard);
        when(cardService.getAllCards()).thenReturn(expectedCards);
        
        // Act
        controller.addCard(newCard);
        
        // Assert
        verify(cardService).addCard(newCard);
        verify(cardService).getAllCards();
        assertEquals(expectedCards, controller.getCardList());
    }
    
    @Test
    void testViewCardHistoryScenario() {
        // Arrange
        Long cardId = 1L;
        List<CardHistory> expectedHistory = Arrays.asList(
            new CardHistory(),
            new CardHistory()
        );
        when(cardService.getCardHistory(cardId)).thenReturn(expectedHistory);
        
        // Act
        List<CardHistory> actualHistory = controller.getCardHistory(cardId);
        
        // Assert
        assertEquals(expectedHistory, actualHistory);
        verify(cardService).getCardHistory(cardId);
    }
    
    @Test
    void testViewCardsScenario() {
        // Arrange
        List<Card> expectedCards = Arrays.asList(
            new Card("Bank1", "Card1", "Category1", 5.0, LocalDate.now(), true),
            new Card("Bank2", "Card2", "Category2", 10.0, LocalDate.now(), true)
        );
        when(cardService.getAllCards()).thenReturn(expectedCards);
        
        // Act
        controller.loadCards();
        
        // Assert
        verify(cardService).getAllCards();
        assertEquals(expectedCards, controller.getCardList());
    }
    
    @Test
    void testEditCardScenario() {
        // Arrange
        Card card = new Card("Test Bank", "Test Card", "Test Category", 5.0, LocalDate.now(), true);
        card.setId(1L);
        List<Card> expectedCards = Arrays.asList(card);
        when(cardService.getAllCards()).thenReturn(expectedCards);
        
        // Act
        controller.updateCard(card);
        
        // Assert
        verify(cardService).updateCard(card);
        verify(cardService).getAllCards();
        assertEquals(expectedCards, controller.getCardList());
    }
    
    @Test
    void testDeleteCardScenario() {
        // Arrange
        Long cardId = 1L;
        List<Card> expectedCards = Arrays.asList();
        when(cardService.getAllCards()).thenReturn(expectedCards);
        
        // Act
        controller.deleteCard(cardId);
        
        // Assert
        verify(cardService).deleteCard(cardId);
        verify(cardService).getAllCards();
        assertEquals(expectedCards, controller.getCardList());
    }
    
    @Test
    void testFilterCardsScenario() {
        // Arrange
        List<Card> allCards = Arrays.asList(
            new Card("Bank1", "Card1", "Category1", 5.0, LocalDate.now(), true),
            new Card("Bank2", "Card2", "Category2", 10.0, LocalDate.now(), true),
            new Card("Bank1", "Card3", "Category1", 15.0, LocalDate.now(), true)
        );
        when(cardService.getAllCards()).thenReturn(allCards);
        
        // Act
        List<Card> filteredCards = controller.applyFilters("Bank1", "Category1", Card.CardStatus.ACTIVE);
        
        // Assert
        assertEquals(2, filteredCards.size());
        assertTrue(filteredCards.stream().allMatch(card -> 
            card.getBankName().equals("Bank1") && 
            card.getCategory().equals("Category1") && 
            card.getStatus() == Card.CardStatus.ACTIVE
        ));
    }
} 