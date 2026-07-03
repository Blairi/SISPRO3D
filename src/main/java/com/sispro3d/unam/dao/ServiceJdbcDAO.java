package com.sispro3d.unam.dao;

import com.sispro3d.unam.db.ConnectionHandler;
import com.sispro3d.unam.domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceJdbcDAO implements GenericDAO<Service> {

    private static final String FIND_ALL = """
            SELECT 
                s.id, s.title, s.description, s.base_price,
                a.account_id as admin_account_id, acc1.id_user as admin_id_user, acc1.name as admin_name, 
                acc1.lastName as admin_lastName, acc1.email as admin_email, acc1.phone as admin_phone, 
                acc1.password as admin_password, acc1.type as admin_type, acc1.created_at as admin_created_at,
                e.account_id as expert_account_id, acc2.id_user as expert_id_user, acc2.name as expert_name,
                acc2.lastName as expert_lastName, acc2.email as expert_email, acc2.phone as expert_phone,
                acc2.password as expert_password, acc2.type as expert_type, acc2.created_at as expert_created_at,
                e.specialty, e.portfolio_url, e.bio, e.years_experience,
                c.id as category_id, c.name as category_name, c.description as category_description,
                s.created_at, s.updated_at, s.delivery_time_days
            FROM service s
            LEFT JOIN admin a ON s.admin_id = a.account_id
            LEFT JOIN account acc1 ON a.account_id = acc1.id_user
            JOIN expert e ON s.expert_id = e.account_id
            JOIN account acc2 ON e.account_id = acc2.id_user
            JOIN category c ON s.category_id = c.id
            """;

    private static final String FIND_BY_ID = """
            SELECT 
                s.id, s.title, s.description, s.base_price,
                a.account_id as admin_account_id, acc1.id_user as admin_id_user, acc1.name as admin_name, 
                acc1.lastName as admin_lastName, acc1.email as admin_email, acc1.phone as admin_phone, 
                acc1.password as admin_password, acc1.type as admin_type, acc1.created_at as admin_created_at,
                e.account_id as expert_account_id, acc2.id_user as expert_id_user, acc2.name as expert_name,
                acc2.lastName as expert_lastName, acc2.email as expert_email, acc2.phone as expert_phone,
                acc2.password as expert_password, acc2.type as expert_type, acc2.created_at as expert_created_at,
                e.specialty, e.portfolio_url, e.bio, e.years_experience,
                c.id as category_id, c.name as category_name, c.description as category_description,
                s.created_at, s.updated_at, s.delivery_time_days
            FROM service s
            LEFT JOIN admin a ON s.admin_id = a.account_id
            LEFT JOIN account acc1 ON a.account_id = acc1.id_user
            JOIN expert e ON s.expert_id = e.account_id
            JOIN account acc2 ON e.account_id = acc2.id_user
            JOIN category c ON s.category_id = c.id
            WHERE s.id = ?
            """;

    private static final String INSERT = """
            INSERT INTO service (title, description, base_price, admin_id, expert_id, category_id, delivery_time_days) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE = """
            UPDATE service SET title = ?, description = ?, base_price = ?, admin_id = ?, expert_id = ?, 
            category_id = ?, delivery_time_days = ? WHERE id = ?
            """;
    private static final String DELETE = "DELETE FROM service WHERE id = ?";

    @Override
    public List<Service> findAll() {
        List<Service> services = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                services.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los servicios", e);
        }
        return services;
    }

    @Override
    public Optional<Service> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar servicio con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Service service) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, service.getTitle());
                ps.setString(2, service.getDescription());
                ps.setBigDecimal(3, service.getBasePrice());
                ps.setObject(4, service.getAdmin() != null ? service.getAdmin().getAccount().getIdUser() : null);
                ps.setInt(5, service.getExpert().getAccount().getIdUser());
                ps.setInt(6, service.getCategory().getId());
                ps.setInt(7, service.getDeliveryTimeDays());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        service.setId(generatedId);
                        conn.commit();
                        return generatedId;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar servicio: " + service.getTitle(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert servicio", e);
        }
        return 0;
    }

    @Override
    public void update(Service service) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setString(1, service.getTitle());
                ps.setString(2, service.getDescription());
                ps.setBigDecimal(3, service.getBasePrice());
                ps.setObject(4, service.getAdmin() != null ? service.getAdmin().getAccount().getIdUser() : null);
                ps.setInt(5, service.getExpert().getAccount().getIdUser());
                ps.setInt(6, service.getCategory().getId());
                ps.setInt(7, service.getDeliveryTimeDays());
                ps.setInt(8, service.getId());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar servicio con id: " + service.getId(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update servicio", e);
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
                throw new RuntimeException("Error al eliminar servicio con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete servicio", e);
        }
    }

    private Service mapRow(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setId(rs.getInt("id"));
        service.setTitle(rs.getString("title"));
        service.setDescription(rs.getString("description"));
        service.setBasePrice(rs.getBigDecimal("base_price"));
        service.setDeliveryTimeDays(rs.getInt("delivery_time_days"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) service.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) service.setUpdatedAt(updatedAt.toLocalDateTime());

        // Mapear Admin si existe
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

        // Mapear Expert
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

        // Mapear Category
        Category category = new Category();
        category.setId(rs.getInt("category_id"));
        category.setName(rs.getString("category_name"));
        category.setDescription(rs.getString("category_description"));
        service.setCategory(category);

        return service;
    }
}
