package com.example.datamigration.repository;

import com.example.datamigration.entity.MigrationTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationTaskRepository extends JpaRepository<MigrationTask, Long> {
}
