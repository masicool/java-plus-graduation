package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.dto.category.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategoryById(long catId, NewCategoryDto newCategoryDto);

    void deleteCategoryById(long catId);

    CategoryDto findCategoryById(long catId);

    List<CategoryDto> findAllCategories(int from, int size);
}
