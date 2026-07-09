package com.sispro3d.unam.thread.dao;

import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.workorder.domain.WorkOrder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class ThreadJdbcDAO extends AbstractJdbcDAO<Thread> implements GenericDAO<Thread> {

    private static final String FIND_ALL = """
            SELECT 
                t.id, t.workorder_id,
                w.id as workorder_id_full, w.status, w.started_at, w.completed_at, w.created_at as workorder_created_at
            FROM thread t
            JOIN work_order w ON t.workorder_id = w.id
            """;

    private static final String FIND_BY_ID = FIND_ALL + "WHERE t.id = ?";

    private static final String INSERT = "INSERT INTO thread (workorder_id) VALUES (?)";
    private static final String UPDATE = "UPDATE thread SET workorder_id = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM thread WHERE id = ?";

    @Override
    public List<Thread> findAll() {
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todos los hilos");
    }

    @Override
    public Optional<Thread> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar hilo con id");
    }

    @Override
    public int insert(Thread thread) {
        return insertWithGeneratedKeys(INSERT,
                ps -> ps.setInt(1, thread.getWorkOrder().getId()),
                thread, Thread::setId, "Error al insertar hilo");
    }

    @Override
    public void update(Thread thread) {
        executeUpdate(UPDATE,
                ps -> {
                    ps.setInt(1, thread.getWorkOrder().getId());
                    ps.setInt(2, thread.getId());
                },
                "Error al actualizar hilo con id: " + thread.getId());
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar hilo con id: " + id);
    }

    private Thread mapRow(ResultSet rs) throws SQLException {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(rs.getInt("workorder_id_full"));
        workOrder.setStatus(rs.getString("status"));
        Timestamp startedAt = rs.getTimestamp("started_at");
        if (startedAt != null) workOrder.setStartedAt(startedAt.toLocalDateTime());
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) workOrder.setCompletedAt(completedAt.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("workorder_created_at");
        if (createdAt != null) workOrder.setCreatedAt(createdAt.toLocalDateTime());

        Thread thread = new Thread();
        thread.setId(rs.getInt("id"));
        thread.setWorkOrder(workOrder);

        return thread;
    }
}
