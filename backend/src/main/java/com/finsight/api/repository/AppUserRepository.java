package com.finsight.api.repository;

import com.finsight.api.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByAuth0Sub(String sub);
}
