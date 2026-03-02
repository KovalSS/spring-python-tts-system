package com.example.backendspring.repository;

import com.example.backendspring.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
    List<Job> findAllByUserId(UUID userID);

    Optional<Job> findByIdAndUserId(UUID id, UUID userId);
}
