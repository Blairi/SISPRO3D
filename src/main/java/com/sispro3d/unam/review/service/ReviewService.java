package com.sispro3d.unam.review.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;

public interface ReviewService extends CrudService<ReviewRequest, ReviewResponse, Long> {
}
