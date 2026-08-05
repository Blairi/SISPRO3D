package com.sispro3d.unam.workorder.repository;

import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    Optional<WorkOrder> findByQuote(Quote quote);
    List<WorkOrder> findByStatus(WorkOrderStatus status);

    Optional<WorkOrder> findByQuote_Id(Long quoteId);
    List<WorkOrder> findByQuote_Client_IdUser(Long clientId);
    List<WorkOrder> findByQuote_OfferedService_Expert_IdUser(Long expertId);
}
