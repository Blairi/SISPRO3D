package com.sispro3d.unam.core.dao;

import com.sispro3d.unam.core.db.ConnectionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class AbstractJdbcDAO<T> {

    protected List<T> findAll(String sql, RowMapper<T> mapper, String errorMessage) {
        List<T> results = new ArrayList<>();
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                results.add(mapper.mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
        return results;
    }

    protected Optional<T> findById(String sql, int id, RowMapper<T> mapper, String errorMessage) {
        try (Connection conn = ConnectionHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(errorMessage + " id: " + id, e);
        }
        return Optional.empty();
    }

    protected void executeUpdate(String sql, ParameterBinder binder, String errorMessage) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                binder.bind(ps);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(errorMessage, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en " + errorMessage, e);
        }
    }

    protected int insertWithGeneratedKeys(String sql, ParameterBinder binder, T entity,
                                          BiConsumer<T, Integer> idSetter, String errorMessage) {
        try (Connection conn = ConnectionHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                binder.bind(ps);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        idSetter.accept(entity, generatedId);
                        conn.commit();
                        return generatedId;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(errorMessage, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en " + errorMessage, e);
        }
        return 0;
    }

    protected void deleteById(String sql, int id, String errorMessage) {
        executeUpdate(sql, ps -> ps.setInt(1, id), errorMessage);
    }
}
