package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.WorkOrderDTO;

import java.util.List;
import java.util.Optional;

public interface WorkOrderService {
    List<WorkOrderDTO> findAll();
    Optional<WorkOrderDTO> findById(int id);
    WorkOrderDTO create(WorkOrderDTO dto);
    WorkOrderDTO update(int id, WorkOrderDTO dto);
    void delete(int id);
}
