package com.sispro3d.unam.review.repository;

import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.user.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByClient(Account client);
    List<Review> findByOfferedService(OfferedService offeredService);
    Optional<Review> findByClientAndOfferedService(Account client, OfferedService offeredService);
}
