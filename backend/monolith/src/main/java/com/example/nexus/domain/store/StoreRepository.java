package com.example.nexus.domain.store;

import com.example.nexus.domain.store.model.Store;
import com.example.nexus.domain.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store,Long> {
    // 사용자 식별자(userIdx)로 가맹점 정보 조회
    Optional<Store> findByUserIdx(Long userIdx);

    // 사용자 객체로 가맹점 정보 조회 (UserService에서 사용)
    Optional<Store> findByUser(User user);

    // 대소문자 구분 없이 가맹점명에 키워드가 포함된 목록 조회
    List<Store> findByStoreNameContainingIgnoreCase(String keyword);

    // 유저 객체와 매칭되는 데이터 조회 (반환 타입 확인 필요)
    Long user(User user);

    // 가맹점명으로 가맹점 정보 조회
    Optional<Store> findByStoreName(String storeName);

    // 사업자 번호로 가맹점 정보 조회
    Optional<Store> findByBusiness(String business);

    // 전체 조회 시 이름 또는 주소 검색
    @Query("SELECT s FROM Store s WHERE " +
            "(s.storeName LIKE %:keyword% OR s.address LIKE %:keyword%) " +
            "ORDER BY s.isDeleted ASC, s.createdAt DESC")
    Page<Store> findByKeywordAll(@Param("keyword")String keyword, Pageable pageable);

    // 특정 상태(isDeleted)에 따라서 이름 또는 주소 검색
    @Query("SELECT s FROM Store s WHERE s.isDeleted = :isDeleted AND " +
            "(s.storeName LIKE %:keyword% OR s.address LIKE %:keyword%) " +
            "ORDER BY s.createdAt DESC")
    Page<Store> findByStatusAndKeyword(@Param("isDeleted")boolean isDeleted, @Param("keyword") String keyword, Pageable pageable);

    // 정렬 조건이 포함된 가맹점 전체 목록 페이징 조회 (상태순 -> 최신 등록순)
    @Query("SELECT s FROM Store s ORDER BY s.isDeleted ASC, s.createdAt DESC")
    Page<Store> findAllCustom(Pageable pageable);

    // 검색 없이 '입점'만 조회할 때: 최신 등록순 정렬
    Page<Store> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable); // 입점(false)
    // 검색 없이 '폐점'만 조회할 때: 최신 폐업일순 또는 등록순 정렬
    Page<Store> findByIsDeletedTrueOrderByClosedAtDesc(Pageable pageable);

    // 본사 대시보드용 - 전체 매장 수 KPI 카드
    long countByIsDeletedFalse();

    // 본사 대시보드용 - 신규 매장 수 (기준일 이후 생성)
    long countByIsDeletedFalseAndCreatedAtAfter(LocalDateTime since);

    // 본사 대시보드용 - 이전 기간 매장 수 (증감률 계산)
    long countByIsDeletedFalseAndCreatedAtBefore(LocalDateTime until);

    // 가맹점 폐점수 조회
    long countByIsDeletedTrue();

    // 월별 추이용 - 기준일 이후 입점(등록)된 가맹점들의 등록일자만 조회
    @Query("SELECT s.createdAt FROM Store s WHERE s.createdAt >= :since")
    List<LocalDateTime> findCreatedAtSince(@Param("since") LocalDateTime since);

    // 월별 추이용 - 기준일 이후 폐점된 가맹점들의 폐점일자만 조회
    @Query("SELECT s.closedAt FROM Store s WHERE s.closedAt IS NOT NULL AND s.closedAt >= :since")
    List<LocalDateTime> findClosedAtSince(@Param("since") LocalDateTime since);
}
