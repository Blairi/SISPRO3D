package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping
    public String list(Model model) {
        Long userId = currentUser.id();

        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Long userId = currentUser.id();

        model.addAttribute("category", new CategoryRequest());
        return "admin/category-create";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("category") CategoryRequest request, BindingResult result) {
        Long userId = currentUser.id();

        if (result.hasErrors()) {
            return "admin/category-create";
        }

        categoryService.create(request);
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Long userId = currentUser.id();

        var category = categoryService.findById(id).orElseThrow();
        model.addAttribute("category", category);
        return "admin/category-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("category") CategoryRequest request, BindingResult result, Model model) {
        Long userId = currentUser.id();

        if (result.hasErrors()) {
            model.addAttribute("categoryId", id);
            return "admin/category-edit";
        }

        categoryService.update(id, request);
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        Long userId = currentUser.id();

        categoryService.delete(id);
        return "redirect:/admin/categories";
    }
}