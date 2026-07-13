package com.sispro3d.unam.review.dao;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;

import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewJdbcDAO extends AbstractJdbcDAO<Review> implements GenericDAO<Review> {

    private static final String FIND_ALL = """
            SELECT 
                r.id, r.rating, r.comment, r.created_at,
                c.id_user as client_id_user, c.name as client_name,
                c.lastName as client_lastName, c.email as client_email, c.phone as client_phone,
                c.password as client_password, c.role as client_role, c.created_at as client_created_at,
                s.id as service_id, s.title, s.description, s.base_price, s.delivery_time_days,
                s.created_at as service_created_at, s.updated_at as service_updated_at,
                a.id_user as admin_id_user, a.name as admin_name,
                a.lastName as admin_lastName, a.email as admin_email,
                e.id_user as expert_id_user, e.name as expert_name,
                e.lastName as expert_lastName, e.email as expert_email,
                e.specialty, e.portfolio_url, e.bio, e.years_experience,
                cat.id as category_id, cat.name as category_name, cat.description as category_description
            FROM review r
            JOIN account c ON r.client_id = c.id_user
            JOIN service s ON r.service_id = s.id
            LEFT JOIN account a ON s.id_admin = a.id_user
            JOIN account e ON s.id_expert = e.id_user
            JOIN category cat ON s.category_id = cat.id
            """;

    private static final String FIND_BY_ID = FIND_ALL + "WHERE r.id = ?";

    private static final String INSERT = """
            INSERT INTO review (rating, comment, client_id, service_id) 
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE = """
            UPDATE review SET rating = ?, comment = ?, client_id = ?, service_id = ? 
            WHERE id = ?
            """;
    private static final String DELETE = "DELETE FROM review WHERE id = ?";

    @Override
    public List<Review> findAll() {
        return findAll(FIND_ALL, this::mapRow, "Error al obtener todas las reseñas");
    }

    @Override
    public Optional<Review> findById(int id) {
        return findById(FIND_BY_ID, id, this::mapRow, "Error al buscar reseña con id");
    }

    @Override
    public int insert(Review review) {
        return insertWithGeneratedKeys(INSERT,
                ps -> {
                    ps.setInt(1, review.getRating());
                    ps.setString(2, review.getComment());
                    ps.setInt(3, review.getClient().getIdUser());
                    ps.setInt(4, review.getOfferedService().getId());
                },
                review, Review::setId, "Error al insertar reseña");
    }

    @Override
    public void update(Review review) {
        executeUpdate(UPDATE,
                ps -> {
                    ps.setInt(1, review.getRating());
                    ps.setString(2, review.getComment());
                    ps.setInt(3, review.getClient().getIdUser());
                    ps.setInt(4, review.getOfferedService().getId());
                    ps.setInt(5, review.getId());
                },
                "Error al actualizar reseña con id: " + review.getId());
    }

    @Override
    public void delete(int id) {
        deleteById(DELETE, id, "Error al eliminar reseña con id: " + id);
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Account client = new Account();
        client.setIdUser(rs.getInt("client_id_user"));
        client.setName(rs.getString("client_name"));
        client.setLastName(rs.getString("client_lastName"));
        client.setEmail(rs.getString("client_email"));
        client.setPhone(rs.getString("client_phone"));
        client.setPassword(rs.getString("client_password"));
        client.setRole(Role.valueOf(rs.getString("client_role")));
        Timestamp clientCreatedAt = rs.getTimestamp("client_created_at");
        if (clientCreatedAt != null) client.setCreatedAt(clientCreatedAt.toLocalDateTime());

        OfferedService offeredService = mapService(rs);

        Review review = new Review();
        review.setId(rs.getInt("id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) review.setCreatedAt(createdAt.toLocalDateTime());
        review.setClient(client);
        review.setOfferedService(offeredService);

        return review;
    }

    private OfferedService mapService(ResultSet rs) throws SQLException {
        OfferedService offeredService = new OfferedService();
        offeredService.setId(rs.getInt("service_id"));
        offeredService.setTitle(rs.getString("title"));
        offeredService.setDescription(rs.getString("description"));
        offeredService.setBasePrice(rs.getBigDecimal("base_price"));
        offeredService.setDeliveryTimeDays(rs.getInt("delivery_time_days"));

        Timestamp serviceCreatedAt = rs.getTimestamp("service_created_at");
        if (serviceCreatedAt != null) offeredService.setCreatedAt(serviceCreatedAt.toLocalDateTime());

        Timestamp serviceUpdatedAt = rs.getTimestamp("service_updated_at");
        if (serviceUpdatedAt != null) offeredService.setUpdatedAt(serviceUpdatedAt.toLocalDateTime());

        if (rs.getObject("admin_id_user") != null) {
            Account admin = new Account();
            admin.setIdUser(rs.getInt("admin_id_user"));
            admin.setName(rs.getString("admin_name"));
            admin.setLastName(rs.getString("admin_lastName"));
            admin.setEmail(rs.getString("admin_email"));
            offeredService.setAdmin(admin);
        }

        Account expert = new Account();
        expert.setIdUser(rs.getInt("expert_id_user"));
        expert.setName(rs.getString("expert_name"));
        expert.setLastName(rs.getString("expert_lastName"));
        expert.setEmail(rs.getString("expert_email"));
        expert.setSpecialty(rs.getString("specialty"));
        expert.setPortfolioUrl(rs.getString("portfolio_url"));
        expert.setBio(rs.getString("bio"));
        expert.setYearsExperience((Integer) rs.getObject("years_experience"));
        offeredService.setExpert(expert);

        Category category = new Category();
        category.setId(rs.getInt("category_id"));
        category.setName(rs.getString("category_name"));
        category.setDescription(rs.getString("category_description"));
        offeredService.setCategory(category);

        return offeredService;
    }
}
