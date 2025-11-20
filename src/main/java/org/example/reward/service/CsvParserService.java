package org.example.reward.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.example.reward.dto.RewardRecord;
import org.example.reward.exception.InvalidFileFormatException;
import org.example.reward.exception.InvalidRecordException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


@Service
public class CsvParserService {
    
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    private static final int EXPECTED_COLUMNS = 5;
    
    /**
     * Парсит CSV файл и возвращает поток записей о наградах.
     * 
     * Формат CSV: employeeId, employeeFullName, rewardId, rewardName, receivedDate
     * 
     * @param inputStream поток данных CSV файла
     * @return Flux<RewardRecord> поток записей о наградах
     */
    public Flux<RewardRecord> parseCsv(InputStream inputStream) {
        return Flux.defer(() -> {
            CSVReader reader = null;
            try {
                reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                List<String[]> allRecords = reader.readAll();
                
                if (allRecords.isEmpty()) {
                    return Flux.error(new InvalidFileFormatException("CSV файл пуст"));
                }
                
                // Проверяем первую строку (заголовок)
                if (allRecords.get(0).length != EXPECTED_COLUMNS) {
                    return Flux.error(new InvalidFileFormatException(
                        String.format("Неверный формат заголовка: ожидалось %d колонок, получено %d. " +
                            "Проверьте, что файл использует запятую как разделитель и кодировку UTF-8.",
                            EXPECTED_COLUMNS, allRecords.get(0).length)
                    ));
                }
                
                return Flux.fromIterable(allRecords)
                    .skip(1) // Пропускаем заголовок
                    .filter(record -> record.length > 0 && !(record.length == 1 && record[0].trim().isEmpty())) // Пропускаем пустые строки
                    .flatMap(record -> {
                        try {
                            return Flux.just(parseRecord(record));
                        } catch (InvalidRecordException e) {
                            return Flux.error(e);
                        } catch (Exception e) {
                            return Flux.error(new InvalidRecordException("Ошибка при парсинге записи", e));
                        }
                    });
            } catch (Exception e) {
                return Flux.error(new InvalidFileFormatException("Не удалось прочитать CSV файл: " + e.getMessage(), e));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        // Игнорируем ошибки при закрытии
                    }
                }
            }
        });
    }
    
    /**
     * Парсит одну запись из CSV.
     */
    private RewardRecord parseRecord(String[] record) {
        if (record.length != EXPECTED_COLUMNS) {
            throw new InvalidRecordException(
                String.format("Неверное количество колонок: ожидалось %d, получено %d", EXPECTED_COLUMNS, record.length)
            );
        }
        
        try {
            Long employeeId = parseLong(record[0], "employeeId");
            String employeeFullName = record[1].trim();
            Long rewardId = parseLong(record[2], "rewardId");
            String rewardName = record[3].trim();
            LocalDateTime receivedDate = parseDateTime(record[4]);
            
            validateRecord(employeeId, employeeFullName, rewardId, rewardName);
            
            return new RewardRecord(employeeId, employeeFullName, rewardId, rewardName, receivedDate);
        } catch (InvalidRecordException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidRecordException("Ошибка при парсинге записи: " + String.join(",", record), e);
        }
    }
    
    private Long parseLong(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidRecordException(String.format("Поле %s не может быть пустым", fieldName));
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidRecordException(String.format("Неверный формат поля %s: %s", fieldName, value), e);
        }
    }
    
    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidRecordException("Поле receivedDate не может быть пустым");
        }
        try {
            return LocalDateTime.parse(value.trim(), ISO_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidRecordException(
                String.format("Неверный формат даты (ожидается ISO-8601): %s", value), e
            );
        }
    }
    
    private void validateRecord(Long employeeId, String employeeFullName, Long rewardId, String rewardName) {
        if (employeeId == null || employeeId <= 0) {
            throw new InvalidRecordException("employeeId должен быть положительным числом");
        }
        if (employeeFullName == null || employeeFullName.trim().isEmpty()) {
            throw new InvalidRecordException("employeeFullName не может быть пустым");
        }
        if (rewardId == null || rewardId <= 0) {
            throw new InvalidRecordException("rewardId должен быть положительным числом");
        }
        if (rewardName == null || rewardName.trim().isEmpty()) {
            throw new InvalidRecordException("rewardName не может быть пустым");
        }
    }
}

