package com.antock.task.service.externalapi.csv;

import com.antock.task.controller.dto.TeleSalesSaveRequest;
import com.opencsv.bean.CsvToBeanBuilder;
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
        // toDo : String -> List CsvDownloadRequest 전환 작업 필요
        log.info("csvString : {}", csvStringData);

        StringReader reader = new StringReader(csvStringData);
        List<CsvDownloadRequest> allRows = new CsvToBeanBuilder<CsvDownloadRequest>(reader)
                .withType(CsvDownloadRequest.class)
                .withSkipLines(0)
                .withIgnoreLeadingWhiteSpace(true)
                .build()
                .parse();

        // "법인" 필터링 후 리턴
        return allRows.stream()
                .filter(row -> "법인".equals(row.getCorporationType()))
                .toList();

    }
}
