package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategorySaveDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(CategorySaveDto categorySaveDto) {
        Category category = categoryMapper.mapToCategory(categorySaveDto);
        checkCategoryName(category.getName());
        return categoryMapper.mapToCategoryDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        return categoryMapper.mapToCategoryDto(findCategoryById(categoryId));
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findCategoriesLimited(from, size).stream()
                .map(categoryMapper::mapToCategoryDto)
                .toList();
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategorySaveDto categorySaveDto) {
        Category category = findCategoryById(categoryId);
        if (!categorySaveDto.getName().equals(category.getName())) {
            checkCategoryName(categorySaveDto.getName());
        }
        category.setName(categorySaveDto.getName());
        return categoryMapper.mapToCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long categoryId) {
        Category category = findCategoryById(categoryId);
        if (!eventRepository.findByCategoryId(categoryId).isEmpty()) {
            throw new ConflictException(String.format(
                    "Can't delete category %s because it contains events", category.getName())
            );
        }
        categoryRepository.deleteById(categoryId);
    }

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id %d not found", categoryId)));
    }

    private void checkCategoryName(String name) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new DuplicatedDataException(String.format(
                    "Category name %s is already in use", name)
            );
        }
    }
}
