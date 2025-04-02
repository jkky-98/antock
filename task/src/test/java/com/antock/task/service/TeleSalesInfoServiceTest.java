package com.antock.task.service;

import com.antock.task.controller.dto.TeleSalesSaveRequest;
import com.antock.task.controller.dto.TeleSalesSaveResponse;
import com.antock.task.service.externalapi.csv.CsvDownloadRequest;
import com.antock.task.service.externalapi.csv.CsvParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class TeleSalesInfoServiceTest {

    @Mock
    private CsvParser csvParser;

    @Mock
    private TeleSalesInfoSaveAsyncWorker teleSalesInfoSaveAsyncWorker;

    @InjectMocks
    private TeleSalesInfoService teleSalesInfoService;

    @Test
    @DisplayName("save() :: CSV 요청 파싱 후 정상 저장된 수를 반환한다")
    void save_ReturnsSuccessCount() {
        // given
        CsvDownloadRequest request1 = new CsvDownloadRequest();
        request1.setBusinessRegiNumber("1234567890");
        CsvDownloadRequest request2 = new CsvDownloadRequest();
        request2.setBusinessRegiNumber("2234567890");

        List<CsvDownloadRequest> csvList = List.of(request1, request2);

        when(csvParser.parse(any())).thenReturn(csvList);
        when(teleSalesInfoSaveAsyncWorker.saveOne(eq(request1), any())).thenAnswer(invocation -> {
            AtomicInteger count = invocation.getArgument(1);
            count.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        });
        when(teleSalesInfoSaveAsyncWorker.saveOne(eq(request2), any())).thenAnswer(invocation -> {
            AtomicInteger count = invocation.getArgument(1);
            count.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        });

        // when
        TeleSalesSaveResponse response = teleSalesInfoService.save(new TeleSalesSaveRequest());

        // then
        assertEquals(2, response.getSavedCount());
        assertEquals(0, response.getFailedCount());
    }

    @Test
    @DisplayName("save() :: 일부 저장 실패 시 실패 건수를 반환한다")
    void save_ReturnsPartialFailCount() {
        // given
        CsvDownloadRequest request1 = new CsvDownloadRequest();
        request1.setBusinessRegiNumber("1234567890");
        CsvDownloadRequest request2 = new CsvDownloadRequest();
        request2.setBusinessRegiNumber("2234567890");

        List<CsvDownloadRequest> csvList = List.of(request1, request2);

        when(csvParser.parse(any())).thenReturn(csvList);

        when(teleSalesInfoSaveAsyncWorker.saveOne(eq(request1), any())).thenAnswer(invocation -> {
            AtomicInteger count = invocation.getArgument(1);
            count.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        });
        when(teleSalesInfoSaveAsyncWorker.saveOne(eq(request2), any())).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("저장 실패"))
        );

        // when
        TeleSalesSaveResponse response = teleSalesInfoService.save(new TeleSalesSaveRequest());

        // then
        assertEquals(1, response.getSavedCount());
        assertEquals(1, response.getFailedCount());
    }

    @Test
    @DisplayName("save() :: CSV 데이터가 없는 경우 결과는 모두 실패로 처리됨")
    void save_EmptyCsvList_ReturnsZeroSuccess() {
        when(csvParser.parse(any())).thenReturn(List.of());

        TeleSalesSaveResponse response = teleSalesInfoService.save(new TeleSalesSaveRequest());

        assertEquals(0, response.getSavedCount());
        assertEquals(0, response.getFailedCount());
    }
}
