package com.example.datamigration.controller;

import com.example.datamigration.entity.MigrationTask;
import com.example.datamigration.repository.MigrationTaskRepository;
import com.example.datamigration.service.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/migration")
public class MigrationTaskController {

    @Autowired
    private MigrationTaskRepository migrationTaskRepository;

    @Autowired
    private MigrationService migrationService;

    @GetMapping
    public ResponseEntity<List<MigrationTask>> getAllTasks() {
        return ResponseEntity.ok(migrationTaskRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<MigrationTask> createTask(@RequestBody MigrationTask task) {
        task.setCreatedAt(new Date());
        task.setUpdatedAt(new Date());
        task.setStatus("PENDING");
        return ResponseEntity.ok(migrationTaskRepository.save(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MigrationTask> updateTask(@PathVariable Long id, @RequestBody MigrationTask task) {
        task.setId(id);
        task.setUpdatedAt(new Date());
        return ResponseEntity.ok(migrationTaskRepository.save(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        migrationTaskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Void> executeTask(@PathVariable Long id) throws Exception {
        migrationService.executeMigration(id);
        return ResponseEntity.ok().build();
    }
}
