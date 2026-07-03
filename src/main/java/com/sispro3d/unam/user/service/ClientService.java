package com.sispro3d.unam.user.service;

import com.sispro3d.unam.user.dto.ClientDTO;

import java.util.List;
import java.util.Optional;

public interface ClientService {
    List<ClientDTO> findAll();
    Optional<ClientDTO> findById(int id);
    ClientDTO create(ClientDTO dto);
    ClientDTO update(int id, ClientDTO dto);
    void delete(int id);
}
