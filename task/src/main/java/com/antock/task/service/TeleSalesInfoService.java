package com.antock.task.service;

import com.antock.task.controller.dto.TeleSalesSaveRequest;
import com.antock.task.domain.TeleSalesInfo;
import com.antock.task.service.externalapi.csv.CsvDownloadRequest;
import com.antock.task.service.externalapi.csv.CsvParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeleSalesInfoService {

    private final CsvParser csvParser;

    @Transactional
    public void save(TeleSalesSaveRequest request) {
        List<CsvDownloadRequest> csvData = csvParser.parse(request);


    }
}
