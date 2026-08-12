package com.example.nexus.domain.category;

import com.example.nexus.domain.category.model.Category;
import com.example.nexus.domain.category.model.CategoryDto;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDto.RegRes addCategory(CategoryDto.RegReq dto) {
        Category entity = categoryRepository.save(dto.toEntity());
        return CategoryDto.RegRes.from(entity);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto.ListRes> findAllCategories() {
        List<Category> categoryList = categoryRepository.findAll();
        return categoryList.stream().map(CategoryDto.ListRes::from).toList();
    }

    @Transactional
    public boolean deleteCategory(Long idx) {
        Category category = categoryRepository.findById(idx).orElse(null);

        if (category != null) {
            categoryRepository.delete(category);
            return true;
        }
        return false;
    }
}
