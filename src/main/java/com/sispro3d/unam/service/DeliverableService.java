package com.sispro3d.unam.service;

import com.sispro3d.unam.dto.DeliverableDTO;

import java.util.List;
import java.util.Optional;

public interface DeliverableService {
    List<DeliverableDTO> findAll();
    Optional<DeliverableDTO> findById(int id);
    DeliverableDTO create(DeliverableDTO dto);
    DeliverableDTO update(int id, DeliverableDTO dto);
    void delete(int id);
}
