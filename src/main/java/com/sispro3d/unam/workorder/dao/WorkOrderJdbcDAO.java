package com.sispro3d.unam.workorder.dao;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.core.db.ConnectionHandler;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.workorder.domain.WorkOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkOrderJdbcDAO implements GenericDAO<WorkOrder> {

    private static final String FIND_ALL = """
            SELECT 
                w.id, w.status, w.started_at, w.completed_at, w.created_at,
                q.id as quote_id, q.total_amount, q.valid_until, q.description, q.created_at as quote_created_at, q.status as quote_status
            FROM work_order w
            LEFT JOIN quote q ON w.quote_id = q.id
            """;

    private static final String FIND_BY_ID = FIND_ALL + "WHERE w.id = ?";

    private static final String INSERT = "INSERT INTO work_order (status, quote_id) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE work_order SET status = ?, started_at = ?, completed_at = ?, quote_id = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM work_order WHERE id = ?";

    @Override
    public List<WorkOrder> findAll() {
        List<WorkOrder> workOrders = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                workOrders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todas las órdenes de trabajo", e);
        }
        return workOrders;
    }

    @Override
    public Optional<WorkOrder> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar orden de trabajo con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(WorkOrder workOrder) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, workOrder.getStatus());
                ps.setObject(2, workOrder.getQuote() != null ? workOrder.getQuote().getId() : null);

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        workOrder.setId(generatedId);
                        conn.commit();
                        return generatedId;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar orden de trabajo", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert orden de trabajo", e);
        }
        return 0;
    }

    @Override
    public void update(WorkOrder workOrder) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setString(1, workOrder.getStatus());
                Timestamp startedAt = null;
                if (workOrder.getStartedAt() != null) {
                    startedAt = Timestamp.valueOf(workOrder.getStartedAt());
                }
                ps.setObject(2, startedAt);

                Timestamp completedAt = null;
                if (workOrder.getCompletedAt() != null) {
                    completedAt = Timestamp.valueOf(workOrder.getCompletedAt());
                }
                ps.setObject(3, completedAt);

                ps.setObject(4, workOrder.getQuote() != null ? workOrder.getQuote().getId() : null);
                ps.setInt(5, workOrder.getId());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar orden de trabajo con id: " + workOrder.getId(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update orden de trabajo", e);
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
                throw new RuntimeException("Error al eliminar orden de trabajo con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete orden de trabajo", e);
        }
    }

    private WorkOrder mapRow(ResultSet rs) throws SQLException {
        Quote quote = null;
        if (rs.getObject("quote_id") != null) {
            quote = new Quote();
            quote.setId(rs.getInt("quote_id"));
            quote.setStatus(rs.getString("quote_status"));
            quote.setTotalAmount(rs.getBigDecimal("total_amount"));
            quote.setValidUntil(rs.getDate("valid_until").toLocalDate());
            quote.setDescription(rs.getString("description"));
            Timestamp qCreatedAt = rs.getTimestamp("quote_created_at");
            if (qCreatedAt != null) quote.setCreatedAt(qCreatedAt.toLocalDateTime());
        }

        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(rs.getInt("id"));
        workOrder.setStatus(rs.getString("status"));
        Timestamp startedAt = rs.getTimestamp("started_at");
        if (startedAt != null) workOrder.setStartedAt(startedAt.toLocalDateTime());
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) workOrder.setCompletedAt(completedAt.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) workOrder.setCreatedAt(createdAt.toLocalDateTime());
        workOrder.setQuote(quote);

        return workOrder;
    }
}
