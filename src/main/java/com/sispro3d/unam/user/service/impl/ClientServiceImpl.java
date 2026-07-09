package com.sispro3d.unam.user.service.impl;

import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Client;
import com.sispro3d.unam.user.dto.ClientRequest;
import com.sispro3d.unam.user.dto.ClientResponse;
import com.sispro3d.unam.user.repository.ClientRepository;
import com.sispro3d.unam.user.service.ClientService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public List<ClientResponse> findAll() {
        return clientRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<ClientResponse> findById(Long id) {
        return clientRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public ClientResponse create(ClientRequest request) {
        Client client = toEntity(request);
        Client saved = clientRepository.save(client);
        return toResponse(saved);
    }

    @Override
    public ClientResponse update(Long id, ClientRequest request) {
        int pk = id.intValue();
        clientRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));

        Client client = toEntity(request);
        client.getAccount().setIdUser(pk);
        Client updated = clientRepository.update(client);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        clientRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
        clientRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return clientRepository.existsById(id.intValue());
    }

    private Client toEntity(ClientRequest request) {
        Client client = new Client();
        client.setAccount(new Account(request.getAccountId()));
        return client;
    }

    private ClientResponse toResponse(Client client) {
        ClientResponse.ClientResponseBuilder builder = ClientResponse.builder();

        if (client.getAccount() != null) {
            builder.id(client.getAccount().getIdUser())
                    .name(client.getAccount().getName())
                    .lastName(client.getAccount().getLastName())
                    .email(client.getAccount().getEmail());
        }

        return builder.build();
    }
}
