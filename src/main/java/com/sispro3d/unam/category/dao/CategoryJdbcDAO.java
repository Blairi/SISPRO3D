package com.sispro3d.unam.category.dao;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoryJdbcDAO extends AbstractJdbcDAO<Category> implements GenericDAO<Category> {

    private static final String FIND_ALL   = "SELECT id, name, description FROM category";
    private static final String FIND_BY_ID = "SELECT id, name, description FROM category WHERE id = ?";
    private static final String INSERT     = "INSERT INTO category (name, description) VALUES (?, ?)";
    private static final String UPDATE     = "UPDATE category SET name = ?, description = ? WHERE id = ?";
    private static final String DELETE     = "DELETE FROM category WHERE id = ?";

    @Override
    public List<Category> findAll() {
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todas las categorías");
    }

    @Override
    public Optional<Category> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar categoría con id");
    }

    @Override
    public int insert(Category category) {
        return insertWithGeneratedKeys(INSERT,
                ps -> {
                    ps.setString(1, category.getName());
                    ps.setString(2, category.getDescription());
                },
                category, Category::setId, "Error al insertar categoría: " + category.getName());
    }

    @Override
    public void update(Category category) {
        executeUpdate(UPDATE,
                ps -> {
                    ps.setString(1, category.getName());
                    ps.setString(2, category.getDescription());
                    ps.setInt(3, category.getId());
                },
                "Error al actualizar categoría con id: " + category.getId());
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar categoría con id: " + id);
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        return category;
    }
}
