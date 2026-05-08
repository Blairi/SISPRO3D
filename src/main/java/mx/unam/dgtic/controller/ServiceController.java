package mx.unam.dgtic.controller;

import mx.unam.dgtic.dto.CategoryDTO;
import mx.unam.dgtic.dto.ExpertDTO;
import mx.unam.dgtic.dto.ServiceDTO;
import mx.unam.dgtic.service.ServiceService;
import mx.unam.dgtic.service.impl.ServiceServiceImpl;

import java.math.BigDecimal;
import java.util.List;

public class ServiceController {
    private final ServiceService serviceService;

    public ServiceController() {
        serviceService = new ServiceServiceImpl();
    }

    public void displayAllServicesFromExpertId(int id) {
        List<ServiceDTO> serviceDTOList = serviceService.getServicesByExpertId(id);
        serviceDTOList.forEach(System.out::println);
    }

    public void displayAllServices() {
        List<ServiceDTO> serviceDTOList = serviceService.findAll();
        serviceDTOList.forEach(System.out::println);
    }

    public void displayServiceById(Integer id) {
        System.out.println(serviceService.findById(id));
    }

    public void updateServiceById(
            Integer id,
            Integer idExpert,
            String title,
            String description,
            BigDecimal basePrice,
            Integer idCategory,
            int deliverableTimeDays
    ) {
        ExpertDTO expertDTO = ExpertDTO.ofId(idExpert);
        CategoryDTO categoryDTO = CategoryDTO.ofId(idCategory);

        ServiceDTO serviceDTO = ServiceDTO.builder()
                .id(id)
                .expert(expertDTO)
                .category(categoryDTO)
                .title(title)
                .description(description)
                .basePrice(basePrice)
                .deliveryTimeDays(deliverableTimeDays)
                .build();
        serviceService.update(id, serviceDTO);
    }

    public Integer createNewServiceByExpertId(int idExpert,
                                           String title,
                                           String description,
                                           BigDecimal basePrice,
                                           int idCategory,
                                           int deliverableTimeDays
    ) {

        ExpertDTO expertDTO = ExpertDTO.ofId(idExpert);
        CategoryDTO categoryDTO = CategoryDTO.ofId(idCategory);

        ServiceDTO serviceDTO = ServiceDTO.builder()
                .title(title)
                .description(description)
                .basePrice(basePrice)
                .expert(expertDTO)
                .category(categoryDTO)
                .deliveryTimeDays(deliverableTimeDays)
                .build();

        return serviceService.create(serviceDTO).getId();
    }

    public void deleteServiceById(Integer id) {
        serviceService.delete(id);
    }

    public boolean serviceIsApprovedByAdmin(Integer id) {
        return serviceService.serviceIsApprovedByAdmin(id);
    }

}
