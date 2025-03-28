package com.antock.task.service.externalapi.csv;

import com.antock.task.controller.dto.TeleSalesSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CsvParser {

    private final CsvDownloader csvDownloader;

    private List<CsvDownloadRequest> parse(TeleSalesSaveRequest request) {
        String csvStringData = csvDownloader.downloadCsv(request.getCity(), request.getDistrict());
        // toDo : String -> List CsvDownloadRequest 전환 작업 필요
        return List.of();
    }
}
