package com.example.datamigration.service;

import com.example.datamigration.entity.MigrationTask;
import com.example.datamigration.repository.MigrationTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MigrationService {

    private static final Logger logger = LoggerFactory.getLogger(MigrationService.class);

    @Autowired
    private ExportService exportService;

    @Autowired
    private ImportService importService;

    @Autowired
    private MigrationTaskRepository migrationTaskRepository;

    public void executeMigration(Long taskId) throws Exception {
        logger.info("Executing migration task: {}", taskId);
        MigrationTask task = migrationTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Migration task not found with id: " + taskId));
        
        // 检查任务状态，已经成功的任务不能再次执行
        if ("SUCCESS".equals(task.getStatus())) {
            throw new IllegalStateException("Migration task has already been executed successfully and cannot be executed again");
        }
        
        // 检查任务是否正在执行中
        if ("RUNNING".equals(task.getStatus())) {
            throw new IllegalStateException("Migration task is currently running, please wait for it to complete");
        }
        
        task.setStatus("RUNNING");
        task.setUpdatedAt(new Date());
        migrationTaskRepository.save(task);

        try {
            // 1. 从Oracle导出数据到CSV
            logger.info("Step 1: Exporting data from Oracle to CSV");
            String csvFile = exportService.exportToCsv(task.getSourceDatasourceId(), task.getTableName());

            // 2. 将CSV导入到TDSQL
            logger.info("Step 2: Importing data from CSV to TDSQL");
            importService.importFromCsv(task.getTargetDatasourceId(), task.getTableName(), csvFile);

            task.setStatus("SUCCESS");
            logger.info("Migration task {} completed successfully", taskId);
        } catch (Exception e) {
            task.setStatus("FAILED");
            logger.error("Migration task {} failed", taskId, e);
            throw e;
        } finally {
            task.setUpdatedAt(new Date());
            migrationTaskRepository.save(task);
        }
    }
}
