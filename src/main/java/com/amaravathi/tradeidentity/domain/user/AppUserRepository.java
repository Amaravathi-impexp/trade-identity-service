package com.amaravathi.tradeidentity.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    @Query("select u from AppUser u left join fetch u.roles where u.id = :userId")
    Optional<AppUser> findByIdWithRoles(@Param("userId") UUID userId);

    @Query("""
        select distinct u
        from AppUser u
        left join fetch u.originCountry
        left join fetch u.destinationCountry
        left join fetch u.productType
        left join fetch u.roles
        where u.email = :email
      """)
    Optional<AppUser> findByEmailWithDetails(@Param("email") String email);

    @Query("""
        select distinct u
        from AppUser u
        left join fetch u.originCountry
        left join fetch u.destinationCountry
        left join fetch u.productType
        left join fetch u.roles
        where u.id = :id
      """)
    Optional<AppUser> findByIdWithDetails(@Param("id") int id);

    @Query("""
      select distinct u
      from AppUser u
      left join fetch u.originCountry
      left join fetch u.destinationCountry
      left join fetch u.productType
      left join fetch u.roles
    """)
    List<AppUser> findAllWithDetails();

    @Query("""
        select distinct u
        from AppUser u
        join UserTraining ut on ut.userId = u.id
        where ut.trainingId = :trainingId
        order by u.id
    """)
    List<AppUser> findAllUsersEnrolledForAnyTraining(@Param("trainingId") int trainingId);
}
