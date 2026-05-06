package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.AdminDTO;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    List<AdminDTO> findAll();
    Optional<AdminDTO> findById(int id);
    AdminDTO create(AdminDTO dto);
    AdminDTO update(int id, AdminDTO dto);
    void delete(int id);
}
