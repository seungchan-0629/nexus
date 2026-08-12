package com.example.pos.domain.store;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository storeInventoryRepository;


    public Long findStoreIdx(Long userIdx) {
        return storeRepository.findByUserIdx(userIdx).orElse(null).getIdx();
    }


}
