package com.sispro3d.unam.user.repository;


import com.sispro3d.unam.user.domain.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
}
