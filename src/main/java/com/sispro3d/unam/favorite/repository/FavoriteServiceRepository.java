package com.sispro3d.unam.favorite.repository;

import com.sispro3d.unam.favorite.domain.FavoriteService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteServiceRepository extends JpaRepository<FavoriteService, Long> {
    List<FavoriteService> findByClient_IdUserOrderByCreatedAtDesc(Long clientId);
    boolean existsByClient_IdUserAndService_Id(Long clientId, Long serviceId);
    void deleteByClient_IdUserAndService_Id(Long clientId, Long serviceId);
}