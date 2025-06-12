package com.example.cashbacktracker.dao;

import com.example.cashbacktracker.model.Card;
import com.example.cashbacktracker.model.CardHistory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class TestCardDao implements CardDao {
    private final List<Card> cards = new ArrayList<>();
    private final List<CardHistory> history = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public void saveCard(Card card) {
        card.setId(nextId.getAndIncrement());
        cards.add(card);
        saveHistory(card);
    }

    @Override
    public void updateCard(Card card) {
        Optional<Card> oldCard = getCardById(card.getId());
        if (oldCard.isPresent()) {
            Card old = oldCard.get();
            if (!old.getCategory().equals(card.getCategory()) || old.getCashback() != card.getCashback()) {
                saveHistory(card);
            }
        }
        cards.removeIf(c -> c.getId().equals(card.getId()));
        cards.add(card);
    }

    @Override
    public void deleteCard(Long id) {
        cards.removeIf(card -> card.getId().equals(id));
    }

    @Override
    public List<Card> getAllCards() {
        return new ArrayList<>(cards);
    }

    @Override
    public Optional<Card> getCardById(Long id) {
        return cards.stream()
                .filter(card -> card.getId().equals(id))
                .findFirst();
    }

    @Override
    public void saveHistory(CardHistory history) {
        this.history.add(history);
    }

    @Override
    public List<CardHistory> findHistoryByCardId(Long cardId) {
        return history.stream()
                .filter(h -> h.getCardId().equals(cardId))
                .toList();
    }

    @Override
    public List<Card> findByCategory(String category) {
        return cards.stream()
                .filter(card -> card.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    @Override
    public List<Card> findByExpiringCategory(LocalDate date) {
        return cards.stream()
                .filter(card -> card.getCategoryChangeDate().isBefore(date) && card.isActive())
                .toList();
    }

    private void saveHistory(Card card) {
        CardHistory historyRecord = new CardHistory();
        historyRecord.setCardId(card.getId());
        historyRecord.setCategory(card.getCategory());
        historyRecord.setCashbackPercentage(card.getCashback());
        historyRecord.setChangeDate(card.getCategoryChangeDate());
        historyRecord.setRecordDate(LocalDateTime.now());
        history.add(historyRecord);
    }
} 