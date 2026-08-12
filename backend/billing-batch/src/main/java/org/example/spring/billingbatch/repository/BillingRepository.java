package org.example.spring.billingbatch.repository;

import org.example.spring.billingbatch.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRepository extends JpaRepository<Billing, Long> {

    Billing findByStoreIdx(Long storeIdx);

}
