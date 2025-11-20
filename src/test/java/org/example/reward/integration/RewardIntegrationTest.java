package org.example.reward.integration;

import io.r2dbc.spi.ConnectionFactory;
import org.example.reward.entity.Employee;
import org.example.reward.entity.Reward;
import org.example.reward.repository.EmployeeRepository;
import org.example.reward.repository.RewardRepository;
import org.example.reward.service.CsvParserService;
import org.example.reward.service.RewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class RewardIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> 
            String.format("r2dbc:postgresql://%s:%d/%s", 
                postgres.getHost(), postgres.getFirstMappedPort(), postgres.getDatabaseName()));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
    }
    
    @Autowired
    private CsvParserService csvParserService;
    
    @Autowired
    private RewardService rewardService;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private RewardRepository rewardRepository;
    
    @Autowired
    private ConnectionFactory connectionFactory;
    
    @BeforeEach
    void setUp() {
        DatabaseClient databaseClient = DatabaseClient.create(connectionFactory);
        
        // Создание схемы БД (PostgreSQL синтаксис)
        databaseClient.sql("""
            CREATE TABLE IF NOT EXISTS employees (
                id BIGSERIAL PRIMARY KEY,
                full_name VARCHAR(255) NOT NULL
            )
            """).fetch().rowsUpdated().block();
        
        databaseClient.sql("""
            CREATE TABLE IF NOT EXISTS rewards (
                id BIGSERIAL PRIMARY KEY,
                employee_id BIGINT NOT NULL,
                reward_id BIGINT NOT NULL,
                reward_name VARCHAR(255) NOT NULL,
                received_date TIMESTAMP NOT NULL,
                FOREIGN KEY (employee_id) REFERENCES employees(id)
            )
            """).fetch().rowsUpdated().block();
        
        // Очистка данных перед каждым тестом (сначала награды, потом сотрудники)
        databaseClient.sql("DELETE FROM rewards").fetch().rowsUpdated().block();
        databaseClient.sql("DELETE FROM employees").fetch().rowsUpdated().block();
        
        // Создание тестовых сотрудников
        Employee employee1 = new Employee(null, "Иванов Иван Иванович");
        Employee employee2 = new Employee(null, "Петров Петр Петрович");
        
        employeeRepository.saveAll(Flux.just(employee1, employee2))
            .collectList()
            .block();
    }
    
    @Test
    void testFullFlow_ValidRecords_SavesOnlyExistingEmployees() {
        // Получаем ID созданных сотрудников
        Employee emp1 = employeeRepository.findAll().blockFirst();
        Employee emp2 = employeeRepository.findAll().skip(1).blockFirst();
        
        Long emp1Id = emp1 != null ? emp1.getId() : 1L;
        Long emp2Id = emp2 != null ? emp2.getId() : 2L;
        
        String csvContent = String.format("""
            employeeId,employeeFullName,rewardId,rewardName,receivedDate
            %d,Иванов Иван Иванович,100,Лучший сотрудник,2024-01-15T10:30:00
            %d,Петров Петр Петрович,101,За отличную работу,2024-02-20T14:45:00
            999,Несуществующий Сотрудник,102,Награда,2024-03-01T12:00:00
            """, emp1Id, emp2Id);
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            csvContent.getBytes(StandardCharsets.UTF_8)
        );
        
        StepVerifier.create(
            csvParserService.parseCsv(inputStream)
                .collectList()
                .flatMap(records -> rewardService.processRewards(Flux.fromIterable(records)))
        )
            .assertNext(response -> {
                assertEquals(3, response.totalRecords());
                assertEquals(2, response.savedRecords());
                assertEquals(1, response.skippedRecords());
            })
            .verifyComplete();
        
        // Проверяем, что сохранены только награды для существующих сотрудников
        StepVerifier.create(rewardRepository.findAll().collectList())
            .assertNext(rewards -> {
                assertEquals(2, rewards.size(), "Должно быть сохранено 2 награды");
                // Проверяем, что награды соответствуют ожидаемым сотрудникам
                boolean hasEmp1Reward = rewards.stream()
                    .anyMatch(r -> r.getEmployeeId().equals(emp1Id) && r.getRewardId().equals(100L));
                boolean hasEmp2Reward = rewards.stream()
                    .anyMatch(r -> r.getEmployeeId().equals(emp2Id) && r.getRewardId().equals(101L));
                assertTrue(hasEmp1Reward, "Должна быть награда для сотрудника 1");
                assertTrue(hasEmp2Reward, "Должна быть награда для сотрудника 2");
            })
            .verifyComplete();
    }
}

