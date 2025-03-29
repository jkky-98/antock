package com.antock.task.service.externalapi.csv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvDownloader {

    private final RestTemplate restTemplate;

    @Value("${csv.download.baseUrl}")
    private String baseUrl;

    /**
     * 지정된 시/도와 구/군에 해당하는 CSV 파일을 다운로드하여 outputFilePath에 저장합니다.
     *
     * @param siDo            시/도 (예: "서울특별시")
     * @param guGun           구/군 (예: "강남구")
     */
    public String downloadCsv(String siDo, String guGun) {
        // 기본 URL (파일 다운로드 URL)
        // 파일 이름 생성: "통신판매업자_{시/도}_{구/군}.csv"
        String fileName = String.format("통신판매사업자_%s_%s.csv", siDo, guGun);
        // 전체 URL 생성
        String fullUrl = baseUrl + fileName;

        log.info("[downloadCsv] fullUrl : {}", fullUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding", "gzip, deflate, br");
        headers.set("User-Agent", "Mozilla/5.0");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // GET 요청으로 CSV 파일 데이터(byte[])를 수신
        ResponseEntity<byte[]> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.GET,
                requestEntity,
                byte[].class
        );

        MediaType contentType = response.getHeaders().getContentType();

        if (response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null
                && contentType != null
                && (MediaType.TEXT_PLAIN.isCompatibleWith(contentType)
                || MediaType.valueOf("text/csv").isCompatibleWith(contentType)
                || MediaType.APPLICATION_OCTET_STREAM.isCompatibleWith(contentType))
                || MediaType.valueOf("application/x-msdownload").isCompatibleWith(contentType)) {

            return new String(Objects.requireNonNull(response.getBody()), Charset.forName("EUC-KR"));
        } else {
            log.error("[downloadCsv] CSV 파일 다운로드 실패 : 상태={}, Content-Type={}", response.getStatusCode(), contentType);
            throw new RuntimeException("CSV 파일 다운로드 실패: 상태=" + response.getStatusCode()
                    + ", Content-Type=" + contentType);
        }

    }
}
