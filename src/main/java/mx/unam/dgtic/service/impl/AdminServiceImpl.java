package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.AdminDTO;
import mx.unam.dgtic.entities.AdminEntity;
import mx.unam.dgtic.mapper.AdminMapper;
import mx.unam.dgtic.repository.IAdminRepository;
import mx.unam.dgtic.repository.impl.AdminRepository;
import mx.unam.dgtic.service.AdminService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class AdminServiceImpl implements AdminService {

    private final IAdminRepository adminRepository;

    public AdminServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.adminRepository = new AdminRepository(em);
    }

    @Override
    public List<AdminDTO> findAll() {
        return AdminMapper.toDtoList(adminRepository.findAll());
    }

    @Override
    public AdminDTO findById(Integer id) {
        return AdminMapper.toDTO(adminRepository.findById(id));
    }

    @Override
    public AdminDTO create(AdminDTO dto) {
        AdminEntity entity = AdminMapper.toEntity(dto);
        adminRepository.save(entity);
        return AdminMapper.toDTO(entity);
    }

    @Override
    public AdminDTO update(Integer id, AdminDTO dto) {
        AdminEntity existingAdmin = adminRepository.findById(id);
        if (existingAdmin == null) {
            throw new RuntimeException("Admin no encontrado con id: " + id);
        }

        AdminEntity adminEntity = AdminMapper.toEntity(dto);
        adminEntity.setIdUser(id);
        adminRepository.update(adminEntity);
        return AdminMapper.toDTO(adminEntity);
    }

    @Override
    public void delete(Integer id) {
        AdminEntity existingAdmin = adminRepository.findById(id);
        if (existingAdmin == null) {
            throw new RuntimeException("Admin no encontrado con id: " + id);
        }

        adminRepository.delete(existingAdmin);
    }
}
