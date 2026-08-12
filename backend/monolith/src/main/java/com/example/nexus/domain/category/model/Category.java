package com.example.nexus.domain.category.model;

import com.example.nexus.domain.product.model.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@SQLDelete(sql = "UPDATE category SET is_deleted = true WHERE category_idx = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_idx")
    private Long idx;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
}
