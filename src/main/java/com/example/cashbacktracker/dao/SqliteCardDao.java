package com.example.cashbacktracker.dao;

import com.example.cashbacktracker.model.Card;
import com.example.cashbacktracker.model.CardHistory;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация CardDao для хранения данных в SQLite базе данных.
 * Использует JDBC для работы с базой данных.
 */
public class SqliteCardDao implements CardDao {
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS cards (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                bank_name TEXT NOT NULL,
                card_name TEXT NOT NULL,
                category TEXT NOT NULL,
                cashback REAL NOT NULL,
                category_change_date TEXT NOT NULL,
                is_active INTEGER NOT NULL
            )
            """;
    private static final String CREATE_HISTORY_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS card_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                card_id INTEGER NOT NULL,
                category TEXT NOT NULL,
                cashback_percentage REAL NOT NULL,
                change_date TEXT NOT NULL,
                record_date TEXT NOT NULL,
                FOREIGN KEY (card_id) REFERENCES cards(id)
            )
            """;
    private static final String INSERT_SQL = """
            INSERT INTO cards (bank_name, card_name, category, cashback, category_change_date, is_active)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE cards
            SET bank_name = ?, card_name = ?, category = ?, cashback = ?, category_change_date = ?, is_active = ?
            WHERE id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM cards WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM cards";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM cards WHERE id = ?";

    private final String dbUrl;
    
    /**
     * Создает новый экземпляр SqliteCardDao.
     *
     * @param dbPath путь к файлу базы данных SQLite
     */
    public SqliteCardDao(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initializeDatabase();
    }
    
    /**
     * Инициализирует базу данных, создавая необходимые таблицы.
     */
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
            stmt.execute(CREATE_HISTORY_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка инициализации базы данных", e);
        }
    }
    
    /**
     * Сохраняет новую карту в базу данных и создает запись в истории.
     *
     * @param card карта для сохранения
     */
    @Override
    public void saveCard(Card card) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, card.getBankName());
            pstmt.setString(2, card.getCardName());
            pstmt.setString(3, card.getCategory());
            pstmt.setDouble(4, card.getCashback());
            pstmt.setString(5, card.getCategoryChangeDate().toString());
            pstmt.setInt(6, card.isActive() ? 1 : 0);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    card.setId(generatedKeys.getLong(1));
                    CardHistory history = new CardHistory();
                    history.setCardId(card.getId());
                    history.setCategory(card.getCategory());
                    history.setCashbackPercentage(card.getCashback());
                    history.setChangeDate(card.getCategoryChangeDate());
                    history.setRecordDate(LocalDateTime.now());
                    saveHistory(history);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения карты", e);
        }
    }
    
    /**
     * Обновляет информацию о карте в базе данных.
     * Если изменилась категория или размер кэшбэка, создает новую запись в истории.
     *
     * @param card карта с обновленными данными
     */
    @Override
    public void updateCard(Card card) {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            Optional<Card> oldCard = getCardById(card.getId());
            if (oldCard.isPresent()) {
                Card old = oldCard.get();
                if (!old.getCategory().equals(card.getCategory()) || 
                    old.getCashback() != card.getCashback()) {
                    CardHistory history = new CardHistory();
                    history.setCardId(card.getId());
                    history.setCategory(card.getCategory());
                    history.setCashbackPercentage(card.getCashback());
                    history.setChangeDate(card.getCategoryChangeDate());
                    history.setRecordDate(LocalDateTime.now());
                    saveHistory(history);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {
                pstmt.setString(1, card.getBankName());
                pstmt.setString(2, card.getCardName());
                pstmt.setString(3, card.getCategory());
                pstmt.setDouble(4, card.getCashback());
                pstmt.setString(5, card.getCategoryChangeDate().toString());
                pstmt.setInt(6, card.isActive() ? 1 : 0);
                pstmt.setLong(7, card.getId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления карты", e);
        }
    }
    
    /**
     * Удаляет карту из базы данных.
     *
     * @param id идентификатор карты для удаления
     */
    @Override
    public void deleteCard(Long id) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(DELETE_SQL)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления карты", e);
        }
    }
    
    /**
     * Получает список всех карт из базы данных.
     *
     * @return список всех карт
     */
    @Override
    public List<Card> getAllCards() {
        List<Card> cards = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                cards.add(extractCardFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения списка карт", e);
        }
        return cards;
    }
    
    /**
     * Находит карту по её идентификатору.
     *
     * @param id идентификатор карты
     * @return Optional, содержащий карту, если она найдена
     */
    @Override
    public Optional<Card> getCardById(Long id) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractCardFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения карты по ID", e);
        }
        return Optional.empty();
    }
    
    /**
     * Сохраняет запись в истории изменений.
     *
     * @param history запись истории для сохранения
     */
    @Override
    public void saveHistory(CardHistory history) {
        String sql = """
            INSERT INTO card_history (card_id, category, cashback_percentage, change_date, record_date)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, history.getCardId());
            pstmt.setString(2, history.getCategory());
            pstmt.setDouble(3, history.getCashbackPercentage());
            pstmt.setString(4, history.getChangeDate().toString());
            pstmt.setString(5, history.getRecordDate().toString());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении истории", e);
        }
    }
    
    /**
     * Получает историю изменений для конкретной карты.
     *
     * @param cardId идентификатор карты
     * @return список записей истории изменений
     */
    @Override
    public List<CardHistory> findHistoryByCardId(Long cardId) {
        String sql = "SELECT * FROM card_history WHERE card_id = ? ORDER BY change_date DESC";
        List<CardHistory> history = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, cardId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(mapHistoryFromResultSet(rs));
                }
            }
            
            return history;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении истории карты", e);
        }
    }
    
    /**
     * Находит все карты с указанной категорией кэшбэка.
     *
     * @param category категория кэшбэка
     * @return список карт с указанной категорией
     */
    @Override
    public List<Card> findByCategory(String category) {
        String sql = "SELECT * FROM cards WHERE category = ?";
        List<Card> cards = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(extractCardFromResultSet(rs));
                }
            }
            
            return cards;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске карт по категории", e);
        }
    }
    
    /**
     * Находит все карты с истекающей категорией кэшбэка.
     *
     * @param date дата истечения категории
     * @return список карт с истекающей категорией
     */
    @Override
    public List<Card> findByExpiringCategory(LocalDate date) {
        String sql = "SELECT * FROM cards WHERE category_change_date <= ? AND is_active = 1";
        List<Card> cards = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, date.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(extractCardFromResultSet(rs));
                }
            }
            
            return cards;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске истекающих карт", e);
        }
    }
    
    /**
     * Извлекает данные карты из ResultSet.
     *
     * @param rs ResultSet с данными карты
     * @return объект Card с данными из ResultSet
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    private Card extractCardFromResultSet(ResultSet rs) throws SQLException {
        Card card = new Card();
        card.setId(rs.getLong("id"));
        card.setBankName(rs.getString("bank_name"));
        card.setCardName(rs.getString("card_name"));
        card.setCategory(rs.getString("category"));
        card.setCashback(rs.getDouble("cashback"));
        card.setCategoryChangeDate(LocalDate.parse(rs.getString("category_change_date")));
        card.setActive(rs.getInt("is_active") == 1);
        return card;
    }
    
    /**
     * Извлекает данные записи истории из ResultSet.
     *
     * @param rs ResultSet с данными истории
     * @return объект CardHistory с данными из ResultSet
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    private CardHistory mapHistoryFromResultSet(ResultSet rs) throws SQLException {
        CardHistory history = new CardHistory();
        history.setId(rs.getLong("id"));
        history.setCardId(rs.getLong("card_id"));
        history.setCategory(rs.getString("category"));
        history.setCashbackPercentage(rs.getDouble("cashback_percentage"));
        history.setChangeDate(LocalDate.parse(rs.getString("change_date")));
        history.setRecordDate(LocalDateTime.parse(rs.getString("record_date")));
        return history;
    }
} 