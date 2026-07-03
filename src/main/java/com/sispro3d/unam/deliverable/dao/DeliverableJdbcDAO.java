package com.sispro3d.unam.deliverable.dao;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.core.db.ConnectionHandler;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.workorder.domain.WorkOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliverableJdbcDAO implements GenericDAO<Deliverable> {

    private static final String FIND_ALL = """
            SELECT 
                d.id, d.name, d.url_file, d.created_at, d.file_type,
                w.id as workorder_id, w.status, w.started_at, w.completed_at, w.created_at as workorder_created_at,
                q.id as quote_id, q.total_amount, q.valid_until, q.description as quote_description,
                q.created_at as quote_created_at, q.status as quote_status
            FROM deliverable d
            JOIN work_order w ON d.workorder_id = w.id
            JOIN quote q ON w.quote_id = q.id
            """;

    private static final String FIND_BY_ID = FIND_ALL + "WHERE d.id = ?";

    private static final String INSERT = "INSERT INTO deliverable (name, url_file, file_type, workorder_id) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE deliverable SET name = ?, url_file = ?, file_type = ?, workorder_id = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM deliverable WHERE id = ?";

    @Override
    public List<Deliverable> findAll() {
        List<Deliverable> deliverables = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                deliverables.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los entregables", e);
        }
        return deliverables;
    }

    @Override
    public Optional<Deliverable> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar entregable con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Deliverable deliverable) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, deliverable.getName());
                ps.setString(2, deliverable.getUrlFile());
                ps.setString(3, deliverable.getFileType());
                ps.setInt(4, deliverable.getWorkOrder().getId());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        deliverable.setId(generatedId);
                        conn.commit();
                        return generatedId;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar entregable: " + deliverable.getName(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert entregable", e);
        }
        return 0;
    }

    @Override
    public void update(Deliverable deliverable) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setString(1, deliverable.getName());
                ps.setString(2, deliverable.getUrlFile());
                ps.setString(3, deliverable.getFileType());
                ps.setInt(4, deliverable.getWorkOrder().getId());
                ps.setInt(5, deliverable.getId());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar entregable con id: " + deliverable.getId(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update entregable", e);
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
                throw new RuntimeException("Error al eliminar entregable con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete entregable", e);
        }
    }

    private Deliverable mapRow(ResultSet rs) throws SQLException {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(rs.getInt("workorder_id"));
        workOrder.setStatus(rs.getString("status"));
        Timestamp startedAt = rs.getTimestamp("started_at");
        if (startedAt != null) workOrder.setStartedAt(startedAt.toLocalDateTime());
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) workOrder.setCompletedAt(completedAt.toLocalDateTime());
        Timestamp woCreatedAt = rs.getTimestamp("workorder_created_at");
        if (woCreatedAt != null) workOrder.setCreatedAt(woCreatedAt.toLocalDateTime());

        Deliverable deliverable = new Deliverable();
        deliverable.setId(rs.getInt("id"));
        deliverable.setName(rs.getString("name"));
        deliverable.setUrlFile(rs.getString("url_file"));
        deliverable.setFileType(rs.getString("file_type"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) deliverable.setCreatedAt(createdAt.toLocalDateTime());
        deliverable.setWorkOrder(workOrder);

        return deliverable;
    }
}
