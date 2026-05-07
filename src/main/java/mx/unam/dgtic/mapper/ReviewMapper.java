package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.ReviewDTO;
import mx.unam.dgtic.entities.ReviewEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewMapper {
    private ReviewMapper() {}

    public static ReviewDTO toDTO(ReviewEntity entity) {
        if (entity == null) {
            return null;
        }
        return ReviewDTO.builder()
                .id(entity.getId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .client(ClientMapper.toDTO(entity.getClient()))
                .service(ServiceMapper.toDTO(entity.getService()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static List<ReviewDTO> toDtoList(List<ReviewEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(ReviewMapper::toDTO).collect(Collectors.toList());
    }

    public static ReviewEntity toEntity(ReviewDTO dto) {
        if (dto == null) {
            return null;
        }
        ReviewEntity entity = new ReviewEntity();
        entity.setId(dto.getId());
        entity.setRating(dto.getRating());
        entity.setComment(dto.getComment());
        entity.setClient(ClientMapper.toEntity(dto.getClient()));
        entity.setService(ServiceMapper.toEntity(dto.getService()));
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }

    public static List<ReviewEntity> toEntityList(List<ReviewDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(ReviewMapper::toEntity).collect(Collectors.toList());
    }
}
