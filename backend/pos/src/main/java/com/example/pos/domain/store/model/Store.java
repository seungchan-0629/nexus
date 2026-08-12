package com.example.pos.domain.store.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store implements Persistable<Long> {

    @Id
    @Column(name = "store_idx")
    private Long idx;

    @Column(name = "store_name", nullable = false, unique = true)
    private String storeName;

    @Column(nullable = false)
    private String address;

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "user_idx", nullable = false)
    private Long userIdx;

    @Transient
    @Builder.Default
    private boolean isNew = false;

    @Override
    public Long getId() {
        return idx;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
