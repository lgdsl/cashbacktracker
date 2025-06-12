package com.example.cashbacktracker.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Класс, представляющий историю изменений кэшбэка по карте.
 * Содержит информацию о предыдущих категориях кэшбэка и их размерах.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardHistory {
    private Long id;
    private Long cardId;
    private String category;
    private Double cashbackPercentage;
    private LocalDate changeDate;
    private LocalDateTime recordDate;

    /**
     * Создает новую запись в истории изменений кэшбэка.
     *
     * @param cardId идентификатор карты
     * @param category категория кэшбэка
     * @param cashbackPercentage размер кэшбэка в процентах
     * @param changeDate дата изменения категории
     */
    public CardHistory(Long cardId, String category, Double cashbackPercentage, LocalDate changeDate) {
        this.cardId = cardId;
        this.category = category;
        this.cashbackPercentage = cashbackPercentage;
        this.changeDate = changeDate;
        this.recordDate = LocalDateTime.now();
    }
} 