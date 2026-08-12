package com.example.nexus.domain.settlement;

import com.example.nexus.domain.settlement.model.AfterBilling;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AfterBillingRepository extends JpaRepository<AfterBilling, Long> {
    List<AfterBilling> findByStoreIdxAndIsRetryFailedTrue(Long storeIdx);
    Optional<AfterBilling> findByStoreIdxAndPayedMonth(Long storeIdx, String payedMonth);
}
