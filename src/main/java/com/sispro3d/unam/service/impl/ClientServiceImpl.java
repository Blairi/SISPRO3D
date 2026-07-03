package com.sispro3d.unam.service.impl;

import com.sispro3d.unam.dao.GenericDAO;
import com.sispro3d.unam.domain.Account;
import com.sispro3d.unam.domain.Client;
import com.sispro3d.unam.dto.AccountDTO;
import com.sispro3d.unam.dto.ClientDTO;
import com.sispro3d.unam.service.ClientService;

import java.util.List;
import java.util.Optional;

public class ClientServiceImpl implements ClientService {

    private final GenericDAO<Client> clientDAO;

    public ClientServiceImpl(GenericDAO<Client> clientDAO) {
        this.clientDAO = clientDAO;
    }

    @Override
    public List<ClientDTO> findAll() {
        return clientDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<ClientDTO> findById(int id) {
        return clientDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public ClientDTO create(ClientDTO dto) {
        Client client = toEntity(dto);
        int generatedId = clientDAO.insert(client);
        client.getAccount().setIdUser(generatedId);
        return toResponseDTO(client);
    }

    @Override
    public ClientDTO update(int id, ClientDTO dto) {
        clientDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));

        Client client = toEntity(dto);
        client.getAccount().setIdUser(id);
        clientDAO.update(client);
        return toResponseDTO(client);
    }

    @Override
    public void delete(int id) {
        clientDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
        clientDAO.delete(id);
    }

    private Client toEntity(ClientDTO dto) {
        Client client = new Client();
        if (dto.getAccount() != null) {
            client.setAccount(new Account(dto.getAccount().getIdUser()));
        }
        return client;
    }

    private ClientDTO toResponseDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        if (client.getAccount() != null) {
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setIdUser(client.getAccount().getIdUser());
            accountDTO.setName(client.getAccount().getName());
            accountDTO.setLastName(client.getAccount().getLastName());
            accountDTO.setEmail(client.getAccount().getEmail());
            accountDTO.setPhone(client.getAccount().getPhone());
            accountDTO.setPassword(client.getAccount().getPassword());
            accountDTO.setType(client.getAccount().getType());
            accountDTO.setCreatedAt(client.getAccount().getCreatedAt());
            dto.setAccount(accountDTO);
        }
        return dto;
    }
}
