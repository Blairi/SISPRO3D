package com.sispro3d.unam.user.dao;

import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountJdbcDAO extends AbstractJdbcDAO<Account> implements GenericDAO<Account> {

    private static final String FIND_ALL   = "SELECT id_user, name, lastName, email, phone, password, role, specialty, portfolio_url, bio, years_experience, created_at FROM account";
    private static final String FIND_BY_ID = "SELECT id_user, name, lastName, email, phone, password, role, specialty, portfolio_url, bio, years_experience, created_at FROM account WHERE id_user = ?";
    private static final String INSERT     = "INSERT INTO account (name, lastName, email, phone, password, role, specialty, portfolio_url, bio, years_experience) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE     = "UPDATE account SET name = ?, lastName = ?, email = ?, phone = ?, password = ?, role = ?, specialty = ?, portfolio_url = ?, bio = ?, years_experience = ? WHERE id_user = ?";
    private static final String DELETE     = "DELETE FROM account WHERE id_user = ?";

    @Override
    public List<Account> findAll() {
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todos los accounts");
    }

    @Override
    public Optional<Account> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar account con id");
    }

    @Override
    public int insert(Account account) {
        return insertWithGeneratedKeys(INSERT,
                ps -> {
                    ps.setString(1, account.getName());
                    ps.setString(2, account.getLastName());
                    ps.setString(3, account.getEmail());
                    ps.setString(4, account.getPhone());
                    ps.setString(5, account.getPassword());
                    ps.setString(6, account.getRole().name());
                    ps.setString(7, account.getSpecialty());
                    ps.setString(8, account.getPortfolioUrl());
                    ps.setString(9, account.getBio());
                    ps.setObject(10, account.getYearsExperience());
                },
                account, Account::setIdUser, "Error al insertar account: " + account.getEmail());
    }

    @Override
    public void update(Account account) {
        executeUpdate(UPDATE,
                ps -> {
                    ps.setString(1, account.getName());
                    ps.setString(2, account.getLastName());
                    ps.setString(3, account.getEmail());
                    ps.setString(4, account.getPhone());
                    ps.setString(5, account.getPassword());
                    ps.setString(6, account.getRole().name());
                    ps.setString(7, account.getSpecialty());
                    ps.setString(8, account.getPortfolioUrl());
                    ps.setString(9, account.getBio());
                    ps.setObject(10, account.getYearsExperience());
                    ps.setInt(11, account.getIdUser());
                },
                "Error al actualizar account con id: " + account.getIdUser());
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar account con id: " + id);
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setIdUser(rs.getInt("id_user"));
        account.setName(rs.getString("name"));
        account.setLastName(rs.getString("lastName"));
        account.setEmail(rs.getString("email"));
        account.setPhone(rs.getString("phone"));
        account.setPassword(rs.getString("password"));
        account.setRole(Role.valueOf(rs.getString("role")));
        account.setSpecialty(rs.getString("specialty"));
        account.setPortfolioUrl(rs.getString("portfolio_url"));
        account.setBio(rs.getString("bio"));
        account.setYearsExperience((Integer) rs.getObject("years_experience"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) account.setCreatedAt(ts.toLocalDateTime());
        return account;
    }
}
