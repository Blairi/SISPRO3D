package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.AccountDTO;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    List<AccountDTO> findAll();
    Optional<AccountDTO> findById(int id);
    AccountDTO create(AccountDTO dto);
    AccountDTO update(int id, AccountDTO dto);
    void delete(int id);
}