package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.AccountDTO;
import mx.unam.dgtic.entities.AccountEntity;
import mx.unam.dgtic.mapper.AccountMapper;
import mx.unam.dgtic.repository.IAccountRepository;
import mx.unam.dgtic.repository.impl.AccountRepository;
import mx.unam.dgtic.service.AccountService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class AccountServiceImpl implements AccountService {

    private final IAccountRepository accountRepository;

    public AccountServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
		EntityManager em = emf.createEntityManager();
		this.accountRepository = new AccountRepository(em);
    }

    @Override
    public List<AccountDTO> findAll() {
        return AccountMapper.toDtoList(accountRepository.findAll());
    }

    @Override
    public AccountDTO findById(Integer id) {
        AccountEntity existingAccount = accountRepository.findById(id);
        if (existingAccount == null) {
            throw new RuntimeException("Account no encontrado con id: " + id);
        }
        return AccountMapper.toDTO(existingAccount);
    }

    @Override
    public AccountDTO create(AccountDTO dto) {
        AccountEntity entity = AccountMapper.toEntity(dto);
        accountRepository.save(entity);
        return AccountMapper.toDTO(entity);
    }

    @Override
    public AccountDTO update(Integer id, AccountDTO dto) {

        AccountEntity existingAccount = accountRepository.findById(id);
        if (existingAccount == null) {
            throw new RuntimeException("Account no encontrado con id: " + id);
        }

        AccountEntity accountEntity = AccountMapper.toEntity(dto);

        accountRepository.update(accountEntity);

        return AccountMapper.toDTO(accountEntity);
    }

    @Override
    public void delete(Integer id) {

        AccountEntity existingAccount = accountRepository.findById(id);
        if (existingAccount == null) {
            throw new RuntimeException("Account no encontrado con id: " + id);
        }

        accountRepository.delete(existingAccount);
    }
}