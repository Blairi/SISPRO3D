package com.sispro3d.unam.deliverable.dao;

import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class DeliverableJdbcDAO extends AbstractJdbcDAO<Deliverable> implements GenericDAO<Deliverable> {

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
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todos los entregables");
    }

    @Override
    public Optional<Deliverable> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar entregable con id");
    }

    @Override
    public int insert(Deliverable deliverable) {
        return insertWithGeneratedKeys(INSERT,
                ps -> {
                    ps.setString(1, deliverable.getName());
                    ps.setString(2, deliverable.getUrlFile());
                    ps.setString(3, deliverable.getFileType());
                    ps.setInt(4, deliverable.getWorkOrder().getId());
                },
                deliverable, Deliverable::setId, "Error al insertar entregable: " + deliverable.getName());
    }

    @Override
    public void update(Deliverable deliverable) {
        executeUpdate(UPDATE,
                ps -> {
                    ps.setString(1, deliverable.getName());
                    ps.setString(2, deliverable.getUrlFile());
                    ps.setString(3, deliverable.getFileType());
                    ps.setInt(4, deliverable.getWorkOrder().getId());
                    ps.setInt(5, deliverable.getId());
                },
                "Error al actualizar entregable con id: " + deliverable.getId());
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar entregable con id: " + id);
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
