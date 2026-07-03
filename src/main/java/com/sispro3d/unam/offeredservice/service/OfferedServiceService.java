package com.sispro3d.unam.offeredservice.service;

import com.sispro3d.unam.offeredservice.dto.OfferedServiceDTO;

import java.util.List;
import java.util.Optional;

public interface OfferedServiceService {
    List<OfferedServiceDTO> findAll();
    Optional<OfferedServiceDTO> findById(int id);
    OfferedServiceDTO create(OfferedServiceDTO dto);
    OfferedServiceDTO update(int id, OfferedServiceDTO dto);
    void delete(int id);
}
