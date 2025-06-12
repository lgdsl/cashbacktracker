package com.example.cashbacktracker.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * Класс, представляющий банковскую карту с информацией о кэшбэке.
 * Содержит информацию о банке, названии карты, категории кэшбэка и его размере.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private Long id;
    private String bankName;
    private String cardName;
    private String category;
    private double cashback;
    private LocalDate categoryChangeDate;
    private CardStatus status;

    /**
     * Перечисление статусов карты.
     */
    public enum CardStatus {
        ACTIVE("Активен"),
        EXPIRED("Истёк");

        private final String displayName;

        CardStatus(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Возвращает отображаемое название статуса карты.
         *
         * @return строковое представление статуса
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Создает новый экземпляр карты с указанными параметрами.
     *
     * @param bankName название банка
     * @param cardName название карты
     * @param category категория кэшбэка
     * @param cashback размер кэшбэка в процентах
     * @param categoryChangeDate дата изменения категории
     * @param isActive статус активности карты
     */
    public Card(String bankName, String cardName, String category, double cashback, LocalDate categoryChangeDate, boolean isActive) {
        this.bankName = bankName;
        this.cardName = cardName;
        this.category = category;
        this.cashback = cashback;
        this.categoryChangeDate = categoryChangeDate;
        this.status = isActive ? CardStatus.ACTIVE : CardStatus.EXPIRED;
    }

    /**
     * Проверяет, активна ли карта.
     *
     * @return true если карта активна, false в противном случае
     */
    public boolean isActive() {
        return status == CardStatus.ACTIVE;
    }

    /**
     * Устанавливает статус активности карты.
     *
     * @param active true для активации карты, false для деактивации
     */
    public void setActive(boolean active) {
        this.status = active ? CardStatus.ACTIVE : CardStatus.EXPIRED;
    }
} 