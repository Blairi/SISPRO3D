package com.sispro3d.unam.user.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;

import java.util.List;
import java.util.Optional;

public interface AccountService extends CrudService<AccountRequest, AccountResponse, Long> {
    Optional<AccountResponse> findByEmail(String email);
    List<AccountResponse> findByRole(Role role);
}
