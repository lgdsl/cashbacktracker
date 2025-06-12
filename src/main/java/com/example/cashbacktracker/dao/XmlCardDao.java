package com.example.cashbacktracker.dao;

import com.example.cashbacktracker.model.Card;
import com.example.cashbacktracker.model.CardHistory;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Реализация CardDao для хранения данных в XML формате.
 * Использует Jackson XML для сериализации/десериализации данных.
 */
public class XmlCardDao implements CardDao {
    private final File file;
    private final File historyFile;
    private final XmlMapper mapper;
    private final AtomicLong nextId;
    private List<Card> cards;
    private List<CardHistory> history;
    
    /**
     * Создает новый экземпляр XmlCardDao.
     *
     * @param filePath путь к файлу для хранения данных карт
     */
    public XmlCardDao(String filePath) {
        this.file = new File(filePath);
        this.historyFile = new File(file.getParent() + "/card_history.xml");
        this.mapper = new XmlMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.nextId = new AtomicLong(1);
        loadData();
        loadHistory();
    }
    
    /**
     * Загружает данные карт из XML файла.
     * Если файл не существует или пуст, создает новый файл с пустым списком карт.
     */
    private void loadData() {
        if (file.exists() && file.length() > 0) {
            try {
                cards = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Card.class));
                if (cards == null) {
                    cards = new ArrayList<>();
                }
                long maxId = cards.stream()
                        .mapToLong(Card::getId)
                        .max()
                        .orElse(0);
                nextId.set(maxId + 1);
            } catch (IOException e) {
                cards = new ArrayList<>();
                try {
                    mapper.writeValue(file, cards);
                } catch (IOException ex) {
                    throw new RuntimeException("Ошибка при создании нового XML файла", ex);
                }
            }
        } else {
            cards = new ArrayList<>();
            try {
                mapper.writeValue(file, cards);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при создании нового XML файла", e);
            }
        }
    }
    
    /**
     * Загружает историю изменений из XML файла.
     * Если файл не существует или пуст, создает новый файл с пустым списком истории.
     */
    private void loadHistory() {
        if (historyFile.exists() && historyFile.length() > 0) {
            try {
                history = mapper.readValue(historyFile, mapper.getTypeFactory().constructCollectionType(List.class, CardHistory.class));
                if (history == null) {
                    history = new ArrayList<>();
                }
            } catch (IOException e) {
                history = new ArrayList<>();
                try {
                    mapper.writeValue(historyFile, history);
                } catch (IOException ex) {
                    throw new RuntimeException("Ошибка при создании нового XML файла истории", ex);
                }
            }
        } else {
            history = new ArrayList<>();
            try {
                mapper.writeValue(historyFile, history);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при создании нового XML файла истории", e);
            }
        }
    }
    
    /**
     * Сохраняет данные карт в XML файл.
     */
    private void saveData() {
        try {
            mapper.writeValue(file, cards);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении данных в XML", e);
        }
    }
    
    /**
     * Сохраняет историю изменений в XML файл.
     */
    private void saveHistory() {
        try {
            mapper.writeValue(historyFile, history);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении истории в XML", e);
        }
    }
    
    /**
     * Сохраняет новую карту в хранилище и создает запись в истории.
     *
     * @param card карта для сохранения
     */
    @Override
    public void saveCard(Card card) {
        card.setId(nextId.getAndIncrement());
        cards.add(card);
        saveData();
        CardHistory historyRecord = new CardHistory();
        historyRecord.setCardId(card.getId());
        historyRecord.setCategory(card.getCategory());
        historyRecord.setCashbackPercentage(card.getCashback());
        historyRecord.setChangeDate(card.getCategoryChangeDate());
        historyRecord.setRecordDate(LocalDateTime.now());
        saveHistory(historyRecord);
    }
    
    /**
     * Обновляет информацию о карте в хранилище.
     * Если изменилась категория или размер кэшбэка, создает новую запись в истории.
     *
     * @param card карта с обновленными данными
     */
    @Override
    public void updateCard(Card card) {
        Optional<Card> oldCardOpt = getCardById(card.getId());
        if (oldCardOpt.isPresent()) {
            Card old = oldCardOpt.get();
            if (!old.getCategory().equals(card.getCategory()) || old.getCashback() != card.getCashback()) {
                CardHistory historyRecord = new CardHistory();
                historyRecord.setCardId(card.getId());
                historyRecord.setCategory(card.getCategory());
                historyRecord.setCashbackPercentage(card.getCashback());
                historyRecord.setChangeDate(card.getCategoryChangeDate());
                historyRecord.setRecordDate(LocalDateTime.now());
                saveHistory(historyRecord);
            }
        }
        cards.removeIf(c -> c.getId().equals(card.getId()));
        cards.add(card);
        saveData();
    }
    
    /**
     * Удаляет карту из хранилища.
     *
     * @param id идентификатор карты для удаления
     */
    @Override
    public void deleteCard(Long id) {
        cards.removeIf(card -> card.getId().equals(id));
        saveData();
    }
    
    /**
     * Получает список всех карт из хранилища.
     *
     * @return список всех карт
     */
    @Override
    public List<Card> getAllCards() {
        return new ArrayList<>(cards);
    }
    
    /**
     * Находит карту по её идентификатору.
     *
     * @param id идентификатор карты
     * @return Optional, содержащий карту, если она найдена
     */
    @Override
    public Optional<Card> getCardById(Long id) {
        return cards.stream()
                .filter(card -> card.getId().equals(id))
                .findFirst();
    }
    
    /**
     * Находит все карты с указанной категорией кэшбэка.
     *
     * @param category категория кэшбэка
     * @return список карт с указанной категорией
     */
    @Override
    public List<Card> findByCategory(String category) {
        return cards.stream()
                .filter(card -> card.getCategory().equalsIgnoreCase(category))
                .toList();
    }
    
    /**
     * Находит все карты с истекающей категорией кэшбэка.
     *
     * @param date дата истечения категории
     * @return список карт с истекающей категорией
     */
    @Override
    public List<Card> findByExpiringCategory(LocalDate date) {
        return cards.stream()
                .filter(card -> card.getCategoryChangeDate().isBefore(date) && card.isActive())
                .toList();
    }
    
    /**
     * Сохраняет запись в истории изменений.
     *
     * @param history запись истории для сохранения
     */
    @Override
    public void saveHistory(CardHistory history) {
        this.history.add(history);
        saveHistory();
    }
    
    /**
     * Получает историю изменений для конкретной карты.
     *
     * @param cardId идентификатор карты
     * @return список записей истории изменений
     */
    @Override
    public List<CardHistory> findHistoryByCardId(Long cardId) {
        return history.stream()
                .filter(h -> h.getCardId().equals(cardId))
                .toList();
    }
} 