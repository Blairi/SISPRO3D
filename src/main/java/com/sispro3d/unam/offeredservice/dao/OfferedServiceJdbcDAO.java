package com.sispro3d.unam.offeredservice.dao;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Admin;
import com.sispro3d.unam.user.domain.Expert;
import com.sispro3d.unam.user.domain.UserType;

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
                a.id_user as admin_account_id, acc1.id_user as admin_id_user, acc1.name as admin_name, 
                acc1.lastName as admin_lastName, acc1.email as admin_email, acc1.phone as admin_phone, 
                acc1.password as admin_password, acc1.type as admin_type, acc1.created_at as admin_created_at,
                e.id_user as expert_account_id, acc2.id_user as expert_id_user, acc2.name as expert_name,
                acc2.lastName as expert_lastName, acc2.email as expert_email, acc2.phone as expert_phone,
                acc2.password as expert_password, acc2.type as expert_type, acc2.created_at as expert_created_at,
                e.specialty, e.portfolio_url, e.bio, e.years_experience,
                c.id as category_id, c.name as category_name, c.description as category_description,
                s.created_at, s.updated_at, s.delivery_time_days
            FROM service s
            LEFT JOIN admin a ON s.id_admin = a.id_user
            LEFT JOIN account acc1 ON a.id_user = acc1.id_user
            JOIN expert e ON s.id_expert = e.id_user
            JOIN account acc2 ON e.id_user = acc2.id_user
            JOIN category c ON s.category_id = c.id
            """;

    private static final String FIND_BY_ID = """
            SELECT 
                s.id, s.title, s.description, s.base_price,
                a.id_user as admin_account_id, acc1.id_user as admin_id_user, acc1.name as admin_name, 
                acc1.lastName as admin_lastName, acc1.email as admin_email, acc1.phone as admin_phone, 
                acc1.password as admin_password, acc1.type as admin_type, acc1.created_at as admin_created_at,
                e.id_user as expert_account_id, acc2.id_user as expert_id_user, acc2.name as expert_name,
                acc2.lastName as expert_lastName, acc2.email as expert_email, acc2.phone as expert_phone,
                acc2.password as expert_password, acc2.type as expert_type, acc2.created_at as expert_created_at,
                e.specialty, e.portfolio_url, e.bio, e.years_experience,
                c.id as category_id, c.name as category_name, c.description as category_description,
                s.created_at, s.updated_at, s.delivery_time_days
            FROM service s
            LEFT JOIN admin a ON s.id_admin = a.id_user
            LEFT JOIN account acc1 ON a.id_user = acc1.id_user
            JOIN expert e ON s.id_expert = e.id_user
            JOIN account acc2 ON e.id_user = acc2.id_user
            JOIN category c ON s.category_id = c.id
            WHERE s.id = ?
            """;

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
                    ps.setObject(4, service.getAdmin() != null ? service.getAdmin().getAccount().getIdUser() : null);
                    ps.setInt(5, service.getExpert().getAccount().getIdUser());
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
                    ps.setObject(4, service.getAdmin() != null ? service.getAdmin().getAccount().getIdUser() : null);
                    ps.setInt(5, service.getExpert().getAccount().getIdUser());
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
            Account adminAccount = new Account();
            adminAccount.setIdUser(rs.getInt("admin_id_user"));
            adminAccount.setName(rs.getString("admin_name"));
            adminAccount.setLastName(rs.getString("admin_lastName"));
            adminAccount.setEmail(rs.getString("admin_email"));
            adminAccount.setPhone(rs.getString("admin_phone"));
            adminAccount.setPassword(rs.getString("admin_password"));
            adminAccount.setType(UserType.valueOf(rs.getString("admin_type")));
            Timestamp adminCreatedAt = rs.getTimestamp("admin_created_at");
            if (adminCreatedAt != null) adminAccount.setCreatedAt(adminCreatedAt.toLocalDateTime());

            Admin admin = new Admin();
            admin.setAccount(adminAccount);
            service.setAdmin(admin);
        }

        Account expertAccount = new Account();
        expertAccount.setIdUser(rs.getInt("expert_id_user"));
        expertAccount.setName(rs.getString("expert_name"));
        expertAccount.setLastName(rs.getString("expert_lastName"));
        expertAccount.setEmail(rs.getString("expert_email"));
        expertAccount.setPhone(rs.getString("expert_phone"));
        expertAccount.setPassword(rs.getString("expert_password"));
        expertAccount.setType(UserType.valueOf(rs.getString("expert_type")));
        Timestamp expertCreatedAt = rs.getTimestamp("expert_created_at");
        if (expertCreatedAt != null) expertAccount.setCreatedAt(expertCreatedAt.toLocalDateTime());

        Expert expert = new Expert();
        expert.setAccount(expertAccount);
        expert.setSpecialty(rs.getString("specialty"));
        expert.setPortfolioUrl(rs.getString("portfolio_url"));
        expert.setBio(rs.getString("bio"));
        expert.setYearsExperience(rs.getInt("years_experience"));
        service.setExpert(expert);

        Category category = new Category();
        category.setId(rs.getInt("category_id"));
        category.setName(rs.getString("category_name"));
        category.setDescription(rs.getString("category_description"));
        service.setCategory(category);

        return service;
    }
}
