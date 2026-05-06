package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.CategoryJdbcDAO;
import mx.unam.dgtic.dto.CategoryDTO;
import mx.unam.dgtic.service.CategoryService;
import mx.unam.dgtic.service.impl.CategoryServiceImpl;

import java.util.Optional;

public class CategoryController {
    private CategoryService categoryService;

    public CategoryController() {
        this.categoryService = new CategoryServiceImpl(new CategoryJdbcDAO());
    }

    public void displayCategory(int id) {
        System.out.println("Displaying category with id = " + id);
        Optional<CategoryDTO> categoryDTO = categoryService.findById(id);
        System.out.println("categoryDTO = " + categoryDTO);
    }

    public void displayAllCategories() {
        System.out.println("Displaying all categories:");
        categoryService.findAll().forEach(System.out::println);
    }
}
