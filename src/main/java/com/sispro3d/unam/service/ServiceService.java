package com.sispro3d.unam.service;

import com.sispro3d.unam.dto.ServiceDTO;

import java.util.List;
import java.util.Optional;

public interface ServiceService {
    List<ServiceDTO> findAll();
    Optional<ServiceDTO> findById(int id);
    ServiceDTO create(ServiceDTO dto);
    ServiceDTO update(int id, ServiceDTO dto);
    void delete(int id);
}
