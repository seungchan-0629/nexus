package com.example.pos.domain.product.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "product")
@SQLDelete(sql = "UPDATE product SET is_deleted = true WHERE product_idx = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Persistable<Long> {

    @Id
    @Column(name = "product_idx")
    private Long idx;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_unit", nullable = false)
    private String productUnit;

    @Column(name = "max_stock", nullable = false)
    private Integer maxStock;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "danger_days", nullable = false)
    private String dangerDays;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

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

    @PostLoad
    @PrePersist
    void markNotnew(){
        this.isNew = false;
    }
}
