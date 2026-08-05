package com.sispro3d.unam.thread.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.thread.dto.ThreadRequest;
import com.sispro3d.unam.thread.dto.ThreadResponse;

import java.util.Optional;

public interface ThreadService extends CrudService<ThreadRequest, ThreadResponse, Long> {
    Optional<ThreadResponse> findByWorkOrderId(Long workOrderId);
}
