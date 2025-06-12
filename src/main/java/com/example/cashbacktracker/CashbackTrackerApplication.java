package com.example.cashbacktracker;

import com.example.cashbacktracker.controller.MainController;
import com.example.cashbacktracker.service.CardService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Главный класс приложения для отслеживания кэшбэка по банковским картам.
 * Отвечает за инициализацию и запуск JavaFX приложения.
 */
public class CashbackTrackerApplication extends Application {
    /**
     * Метод инициализации и запуска JavaFX приложения.
     * Загружает главное окно приложения и настраивает необходимые зависимости.
     *
     * @param stage Основное окно приложения
     * @throws Exception если произошла ошибка при загрузке FXML или инициализации приложения
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        
        MainController controller = fxmlLoader.getController();
        CardService cardService = new CardService();
        controller.setCardService(cardService);
        
        stage.setTitle("Трекер кэшбэка");
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Точка входа в приложение.
     * Запускает JavaFX приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch();
    }
} 