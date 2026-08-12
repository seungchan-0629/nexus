package com.example.pos.domain.store;

import com.example.pos.domain.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store,Long> {
    // 사용자 식별자(userIdx)로 가맹점 정보 조회
    Optional<Store> findByUserIdx(Long userIdx);


}
