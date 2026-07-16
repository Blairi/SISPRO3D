package com.sispro3d.unam.offeredservice.mapper;

import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import org.springframework.stereotype.Component;

@Component
public class OfferedServiceMapper {

    /**
     * Maps the flat/scalar fields only. Relations (expert, admin, category)
     * are NOT set here — the service layer resolves and assigns them,
     * since that requires repository access and business validation.
     */
    public OfferedService toEntity(OfferedServiceRequest request) {
        if (request == null) {
            return null;
        }
        var service = new OfferedService();
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setDeliveryTimeDays(request.getDeliveryTimeDays());
        return service;
    }

    public OfferedServiceResponse toResponse(OfferedService service) {
        if (service == null) {
            return null;
        }
        return OfferedServiceResponse.builder()
                .id(service.getId())
                .title(service.getTitle())
                .description(service.getDescription())
                .basePrice(service.getBasePrice())
                .expertId(service.getExpert() != null ? service.getExpert().getIdUser() : null)
                .adminId(service.getAdmin() != null ? service.getAdmin().getIdUser() : null)
                .categoryId(service.getCategory() != null ? service.getCategory().getId() : null)
                .status(service.getStatus())
                .deliveryTimeDays(service.getDeliveryTimeDays())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequest(OfferedServiceRequest request, OfferedService service) {
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setDeliveryTimeDays(request.getDeliveryTimeDays());
    }
}