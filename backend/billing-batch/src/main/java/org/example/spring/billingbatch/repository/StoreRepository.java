package org.example.spring.billingbatch.repository;

import org.example.spring.billingbatch.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
