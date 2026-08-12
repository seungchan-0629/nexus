package com.example.nexus.domain.head;

import com.example.nexus.common.model.PageResponse;
import com.example.nexus.domain.head.model.HeadInventory;
import com.example.nexus.domain.head.model.HeadInventoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeadService {
    private final HeadInventoryRepository headInventoryRepository;

    @Transactional
    public List<HeadInventoryDto.ListRes> list() {
        headInventoryRepository.bulkUpdateExpiryStatus();
        List<HeadInventory> headInventoryList = headInventoryRepository.findAllWithProduct();
        return headInventoryList.stream().map(HeadInventoryDto.ListRes::from).toList();
    }

    @Transactional
    public PageResponse<HeadInventoryDto.ListRes> listPaged(int page, int size) {
        headInventoryRepository.bulkUpdateExpiryStatus();
        Page<HeadInventory> headInventoryPage = headInventoryRepository.findAllBy(PageRequest.of(page, size));
        return PageResponse.from(headInventoryPage.map(HeadInventoryDto.ListRes::from));
    }
}
