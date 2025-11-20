package org.example.reward.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reward.entity.Employee;
import org.example.reward.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

/**
 * Компонент для локальной инициализации и проверки данных и работы репозитория
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data.init.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {
    
    private final EmployeeRepository employeeRepository;
    
    @Override
    public void run(String... args) {
        log.info("Инициализация начальных данных...");
        
        List<Employee> initialEmployees = Arrays.asList(
            new Employee(null, "Иванов Иван Иванович"),
            new Employee(null, "Петров Петр Петрович"),
            new Employee(null, "Сидоров Сидор Сидорович"),
            new Employee(null, "Козлова Анна Сергеевна"),
            new Employee(null, "Смирнов Алексей Владимирович")
        );
        
        employeeRepository.count()
            .flatMap(count -> {
                if (count == 0) {
                    log.info("Таблица сотрудников пуста, добавляем начальные данные");
                    return employeeRepository.saveAll(Flux.fromIterable(initialEmployees))
                        .then();
                } else {
                    log.info("Таблица сотрудников уже содержит {} записей, пропускаем инициализацию", count);
                    return Flux.empty().then();
                }
            })
            .block();
        
        log.info("Инициализация данных завершена");
    }
}

