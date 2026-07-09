package com.sispro3d.unam.review.dao;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.core.dao.AbstractJdbcDAO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.user.domain.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class ReviewJdbcDAO extends AbstractJdbcDAO<Review> implements GenericDAO<Review> {

    private static final String FIND_ALL = """
            SELECT 
                r.id, r.rating, r.comment, r.created_at,
                c.account_id as client_account_id, acc1.id_user as client_id_user, acc1.name as client_name,
                acc1.lastName as client_lastName, acc1.email as client_email, acc1.phone as client_phone,
                acc1.password as client_password, acc1.type as client_type, acc1.created_at as client_created_at,
                s.id as service_id, s.title, s.description, s.base_price, s.delivery_time_days,
                s.created_at as service_created_at, s.updated_at as service_updated_at,
                a.account_id as admin_account_id, acc2.id_user as admin_id_user, acc2.name as admin_name,
                acc2.lastName as admin_lastName, acc2.email as admin_email, acc2.phone as admin_phone,
                acc2.password as admin_password, acc2.type as admin_type, acc2.created_at as admin_created_at,
                e.account_id as expert_account_id, acc3.id_user as expert_id_user, acc3.name as expert_name,
                acc3.lastName as expert_lastName, acc3.email as expert_email, acc3.phone as expert_phone,
                acc3.password as expert_password, acc3.type as expert_type, acc3.created_at as expert_created_at,
                e.specialty, e.portfolio_url, e.bio, e.years_experience,
                cat.id as category_id, cat.name as category_name, cat.description as category_description
            FROM review r
            JOIN client c ON r.client_id = c.account_id
            JOIN account acc1 ON c.account_id = acc1.id_user
            JOIN service s ON r.service_id = s.id
            LEFT JOIN admin a ON s.admin_id = a.account_id
            LEFT JOIN account acc2 ON a.account_id = acc2.id_user
            JOIN expert e ON s.expert_id = e.account_id
            JOIN account acc3 ON e.account_id = acc3.id_user
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
                    ps.setInt(3, review.getClient().getAccount().getIdUser());
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
                    ps.setInt(3, review.getClient().getAccount().getIdUser());
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
        Account clientAccount = new Account();
        clientAccount.setIdUser(rs.getInt("client_id_user"));
        clientAccount.setName(rs.getString("client_name"));
        clientAccount.setLastName(rs.getString("client_lastName"));
        clientAccount.setEmail(rs.getString("client_email"));
        clientAccount.setPhone(rs.getString("client_phone"));
        clientAccount.setPassword(rs.getString("client_password"));
        clientAccount.setType(UserType.valueOf(rs.getString("client_type")));
        Timestamp clientCreatedAt = rs.getTimestamp("client_created_at");
        if (clientCreatedAt != null) clientAccount.setCreatedAt(clientCreatedAt.toLocalDateTime());

        Client client = new Client();
        client.setAccount(clientAccount);

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
            Account adminAccount = new Account();
            adminAccount.setIdUser(rs.getInt("admin_id_user"));
            adminAccount.setName(rs.getString("admin_name"));
            adminAccount.setLastName(rs.getString("admin_lastName"));
            adminAccount.setEmail(rs.getString("admin_email"));
            adminAccount.setPhone(rs.getString("admin_phone"));
            adminAccount.setPassword(rs.getString("admin_password"));
            adminAccount.setType(UserType.valueOf(rs.getString("admin_type")));
            Timestamp adminCreatedAt = rs.getTimestamp("admin_created_at");
            if (adminCreatedAt != null) adminAccount.setCreatedAt(adminCreatedAt.toLocalDateTime());

            Admin admin = new Admin();
            admin.setAccount(adminAccount);
            offeredService.setAdmin(admin);
        }

        Account expertAccount = new Account();
        expertAccount.setIdUser(rs.getInt("expert_id_user"));
        expertAccount.setName(rs.getString("expert_name"));
        expertAccount.setLastName(rs.getString("expert_lastName"));
        expertAccount.setEmail(rs.getString("expert_email"));
        expertAccount.setPhone(rs.getString("expert_phone"));
        expertAccount.setPassword(rs.getString("expert_password"));
        expertAccount.setType(UserType.valueOf(rs.getString("expert_type")));
        Timestamp expertCreatedAt = rs.getTimestamp("expert_created_at");
        if (expertCreatedAt != null) expertAccount.setCreatedAt(expertCreatedAt.toLocalDateTime());

        Expert expert = new Expert();
        expert.setAccount(expertAccount);
        expert.setSpecialty(rs.getString("specialty"));
        expert.setPortfolioUrl(rs.getString("portfolio_url"));
        expert.setBio(rs.getString("bio"));
        expert.setYearsExperience(rs.getInt("years_experience"));
        offeredService.setExpert(expert);

        Category category = new Category();
        category.setId(rs.getInt("category_id"));
        category.setName(rs.getString("category_name"));
        category.setDescription(rs.getString("category_description"));
        offeredService.setCategory(category);

        return offeredService;
    }
}
