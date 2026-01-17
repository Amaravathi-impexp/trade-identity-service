package com.amaravathi.tradeidentity.domain.training;

import com.amaravathi.tradeidentity.domain.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainingRepository extends JpaRepository<Training, Integer> {

    @Query("""
        select t
        from Training t
        join UserTraining ut on ut.trainingId = t.id
        where ut.userId = :userId
    """)
    List<Training> findAllTrainingsByUserId(@Param("userId") int userId);

}
