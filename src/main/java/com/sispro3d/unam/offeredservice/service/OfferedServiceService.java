package com.sispro3d.unam.offeredservice.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;

public interface OfferedServiceService extends CrudService<OfferedServiceRequest, OfferedServiceResponse, Long> {
    OfferedServiceResponse approve(Long id, Long adminId);
    OfferedServiceResponse reject(Long id, Long adminId);
}
