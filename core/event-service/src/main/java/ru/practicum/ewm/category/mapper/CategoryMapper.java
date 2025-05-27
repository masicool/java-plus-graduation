package ru.practicum.ewm.category.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.dto.category.CategoryDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {
    public static Category mapToCategory(NewCategoryDto request) {
        Category category = new Category();
        category.setName(request.getName());
        return category;
    }

    public static CategoryDto mapToCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
