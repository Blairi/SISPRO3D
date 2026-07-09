package com.sispro3d.unam.preview.dao;

import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.preview.domain.Preview;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class PreviewJdbcDAO extends AbstractJdbcDAO<Preview> implements GenericDAO<Preview> {

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
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todas las vistas previas");
    }

    @Override
    public Optional<Preview> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar vista previa con id");
    }

    @Override
    public int insert(Preview preview) {
        return insertWithGeneratedKeys(INSERT,
                ps -> {
                    ps.setString(1, preview.getCaption());
                    ps.setString(2, preview.getUrlFile());
                    ps.setInt(3, preview.getDeliverable().getId());
                },
                preview, Preview::setId, "Error al insertar vista previa");
    }

    @Override
    public void update(Preview preview) {
        executeUpdate(UPDATE,
                ps -> {
                    ps.setString(1, preview.getCaption());
                    ps.setString(2, preview.getUrlFile());
                    ps.setInt(3, preview.getDeliverable().getId());
                    ps.setInt(4, preview.getId());
                },
                "Error al actualizar vista previa con id: " + preview.getId());
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar vista previa con id: " + id);
    }

    private Preview mapRow(ResultSet rs) throws SQLException {
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
