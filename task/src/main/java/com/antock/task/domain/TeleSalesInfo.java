package com.antock.task.domain;

import com.antock.task.domain.base.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeleSalesInfo extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "tele_sales_info_id")
    private Long id;

    // 통신 판매 번호
    private String teleSalesNumber;
    // 상호
    private String teleSalesName;
    // 사업자 등록 번호
    @Column(unique = true)
    private String businessRegiNumber;
    // 법인 등록 번호
    private String corporationNumber;
    // 행정 구역 코드
    private String regionCode;
}
