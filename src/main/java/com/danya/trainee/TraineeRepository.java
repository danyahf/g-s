package com.danya.trainee;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUserUsername(String username);

    @EntityGraph(attributePaths = {"trainers", "trainers.specialization"})
    Optional<Trainee> findWithTrainersByUserUsername(String username);

    @Query("SELECT t FROM Trainee t " +
            "LEFT JOIN FETCH t.trainings tr " +
            "LEFT JOIN FETCH tr.trainer " +
            "WHERE t.user.username = :username " +
            "AND (tr IS NULL OR tr.date >= CURRENT_DATE)")
    Optional<Trainee> findWithUpcomingTrainingsAndTrainersByUsername(@Param("username") String username);
}

