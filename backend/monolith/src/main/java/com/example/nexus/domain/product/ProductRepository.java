package com.example.nexus.domain.product;

import com.example.nexus.domain.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductNameContaining(String productName);

    @Query("SELECT p FROM Product p " +
            "JOIN StoreInventory s ON s.product.idx = p.idx " +
            "WHERE s.store.idx = :storeIdx")
    Page<Product> findAllByStoreIdx(@Param("storeIdx") Long storeIdx, Pageable pageable);

    @Query("SELECT s.product FROM StoreInventory s " +
            "WHERE s.store.idx = :storeIdx " +
            "AND s.product.productName LIKE %:productName%")
    List<Product> findByStoreIdxAndProductNameContaining(
            @Param("storeIdx") Long storeIdx,
            @Param("productName") String productName);

    @Query("SELECT p FROM Product p JOIN p.category c WHERE c.categoryName = :categoryName")
    Page<Product> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);
}
