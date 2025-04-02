package com.antock.task.controller.dto;

import lombok.Data;

@Data
public class TeleSalesSaveResponse {
    private int savedCount;
    private int failedCount;
}
