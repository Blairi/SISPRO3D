package com.sispro3d.unam.deliverable.repository;

import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliverableRepository extends JpaRepository<Deliverable, Long> {
    List<Deliverable> findByWorkOrder(WorkOrder workOrder);
    List<Deliverable> findByWorkOrder_Id(Long workOrderId);
}
