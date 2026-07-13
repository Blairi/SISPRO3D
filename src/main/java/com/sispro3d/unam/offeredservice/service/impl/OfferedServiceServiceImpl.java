package com.sispro3d.unam.offeredservice.service.impl;

import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OfferedServiceServiceImpl implements OfferedServiceService {

    private final OfferedServiceRepository offeredServiceRepository;

    public OfferedServiceServiceImpl(OfferedServiceRepository offeredServiceRepository) {
        this.offeredServiceRepository = offeredServiceRepository;
    }

    @Override
    public List<OfferedServiceResponse> findAll() {
        return null;
    }

    @Override
    public Optional<OfferedServiceResponse> findById(Long id) {
        return null;
    }

    @Override
    public OfferedServiceResponse create(OfferedServiceRequest request) {
        return null;
    }

    @Override
    public OfferedServiceResponse update(Long id, OfferedServiceRequest request) {
        int pk = id.intValue();
        offeredServiceRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + id));

        return null;
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        offeredServiceRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + id));
        offeredServiceRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return offeredServiceRepository.existsById(id.intValue());
    }

}
