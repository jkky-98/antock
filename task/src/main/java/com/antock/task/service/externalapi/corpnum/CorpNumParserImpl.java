package com.antock.task.service.externalapi.corpnum;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RequiredArgsConstructor
@Component
@Slf4j
public class CorpNumParserImpl implements CorpNumParser {

    @Value("${openapi.api.key}")
    private String key;
    private static final String BASE_URL = "https://apis.data.go.kr/1130000/MllBsDtl_2Service";
    private static final String ENDPOINT = "/getMllBsInfoDetail_2";

    private static final int PAGENO = 1;
    private static final int NUMOFROWS = 10;
    private static final String RESULT_TYPE = "json";

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String parse(String businessRegiNumber) {

        businessRegiNumber = businessRegiNumber.replaceAll("-", "");

        String rawUrl = BASE_URL + ENDPOINT +
                "?serviceKey=" + key +
                "&pageNo=" + PAGENO +
                "&numOfRows=" + NUMOFROWS +
                "&resultType=" + RESULT_TYPE +
                "&brno=" + businessRegiNumber.replaceAll("-", "");

        URI requestUri = URI.create(rawUrl);


        log.info("[CorpNumParser] 요청 URL : {}", requestUri);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUri,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode crnoNode = root
                        .path("items")
                        .get(0)
                        .path("crno");

                String crno = crnoNode.asText();
                log.info("[CorpNumParser] 추출된 법인등록번호: {}", crno);
                return crno;
            } else {
                log.warn("[CorpNumParser] 응답 실패. 상태코드: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("[CorpNumParser] 예외 발생", e);
        }

        return null; // 실패 시 null
    }
}
