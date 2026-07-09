package com.sispro3d.unam.user.dao;

import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Expert;
import com.sispro3d.unam.user.domain.UserType;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ExpertJdbcDAO extends AbstractJdbcDAO<Expert> implements GenericDAO<Expert> {

    private static final String FIND_ALL = """
            SELECT 
                e.account_id,
                acc.id_user, acc.name, acc.lastName, acc.email, acc.phone, acc.password, acc.type, acc.created_at,
                e.specialty, e.portfolio_url, e.bio, e.years_experience
            FROM expert e
            JOIN account acc ON e.account_id = acc.id_user
            """;

    private static final String FIND_BY_ID = """
            SELECT 
                e.account_id,
                acc.id_user, acc.name, acc.lastName, acc.email, acc.phone, acc.password, acc.type, acc.created_at,
                e.specialty, e.portfolio_url, e.bio, e.years_experience
            FROM expert e
            JOIN account acc ON e.account_id = acc.id_user
            WHERE e.account_id = ?
            """;

    private static final String INSERT = "INSERT INTO expert (id_user, specialty, portfolio_url, bio, years_experience) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE expert SET specialty = ?, portfolio_url = ?, bio = ?, years_experience = ? WHERE id_user = ?";
    private static final String DELETE = "DELETE FROM expert WHERE id_user = ?";

    @Override
    public List<Expert> findAll() {
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todos los expertos");
    }

    @Override
    public Optional<Expert> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar experto con id");
    }

    @Override
    public int insert(Expert expert) {
        executeUpdate(INSERT,
                ps -> {
                    ps.setInt(1, expert.getAccount().getIdUser());
                    ps.setString(2, expert.getSpecialty());
                    ps.setString(3, expert.getPortfolioUrl());
                    ps.setString(4, expert.getBio());
                    ps.setInt(5, expert.getYearsExperience());
                },
                "Error al insertar experto: " + expert.getAccount().getEmail());
        return expert.getAccount().getIdUser();
    }

    @Override
    public void update(Expert expert) {
        executeUpdate(UPDATE,
                ps -> {
                    ps.setString(1, expert.getSpecialty());
                    ps.setString(2, expert.getPortfolioUrl());
                    ps.setString(3, expert.getBio());
                    ps.setInt(4, expert.getYearsExperience());
                    ps.setInt(5, expert.getAccount().getIdUser());
                },
                "Error al actualizar experto con id: " + expert.getAccount().getIdUser());
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar experto con id: " + id);
    }

    private Expert mapRow(ResultSet rs) throws SQLException {
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

        Expert expert = new Expert();
        expert.setAccount(account);
        expert.setSpecialty(rs.getString("specialty"));
        expert.setPortfolioUrl(rs.getString("portfolio_url"));
        expert.setBio(rs.getString("bio"));
        expert.setYearsExperience(rs.getInt("years_experience"));
        return expert;
    }
}
