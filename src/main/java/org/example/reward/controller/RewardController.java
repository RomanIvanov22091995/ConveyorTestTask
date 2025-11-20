package org.example.reward.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reward.dto.RewardUploadResponse;
import org.example.reward.exception.InvalidFileFormatException;
import org.example.reward.exception.InvalidRecordException;
import org.example.reward.service.CsvParserService;
import org.example.reward.service.RewardService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {
    
    private final CsvParserService csvParserService;
    private final RewardService rewardService;
    
    /**
     * Загружает CSV файл с наградами сотрудников.
     * 
     * @param file загружаемый CSV файл
     * @return Mono<RewardUploadResponse> результат обработки
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<RewardUploadResponse> uploadRewards(@RequestPart("file") FilePart file) {
        log.info("Получен запрос на загрузку файла: {}", file.filename());
        
        if (!isCsvFile(file.filename())) {
            return Mono.error(new InvalidFileFormatException("Поддерживаются только CSV файлы"));
        }
        
        return file.content()
            .collectList()
            .map(dataBuffers -> {
                int totalSize = dataBuffers.stream()
                    .mapToInt(org.springframework.core.io.buffer.DataBuffer::readableByteCount)
                    .sum();
                
                byte[] allBytes = new byte[totalSize];
                int offset = 0;
                
                for (org.springframework.core.io.buffer.DataBuffer buffer : dataBuffers) {
                    int readableBytes = buffer.readableByteCount();
                    buffer.read(allBytes, offset, readableBytes);
                    DataBufferUtils.release(buffer);
                    offset += readableBytes;
                }
                
                log.debug("Прочитано {} байт из файла {}", totalSize, file.filename());
                return new java.io.ByteArrayInputStream(allBytes);
            })
            .flatMap(inputStream -> 
                csvParserService.parseCsv(inputStream)
                    .collectList()
                    .flatMap(records -> rewardService.processRewards(Flux.fromIterable(records)))
            )
            .onErrorResume(InvalidFileFormatException.class, e -> {
                log.error("Ошибка формата файла: {}", e.getMessage());
                return Mono.error(e);
            })
            .onErrorResume(InvalidRecordException.class, e -> {
                log.error("Ошибка в записи файла: {}", e.getMessage());
                return Mono.error(e);
            })
            .onErrorResume(Exception.class, e -> {
                log.error("Неожиданная ошибка при обработке файла", e);
                return Mono.error(new RuntimeException("Ошибка при обработке файла: " + e.getMessage(), e));
            });
    }
    
    private boolean isCsvFile(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }
}

