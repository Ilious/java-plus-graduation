package ru.practicum.service.category;


import ru.practicum.dal.dao.category.Category;
import ru.practicum.dal.dto.category.NewCategoryDto;
import ru.practicum.dto.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    CategoryDto create(NewCategoryDto categoryDto);

    CategoryDto update(NewCategoryDto categoryDto, Long categoryId);

    void delete(Long categoryId);

    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long categoryId);

    Optional<Category> findById(Long categoryId);

    Category getCategoryById(Long categoryId);
}
