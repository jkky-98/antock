package com.antock.task.controller;

import com.antock.task.controller.dto.TeleSalesSaveRequest;
import com.antock.task.domain.TeleSalesInfo;
import com.antock.task.service.TeleSalesInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TeleSalesInfoImportController {

    private final TeleSalesInfoService teleSalesInfoService;

    @PostMapping("/save")
    public ResponseEntity<?> saveTeleSalesInfo(
            @RequestBody TeleSalesSaveRequest request
    ) {
        log.info("request : {}", request);
        teleSalesInfoService.save(request);
        return ResponseEntity.ok().build();
    }
}
