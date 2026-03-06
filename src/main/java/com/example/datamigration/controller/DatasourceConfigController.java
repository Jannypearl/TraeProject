package com.example.datamigration.controller;

import com.example.datamigration.entity.DatasourceConfig;
import com.example.datamigration.repository.DatasourceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/datasource")
public class DatasourceConfigController {

    @Autowired
    private DatasourceConfigRepository datasourceConfigRepository;

    @GetMapping
    public ResponseEntity<List<DatasourceConfig>> getAllDatasources() {
        return ResponseEntity.ok(datasourceConfigRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<DatasourceConfig> createDatasource(@RequestBody DatasourceConfig config) {
        return ResponseEntity.ok(datasourceConfigRepository.save(config));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatasourceConfig> updateDatasource(@PathVariable Long id, @RequestBody DatasourceConfig config) {
        config.setId(id);
        return ResponseEntity.ok(datasourceConfigRepository.save(config));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDatasource(@PathVariable Long id) {
        datasourceConfigRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
