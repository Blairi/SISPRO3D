package com.sispro3d.unam.review.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceDTO;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.review.dto.ReviewDTO;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Client;
import com.sispro3d.unam.user.dto.AccountDTO;
import com.sispro3d.unam.user.dto.ClientDTO;

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
            Client client = new Client();
            client.setAccount(new Account(dto.getClient().getAccount().getIdUser()));
            review.setClient(client);
        }

        if (dto.getOfferedService() != null) {
            review.setOfferedService(mapOfferedServiceByDTO(dto.getOfferedService()));
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

        if (review.getOfferedService() != null) {
            dto.setOfferedService(mapOfferedServiceToDTO(review.getOfferedService()));
        }

        return dto;
    }

    private OfferedService mapOfferedServiceByDTO(OfferedServiceDTO offeredServiceDTO) {
        OfferedService offeredService = new OfferedService();
        offeredService.setId(offeredServiceDTO.getId());
        offeredService.setTitle(offeredServiceDTO.getTitle());
        offeredService.setDescription(offeredServiceDTO.getDescription());
        offeredService.setBasePrice(offeredServiceDTO.getBasePrice());
        offeredService.setDeliveryTimeDays(offeredServiceDTO.getDeliveryTimeDays());
        return offeredService;
    }

    private OfferedServiceDTO mapOfferedServiceToDTO(OfferedService offeredService) {
        OfferedServiceDTO dto = new OfferedServiceDTO();
        dto.setId(offeredService.getId());
        dto.setTitle(offeredService.getTitle());
        dto.setDescription(offeredService.getDescription());
        dto.setBasePrice(offeredService.getBasePrice());
        dto.setDeliveryTimeDays(offeredService.getDeliveryTimeDays());
        dto.setCreatedAt(offeredService.getCreatedAt());
        dto.setUpdatedAt(offeredService.getUpdatedAt());
        return dto;
    }

    private AccountDTO mapAccountToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
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
