package com.sispro3d.unam.review.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;

import java.util.List;
import java.util.Optional;

public interface ReviewService extends CrudService<ReviewRequest, ReviewResponse, Long> {
    List<ReviewResponse> findByClientId(Long clientId);
    List<ReviewResponse> findByOfferedServiceId(Long offeredServiceId);
    Optional<ReviewResponse> findByClientIdAndOfferedServiceId(Long clientId, Long offeredServiceId);
}
