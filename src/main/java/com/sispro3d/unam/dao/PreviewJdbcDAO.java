package com.sispro3d.unam.dao;

import com.sispro3d.unam.db.ConnectionHandler;
import com.sispro3d.unam.domain.Deliverable;
import com.sispro3d.unam.domain.Preview;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PreviewJdbcDAO implements GenericDAO<Preview> {

    private static final String FIND_ALL = """
            SELECT 
                p.id, p.caption, p.url_file,
                d.id as deliverable_id, d.name as deliverable_name, d.url_file as deliverable_url,
                d.created_at as deliverable_created_at, d.file_type
            FROM preview p
            JOIN deliverable d ON p.deliverable_id = d.id
            """;

    private static final String FIND_BY_ID = FIND_ALL + "WHERE p.id = ?";

    private static final String INSERT = "INSERT INTO preview (caption, url_file, deliverable_id) VALUES (?, ?, ?)";
    private static final String UPDATE = "UPDATE preview SET caption = ?, url_file = ?, deliverable_id = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM preview WHERE id = ?";

    @Override
    public List<Preview> findAll() {
        List<Preview> previews = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                previews.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todas las vistas previas", e);
        }
        return previews;
    }

    @Override
    public Optional<Preview> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar vista previa con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Preview preview) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, preview.getCaption());
                ps.setString(2, preview.getUrlFile());
                ps.setInt(3, preview.getDeliverable().getId());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        preview.setId(generatedId);
                        conn.commit();
                        return generatedId;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar vista previa", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert vista previa", e);
        }
        return 0;
    }

    @Override
    public void update(Preview preview) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setString(1, preview.getCaption());
                ps.setString(2, preview.getUrlFile());
                ps.setInt(3, preview.getDeliverable().getId());
                ps.setInt(4, preview.getId());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar vista previa con id: " + preview.getId(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update vista previa", e);
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
                throw new RuntimeException("Error al eliminar vista previa con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete vista previa", e);
        }
    }

    private Preview mapRow(ResultSet rs) throws SQLException {
        // Mapear Deliverable
        Deliverable deliverable = new Deliverable();
        deliverable.setId(rs.getInt("deliverable_id"));
        deliverable.setName(rs.getString("deliverable_name"));
        deliverable.setUrlFile(rs.getString("deliverable_url"));
        deliverable.setFileType(rs.getString("file_type"));
        Timestamp delCreatedAt = rs.getTimestamp("deliverable_created_at");
        if (delCreatedAt != null) deliverable.setCreatedAt(delCreatedAt.toLocalDateTime());

        Preview preview = new Preview();
        preview.setId(rs.getInt("id"));
        preview.setCaption(rs.getString("caption"));
        preview.setUrlFile(rs.getString("url_file"));
        preview.setDeliverable(deliverable);

        return preview;
    }
}
