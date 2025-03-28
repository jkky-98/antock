package com.antock.task.controller;

import com.antock.task.domain.TeleSalesInfo;
import com.antock.task.service.TeleSalesInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TeleSalesInfoImportController {

    private final TeleSalesInfoService teleSalesInfoService;

    @PostMapping("/save")
    public ResponseEntity<?> saveTeleSalesInfo(
            @RequestBody TeleSalesInfo teleSalesInfo
    ) {
        teleSalesInfoService.save(teleSalesInfo);
        return ResponseEntity.ok().build();
    }
}
