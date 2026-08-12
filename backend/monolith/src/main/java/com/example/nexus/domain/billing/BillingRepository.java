package com.example.nexus.domain.billing;

import com.example.nexus.domain.billing.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingRepository extends JpaRepository<Billing, Long> {
    Optional<Billing> findByStoreIdx(Long storeIdx);
    void deleteByStoreIdx(Long storeIdx);
}
