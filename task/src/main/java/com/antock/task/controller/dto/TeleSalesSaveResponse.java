package com.antock.task.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeleSalesSaveResponse {
    private int savedCount;
    private int failedCount;
}
