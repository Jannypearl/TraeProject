package com.example.datamigration.dto;

import java.util.Date;

public class MigrationTaskResponse {
    private Long id;
    private String taskName;
    private String status;
    private String message;
    private Date updatedAt;

    public MigrationTaskResponse() {
    }

    public MigrationTaskResponse(Long id, String taskName, String status, String message, Date updatedAt) {
        this.id = id;
        this.taskName = taskName;
        this.status = status;
        this.message = message;
        this.updatedAt = updatedAt;
    }

    public static MigrationTaskResponse success(Long id, String taskName, String status, String message, Date updatedAt) {
        return new MigrationTaskResponse(id, taskName, status, message, updatedAt);
    }

    public static MigrationTaskResponse error(String message) {
        return new MigrationTaskResponse(null, null, "FAILED", message, new Date());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
