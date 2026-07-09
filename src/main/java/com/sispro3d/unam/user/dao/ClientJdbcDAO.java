package com.sispro3d.unam.user.dao;

import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Client;
import com.sispro3d.unam.user.domain.UserType;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ClientJdbcDAO extends AbstractJdbcDAO<Client> implements GenericDAO<Client> {

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
    private static final String DELETE = "DELETE FROM client WHERE account_id = ?";

    @Override
    public List<Client> findAll() {
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todos los clientes");
    }

    @Override
    public Optional<Client> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar cliente con id");
    }

    @Override
    public int insert(Client client) {
        executeUpdate(INSERT,
                ps -> ps.setInt(1, client.getAccount().getIdUser()),
                "Error al insertar cliente: " + client.getAccount().getEmail());
        return client.getAccount().getIdUser();
    }

    @Override
    public void update(Client client) {
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar cliente con id: " + id);
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
