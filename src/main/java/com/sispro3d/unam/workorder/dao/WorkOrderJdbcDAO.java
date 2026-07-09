package com.sispro3d.unam.workorder.dao;

import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkOrderJdbcDAO extends AbstractJdbcDAO<WorkOrder> implements GenericDAO<WorkOrder> {

    private static final String FIND_ALL = """
            SELECT 
                w.id, w.status, w.started_at, w.completed_at, w.created_at,
                q.id as id_quote, q.total_amount, q.valid_until, q.description, q.created_at as quote_created_at, q.status as quote_status
            FROM work_order w
            LEFT JOIN quote q ON w.id_quote = q.id
            """;

    private static final String FIND_BY_ID = FIND_ALL + "WHERE w.id = ?";

    private static final String INSERT = "INSERT INTO work_order (status, id_quote) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE work_order SET status = ?, started_at = ?, completed_at = ?, id_quote = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM work_order WHERE id = ?";

    @Override
    public List<WorkOrder> findAll() {
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todas las órdenes de trabajo");
    }

    @Override
    public Optional<WorkOrder> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar orden de trabajo con id");
    }

    @Override
    public int insert(WorkOrder workOrder) {
        return insertWithGeneratedKeys(INSERT,
                ps -> {
                    ps.setString(1, workOrder.getStatus());
                    ps.setObject(2, workOrder.getQuote() != null ? workOrder.getQuote().getId() : null);
                },
                workOrder, WorkOrder::setId, "Error al insertar orden de trabajo");
    }

    @Override
    public void update(WorkOrder workOrder) {
        executeUpdate(UPDATE,
                ps -> {
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
                },
                "Error al actualizar orden de trabajo con id: " + workOrder.getId());
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar orden de trabajo con id: " + id);
    }

    private WorkOrder mapRow(ResultSet rs) throws SQLException {
        Quote quote = null;
        if (rs.getObject("id_quote") != null) {
            quote = new Quote();
            quote.setId(rs.getInt("id_quote"));
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
