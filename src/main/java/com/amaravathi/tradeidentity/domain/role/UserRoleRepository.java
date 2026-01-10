package com.amaravathi.tradeidentity.domain.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.PK> {

    @Query("select r.code from Role r join UserRole ur on ur.roleId = r.id where ur.userId = :userId")
    List<String> findRoleCodesByUserId(int userId);

    void deleteByUserId(int userId);
}
