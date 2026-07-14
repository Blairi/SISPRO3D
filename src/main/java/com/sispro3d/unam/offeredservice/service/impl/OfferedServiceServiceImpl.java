package com.sispro3d.unam.offeredservice.service.impl;

import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class OfferedServiceServiceImpl implements OfferedServiceService {

    @Override
    public List<OfferedServiceResponse> findAll() {
        return List.of();
    }

    @Override
    public Optional<OfferedServiceResponse> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public OfferedServiceResponse create(OfferedServiceRequest request) {
        return null;
    }

    @Override
    public OfferedServiceResponse update(Long aLong, OfferedServiceRequest request) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }
}
