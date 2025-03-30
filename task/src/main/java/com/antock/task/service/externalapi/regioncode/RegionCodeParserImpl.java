package com.antock.task.service.externalapi.regioncode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class RegionCodeParserImpl implements RegionCodeParser{

    @Value("${juso.searchapi.key}")
    private String key;

    private static final String BASE_URL = "https://business.juso.go.kr/addrlink/addrLinkApi.do";
    private static final int CURRENT_PAGE = 1;
    private static final int COUNT_PER_PAGE = 10;
    private static final String RESULT_TYPE = "json";

    @Override
    public String parse(String businessAddress) {
        String requestUrl = BASE_URL +
                "?confmKey=" + key +
                "&currentPage=" + CURRENT_PAGE +
                "&countPerPage=" + COUNT_PER_PAGE +
                "&resultType=" + RESULT_TYPE +
                "&keyword=" + businessAddress;
        return "";
    }
}
