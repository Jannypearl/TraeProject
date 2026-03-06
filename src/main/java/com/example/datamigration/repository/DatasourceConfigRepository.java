package com.example.datamigration.repository;

import com.example.datamigration.entity.DatasourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasourceConfigRepository extends JpaRepository<DatasourceConfig, Long> {
}
