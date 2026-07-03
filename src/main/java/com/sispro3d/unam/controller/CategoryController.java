package com.sispro3d.unam.controller;

import com.sispro3d.unam.dao.CategoryJdbcDAO;
import com.sispro3d.unam.dto.CategoryDTO;
import com.sispro3d.unam.service.CategoryService;
import com.sispro3d.unam.service.impl.CategoryServiceImpl;

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
