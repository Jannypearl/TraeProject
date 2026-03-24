package com.example.datamigration.controller;

import com.example.datamigration.dto.MigrationTaskResponse;
import com.example.datamigration.entity.MigrationTask;
import com.example.datamigration.repository.MigrationTaskRepository;
import com.example.datamigration.service.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> executeTask(@PathVariable Long id) {
        try {
            migrationService.executeMigration(id);
            
            // 获取更新后的任务信息
            MigrationTask task = migrationTaskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Migration task not found with id: " + id));
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "迁移任务执行成功");
            response.put("data", Map.of(
                "taskId", task.getId(),
                "taskName", task.getTaskName(),
                "status", task.getStatus(),
                "tableName", task.getTableName(),
                "updatedAt", task.getUpdatedAt()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 返回错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "迁移任务执行失败：" + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
