package com.amaravathi.tradeidentity.domain.training;

import com.amaravathi.tradeidentity.domain.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserTrainingRepository extends JpaRepository<UserTraining, UserTraining.PK> {
    @Transactional
    @Modifying
    @Query("delete from UserTraining ut where ut.userId = :userId")
    int deleteAllByUserId(@Param("userId") int userId);


    Optional<AppUser> findAllUsersByTrainingId(int trainingId);

    @Transactional
    @Modifying
    @Query("delete from UserTraining ut where ut.trainingId = :trainingId")
    void deleteAllByTrainingId(int trainingId);
}
