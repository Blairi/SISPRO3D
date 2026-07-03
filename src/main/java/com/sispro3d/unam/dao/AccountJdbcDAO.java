package com.sispro3d.unam.dao;

import com.sispro3d.unam.db.ConnectionHandler;
import com.sispro3d.unam.domain.Account;
import com.sispro3d.unam.domain.UserType;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountJdbcDAO implements GenericDAO<Account> {

    private static final String FIND_ALL   = "SELECT id_user, name, lastName, email, phone, password, type, created_at FROM account";
    private static final String FIND_BY_ID = "SELECT id_user, name, lastName, email, phone, password, type, created_at FROM account WHERE id_user = ?";
    private static final String INSERT     = "INSERT INTO account (name, lastName, email, phone, password, type) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE     = "UPDATE account SET name = ?, lastName = ?, email = ?, phone = ?, password = ?, type = ? WHERE id_user = ?";
    private static final String DELETE     = "DELETE FROM account WHERE id_user = ?";

    @Override
    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los accounts", e);
        }
        return accounts;
    }

    @Override
    public Optional<Account> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar account con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Account account) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, account.getName());
                ps.setString(2, account.getLastName());
                ps.setString(3, account.getEmail());
                ps.setString(4, account.getPhone());
                ps.setString(5, account.getPassword());
                ps.setString(6, account.getType().name());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        account.setIdUser(generatedId);
                        conn.commit();
                        return generatedId;
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar account: " + account.getEmail(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert account", e);
        }
        return 0;
    }

    @Override
    public void update(Account account) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setString(1, account.getName());
                ps.setString(2, account.getLastName());
                ps.setString(3, account.getEmail());
                ps.setString(4, account.getPhone());
                ps.setString(5, account.getPassword());
                ps.setString(6, account.getType().name());
                ps.setInt(7, account.getIdUser());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar account con id: " + account.getIdUser(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update account", e);
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
                throw new RuntimeException("Error al eliminar account con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete account", e);
        }
    }

    // este lo usamos para no repetir la parte de convertir la
    // info de la bd a la clase...
    private Account mapRow(ResultSet rs) throws SQLException {
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
        return account;
    }
}