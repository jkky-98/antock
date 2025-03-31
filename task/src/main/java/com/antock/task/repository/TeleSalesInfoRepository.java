package com.antock.task.repository;

import com.antock.task.domain.TeleSalesInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeleSalesInfoRepository extends JpaRepository<TeleSalesInfo, Long> {
    boolean existsByBusinessRegiNumber(String businessRegiNumber);
}
