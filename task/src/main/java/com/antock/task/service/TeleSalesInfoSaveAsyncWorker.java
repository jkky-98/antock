package com.antock.task.service;

import com.antock.task.domain.TeleSalesInfo;
import com.antock.task.repository.TeleSalesInfoRepository;
import com.antock.task.service.externalapi.corpnum.CorpNumParser;
import com.antock.task.service.externalapi.csv.CsvDownloadRequest;
import com.antock.task.service.externalapi.regioncode.RegionCodeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
@Component
@RequiredArgsConstructor
@Slf4j
public class TeleSalesInfoSaveAsyncWorker {

    private final TeleSalesInfoRepository teleSalesInfoRepository;
    private final CorpNumParser corpNumParser;
    private final RegionCodeParser regionCodeParser;

    @Async("teleSalesTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> saveOne(CsvDownloadRequest csvDatum, AtomicInteger savedCount) {
        try {
            if (teleSalesInfoRepository.existsByBusinessRegiNumber(csvDatum.getBusinessRegiNumber())) {
                return CompletableFuture.completedFuture(null);
            }

            String corpNum = corpNumParser.parse(csvDatum.getBusinessRegiNumber());
            String regionCode = regionCodeParser.parse(csvDatum.getCorpAddress());

            TeleSalesInfo teleSalesInfo = TeleSalesInfo.builder()
                    .teleSalesNumber(csvDatum.getTeleSalesNumber())
                    .teleSalesName(csvDatum.getTeleSalesName())
                    .businessRegiNumber(csvDatum.getBusinessRegiNumber())
                    .corporationNumber(corpNum)
                    .regionCode(regionCode)
                    .build();

            teleSalesInfoRepository.save(teleSalesInfo);
            savedCount.incrementAndGet();

        } catch (Exception e) {
            log.error("비동기 저장 실패: {}, 예외: {}", csvDatum.getBusinessRegiNumber(), e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }
}
