package com.sispro3d.unam.workorder.service;

import com.sispro3d.unam.workorder.dto.WorkOrderDTO;

import java.util.List;
import java.util.Optional;

public interface WorkOrderService {
    List<WorkOrderDTO> findAll();
    Optional<WorkOrderDTO> findById(int id);
    WorkOrderDTO create(WorkOrderDTO dto);
    WorkOrderDTO update(int id, WorkOrderDTO dto);
    void delete(int id);
}
