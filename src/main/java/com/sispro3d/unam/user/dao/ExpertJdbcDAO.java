package com.sispro3d.unam.user.dao;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.core.db.ConnectionHandler;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Expert;
import com.sispro3d.unam.user.domain.UserType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpertJdbcDAO implements GenericDAO<Expert> {

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

    private static final String INSERT = "INSERT INTO expert (account_id, specialty, portfolio_url, bio, years_experience) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE expert SET account_id = ?, specialty = ?, portfolio_url = ?, bio = ?, years_experience = ? WHERE account_id = ?";
    private static final String DELETE = "DELETE FROM expert WHERE account_id = ?";

    @Override
    public List<Expert> findAll() {
        List<Expert> experts = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                experts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los expertos", e);
        }
        return experts;
    }

    @Override
    public Optional<Expert> findById(int id) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar experto con id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Expert expert) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT)) {

                ps.setInt(1, expert.getAccount().getIdUser());
                ps.setString(2, expert.getSpecialty());
                ps.setString(3, expert.getPortfolioUrl());
                ps.setString(4, expert.getBio());
                ps.setInt(5, expert.getYearsExperience());
                ps.executeUpdate();
                conn.commit();
                return expert.getAccount().getIdUser();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al insertar experto: " + expert.getAccount().getEmail(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en insert experto", e);
        }
    }

    @Override
    public void update(Expert expert) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {

                ps.setInt(1, expert.getAccount().getIdUser());
                ps.setString(2, expert.getSpecialty());
                ps.setString(3, expert.getPortfolioUrl());
                ps.setString(4, expert.getBio());
                ps.setInt(5, expert.getYearsExperience());
                ps.setInt(6, expert.getAccount().getIdUser());

                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al actualizar experto con id: " + expert.getAccount().getIdUser(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en update experto", e);
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
                throw new RuntimeException("Error al eliminar experto con id: " + id, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en delete experto", e);
        }
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
