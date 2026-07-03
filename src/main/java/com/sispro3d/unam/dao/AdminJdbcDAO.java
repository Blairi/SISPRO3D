package com.sispro3d.unam.dao;

import com.sispro3d.unam.db.ConnectionHandler;
import com.sispro3d.unam.domain.Account;
import com.sispro3d.unam.domain.Admin;
import com.sispro3d.unam.domain.UserType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminJdbcDAO implements GenericDAO<Admin> {

    private static final String FIND_ALL = """
            SELECT 
                a.account_id,
                acc.id_user, acc.name, acc.lastName, acc.email, acc.phone, acc.password, acc.type, acc.created_at
            FROM admin a
            JOIN account acc ON a.account_id = acc.id_user
            """;

    private static final String FIND_BY_ID = """
            SELECT 
                a.account_id,
                acc.id_user, acc.name, acc.lastName, acc.email, acc.phone, acc.password, acc.type, acc.created_at
            FROM admin a
            JOIN account acc ON a.account_id = acc.id_user
            WHERE a.account_id = ?
            """;

    private static final String INSERT = "INSERT INTO admin (account_id) VALUES (?)";
    private static final String UPDATE = "UPDATE admin SET account_id = ? WHERE account_id = ?";
    private static final String DELETE = "DELETE FROM admin WHERE account_id = ?";

    @Override
    public List<Admin> findAll() {
        List<Admin> admins = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                admins.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los admins", e);
        }
        return admins;
    }

    @Override
    public Optional<Admin> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar admin con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Admin admin) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT)) {

                ps.setInt(1, admin.getAccount().getIdUser());
                ps.executeUpdate();
                conn.commit();
                return admin.getAccount().getIdUser();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar admin: " + admin.getAccount().getEmail(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert admin", e);
        }
    }

    @Override
    public void update(Admin admin) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setInt(1, admin.getAccount().getIdUser());
                ps.setInt(2, admin.getAccount().getIdUser());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar admin con id: " + admin.getAccount().getIdUser(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update admin", e);
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
                throw new RuntimeException("Error al eliminar admin con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete admin", e);
        }
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setIdUser(rs.getInt("id_user"));
        account.setName(rs.getString("name"));
        account.setLastName(rs.getString("lastName"));
        account.setEmail(rs.getString("email"));
        account.setPhone(rs.getString("phone"));
        account.setPassword(rs.getString("password"));
        account.setType(UserType.valueOf(rs.getString("type")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) account.setCreatedAt(ts.toLocalDateTime());

        Admin admin = new Admin();
        admin.setAccount(account);
        return admin;
    }
}
