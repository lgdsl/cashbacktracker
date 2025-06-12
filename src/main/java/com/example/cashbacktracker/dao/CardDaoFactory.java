package com.example.cashbacktracker.dao;

/**
 * Фабричный класс для создания экземпляров CardDao.
 * Предоставляет методы для создания DAO объектов различных типов хранилищ.
 */
public class CardDaoFactory {
    private static final String DATA_DIR = "data";
    private static final String JSON_DIR = DATA_DIR + "/JSON";
    private static final String XML_DIR = DATA_DIR + "/XML";
    private static final String SQLITE_DIR = DATA_DIR + "/SQLite";
    
    private static final String JSON_FILE_PATH = JSON_DIR + "/cards.json";
    private static final String XML_FILE_PATH = XML_DIR + "/cards.xml";
    private static final String SQLITE_FILE_PATH = SQLITE_DIR + "/cashback.db";
    
    /**
     * Перечисление доступных типов хранилищ данных.
     */
    public enum StorageType {
        SQLITE,
        JSON,
        XML
    }
    
    /**
     * Создает экземпляр CardDao указанного типа.
     *
     * @param type тип хранилища данных
     * @return экземпляр CardDao соответствующего типа
     */
    public static CardDao createDao(StorageType type) {
        return switch (type) {
            case SQLITE -> new SqliteCardDao(SQLITE_FILE_PATH);
            case JSON -> new JsonCardDao(JSON_FILE_PATH);
            case XML -> new XmlCardDao(XML_FILE_PATH);
        };
    }
}