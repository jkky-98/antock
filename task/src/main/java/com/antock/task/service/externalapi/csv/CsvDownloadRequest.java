package com.antock.task.service.externalapi.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class CsvDownloadRequest {

    // 상호
    @CsvBindByName(column = "상호")
    private String teleSalesName;
    // 사업자 등록 번호
    @CsvBindByName(column = "사업자등록번호")
    private String businessRegiNumber;
    // 통신 판매 번호
    @CsvBindByName(column = "통신판매번호")
    private String teleSalesNumber;

    @CsvBindByName(column = "법인여부")
    private String corporationType;

    @CsvBindByName(column = "사업장소재지")
    private String corpAddress;
}
