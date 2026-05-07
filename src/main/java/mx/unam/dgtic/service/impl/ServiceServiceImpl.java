package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.ServiceDTO;
import mx.unam.dgtic.entities.ServiceEntity;
import mx.unam.dgtic.mapper.ServiceMapper;
import mx.unam.dgtic.repository.IServiceRepository;
import mx.unam.dgtic.repository.impl.ServiceRepository;
import mx.unam.dgtic.service.ServiceService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ServiceServiceImpl implements ServiceService {

    private final IServiceRepository serviceRepository;

    public ServiceServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.serviceRepository = new ServiceRepository(em);
    }

    @Override
    public List<ServiceDTO> findAll() {
        return ServiceMapper.toDtoList(serviceRepository.findAll());
    }

    @Override
    public ServiceDTO findById(Integer id) {
        return ServiceMapper.toDTO(serviceRepository.findById(id));
    }

    @Override
    public ServiceDTO create(ServiceDTO dto) {
        ServiceEntity entity = ServiceMapper.toEntity(dto);
        serviceRepository.save(entity);
        return ServiceMapper.toDTO(entity);
    }

    @Override
    public ServiceDTO update(Integer id, ServiceDTO dto) {
        ServiceEntity existingService = serviceRepository.findById(id);
        if (existingService == null) {
            throw new RuntimeException("Servicio no encontrado con id: " + id);
        }

        ServiceEntity serviceEntity = ServiceMapper.toEntity(dto);
        serviceEntity.setId(id);
        serviceRepository.update(serviceEntity);
        return ServiceMapper.toDTO(serviceEntity);
    }

    @Override
    public void delete(Integer id) {
        ServiceEntity existingService = serviceRepository.findById(id);
        if (existingService == null) {
            throw new RuntimeException("Servicio no encontrado con id: " + id);
        }
        serviceRepository.delete(existingService);
    }
}
