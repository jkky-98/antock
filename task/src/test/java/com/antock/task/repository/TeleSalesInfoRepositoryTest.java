package com.antock.task.repository;

import com.antock.task.domain.TeleSalesInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TeleSalesInfoRepositoryTest {

    @Autowired
    private TeleSalesInfoRepository teleSalesInfoRepository;

    @Test
    @DisplayName("TeleSalesInfoRepository existsByBusinessRegiNumber 성공 케이스 테스트")
    void trueWhenBusinessRegiNumberExists() {
        // given
        String businessRegiNumber = "123-45-67890";
        TeleSalesInfo teleSalesInfo = TeleSalesInfo.builder()
                .businessRegiNumber(businessRegiNumber)
                .build();
        teleSalesInfoRepository.save(teleSalesInfo);
        // when
        boolean exists = teleSalesInfoRepository.existsByBusinessRegiNumber(businessRegiNumber);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("TeleSalesInfoRepository existsByBusinessRegiNumber 실패 케이스 테스트")
    void falseWhenBusinessRegiNumberDoesNotExist() {
        // given
        String nonExistBusinessRegiNumber = "123-45-67890";
        // when
        boolean exists = teleSalesInfoRepository.existsByBusinessRegiNumber(nonExistBusinessRegiNumber);
        // then
        assertThat(exists).isFalse();
    }
}
