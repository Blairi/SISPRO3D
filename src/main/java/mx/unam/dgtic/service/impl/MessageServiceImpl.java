package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.MessageDTO;
import mx.unam.dgtic.entities.MessageEntity;
import mx.unam.dgtic.mapper.MessageMapper;
import mx.unam.dgtic.repository.IMessageRepository;
import mx.unam.dgtic.repository.impl.MessageRepository;
import mx.unam.dgtic.service.MessageService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class MessageServiceImpl implements MessageService {

    private final IMessageRepository messageRepository;

    public MessageServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.messageRepository = new MessageRepository(em);
    }

    @Override
    public List<MessageDTO> findAll() {
        return MessageMapper.toDtoList(messageRepository.findAll());
    }

    @Override
    public MessageDTO findById(Integer id) {
        MessageEntity existingMessage = messageRepository.findById(id);
        if (existingMessage == null) {
            throw new RuntimeException("Mensaje no encontrado con id: " + id);
        }
        return MessageMapper.toDTO(existingMessage);
    }

    @Override
    public MessageDTO create(MessageDTO dto) {
        MessageEntity entity = MessageMapper.toEntity(dto);
        messageRepository.save(entity);
        return MessageMapper.toDTO(entity);
    }

    @Override
    public MessageDTO update(Integer id, MessageDTO dto) {
        MessageEntity existingMessage = messageRepository.findById(id);
        if (existingMessage == null) {
            throw new RuntimeException("Mensaje no encontrado con id: " + id);
        }

        MessageEntity messageEntity = MessageMapper.toEntity(dto);
        messageEntity.setId(id);
        messageRepository.update(messageEntity);
        return MessageMapper.toDTO(messageEntity);
    }

    @Override
    public void delete(Integer id) {
        MessageEntity existingMessage = messageRepository.findById(id);
        if (existingMessage == null) {
            throw new RuntimeException("Mensaje no encontrado con id: " + id);
        }
        messageRepository.delete(existingMessage);
    }
}
