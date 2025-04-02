package com.antock.task.service;

import com.antock.task.controller.dto.TeleSalesSaveRequest;
import com.antock.task.controller.dto.TeleSalesSaveResponse;
import com.antock.task.service.externalapi.csv.CsvDownloadRequest;
import com.antock.task.service.externalapi.csv.CsvParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
@Service
@RequiredArgsConstructor
@Slf4j
public class TeleSalesInfoService {

    private final CsvParser csvParser;
    private final TeleSalesInfoSaveAsyncWorker teleSalesInfoSaveAsyncWorker;

    private static final int CHUNK_SIZE = 500;

    @Transactional
    public TeleSalesSaveResponse save(TeleSalesSaveRequest request) {
        List<CsvDownloadRequest> csvData = csvParser.parse(request);
        AtomicInteger savedCount = new AtomicInteger(0);

        List<List<CsvDownloadRequest>> chunks = chunkList(csvData, CHUNK_SIZE);

        for (List<CsvDownloadRequest> chunk : chunks) {
            List<CompletableFuture<Void>> futures = chunk.stream()
                    .map(datum -> teleSalesInfoSaveAsyncWorker
                            .saveOne(datum, savedCount)
                            .exceptionally(ex -> {
                                log.error("비동기 처리 중 예외 발생: {}", ex.getMessage(), ex);
                                return null;
                            }))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // chunk 단위로 wait
        }

        int failedCount = csvData.size() - savedCount.get();
        return new TeleSalesSaveResponse(savedCount.get(), failedCount);
    }

    private <T> List<List<T>> chunkList(List<T> list, int size) {
        int totalSize = list.size();
        int fullChunks = (int) Math.ceil((double) totalSize / size);

        List<List<T>> result = new java.util.ArrayList<>(fullChunks);
        for (int i = 0; i < totalSize; i += size) {
            result.add(list.subList(i, Math.min(totalSize, i + size)));
        }
        return result;
    }

}
