package com.amaravathi.tradeidentity.domain.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.PK> {

    @Query("select r.code from Role r join UserRole ur on ur.roleId = r.id where ur.userId = :userId")
    List<String> findRoleCodesByUserId(int userId);

    @Transactional
    @Modifying
    @Query("delete from UserRole ur where ur.userId = :userId")
    int deleteByUserId(@Param("userId") int userId);
}
