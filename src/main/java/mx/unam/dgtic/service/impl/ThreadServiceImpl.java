package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.ThreadDTO;
import mx.unam.dgtic.entities.ThreadEntity;
import mx.unam.dgtic.mapper.ThreadMapper;
import mx.unam.dgtic.repository.IThreadRepository;
import mx.unam.dgtic.repository.impl.ThreadRepository;
import mx.unam.dgtic.service.ThreadService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ThreadServiceImpl implements ThreadService {

    private final IThreadRepository threadRepository;

    public ThreadServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.threadRepository = new ThreadRepository(em);
    }

    @Override
    public List<ThreadDTO> findAll() {
        return ThreadMapper.toDtoList(threadRepository.findAll());
    }

    @Override
    public ThreadDTO findById(Integer id) {
        return ThreadMapper.toDTO(threadRepository.findById(id));
    }

    @Override
    public ThreadDTO create(ThreadDTO dto) {
        ThreadEntity entity = ThreadMapper.toEntity(dto);
        threadRepository.save(entity);
        return ThreadMapper.toDTO(entity);
    }

    @Override
    public ThreadDTO update(Integer id, ThreadDTO dto) {
        ThreadEntity existingThread = threadRepository.findById(id);
        if (existingThread == null) {
            throw new RuntimeException("Hilo no encontrado con id: " + id);
        }

        ThreadEntity threadEntity = ThreadMapper.toEntity(dto);
        threadEntity.setId(id);
        threadRepository.update(threadEntity);
        return ThreadMapper.toDTO(threadEntity);
    }

    @Override
    public void delete(Integer id) {
        ThreadEntity existingThread = threadRepository.findById(id);
        if (existingThread == null) {
            throw new RuntimeException("Hilo no encontrado con id: " + id);
        }
        threadRepository.delete(existingThread);
    }
}
