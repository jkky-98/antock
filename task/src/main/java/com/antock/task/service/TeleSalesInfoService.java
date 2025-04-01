package com.antock.task.service;

import com.antock.task.controller.dto.TeleSalesSaveRequest;
import com.antock.task.domain.TeleSalesInfo;
import com.antock.task.repository.TeleSalesInfoRepository;
import com.antock.task.service.externalapi.corpnum.CorpNumParser;
import com.antock.task.service.externalapi.csv.CsvDownloadRequest;
import com.antock.task.service.externalapi.csv.CsvParser;
import com.antock.task.service.externalapi.regioncode.RegionCodeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeleSalesInfoService {

    private final CsvParser csvParser;
    private final CorpNumParser corpNumParser;
    private final RegionCodeParser regionCodeParser;

    private final TeleSalesInfoRepository teleSalesInfoRepository;

    @Transactional
    public void save(TeleSalesSaveRequest request) {
        List<CsvDownloadRequest> csvData = csvParser.parse(request);

        int csvDataSize = csvData.size();
        log.info("csv data size: {}", csvDataSize);

        // 멀티스레드 처리
        // 스레드 10
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<>();

        // 저장 성공 카운터 (thread-safe)
        AtomicInteger savedCount = new AtomicInteger(0);

        for (CsvDownloadRequest csvDatum : csvData) {
            // 먼저 존재 여부를 체크하여 이미 저장된 경우에는 future를 제출하지 않음
            if (teleSalesInfoRepository.existsByBusinessRegiNumber(csvDatum.getBusinessRegiNumber())) {
                continue;
            }
            Future<?> future = executor.submit(() -> {
                try {
                    String corpNum = corpNumParser.parse(csvDatum.getBusinessRegiNumber());
                    String regionCode = regionCodeParser.parse(csvDatum.getCorpAddress());

                    TeleSalesInfo teleSalesInfo = TeleSalesInfo.builder()
                            .teleSalesNumber(csvDatum.getTeleSalesNumber())
                            .teleSalesName(csvDatum.getTeleSalesName())
                            .businessRegiNumber(csvDatum.getBusinessRegiNumber())
                            .corporationNumber(corpNum)
                            .regionCode(regionCode)
                            .build();

                    log.info("저장될 TeleSalesInfo : {}", teleSalesInfo);
                    teleSalesInfoRepository.save(teleSalesInfo);
                    savedCount.incrementAndGet(); // 저장 성공 시 카운트 증가
                } catch (DataIntegrityViolationException e) {
                    log.warn("중복된 사업자 등록 번호로 인해 저장 실패 (무시), 무시된 사업자 등록 번호: {}", csvDatum.getBusinessRegiNumber());
                } catch (Exception e) {
                    // parse() 등 다른 부분에서 발생한 예외 처리
                    log.error("CSV 데이터 처리 중 예외 발생, 사업자 등록 번호: {} - 예외: {}",
                            csvDatum.getBusinessRegiNumber(), e.getMessage());
                }
            });

            futures.add(future);
        }

        // 모든 병렬 작업이 완료될 때까지 대기
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("병렬 작업 처리 중 에러 발생", e);
            }
        }

        // 자원 정리
        executor.shutdown();

        // 결과 로그
        int failedCount = csvDataSize - savedCount.get();
        log.info("총 입력 데이터 수: {}", csvDataSize);
        log.info("저장된 TeleSalesInfo 수: {}", savedCount.get());
        log.info("누락된 수 (중복/실패): {}", failedCount);

    }

}
