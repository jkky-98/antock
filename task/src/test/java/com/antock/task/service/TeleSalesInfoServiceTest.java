package com.antock.task.service;

import com.antock.task.controller.dto.TeleSalesSaveRequest;
import com.antock.task.domain.TeleSalesInfo;
import com.antock.task.repository.TeleSalesInfoRepository;
import com.antock.task.service.externalapi.corpnum.CorpNumParser;
import com.antock.task.service.externalapi.csv.CsvDownloadRequest;
import com.antock.task.service.externalapi.csv.CsvParser;
import com.antock.task.service.externalapi.regioncode.RegionCodeParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class TeleSalesInfoServiceTest {

    @Mock
    private CsvParser csvParser;

    @Mock
    private CorpNumParser corpNumParser;

    @Mock
    private RegionCodeParser regionCodeParser;

    @Mock
    private TeleSalesInfoRepository teleSalesInfoRepository;

    @InjectMocks
    private TeleSalesInfoService teleSalesInfoService;

    @Test
    @DisplayName("TeleSalesInfoService save() :: 성공 로직 테스트")
    void saveTeleSalesInfos() {
        // given
        CsvDownloadRequest csvRequest = new CsvDownloadRequest();
        csvRequest.setTeleSalesName("antock");
        csvRequest.setTeleSalesNumber("123456");
        csvRequest.setCorpAddress("수원시 영통구 인계동");
        csvRequest.setCorporationType("법인");
        csvRequest.setBusinessRegiNumber("123-45-67890");

        List<CsvDownloadRequest> csvList = List.of(csvRequest);

        when(csvParser.parse(any())).thenReturn(csvList);
        when(teleSalesInfoRepository.existsByBusinessRegiNumber("123-45-67890")).thenReturn(false);
        when(corpNumParser.parse("123-45-67890")).thenReturn("0987654321");
        when(regionCodeParser.parse("수원시 영통구 인계동")).thenReturn("0987654321");

        // when
        teleSalesInfoService.save(new TeleSalesSaveRequest());

        // then
        verify(teleSalesInfoRepository, times(1)).save(any(TeleSalesInfo.class));
    }

    @DisplayName("TeleSalesInfoService save() :: 이미 존재하는 사업자등록번호는 저장되지 않음 검증")
    @Test
    void shouldNotSaveIfBusinessRegiNumberExists() {
        // given
        CsvDownloadRequest csvRequest = new CsvDownloadRequest();
        csvRequest.setBusinessRegiNumber("123-45-67890");
        csvRequest.setCorpAddress("수원시 영통구 인계동");
        csvRequest.setTeleSalesName("중복회사");
        csvRequest.setTeleSalesNumber("999");

        when(csvParser.parse(any())).thenReturn(List.of(csvRequest));
        when(teleSalesInfoRepository.existsByBusinessRegiNumber("123-45-67890")).thenReturn(true); // 중복
        // when
        teleSalesInfoService.save(new TeleSalesSaveRequest());

        // then
        verify(teleSalesInfoRepository, never()).save(any());
    }

    @DisplayName("TeleSalesInfoService save() :: CorpNumParser 예외 발생 시 저장되지 않음 검증")
    @Test
    void shouldNotSaveWhenCorpNumParseFails() {
        // given
        CsvDownloadRequest csvRequest = new CsvDownloadRequest();
        csvRequest.setBusinessRegiNumber("123-45-67890");
        csvRequest.setCorpAddress("서울 강남구");

        when(csvParser.parse(any())).thenReturn(List.of(csvRequest));
        when(teleSalesInfoRepository.existsByBusinessRegiNumber(any())).thenReturn(false);
        when(corpNumParser.parse(any())).thenThrow(new RuntimeException("파싱 실패"));

        // when
        teleSalesInfoService.save(new TeleSalesSaveRequest());

        // then
        verify(teleSalesInfoRepository, never()).save(any());
    }

}
