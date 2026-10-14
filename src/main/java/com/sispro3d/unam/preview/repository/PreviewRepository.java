package com.sispro3d.unam.preview.repository;

import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.preview.domain.Preview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreviewRepository extends JpaRepository<Preview, Long> {
    List<Preview> findByDeliverable(Deliverable deliverable);
    List<Preview> findByDeliverable_Id(Long deliverableId);

    List<Preview> findByDeliverable_IdOrderByCreatedAtAsc(Long deliverableId);

    Optional<Preview> findFirstByUrlFile(String urlFile);
}
