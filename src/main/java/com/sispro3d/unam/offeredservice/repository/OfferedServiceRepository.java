package com.sispro3d.unam.offeredservice.repository;

import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferedServiceRepository extends JpaRepository<OfferedService, Long> {
    List<OfferedService> findByExpert(Account expert);
    List<OfferedService> findByCategory(Category category);
    List<OfferedService> findByStatus(ServiceStatus status);

    List<OfferedService> findByExpert_IdUser(Long expertId);
    List<OfferedService> findByCategory_Id(Long categoryId);
}
