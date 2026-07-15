package com.sispro3d.unam.user.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;

public interface AccountService extends CrudService<AccountRequest, AccountResponse, Long> {
}
