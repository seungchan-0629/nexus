package com.example.nexus.domain.menu;

import com.example.nexus.domain.menu.model.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    // 메뉴명 중복 여부
    boolean existsByMenuName(String menuName);

    // 메뉴 이름 중복 확인
    boolean existsByMenuNameAndIdxNot(String menuName, Long idx);

    Optional<Menu> findByMenuName(String menuName);
    Optional<Menu> findByMenuNameAndIdxNot(String menuName, Long idx);

    // 메뉴 삭제 여부 확인
    Optional<Menu> findByIdxAndIsDeletedFalse(Long menuIdx);

    // 메뉴 삭제 felse만 조회
    Page<Menu> findAllByIsDeletedFalse(Pageable pageable);


    @Query("SELECT m FROM Menu m " +
            "WHERE m.isDeleted = false " +
            "AND (:categoryIdx IS NULL OR m.menuCategory.idx = :categoryIdx) " +
            "AND (:keyword IS NULL OR m.menuName LIKE %:keyword%)")
    Page<Menu> findMenusBySearch(@Param("keyword") String keyword,
                                 @Param("categoryIdx") Long categoryIdx,
                                 Pageable pageable);

    boolean existsByMenuCategoryIdxAndIsDeletedFalse(Long categoryIdx);
}
