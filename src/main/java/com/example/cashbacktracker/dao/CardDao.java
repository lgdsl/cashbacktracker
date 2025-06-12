package com.example.cashbacktracker.dao;

import com.example.cashbacktracker.model.Card;
import com.example.cashbacktracker.model.CardHistory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с хранилищем данных карт и их истории.
 * Определяет основные операции для работы с картами и историей их изменений.
 */
public interface CardDao {

    /**
     * Сохраняет новую карту в хранилище.
     *
     * @param card карта для сохранения
     */
    void saveCard(Card card);

    /**
     * Обновляет информацию о существующей карте.
     *
     * @param card карта с обновленными данными
     */
    void updateCard(Card card);

    /**
     * Удаляет карту из хранилища по её идентификатору.
     *
     * @param id идентификатор карты для удаления
     */
    void deleteCard(Long id);

    /**
     * Получает список всех карт из хранилища.
     *
     * @return список всех карт
     */
    List<Card> getAllCards();

    /**
     * Находит карту по её идентификатору.
     *
     * @param id идентификатор карты
     * @return Optional, содержащий карту, если она найдена
     */
    Optional<Card> getCardById(Long id);

    /**
     * Сохраняет запись в истории изменений карты.
     *
     * @param history запись истории для сохранения
     */
    void saveHistory(CardHistory history);

    /**
     * Получает историю изменений для конкретной карты.
     *
     * @param cardId идентификатор карты
     * @return список записей истории изменений
     */
    List<CardHistory> findHistoryByCardId(Long cardId);

    /**
     * Находит все карты с указанной категорией кэшбэка.
     *
     * @param category категория кэшбэка
     * @return список карт с указанной категорией
     */
    List<Card> findByCategory(String category);

    /**
     * Находит все карты, у которых категория кэшбэка истекает на указанную дату.
     *
     * @param date дата истечения категории
     * @return список карт с истекающей категорией
     */
    List<Card> findByExpiringCategory(LocalDate date);
}