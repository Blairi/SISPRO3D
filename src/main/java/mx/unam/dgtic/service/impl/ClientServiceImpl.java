package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.ClientDTO;
import mx.unam.dgtic.entities.ClientEntity;
import mx.unam.dgtic.mapper.ClientMapper;
import mx.unam.dgtic.repository.IClientRepository;
import mx.unam.dgtic.repository.impl.ClientRepository;
import mx.unam.dgtic.service.ClientService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ClientServiceImpl implements ClientService {

    private final IClientRepository clientRepository;

    public ClientServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.clientRepository = new ClientRepository(em);
    }

    @Override
    public List<ClientDTO> findAll() {
        return ClientMapper.toDtoList(clientRepository.findAll());
    }

    @Override
    public ClientDTO findById(Integer id) {
        return ClientMapper.toDTO(clientRepository.findById(id));
    }

    @Override
    public ClientDTO create(ClientDTO dto) {
        ClientEntity entity = ClientMapper.toEntity(dto);
        clientRepository.save(entity);
        return ClientMapper.toDTO(entity);
    }

    @Override
    public ClientDTO update(Integer id, ClientDTO dto) {
        ClientEntity existingClient = clientRepository.findById(id);
        if (existingClient == null) {
            throw new RuntimeException("Client no encontrado con id: " + id);
        }

        ClientEntity clientEntity = ClientMapper.toEntity(dto);
        clientEntity.setIdUser(id);
        clientRepository.update(clientEntity);
        return ClientMapper.toDTO(clientEntity);
    }

    @Override
    public void delete(Integer id) {
        ClientEntity existingClient = clientRepository.findById(id);
        if (existingClient == null) {
            throw new RuntimeException("Client no encontrado con id: " + id);
        }
        clientRepository.delete(existingClient);
    }
}
