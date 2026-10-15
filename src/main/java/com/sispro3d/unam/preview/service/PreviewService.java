package com.sispro3d.unam.preview.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.preview.dto.PreviewRequest;
import com.sispro3d.unam.preview.dto.PreviewResponse;

import java.util.List;

public interface PreviewService extends CrudService<PreviewRequest, PreviewResponse, Long> {
    List<PreviewResponse> findByDeliverableId(Long deliverableId);
}
