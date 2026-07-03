package com.sispro3d.unam.dao;

import com.sispro3d.unam.db.ConnectionHandler;
import com.sispro3d.unam.domain.Account;
import com.sispro3d.unam.domain.Message;
import com.sispro3d.unam.domain.Thread;
import com.sispro3d.unam.domain.UserType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageJdbcDAO implements GenericDAO<Message> {

    private static final String FIND_ALL = """
            SELECT 
                m.id, m.content, m.time_stamp,
                t.id as thread_id, t.workorder_id,
                acc.id_user, acc.name, acc.lastName, acc.email, acc.phone, acc.password, acc.type, acc.created_at
            FROM message m
            JOIN thread t ON m.thread_id = t.id
            JOIN account acc ON m.account_id = acc.id_user
            """;

    private static final String FIND_BY_ID = FIND_ALL + "WHERE m.id = ?";

    private static final String INSERT = "INSERT INTO message (thread_id, account_id, content) VALUES (?, ?, ?)";
    private static final String UPDATE = "UPDATE message SET thread_id = ?, account_id = ?, content = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM message WHERE id = ?";

    @Override
    public List<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                messages.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los mensajes", e);
        }
        return messages;
    }

    @Override
    public Optional<Message> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar mensaje con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Message message) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, message.getThread().getId());
                ps.setInt(2, message.getAccount().getIdUser());
                ps.setString(3, message.getContent());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        message.setId(generatedId);
                        conn.commit();
                        return generatedId;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar mensaje", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert mensaje", e);
        }
        return 0;
    }

    @Override
    public void update(Message message) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setInt(1, message.getThread().getId());
                ps.setInt(2, message.getAccount().getIdUser());
                ps.setString(3, message.getContent());
                ps.setInt(4, message.getId());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar mensaje con id: " + message.getId(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update mensaje", e);
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
                throw new RuntimeException("Error al eliminar mensaje con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete mensaje", e);
        }
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        // Mapear Thread
        Thread thread = new Thread();
        thread.setId(rs.getInt("thread_id"));

        // Mapear Account
        Account account = new Account();
        account.setIdUser(rs.getInt("id_user"));
        account.setName(rs.getString("name"));
        account.setLastName(rs.getString("lastName"));
        account.setEmail(rs.getString("email"));
        account.setPhone(rs.getString("phone"));
        account.setPassword(rs.getString("password"));
        account.setType(UserType.valueOf(rs.getString("type")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) account.setCreatedAt(createdAt.toLocalDateTime());

        Message message = new Message();
        message.setId(rs.getInt("id"));
        message.setContent(rs.getString("content"));
        Timestamp timeStamp = rs.getTimestamp("time_stamp");
        if (timeStamp != null) message.setTimeStamp(timeStamp.toLocalDateTime());
        message.setThread(thread);
        message.setAccount(account);

        return message;
    }
}
