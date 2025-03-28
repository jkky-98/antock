package com.antock.task.service.externalapi.csv;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
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
        String fileName = String.format("통신판매업자_%s_%s.csv", siDo, guGun);
        // 파일명을 퍼센트 인코딩 (UTF-8)
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        // 전체 URL 생성
        String fullUrl = baseUrl + encodedFileName;

        // GET 요청으로 CSV 파일 데이터(byte[])를 수신
        ResponseEntity<byte[]> response = restTemplate.getForEntity(fullUrl, byte[].class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // byte 배열을 UTF-8 문자열로 변환하여 반환
            return new String(response.getBody(), StandardCharsets.UTF_8);
        } else {
            throw new RuntimeException("CSV 파일 다운로드 실패: " + response.getStatusCode());
        }
    }
}
