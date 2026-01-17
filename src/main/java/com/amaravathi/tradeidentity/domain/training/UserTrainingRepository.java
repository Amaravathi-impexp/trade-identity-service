package com.amaravathi.tradeidentity.domain.training;

import com.amaravathi.tradeidentity.domain.role.UserRole;
import com.amaravathi.tradeidentity.domain.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserTrainingRepository extends JpaRepository<UserTraining, UserTraining.PK> {
    @Transactional
    void deleteAllByUserId(int userId);

    Optional<AppUser> findAllUsersByTrainingId(int trainingId);
}
