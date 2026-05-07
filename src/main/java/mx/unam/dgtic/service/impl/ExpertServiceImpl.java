package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.ExpertDTO;
import mx.unam.dgtic.entities.ExpertEntity;
import mx.unam.dgtic.mapper.ExpertMapper;
import mx.unam.dgtic.repository.IExpertRepository;
import mx.unam.dgtic.repository.impl.ExpertRepository;
import mx.unam.dgtic.service.ExpertService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ExpertServiceImpl implements ExpertService {

    private final IExpertRepository expertRepository;

    public ExpertServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.expertRepository = new ExpertRepository(em);
    }

    @Override
    public List<ExpertDTO> findAll() {
        return ExpertMapper.toDtoList(expertRepository.findAll());
    }

    @Override
    public ExpertDTO findById(Integer id) {
        return ExpertMapper.toDTO(expertRepository.findById(id));
    }

    @Override
    public ExpertDTO create(ExpertDTO dto) {
        ExpertEntity entity = ExpertMapper.toEntity(dto);
        expertRepository.save(entity);
        return ExpertMapper.toDTO(entity);
    }

    @Override
    public ExpertDTO update(Integer id, ExpertDTO dto) {
        ExpertEntity existingExpert = expertRepository.findById(id);
        if (existingExpert == null) {
            throw new RuntimeException("Expert no encontrado con id: " + id);
        }

        ExpertEntity expertEntity = ExpertMapper.toEntity(dto);
        expertEntity.setIdUser(id);
        expertRepository.update(expertEntity);
        return ExpertMapper.toDTO(expertEntity);
    }

    @Override
    public void delete(Integer id) {
        ExpertEntity existingExpert = expertRepository.findById(id);
        if (existingExpert == null) {
            throw new RuntimeException("Expert no encontrado con id: " + id);
        }
        expertRepository.delete(existingExpert);
    }
}
