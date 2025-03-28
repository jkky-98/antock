package com.antock.task.service.externalapi.csv;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class CsvDownloaderTest {

    @Mock
    private RestTemplate restTemplate;

    private CsvDownloader csvDownloader;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        csvDownloader = new CsvDownloader(restTemplate);
    }

    @Test
    @DisplayName("DownloadCsv 성공 검증 단위테스트")
    public void testDownloadCsv_success() {
        // given
        String siDo = "서울특별시";
        String guGun = "강남구";
        String fileName = String.format("통신판매업자_%s_%s.csv", siDo, guGun);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        String baseUrl = "https://www.ftc.go.kr/www/downloadBizComm.do?atchFileUrl=dataopen&atchFileNm=";
        String fullUrl = baseUrl + encodedFileName;

        // 테스트용 CSV 내용
        String expectedCsvContent = "id,name\n1,테스트";
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(
                expectedCsvContent.getBytes(StandardCharsets.UTF_8), HttpStatus.OK);

        when(restTemplate.getForEntity(fullUrl, byte[].class)).thenReturn(responseEntity);

        // when
        String actualContent = csvDownloader.downloadCsv(siDo, guGun);

        // then
        assertEquals(expectedCsvContent, actualContent);
    }

    @Test
    @DisplayName("DownloadCsv 예외 검증 단위테스트")
    public void testDownloadCsv_failure() {
        // given
        String siDo = "서울광역시";
        String guGun = "강남남구";
        String fileName = String.format("통신판매업자_%s_%s.csv", siDo, guGun);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        String baseUrl = "https://www.ftc.go.kr/www/downloadBizComm.do?atchFileUrl=dataopen&atchFileNm=";
        String fullUrl = baseUrl + encodedFileName;

        // 실패 케이스: 404 NOT_FOUND 응답 (body는 null)
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(fullUrl, byte[].class)).thenReturn(responseEntity);

        // when
        // then : 예외가 발생하는지 검증
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            csvDownloader.downloadCsv(siDo, guGun);
        });
        assertTrue(exception.getMessage().contains("CSV 파일 다운로드 실패"));
    }
}

