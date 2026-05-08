package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.PreviewDTO;
import mx.unam.dgtic.entities.PreviewEntity;
import mx.unam.dgtic.mapper.PreviewMapper;
import mx.unam.dgtic.repository.IPreviewRepository;
import mx.unam.dgtic.repository.impl.PreviewRepository;
import mx.unam.dgtic.service.PreviewService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class PreviewServiceImpl implements PreviewService {

    private final IPreviewRepository previewRepository;

    public PreviewServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.previewRepository = new PreviewRepository(em);
    }

    @Override
    public List<PreviewDTO> findAll() {
        return PreviewMapper.toDtoList(previewRepository.findAll());
    }

    @Override
    public PreviewDTO findById(Integer id) {
        PreviewEntity existingPreview = previewRepository.findById(id);
        if (existingPreview == null) {
            throw new RuntimeException("Vista previa no encontrada con id: " + id);
        }
        return PreviewMapper.toDTO(existingPreview);
    }

    @Override
    public PreviewDTO create(PreviewDTO dto) {
        PreviewEntity entity = PreviewMapper.toEntity(dto);
        previewRepository.save(entity);
        return PreviewMapper.toDTO(entity);
    }

    @Override
    public PreviewDTO update(Integer id, PreviewDTO dto) {
        PreviewEntity existingPreview = previewRepository.findById(id);
        if (existingPreview == null) {
            throw new RuntimeException("Vista previa no encontrada con id: " + id);
        }

        PreviewEntity previewEntity = PreviewMapper.toEntity(dto);
        previewEntity.setId(id);
        previewRepository.update(previewEntity);
        return PreviewMapper.toDTO(previewEntity);
    }

    @Override
    public void delete(Integer id) {
        PreviewEntity existingPreview = previewRepository.findById(id);
        if (existingPreview == null) {
            throw new RuntimeException("Vista previa no encontrada con id: " + id);
        }
        previewRepository.delete(existingPreview);
    }
}
