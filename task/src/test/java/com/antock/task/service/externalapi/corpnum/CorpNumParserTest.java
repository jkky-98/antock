package com.antock.task.service.externalapi.corpnum;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class CorpNumParserTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CorpNumParserImpl parser;

    private final String validBusinessNumber = "123-45-67890";

    @Test
    @DisplayName("CorpNumParser parse() :: 성공적으로 법인등록번호를 파싱하는 경우")
    void shouldParseCrnoSuccessfully() {
        // given
        ReflectionTestUtils.setField(parser, "key", "test-api-key");

        String responseJson = """
            {
              "items": [
                {
                  "crno": "CORP-123456"
                }
              ]
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // when
        String result = parser.parse(validBusinessNumber);

        // then
        assertEquals("CORP-123456", result);
    }

    @Test
    @DisplayName("CorpNumParser parse() :: 응답 코드가 200이 아닌 경우 N/A 반환")
    void shouldReturnNAWhenResponseIsNotOk() {
        // given
        ReflectionTestUtils.setField(parser, "key", "test-api-key");

        ResponseEntity<String> responseEntity = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(responseEntity);

        // when
        String result = parser.parse(validBusinessNumber);

        // then
        assertEquals("N/A", result);
    }

    @Test
    @DisplayName("CorpNumParser parse() :: 응답 바디가 null인 경우 N/A 반환")
    void shouldReturnNAWhenResponseBodyIsNull() {
        // given
        ReflectionTestUtils.setField(parser, "key", "test-api-key");

        ResponseEntity<String> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(responseEntity);

        // when
        String result = parser.parse(validBusinessNumber);

        // then
        assertEquals("N/A", result);
    }

    @Test
    @DisplayName("CorpNumParser parse() :: 응답 JSON이 잘못되어 JsonParseException이 발생하는 경우")
    void shouldReturnNAOnJsonParseException() {
        // given
        ReflectionTestUtils.setField(parser, "key", "test-api-key");

        String invalidJson = "NOT_JSON";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(responseEntity);

        // when
        String result = parser.parse(validBusinessNumber);

        // then
        assertEquals("N/A", result);
    }

    @Test
    @DisplayName("CorpNumParser parse() :: RestTemplate 호출 자체에서 예외가 발생하는 경우")
    void shouldReturnNAOnRestTemplateException() {
        // given
        ReflectionTestUtils.setField(parser, "key", "test-api-key");

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection error"));

        // when
        String result = parser.parse(validBusinessNumber);

        // then
        assertEquals("N/A", result);
    }
}
