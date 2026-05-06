package mx.unam.dgtic.dao;

import mx.unam.dgtic.db.ConnectionHandler;
import mx.unam.dgtic.domain.Thread;
import mx.unam.dgtic.domain.WorkOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThreadJdbcDAO implements GenericDAO<Thread> {

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
        List<Thread> threads = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                threads.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los hilos", e);
        }
        return threads;
    }

    @Override
    public Optional<Thread> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar hilo con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Thread thread) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, thread.getWorkOrder().getId());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        thread.setId(generatedId);
                        conn.commit();
                        return generatedId;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar hilo", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert hilo", e);
        }
        return 0;
    }

    @Override
    public void update(Thread thread) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setInt(1, thread.getWorkOrder().getId());
                ps.setInt(2, thread.getId());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar hilo con id: " + thread.getId(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update hilo", e);
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
                throw new RuntimeException("Error al eliminar hilo con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete hilo", e);
        }
    }

    private Thread mapRow(ResultSet rs) throws SQLException {
        // Mapear WorkOrder
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
