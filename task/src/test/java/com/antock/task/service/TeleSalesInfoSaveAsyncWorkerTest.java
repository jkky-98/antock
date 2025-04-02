package com.antock.task.service;

import com.antock.task.domain.TeleSalesInfo;
import com.antock.task.repository.TeleSalesInfoRepository;
import com.antock.task.service.externalapi.corpnum.CorpNumParser;
import com.antock.task.service.externalapi.csv.CsvDownloadRequest;
import com.antock.task.service.externalapi.regioncode.RegionCodeParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeleSalesInfoSaveAsyncWorkerTest {

    @Mock
    private TeleSalesInfoRepository teleSalesInfoRepository;

    @Mock
    private CorpNumParser corpNumParser;

    @Mock
    private RegionCodeParser regionCodeParser;

    @InjectMocks
    private TeleSalesInfoSaveAsyncWorker worker;

    private CsvDownloadRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new CsvDownloadRequest();
        mockRequest.setTeleSalesNumber("010-1234-5678");
        mockRequest.setTeleSalesName("홍길동");
        mockRequest.setBusinessRegiNumber("1234567890");
        mockRequest.setCorpAddress("서울특별시 강남구");
    }

    @Test
    @DisplayName("saveOne() :: 신규 사업자번호인 경우 저장 및 카운트 증가")
    void saveOneSuccess() {
        AtomicInteger count = new AtomicInteger(0);

        when(teleSalesInfoRepository.existsByBusinessRegiNumber("1234567890")).thenReturn(false);
        when(corpNumParser.parse("1234567890")).thenReturn("9876543210");
        when(regionCodeParser.parse("서울특별시 강남구")).thenReturn("SEOUL-GANGNAM");

        CompletableFuture<Void> future = worker.saveOne(mockRequest, count);
        future.join();

        verify(teleSalesInfoRepository, times(1)).save(any(TeleSalesInfo.class));
        assertEquals(1, count.get());
    }

    @Test
    @DisplayName("saveOne() :: 이미 존재하는 사업자번호면 저장되지 않음")
    void saveOneDuplicateNotSave() {
        AtomicInteger count = new AtomicInteger(0);

        when(teleSalesInfoRepository.existsByBusinessRegiNumber("1234567890")).thenReturn(true);

        CompletableFuture<Void> future = worker.saveOne(mockRequest, count);
        future.join();

        verify(teleSalesInfoRepository, never()).save(any());
        assertEquals(0, count.get());
    }

    @Test
    @DisplayName("saveOne() :: 예외 발생 시 저장되지 않고 카운트도 증가하지 않음")
    void saveOneExceptionLogErrorAndNotThrow() {
        AtomicInteger count = new AtomicInteger(0);

        when(teleSalesInfoRepository.existsByBusinessRegiNumber("1234567890")).thenReturn(false);
        when(corpNumParser.parse(anyString())).thenThrow(new RuntimeException("파싱 실패"));

        CompletableFuture<Void> future = worker.saveOne(mockRequest, count);
        future.join();  // 예외 발생해도 CompletableFuture는 정상 완료 처리됨

        verify(teleSalesInfoRepository, never()).save(any());
        assertEquals(0, count.get());
    }
}
