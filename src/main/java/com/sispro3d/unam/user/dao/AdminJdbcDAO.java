package com.sispro3d.unam.user.dao;

import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Admin;
import com.sispro3d.unam.user.domain.UserType;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminJdbcDAO extends AbstractJdbcDAO<Admin> implements GenericDAO<Admin> {

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
    private static final String DELETE = "DELETE FROM admin WHERE account_id = ?";

    @Override
    public List<Admin> findAll() {
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todos los admins");
    }

    @Override
    public Optional<Admin> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar admin con id");
    }

    @Override
    public int insert(Admin admin) {
        executeUpdate(INSERT,
                ps -> ps.setInt(1, admin.getAccount().getIdUser()),
                "Error al insertar admin: " + admin.getAccount().getEmail());
        return admin.getAccount().getIdUser();
    }

    @Override
    public void update(Admin admin) {
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar admin con id: " + id);
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
