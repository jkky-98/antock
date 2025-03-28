package com.antock.task.service;

import com.antock.task.domain.TeleSalesInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeleSalesInfoService {

    @Transactional
    public void save(TeleSalesInfo teleSalesInfo) {

    }
}
