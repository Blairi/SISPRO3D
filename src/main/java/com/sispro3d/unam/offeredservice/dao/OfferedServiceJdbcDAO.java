package com.sispro3d.unam.offeredservice.dao;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;

import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class OfferedServiceJdbcDAO extends AbstractJdbcDAO<OfferedService> implements GenericDAO<OfferedService> {

    private static final String FIND_ALL = """
            SELECT 
                s.id, s.title, s.description, s.base_price,
                a.id_user as admin_id_user, a.name as admin_name,
                a.lastName as admin_lastName, a.email as admin_email, a.phone as admin_phone,
                a.password as admin_password, a.role as admin_role, a.created_at as admin_created_at,
                e.id_user as expert_id_user, e.name as expert_name,
                e.lastName as expert_lastName, e.email as expert_email, e.phone as expert_phone,
                e.password as expert_password, e.role as expert_role, e.created_at as expert_created_at,
                e.specialty, e.portfolio_url, e.bio, e.years_experience,
                c.id as category_id, c.name as category_name, c.description as category_description,
                s.created_at, s.updated_at, s.delivery_time_days
            FROM service s
            LEFT JOIN account a ON s.id_admin = a.id_user
            JOIN account e ON s.id_expert = e.id_user
            JOIN category c ON s.category_id = c.id
            """;

    private static final String FIND_BY_ID = FIND_ALL + "WHERE s.id = ?";

    private static final String INSERT = """
            INSERT INTO service (title, description, base_price, id_admin, id_expert, category_id, delivery_time_days) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE = """
            UPDATE service SET title = ?, description = ?, base_price = ?, id_admin = ?, id_expert = ?, 
            category_id = ?, delivery_time_days = ? WHERE id = ?
            """;
    private static final String DELETE = "DELETE FROM service WHERE id = ?";

    @Override
    public List<OfferedService> findAll() {
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todos los servicios");
    }

    @Override
    public Optional<OfferedService> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar servicio con id");
    }

    @Override
    public int insert(OfferedService service) {
        return insertWithGeneratedKeys(INSERT,
                ps -> {
                    ps.setString(1, service.getTitle());
                    ps.setString(2, service.getDescription());
                    ps.setBigDecimal(3, service.getBasePrice());
                    ps.setObject(4, service.getAdmin() != null ? service.getAdmin().getIdUser() : null);
                    ps.setInt(5, service.getExpert().getIdUser());
                    ps.setInt(6, service.getCategory().getId());
                    ps.setInt(7, service.getDeliveryTimeDays());
                },
                service, OfferedService::setId, "Error al insertar servicio: " + service.getTitle());
    }

    @Override
    public void update(OfferedService service) {
        executeUpdate(UPDATE,
                ps -> {
                    ps.setString(1, service.getTitle());
                    ps.setString(2, service.getDescription());
                    ps.setBigDecimal(3, service.getBasePrice());
                    ps.setObject(4, service.getAdmin() != null ? service.getAdmin().getIdUser() : null);
                    ps.setInt(5, service.getExpert().getIdUser());
                    ps.setInt(6, service.getCategory().getId());
                    ps.setInt(7, service.getDeliveryTimeDays());
                    ps.setInt(8, service.getId());
                },
                "Error al actualizar servicio con id: " + service.getId());
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar servicio con id: " + id);
    }

    private OfferedService mapRow(ResultSet rs) throws SQLException {
        OfferedService service = new OfferedService();
        service.setId(rs.getInt("id"));
        service.setTitle(rs.getString("title"));
        service.setDescription(rs.getString("description"));
        service.setBasePrice(rs.getBigDecimal("base_price"));
        service.setDeliveryTimeDays(rs.getInt("delivery_time_days"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) service.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) service.setUpdatedAt(updatedAt.toLocalDateTime());

        if (rs.getObject("admin_id_user") != null) {
            Account admin = new Account();
            admin.setIdUser(rs.getInt("admin_id_user"));
            admin.setName(rs.getString("admin_name"));
            admin.setLastName(rs.getString("admin_lastName"));
            admin.setEmail(rs.getString("admin_email"));
            admin.setPhone(rs.getString("admin_phone"));
            admin.setPassword(rs.getString("admin_password"));
            admin.setRole(Role.valueOf(rs.getString("admin_role")));
            Timestamp adminCreatedAt = rs.getTimestamp("admin_created_at");
            if (adminCreatedAt != null) admin.setCreatedAt(adminCreatedAt.toLocalDateTime());
            service.setAdmin(admin);
        }

        Account expert = new Account();
        expert.setIdUser(rs.getInt("expert_id_user"));
        expert.setName(rs.getString("expert_name"));
        expert.setLastName(rs.getString("expert_lastName"));
        expert.setEmail(rs.getString("expert_email"));
        expert.setPhone(rs.getString("expert_phone"));
        expert.setPassword(rs.getString("expert_password"));
        expert.setRole(Role.valueOf(rs.getString("expert_role")));
        Timestamp expertCreatedAt = rs.getTimestamp("expert_created_at");
        if (expertCreatedAt != null) expert.setCreatedAt(expertCreatedAt.toLocalDateTime());
        expert.setSpecialty(rs.getString("specialty"));
        expert.setPortfolioUrl(rs.getString("portfolio_url"));
        expert.setBio(rs.getString("bio"));
        expert.setYearsExperience((Integer) rs.getObject("years_experience"));
        service.setExpert(expert);

        Category category = new Category();
        category.setId(rs.getInt("category_id"));
        category.setName(rs.getString("category_name"));
        category.setDescription(rs.getString("category_description"));
        service.setCategory(category);

        return service;
    }
}
