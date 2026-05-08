package mx.unam.dgtic.controller;

import mx.unam.dgtic.dto.CategoryDTO;
import mx.unam.dgtic.service.CategoryService;
import mx.unam.dgtic.service.impl.CategoryServiceImpl;

import java.util.List;

public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController() {
        this.categoryService = new CategoryServiceImpl();
    }

    public void displayAllCategories() {
        List<CategoryDTO> categoryDTOS = categoryService.findAll();
        categoryDTOS.forEach(System.out::println);
    }

}
