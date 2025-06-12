package com.example.cashbacktracker.service;

import com.example.cashbacktracker.dao.CardDao;
import com.example.cashbacktracker.dao.CardDaoFactory;
import com.example.cashbacktracker.dao.CardDaoFactory.StorageType;
import com.example.cashbacktracker.model.Card;
import com.example.cashbacktracker.model.CardHistory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Сервисный класс для работы с банковскими картами.
 * Предоставляет бизнес-логику для управления картами и их историей.
 */
public class CardService {
    private CardDao cardDao;
    private CardDaoFactory cardDaoFactory;
    
    /**
     * Создает новый экземпляр сервиса с хранилищем SQLite по умолчанию.
     */
    public CardService() {
        this.cardDaoFactory = new CardDaoFactory();
        this.cardDao = cardDaoFactory.createDao(StorageType.SQLITE);
    }
    
    /**
     * Создает новый экземпляр сервиса с указанным DAO объектом.
     * Используется для тестирования.
     *
     * @param cardDao DAO объект для работы с хранилищем
     */
    public CardService(CardDao cardDao) {
        this.cardDao = cardDao;
    }
    
    /**
     * Переключает тип хранилища данных.
     *
     * @param type тип хранилища для переключения
     */
    public void switchStorage(StorageType type) {
        if (cardDaoFactory != null) {
            this.cardDao = cardDaoFactory.createDao(type);
        }
    }
    
    /**
     * Добавляет новую карту в хранилище.
     *
     * @param card карта для добавления
     */
    public void addCard(Card card) {
        cardDao.saveCard(card);
    }
    
    /**
     * Получает список всех карт из хранилища.
     *
     * @return список всех карт
     */
    public List<Card> getAllCards() {
        return cardDao.getAllCards();
    }
    
    /**
     * Обновляет информацию о существующей карте.
     *
     * @param card карта с обновленными данными
     */
    public void updateCard(Card card) {
        cardDao.updateCard(card);
    }

    /**
     * Удаляет карту из хранилища.
     *
     * @param id идентификатор карты для удаления
     */
    public void deleteCard(Long id) {
        cardDao.deleteCard(id);
    }

    /**
     * Получает список карт с истекающей категорией кэшбэка.
     *
     * @param date дата истечения категории
     * @return список карт с истекающей категорией
     */
    public List<Card> getExpiringCards(LocalDate date) {
        return cardDao.findByExpiringCategory(date);
    }
    
    /**
     * Получает историю изменений для конкретной карты.
     *
     * @param cardId идентификатор карты
     * @return список записей истории изменений
     */
    public List<CardHistory> getCardHistory(Long cardId) {
        return cardDao.findHistoryByCardId(cardId);
    }
    
    /**
     * Находит лучшую карту для указанной категории кэшбэка.
     * Возвращает карту с максимальным процентом кэшбэка среди активных карт.
     *
     * @param category категория кэшбэка
     * @return Optional, содержащий карту с максимальным кэшбэком, если такая найдена
     */
    public Optional<Card> findBestCardForCategory(String category) {
        List<Card> cards = cardDao.findByCategory(category);
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        
        return cards.stream()
                .filter(Card::isActive)
                .max((c1, c2) -> Double.compare(c1.getCashback(), c2.getCashback()));
    }
} 