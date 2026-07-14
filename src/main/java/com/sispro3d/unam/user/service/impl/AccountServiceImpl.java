package com.sispro3d.unam.user.service.impl;


import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Override
    public List<AccountResponse> findAll() {
        return List.of();
    }

    @Override
    public Optional<AccountResponse> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public AccountResponse create(AccountRequest request) {
        return null;
    }

    @Override
    public AccountResponse update(Long aLong, AccountRequest request) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }
}
