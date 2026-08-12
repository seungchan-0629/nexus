package com.example.nexus.domain.settlement;

import com.example.nexus.domain.settlement.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    // storeIdx 와 settlementYm 으로 일치하는 settlementIdx 찾기 위함
    Optional<Settlement> findByStoreIdxAndSettlementYm(Long storeIdx, String settlementYm);

}
