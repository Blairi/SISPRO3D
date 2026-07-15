package com.sispro3d.unam.offeredservice.service.impl;

import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.mapper.OfferedServiceMapper;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
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
        if (!accountRepository.existsById(request.getExpertId())) {
            throw ResourceNotFoundException.forId("Account (expert)", request.getExpertId());
        }
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw ResourceNotFoundException.forId("Category", request.getCategoryId());
        }
        OfferedService service = offeredServiceMapper.toEntity(request);
        service.setExpert(accountRepository.getReferenceById(request.getExpertId()));
        service.setCategory(categoryRepository.getReferenceById(request.getCategoryId()));
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
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
}
