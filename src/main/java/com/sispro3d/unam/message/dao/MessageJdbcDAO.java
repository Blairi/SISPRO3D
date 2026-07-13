package com.sispro3d.unam.message.dao;

import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class MessageJdbcDAO extends AbstractJdbcDAO<Message> implements GenericDAO<Message> {

    private static final String FIND_ALL = """
            SELECT 
                m.id, m.content, m.time_stamp,
                t.id as thread_id, t.workorder_id,
                 acc.id_user, acc.name, acc.lastName, acc.email, acc.phone, acc.password, acc.role, acc.created_at
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
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todos los mensajes");
    }

    @Override
    public Optional<Message> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar mensaje con id");
    }

    @Override
    public int insert(Message message) {
        return insertWithGeneratedKeys(INSERT,
                ps -> {
                    ps.setInt(1, message.getThread().getId());
                    ps.setInt(2, message.getAccount().getIdUser());
                    ps.setString(3, message.getContent());
                },
                message, Message::setId, "Error al insertar mensaje");
    }

    @Override
    public void update(Message message) {
        executeUpdate(UPDATE,
                ps -> {
                    ps.setInt(1, message.getThread().getId());
                    ps.setInt(2, message.getAccount().getIdUser());
                    ps.setString(3, message.getContent());
                    ps.setInt(4, message.getId());
                },
                "Error al actualizar mensaje con id: " + message.getId());
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar mensaje con id: " + id);
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Thread thread = new Thread();
        thread.setId(rs.getInt("thread_id"));

        Account account = new Account();
        account.setIdUser(rs.getInt("id_user"));
        account.setName(rs.getString("name"));
        account.setLastName(rs.getString("lastName"));
        account.setEmail(rs.getString("email"));
        account.setPhone(rs.getString("phone"));
        account.setPassword(rs.getString("password"));
        account.setRole(Role.valueOf(rs.getString("role")));
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
