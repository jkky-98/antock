package com.antock.task.service.externalapi.regioncode;

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
class RegionCodeParserImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RegionCodeParserImpl parser;

    @Test
    @DisplayName("RegionCodeParser parse() :: 정상적으로 행정구역 코드를 파싱하는 경우")
    void shouldReturnRegionCodeSuccessfully() {
        // given
        String address = "서울특별시 강남구 역삼동";
        String jsonResponse = """
            {
              "results": {
                "juso": [
                  {
                    "admCd": "1234567890"
                  }
                ]
              }
            }
        """;
        ResponseEntity<String> response = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        ReflectionTestUtils.setField(parser, "key", "test-api-key");

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(response);

        // when
        String result = parser.parse(address);

        // then
        assertEquals("1234567890", result);
    }

    @Test
    @DisplayName("RegionCodeParser parse() :: juso 배열이 비어있는 경우 빈 문자열 반환")
    void shouldReturnEmptyWhenJusoArrayEmpty() {
        // given
        String address = "서울특별시 강남구 역삼동";
        String jsonResponse = """
            {
              "results": {
                "juso": []
              }
            }
        """;
        ResponseEntity<String> response = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        ReflectionTestUtils.setField(parser, "key", "test-api-key");

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(response);

        // when
        String result = parser.parse(address);

        // then
        assertEquals("", result);
    }

    @Test
    @DisplayName("RegionCodeParser parse() :: 주소가 잘못된 형식이면 'N/A' 반환")
    void shouldReturnNAWhenAddressIsInvalid() {
        // given
        String wrongAddress = "서울특별시";

        // when
        String result = parser.parse(wrongAddress);

        // then
        assertEquals("N/A", result);
    }

    @Test
    @DisplayName("RegionCodeParser parse() :: HTTP 응답이 실패하면 'N/A' 반환")
    void shouldReturnNAWhenHttpResponseIsError() {
        // given
        String address = "서울특별시 강남구 역삼동";
        ResponseEntity<String> response = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        ReflectionTestUtils.setField(parser, "key", "test-api-key");

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(response);

        // when
        String result = parser.parse(address);

        // then
        assertEquals("N/A", result);
    }

    @Test
    @DisplayName("RegionCodeParser parse() :: 예기치 못한 예외 발생 시 'N/A' 반환")
    void shouldReturnNAOnUnexpectedException() {
        // given
        String address = "서울특별시 강남구 역삼동";

        ReflectionTestUtils.setField(parser, "key", "test-api-key");

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new RuntimeException("네트워크 실패"));

        // when
        String result = parser.parse(address);

        // then
        assertEquals("N/A", result);
    }
}

