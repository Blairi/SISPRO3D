package com.sispro3d.unam.offeredservice.service.impl;

import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.mapper.OfferedServiceMapper;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OfferedServiceServiceImpl implements OfferedServiceService {

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;
    @Autowired
    private OfferedServiceMapper offeredServiceMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<OfferedServiceResponse> findAll() {
        return offeredServiceRepository.findAll().stream()
                .map(offeredServiceMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<OfferedServiceResponse> findById(Long id) {
        return offeredServiceRepository.findById(id)
                .map(offeredServiceMapper::toResponse);
    }

    @Override
    public OfferedServiceResponse create(OfferedServiceRequest request) {
        var expert = accountRepository.findById(request.getExpertId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", request.getExpertId()));

        if (expert.getRole() != Role.EXPERT) {
            throw new IllegalStateException("Solo cuentas de tipo EXPERT pueden publicar servicios");
        }

        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw ResourceNotFoundException.forId("Category", request.getCategoryId());
        }

        OfferedService service = offeredServiceMapper.toEntity(request);
        service.setExpert(expert);
        service.setCategory(categoryRepository.getReferenceById(request.getCategoryId()));
        service.setStatus(ServiceStatus.PENDING);

        OfferedService saved = offeredServiceRepository.save(service);
        return offeredServiceMapper.toResponse(saved);
    }

    @Override
    public OfferedServiceResponse update(Long id, OfferedServiceRequest request) {
        OfferedService existing = offeredServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));

        if (!accountRepository.existsById(request.getExpertId())) {
            throw ResourceNotFoundException.forId("Account (expert)", request.getExpertId());
        }
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw ResourceNotFoundException.forId("Category", request.getCategoryId());
        }

        offeredServiceMapper.updateEntityFromRequest(request, existing);
        existing.setExpert(accountRepository.getReferenceById(request.getExpertId()));
        existing.setCategory(categoryRepository.getReferenceById(request.getCategoryId()));
        existing.setUpdatedAt(LocalDateTime.now());
        OfferedService updated = offeredServiceRepository.save(existing);
        return offeredServiceMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!offeredServiceRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("OfferedService", id);
        }
        offeredServiceRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return offeredServiceRepository.existsById(id);
    }

    @Override
    public OfferedServiceResponse approve(Long id, Long adminId) {
        var service = offeredServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));

        var admin = accountRepository.findById(adminId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (admin)", adminId));

        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Solo cuentas de tipo ADMIN pueden aprobar servicios");
        }

        if (service.getStatus() != ServiceStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden aprobar servicios en estado PENDING");
        }

        service.setStatus(ServiceStatus.APPROVED);
        service.setAdmin(admin);

        OfferedService updated = offeredServiceRepository.save(service);
        return offeredServiceMapper.toResponse(updated);
    }

    // TODO: agregar un mensaje con la razón del rechazo
    @Override
    public OfferedServiceResponse reject(Long id, Long adminId) {
        var service = offeredServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));

        var admin = accountRepository.findById(adminId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (admin)", adminId));

        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Solo cuentas de tipo ADMIN pueden aprobar servicios");
        }

        if (service.getStatus() != ServiceStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden aprobar servicios en estado PENDING");
        }

        service.setStatus(ServiceStatus.REJECTED);
        service.setAdmin(admin);

        OfferedService updated = offeredServiceRepository.save(service);
        return offeredServiceMapper.toResponse(updated);
    }

    @Override
    public List<OfferedServiceResponse> findByExpertId(Long expertId) {
        return offeredServiceRepository.findByExpert_IdUser(expertId).stream()
                .map(offeredServiceMapper::toResponse)
                .toList();
    }

    @Override
    public List<OfferedServiceResponse> findByCategoryId(Long categoryId) {
        return offeredServiceRepository.findByCategory_Id(categoryId).stream()
                .map(offeredServiceMapper::toResponse)
                .toList();
    }

    @Override
    public List<OfferedServiceResponse> findByStatus(ServiceStatus status) {
        return offeredServiceRepository.findByStatus(status).stream()
                .map(offeredServiceMapper::toResponse)
                .toList();
    }
}
