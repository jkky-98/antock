package com.antock.task.service;

import com.antock.task.controller.dto.TeleSalesSaveRequest;
import com.antock.task.domain.TeleSalesInfo;
import com.antock.task.service.externalapi.corpnum.CorpNumParser;
import com.antock.task.service.externalapi.csv.CsvDownloadRequest;
import com.antock.task.service.externalapi.csv.CsvParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeleSalesInfoService {

    private final CsvParser csvParser;
    private final CorpNumParser corpNumParser;

    @Transactional
    public void save(TeleSalesSaveRequest request) {
        List<CsvDownloadRequest> csvData = csvParser.parse(request);

        for (CsvDownloadRequest csvDatum : csvData) {
            String parseResult = corpNumParser.parse(csvDatum.getBusinessRegiNumber());
            log.info("parseResult : {}", parseResult);
            break;
        }
    }
}
