package com.antock.task.service.externalapi.csv;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.Charset;

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

        // baseUrl 필드 수동 주입
        var baseUrlField = CsvDownloader.class.getDeclaredFields();
        try {
            var field = CsvDownloader.class.getDeclaredField("baseUrl");
            field.setAccessible(true);
            field.set(csvDownloader, "https://www.ftc.go.kr/www/downloadBizComm.do?atchFileUrl=dataopen&atchFileNm=");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("DownloadCsv 성공 검증 단위테스트")
    public void testDownloadCsv_success() {
        // given
        String siDo = "서울특별시";
        String guGun = "강남구";
        String fileName = String.format("통신판매사업자_%s_%s.csv", siDo, guGun);
        String fullUrl = "https://www.ftc.go.kr/www/downloadBizComm.do?atchFileUrl=dataopen&atchFileNm=" + fileName;

        String expectedCsvContent = "id,name\n1,테스트";

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.valueOf("application/x-msdownload"));
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(
                expectedCsvContent.getBytes(Charset.forName("EUC-KR")),
                responseHeaders,
                HttpStatus.OK
        );

        when(restTemplate.exchange(eq(fullUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(responseEntity);

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
        String fileName = String.format("통신판매사업자_%s_%s.csv", siDo, guGun);
        String fullUrl = "https://www.ftc.go.kr/www/downloadBizComm.do?atchFileUrl=dataopen&atchFileNm=" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(null, headers, HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(eq(fullUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(responseEntity);

        // when + then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            csvDownloader.downloadCsv(siDo, guGun);
        });
        assertTrue(exception.getMessage().contains("CSV 파일 다운로드 실패"));
    }
}
