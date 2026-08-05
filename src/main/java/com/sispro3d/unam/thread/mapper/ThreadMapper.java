package com.sispro3d.unam.thread.mapper;

import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.thread.dto.ThreadRequest;
import com.sispro3d.unam.thread.dto.ThreadResponse;
import org.springframework.stereotype.Component;

@Component
public class ThreadMapper {

    /**
     * The request has no scalar fields that map to the entity — the service
     * layer assigns the workOrder relation after validation.
     */
    public Thread toEntity(ThreadRequest request) {
        return new Thread();
    }

    public ThreadResponse toResponse(Thread thread) {
        if (thread == null) {
            return null;
        }
        return ThreadResponse.builder()
                .id(thread.getId())
                .workOrderId(thread.getWorkOrder() != null ? thread.getWorkOrder().getId() : null)
                .build();
    }
}
