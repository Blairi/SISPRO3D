package com.sispro3d.unam.revokedtoken.repository;

import com.sispro3d.unam.revokedtoken.domain.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
}
