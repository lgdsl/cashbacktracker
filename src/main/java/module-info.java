/**
 * Модуль приложения для отслеживания кэшбэка по банковским картам.
 * 
 * Этот модуль определяет зависимости и экспорты для приложения CashbackTracker.
 * Он включает в себя:
 * - Зависимости от JavaFX для графического интерфейса
 * - Зависимости от Jackson для работы с JSON и XML
 * - Зависимости от SQL для работы с базой данных
 * - Зависимость от Lombok для уменьшения шаблонного кода
 * 
 * Модуль экспортирует следующие пакеты:
 * - com.example.cashbacktracker - основной пакет приложения
 * - com.example.cashbacktracker.controller - контроллеры JavaFX
 * - com.example.cashbacktracker.model - модели данных
 * - com.example.cashbacktracker.service - сервисный слой
 */
module com.example.cashbacktracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires static lombok;
    
    opens com.example.cashbacktracker to javafx.fxml;
    opens com.example.cashbacktracker.controller to javafx.fxml;
    opens com.example.cashbacktracker.model to com.fasterxml.jackson.databind;
    exports com.example.cashbacktracker;
    exports com.example.cashbacktracker.controller;
    exports com.example.cashbacktracker.model;
    exports com.example.cashbacktracker.service;
}