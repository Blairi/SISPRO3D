package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dao.GenericDAO;
import mx.unam.dgtic.domain.Admin;
import mx.unam.dgtic.dto.AdminDTO;
import mx.unam.dgtic.dto.AccountDTO;
import mx.unam.dgtic.service.AdminService;

import java.util.List;
import java.util.Optional;

public class AdminServiceImpl implements AdminService {

    private final GenericDAO<Admin> adminDAO;

    public AdminServiceImpl(GenericDAO<Admin> adminDAO) {
        this.adminDAO = adminDAO;
    }

    @Override
    public List<AdminDTO> findAll() {
        return adminDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<AdminDTO> findById(int id) {
        return adminDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public AdminDTO create(AdminDTO dto) {
        Admin admin = toEntity(dto);
        int generatedId = adminDAO.insert(admin);
        admin.getAccount().setIdUser(generatedId);
        return toResponseDTO(admin);
    }

    @Override
    public AdminDTO update(int id, AdminDTO dto) {
        adminDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado con id: " + id));

        Admin admin = toEntity(dto);
        admin.getAccount().setIdUser(id);
        adminDAO.update(admin);
        return toResponseDTO(admin);
    }

    @Override
    public void delete(int id) {
        adminDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado con id: " + id));
        adminDAO.delete(id);
    }

    private Admin toEntity(AdminDTO dto) {
        Admin admin = new Admin();
        if (dto.getAccount() != null) {
            // Conversion handled, admin account is set from DTO
            admin.setAccount(dto.getAccount() != null ? 
                new mx.unam.dgtic.domain.Account(dto.getAccount().getIdUser()) : 
                new mx.unam.dgtic.domain.Account());
        }
        return admin;
    }

    private AdminDTO toResponseDTO(Admin admin) {
        AdminDTO dto = new AdminDTO();
        if (admin.getAccount() != null) {
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setIdUser(admin.getAccount().getIdUser());
            accountDTO.setName(admin.getAccount().getName());
            accountDTO.setLastName(admin.getAccount().getLastName());
            accountDTO.setEmail(admin.getAccount().getEmail());
            accountDTO.setPhone(admin.getAccount().getPhone());
            accountDTO.setPassword(admin.getAccount().getPassword());
            accountDTO.setType(admin.getAccount().getType());
            accountDTO.setCreatedAt(admin.getAccount().getCreatedAt());
            dto.setAccount(accountDTO);
        }
        return dto;
    }
}
