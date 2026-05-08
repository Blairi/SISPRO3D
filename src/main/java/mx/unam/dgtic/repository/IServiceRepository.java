package mx.unam.dgtic.repository;

import mx.unam.dgtic.entities.ServiceEntity;

import java.util.List;

public interface IServiceRepository extends IRepository<ServiceEntity, Integer> {
    public List<ServiceEntity> findAllServicesFromExpertId(Integer id);
    public boolean isApprovedByAdmin(Integer id);
}
