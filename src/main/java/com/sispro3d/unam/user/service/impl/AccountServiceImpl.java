package com.sispro3d.unam.user.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.dto.AccountDTO;
import com.sispro3d.unam.user.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final GenericDAO<Account> accountDAO;

    public AccountServiceImpl(GenericDAO<Account> accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Override
    public List<AccountDTO> findAll() {
        return accountDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<AccountDTO> findById(int id) {
        return accountDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public AccountDTO create(AccountDTO dto) {
        Account account = toEntity(dto);
        int generatedId = accountDAO.insert(account);
        account.setIdUser(generatedId);
        return toResponseDTO(account);
    }

    @Override
    public AccountDTO update(int id, AccountDTO dto) {
        accountDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Account no encontrado con id: " + id));

        Account account = toEntity(dto);
        account.setIdUser(id);
        accountDAO.update(account);
        return toResponseDTO(account);
    }

    @Override
    public void delete(int id) {
        accountDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Account no encontrado con id: " + id));
        accountDAO.delete(id);
    }

    // ------------------------------------------------------------------
    //  Mappers
    // ------------------------------------------------------------------

    private Account toEntity(AccountDTO dto) {
        Account account = new Account();
        account.setName(dto.getName());
        account.setLastName(dto.getLastName());
        account.setEmail(dto.getEmail());
        account.setPhone(dto.getPhone());
        account.setPassword(dto.getPassword());
        account.setType(dto.getType());
        return account;
    }

    private AccountDTO toResponseDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setIdUser(account.getIdUser());
        dto.setName(account.getName());
        dto.setLastName(account.getLastName());
        dto.setEmail(account.getEmail());
        dto.setPhone(account.getPhone());
        dto.setPassword(account.getPassword());
        dto.setType(account.getType());
        dto.setCreatedAt(account.getCreatedAt());
        return dto;
    }
}