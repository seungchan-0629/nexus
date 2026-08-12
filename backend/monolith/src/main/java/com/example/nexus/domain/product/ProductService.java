package com.example.nexus.domain.product;

import com.example.nexus.domain.category.CategoryRepository;
import com.example.nexus.domain.category.model.Category;
import com.example.nexus.domain.product.model.Product;
import com.example.nexus.domain.product.model.ProductDto;
import com.example.nexus.event.KafkaTopics;
import com.example.nexus.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public ProductDto.RegRes addProduct(ProductDto.RegReq dto) {
        Category category = categoryRepository.findById(dto.getCategoryIdx()).orElse(null);

        if (category != null) {
            Product product = dto.toEntity(category);
            Product saved = productRepository.save(product);

            ProductEvent event = new ProductEvent(
                    saved.getIdx(),
                    saved.getProductName(),
                    saved.getProductUnit(),
                    saved.getMaxStock(),
                    saved.getMinStock(),
                    saved.getUnitPrice(),
                    saved.getDangerDays(),
                    saved.isDeleted(),
                    saved.getCategory().getCategoryName()
            );
            kafkaTemplate.send(KafkaTopics.PRODUCT_CREATED, event);

            return ProductDto.RegRes.from(saved);
        }
        return null;
    }

    // 본사 전체 제품 조회 페이징처리
    @Transactional(readOnly = true)
    public ProductDto.ProductPageRes findAllProduct(Pageable pageable, String categoryName) {
        Page<Product> productPage;
        if (categoryName != null && !categoryName.isBlank()) {
            productPage = productRepository.findByCategoryName(categoryName, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }
        return ProductDto.ProductPageRes.from(productPage);
    }

    @Transactional
    public boolean updateProduct(Long idx, ProductDto.RegReq dto) {
        Product product = productRepository.findById(idx).orElse(null);
        if (product == null) return false;

        Category category = categoryRepository.findById(dto.getCategoryIdx()).orElse(null);
        if (category == null) return false;

        product.update(
                dto.getProductName(),
                dto.getProductUnit(),
                dto.getMaxStock(),
                dto.getMinStock(),
                dto.getUnitPrice(),
                dto.getDangerDays(),
                category
        );

        ProductEvent event = new ProductEvent(
                product.getIdx(),
                product.getProductName(),
                product.getProductUnit(),
                product.getMaxStock(),
                product.getMinStock(),
                product.getUnitPrice(),
                product.getDangerDays(),
                product.isDeleted(),
                product.getCategory().getCategoryName()
        );
        kafkaTemplate.send(KafkaTopics.PRODUCT_UPDATED, event);

        return true;
    }

    @Transactional
    public boolean deleteProduct(Long idx) {
        return productRepository.findById(idx).map(product -> {
            productRepository.delete(product);

            ProductEvent event = new ProductEvent(
                    product.getIdx(),
                    product.getProductName(),
                    product.getProductUnit(),
                    product.getMaxStock(),
                    product.getMinStock(),
                    product.getUnitPrice(),
                    product.getDangerDays(),
                    true,
                    product.getCategory().getCategoryName()
            );
            kafkaTemplate.send(KafkaTopics.PRODUCT_DELETED, event);

            return true;
        }).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<ProductDto.ListRes> searchProduct(String productName) {
        return productRepository.findByProductNameContaining(productName).stream()
                .map(ProductDto.ListRes::from)
                .toList();
    }

    // 가맹점별 제품 조회
    @Transactional
    public Page<ProductDto.ListRes> findProductsByStore(Long storeIdx, Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByStoreIdx(storeIdx, pageable);

        return productPage.map(product -> ProductDto.ListRes.from(product));
    }

    @Transactional(readOnly = true)
    public List<ProductDto.ListRes> searchProductInStore(Long storeIdx, String productName) {
        List<Product> entities = productRepository.findByStoreIdxAndProductNameContaining(storeIdx, productName);

        List<ProductDto.ListRes> dtos = new ArrayList<>();
        for (Product entity : entities) {
            dtos.add(ProductDto.ListRes.from(entity));
        }

        return dtos;
    }
}
