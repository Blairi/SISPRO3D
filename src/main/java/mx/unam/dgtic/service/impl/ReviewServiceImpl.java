package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dao.GenericDAO;
import mx.unam.dgtic.domain.Review;
import mx.unam.dgtic.dto.ReviewDTO;
import mx.unam.dgtic.dto.ClientDTO;
import mx.unam.dgtic.dto.ServiceDTO;
import mx.unam.dgtic.service.ReviewService;

import java.util.List;
import java.util.Optional;

public class ReviewServiceImpl implements ReviewService {

    private final GenericDAO<Review> reviewDAO;

    public ReviewServiceImpl(GenericDAO<Review> reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    @Override
    public List<ReviewDTO> findAll() {
        return reviewDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<ReviewDTO> findById(int id) {
        return reviewDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public ReviewDTO create(ReviewDTO dto) {
        Review review = toEntity(dto);
        int generatedId = reviewDAO.insert(review);
        review.setId(generatedId);
        return toResponseDTO(review);
    }

    @Override
    public ReviewDTO update(int id, ReviewDTO dto) {
        reviewDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada con id: " + id));

        Review review = toEntity(dto);
        review.setId(id);
        reviewDAO.update(review);
        return toResponseDTO(review);
    }

    @Override
    public void delete(int id) {
        reviewDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada con id: " + id));
        reviewDAO.delete(id);
    }

    private Review toEntity(ReviewDTO dto) {
        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        if (dto.getClient() != null && dto.getClient().getAccount() != null) {
            mx.unam.dgtic.domain.Client client = new mx.unam.dgtic.domain.Client();
            client.setAccount(new mx.unam.dgtic.domain.Account(dto.getClient().getAccount().getIdUser()));
            review.setClient(client);
        }

        if (dto.getService() != null) {
            review.setService(mapServiceByDTO(dto.getService()));
        }

        return review;
    }

    private ReviewDTO toResponseDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        if (review.getClient() != null) {
            ClientDTO clientDTO = new ClientDTO();
            if (review.getClient().getAccount() != null) {
                clientDTO.setAccount(mapAccountToDTO(review.getClient().getAccount()));
            }
            dto.setClient(clientDTO);
        }

        if (review.getService() != null) {
            dto.setService(mapServiceToDTO(review.getService()));
        }

        return dto;
    }

    private mx.unam.dgtic.domain.Service mapServiceByDTO(ServiceDTO serviceDTO) {
        mx.unam.dgtic.domain.Service service = new mx.unam.dgtic.domain.Service();
        service.setId(serviceDTO.getId());
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setBasePrice(serviceDTO.getBasePrice());
        service.setDeliveryTimeDays(serviceDTO.getDeliveryTimeDays());
        return service;
    }

    private ServiceDTO mapServiceToDTO(mx.unam.dgtic.domain.Service service) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(service.getId());
        dto.setTitle(service.getTitle());
        dto.setDescription(service.getDescription());
        dto.setBasePrice(service.getBasePrice());
        dto.setDeliveryTimeDays(service.getDeliveryTimeDays());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }

    private mx.unam.dgtic.dto.AccountDTO mapAccountToDTO(mx.unam.dgtic.domain.Account account) {
        mx.unam.dgtic.dto.AccountDTO dto = new mx.unam.dgtic.dto.AccountDTO();
        dto.setIdUser(account.getIdUser());
        dto.setName(account.getName());
        dto.setLastName(account.getLastName());
        dto.setEmail(account.getEmail());
        dto.setPhone(account.getPhone());
        dto.setPassword(account.getPassword());
        dto.setType(account.getType());
        dto.setCreatedAt(account.getCreatedAt());
        return dto;
    }
}
