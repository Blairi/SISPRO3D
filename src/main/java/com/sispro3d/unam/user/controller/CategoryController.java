package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.service.CategoryService;
import jakarta.servlet.http.HttpSession;
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

    @GetMapping
    public String list(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("category", new CategoryRequest());
        return "admin/category-create";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("category") CategoryRequest request, BindingResult result, HttpSession session) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            return "admin/category-create";
        }

        categoryService.create(request);
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        var category = categoryService.findById(id).orElseThrow();
        model.addAttribute("category", category);
        return "admin/category-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("category") CategoryRequest request, BindingResult result, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("categoryId", id);
            return "admin/category-edit";
        }

        categoryService.update(id, request);
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        categoryService.delete(id);
        return "redirect:/admin/categories";
    }
}
