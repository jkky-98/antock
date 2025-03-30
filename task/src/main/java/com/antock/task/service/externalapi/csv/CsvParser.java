package com.antock.task.service.externalapi.csv;

import com.antock.task.controller.dto.TeleSalesSaveRequest;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvParser {

    private final CsvDownloader csvDownloader;

    public List<CsvDownloadRequest> parse(TeleSalesSaveRequest request) {
        String csvStringData = csvDownloader.downloadCsv(request.getCity(), request.getDistrict());

        StringReader reader = new StringReader(csvStringData);

        CsvToBean<CsvDownloadRequest> csvToBean = new CsvToBeanBuilder<CsvDownloadRequest>(reader)
                .withType(CsvDownloadRequest.class)
                .withSkipLines(0)
                .withIgnoreLeadingWhiteSpace(true)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .withThrowExceptions(false)
                .build();

        csvToBean.getCapturedExceptions().forEach(ex -> {
            log.warn("[CsvParser] CSV 파싱 실패 (line {}): {}", ex.getLineNumber(), ex.getMessage());
        });

        List<CsvDownloadRequest> allRows = csvToBean.parse();

        // "법인" 필터링 후 리턴
        List<CsvDownloadRequest> filtered = allRows.stream()
                .filter(row -> "법인".equals(row.getCorporationType()))
                .toList();

        log.info("[CsvParser] CSV 파싱 완료. 전체 데이터 수() : {}", filtered.size());

        return filtered;
    }
}
