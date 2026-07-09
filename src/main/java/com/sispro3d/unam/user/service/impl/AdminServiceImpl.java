package com.sispro3d.unam.user.service.impl;

import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Admin;
import com.sispro3d.unam.user.dto.AdminRequest;
import com.sispro3d.unam.user.dto.AdminResponse;
import com.sispro3d.unam.user.repository.AdminRepository;
import com.sispro3d.unam.user.service.AdminService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;

    public AdminServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public List<AdminResponse> findAll() {
        return adminRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<AdminResponse> findById(Long id) {
        return adminRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public AdminResponse create(AdminRequest request) {
        Admin admin = toEntity(request);
        Admin saved = adminRepository.save(admin);
        return toResponse(saved);
    }

    @Override
    public AdminResponse update(Long id, AdminRequest request) {
        int pk = id.intValue();
        adminRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado con id: " + id));

        Admin admin = toEntity(request);
        admin.getAccount().setIdUser(pk);
        Admin updated = adminRepository.update(admin);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        adminRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado con id: " + id));
        adminRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return adminRepository.existsById(id.intValue());
    }

    private Admin toEntity(AdminRequest request) {
        Admin admin = new Admin();
        admin.setAccount(new Account(request.getAccountId()));
        return admin;
    }

    private AdminResponse toResponse(Admin admin) {
        AdminResponse.AdminResponseBuilder builder = AdminResponse.builder();

        if (admin.getAccount() != null) {
            builder.id(admin.getAccount().getIdUser())
                    .name(admin.getAccount().getName())
                    .lastName(admin.getAccount().getLastName())
                    .email(admin.getAccount().getEmail());
        }

        return builder.build();
    }
}
