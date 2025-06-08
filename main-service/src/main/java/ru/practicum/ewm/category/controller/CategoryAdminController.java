package ru.practicum.ewm.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategorySaveDto;
import ru.practicum.ewm.category.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid CategorySaveDto categorySaveDto) {
        log.info("Save category {}", categorySaveDto);
        CategoryDto categoryDto = categoryService.addCategory(categorySaveDto);
        log.info("Category saved successfully, categoryDto: {}", categoryDto);
        return categoryDto;
    }

    @PatchMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable Long categoryId,
                                      @RequestBody @Valid CategorySaveDto categorySaveDto) {
        log.info("Update category {}", categorySaveDto);
        CategoryDto categoryDto = categoryService.updateCategory(categoryId, categorySaveDto);
        log.info("Category updated successfully, categoryDto: {}", categoryDto);
        return categoryDto;
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long categoryId) {
        log.info("Delete category with id {}", categoryId);
        categoryService.deleteCategoryById(categoryId);
    }
}
