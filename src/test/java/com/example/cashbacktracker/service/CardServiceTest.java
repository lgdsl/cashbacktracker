package com.example.cashbacktracker.service;

import com.example.cashbacktracker.dao.CardDao;
import com.example.cashbacktracker.model.Card;
import com.example.cashbacktracker.model.CardHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock
    private CardDao cardDao;
    
    private CardService cardService;
    
    @BeforeEach
    void setUp() {
        cardService = new CardService(cardDao);
    }
    
    @Test
    void testAddCard() {
        // Arrange
        Card card = new Card("Test Bank", "Test Card", "Test Category", 5.0, LocalDate.now(), true);
        
        // Act
        cardService.addCard(card);
        
        // Assert
        verify(cardDao).saveCard(card);
    }
    
    @Test
    void testGetAllCards() {
        // Arrange
        List<Card> expectedCards = Arrays.asList(
            new Card("Bank1", "Card1", "Category1", 5.0, LocalDate.now(), true),
            new Card("Bank2", "Card2", "Category2", 10.0, LocalDate.now(), true)
        );
        when(cardDao.getAllCards()).thenReturn(expectedCards);
        
        // Act
        List<Card> actualCards = cardService.getAllCards();
        
        // Assert
        assertEquals(expectedCards, actualCards);
        verify(cardDao).getAllCards();
    }
    
    @Test
    void testUpdateCard() {
        // Arrange
        Card card = new Card("Test Bank", "Test Card", "Test Category", 5.0, LocalDate.now(), true);
        card.setId(1L);
        
        // Act
        cardService.updateCard(card);
        
        // Assert
        verify(cardDao).updateCard(card);
    }
    
    @Test
    void testDeleteCard() {
        // Arrange
        Long cardId = 1L;
        
        // Act
        cardService.deleteCard(cardId);
        
        // Assert
        verify(cardDao).deleteCard(cardId);
    }
    
    @Test
    void testGetExpiringCards() {
        // Arrange
        LocalDate date = LocalDate.now();
        List<Card> expectedCards = Arrays.asList(
            new Card("Bank1", "Card1", "Category1", 5.0, date.minusDays(1), true),
            new Card("Bank2", "Card2", "Category2", 10.0, date.minusDays(2), true)
        );
        when(cardDao.findByExpiringCategory(date)).thenReturn(expectedCards);
        
        // Act
        List<Card> actualCards = cardService.getExpiringCards(date);
        
        // Assert
        assertEquals(expectedCards, actualCards);
        verify(cardDao).findByExpiringCategory(date);
    }
    
    @Test
    void testGetCardHistory() {
        // Arrange
        Long cardId = 1L;
        List<CardHistory> expectedHistory = Arrays.asList(
            new CardHistory(),
            new CardHistory()
        );
        when(cardDao.findHistoryByCardId(cardId)).thenReturn(expectedHistory);
        
        // Act
        List<CardHistory> actualHistory = cardService.getCardHistory(cardId);
        
        // Assert
        assertEquals(expectedHistory, actualHistory);
        verify(cardDao).findHistoryByCardId(cardId);
    }
    
    @Test
    void testFindBestCardForCategory() {
        // Arrange
        String category = "Test Category";
        List<Card> cards = Arrays.asList(
            new Card("Bank1", "Card1", category, 5.0, LocalDate.now(), true),
            new Card("Bank2", "Card2", category, 10.0, LocalDate.now(), true),
            new Card("Bank3", "Card3", category, 15.0, LocalDate.now(), true)
        );
        when(cardDao.findByCategory(category)).thenReturn(cards);
        
        // Act
        Optional<Card> bestCard = cardService.findBestCardForCategory(category);
        
        // Assert
        assertTrue(bestCard.isPresent());
        assertEquals(15.0, bestCard.get().getCashback());
        verify(cardDao).findByCategory(category);
    }
    
    @Test
    void testFindBestCardForCategory_NoCards() {
        // Arrange
        String category = "Test Category";
        when(cardDao.findByCategory(category)).thenReturn(List.of());
        
        // Act
        Optional<Card> bestCard = cardService.findBestCardForCategory(category);
        
        // Assert
        assertTrue(bestCard.isEmpty());
        verify(cardDao).findByCategory(category);
    }
} 