package com.antock.task.service.externalapi.regioncode;

import com.fasterxml.jackson.core.JsonParseException;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String parse(String corpAddress) {

        try {
            String corpAddressPrefix = getCorpAddressPrefix(corpAddress);

            String encodedKeyword = URLEncoder.encode(corpAddressPrefix, StandardCharsets.UTF_8);
            String rawUrl = BASE_URL +
                    "?confmKey=" + key +
                    "&currentPage=" + CURRENT_PAGE +
                    "&countPerPage=" + COUNT_PER_PAGE +
                    "&resultType=" + RESULT_TYPE +
                    "&keyword=" + encodedKeyword;

            URI requestUri = URI.create(rawUrl);

            log.info("[RegionCodeParser] 요청 URL : {}", requestUri);

            ResponseEntity<String> response = restTemplate.exchange(
                    requestUri,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode jusoArray = root.path("results").path("juso");

                if (jusoArray.isArray() && !jusoArray.isEmpty()) {
                    JsonNode admCdNode = jusoArray.get(0).path("admCd");
                    String admCd = admCdNode.asText();
                    log.info("[RegionCodeParser] 추출된 행정구역 코드: {}", admCd);
                    return admCd;
                } else {
                    log.warn("[RegionCodeParser] juso 배열에 결과가 없습니다.");
                    return "";
                }
            } else {
                log.warn("[RegionCodeParser] 응답 실패. 상태코드: {}", response.getStatusCode());
            }

        } catch (JsonParseException e) {
            log.error("[RegionCodeParser] 요청 리미트 초과", e);
        } catch (WrongAddressException e) {
            log.warn("[RegionCodeParser] 잘못된 주소 입력입니다. {}", e.getMessage());
            return "N/A";
        } catch (Exception e) {
            log.error("[RegionCodeParser] 예외 발생", e);
        }

        return "N/A";
    }

    private static String getCorpAddressPrefix(String corpAddress) {
        try {
            String[] corpAddressSplit = corpAddress.split(" ");
            return corpAddressSplit[0] + " " + corpAddressSplit[1] + " " + corpAddressSplit[2];
        } catch (Exception e) {
            throw new WrongAddressException("[RegionCodeParser] Address 주소가 비어있거나 잘못된 형식입니다.");
        }
    }
}
