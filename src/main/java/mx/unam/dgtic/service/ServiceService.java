package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.ServiceDTO;

import java.util.List;

public interface ServiceService extends IService<ServiceDTO, Integer> {
    public List<ServiceDTO> getServicesByExpertId(Integer id);
    public boolean serviceIsApprovedByAdmin(Integer id);
}
