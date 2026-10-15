package com.sispro3d.unam.user.repository;


import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    List<Account> findByRole(Role role);
}
