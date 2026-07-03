package com.sispro3d.unam.dao;

import com.sispro3d.unam.db.ConnectionHandler;
import com.sispro3d.unam.domain.Account;
import com.sispro3d.unam.domain.Client;
import com.sispro3d.unam.domain.UserType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientJdbcDAO implements GenericDAO<Client> {

    private static final String FIND_ALL = """
            SELECT 
                c.account_id,
                acc.id_user, acc.name, acc.lastName, acc.email, acc.phone, acc.password, acc.type, acc.created_at
            FROM client c
            JOIN account acc ON c.account_id = acc.id_user
            """;

    private static final String FIND_BY_ID = """
            SELECT 
                c.account_id,
                acc.id_user, acc.name, acc.lastName, acc.email, acc.phone, acc.password, acc.type, acc.created_at
            FROM client c
            JOIN account acc ON c.account_id = acc.id_user
            WHERE c.account_id = ?
            """;

    private static final String INSERT = "INSERT INTO client (account_id) VALUES (?)";
    private static final String UPDATE = "UPDATE client SET account_id = ? WHERE account_id = ?";
    private static final String DELETE = "DELETE FROM client WHERE account_id = ?";

    @Override
    public List<Client> findAll() {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clients.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los clientes", e);
        }
        return clients;
    }

    @Override
    public Optional<Client> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Client client) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT)) {

                ps.setInt(1, client.getAccount().getIdUser());
                ps.executeUpdate();
                conn.commit();
                return client.getAccount().getIdUser();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar cliente: " + client.getAccount().getEmail(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert cliente", e);
        }
    }

    @Override
    public void update(Client client) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setInt(1, client.getAccount().getIdUser());
                ps.setInt(2, client.getAccount().getIdUser());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar cliente con id: " + client.getAccount().getIdUser(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update cliente", e);
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
                throw new RuntimeException("Error al eliminar cliente con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete cliente", e);
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
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

        Client client = new Client();
        client.setAccount(account);
        return client;
    }
}
