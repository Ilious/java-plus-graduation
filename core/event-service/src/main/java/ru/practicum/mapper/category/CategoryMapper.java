package ru.practicum.mapper.category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.dal.dao.category.Category;
import ru.practicum.dal.dto.category.NewCategoryDto;
import ru.practicum.dto.CategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Category toCategory(CategoryDto categoryDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Category toCategory(NewCategoryDto newCategoryDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Category updateCategoryFromDto(CategoryDto categoryDto, @MappingTarget Category category);
}
