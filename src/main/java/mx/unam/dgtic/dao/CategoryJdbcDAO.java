package mx.unam.dgtic.dao;

import mx.unam.dgtic.db.ConnectionHandler;
import mx.unam.dgtic.domain.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryJdbcDAO implements GenericDAO<Category> {

    private static final String FIND_ALL   = "SELECT id, name, description FROM category";
    private static final String FIND_BY_ID = "SELECT id, name, description FROM category WHERE id = ?";
    private static final String INSERT     = "INSERT INTO category (name, description) VALUES (?, ?)";
    private static final String UPDATE     = "UPDATE category SET name = ?, description = ? WHERE id = ?";
    private static final String DELETE     = "DELETE FROM category WHERE id = ?";

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todas las categorías", e);
        }
        return categories;
    }

    @Override
    public Optional<Category> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar categoría con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Category category) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, category.getName());
                ps.setString(2, category.getDescription());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        category.setId(generatedId);
                        conn.commit();
                        return generatedId;
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar categoría: " + category.getName(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert categoría", e);
        }
        return 0;
    }

    @Override
    public void update(Category category) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setString(1, category.getName());
                ps.setString(2, category.getDescription());
                ps.setInt(3, category.getId());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar categoría con id: " + category.getId(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update categoría", e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(DELETE)) {

                ps.setInt(1, id);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al eliminar categoría con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete categoría", e);
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        return category;
    }
}
