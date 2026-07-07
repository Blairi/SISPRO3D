package com.sispro3d.unam.category.repository.impl;

import com.sispro3d.unam.category.dao.CategoryJdbcDAO;
import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepositoryJdbc implements CategoryRepository {

    @Autowired
    private CategoryJdbcDAO dao;

    @Override
    public Category save(Category category) {
        dao.insert(category);
        return category;
    }

    @Override
    public List<Category> saveAll(Iterable<Category> entities) {
        List<Category> result = new ArrayList<>();
        for (Category entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Category> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return dao.findAll();
    }

    @Override
    public Category update(Category category) {
        dao.update(category);
        return category;
    }

    @Override
    public void delete(Category category) {
        dao.delete(category.getId());
    }

    @Override
    public void deleteById(Integer id) {
        dao.delete(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return dao.findById(id).isPresent();
    }

    @Override
    public long count() {
        return dao.findAll().size();
    }
}
