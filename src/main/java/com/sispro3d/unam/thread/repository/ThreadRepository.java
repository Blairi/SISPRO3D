package com.sispro3d.unam.thread.repository;

import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
    Optional<Thread> findByWorkOrder(WorkOrder workOrder);
    Optional<Thread> findByWorkOrder_Id(Long workOrderId);
    boolean existsByWorkOrder_Id(Long workOrderId);
}
