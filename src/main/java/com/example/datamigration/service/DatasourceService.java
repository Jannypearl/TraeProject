package com.example.datamigration.service;

import com.example.datamigration.entity.DatasourceConfig;
import com.example.datamigration.repository.DatasourceConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class DatasourceService {

    private static final Logger logger = LoggerFactory.getLogger(DatasourceService.class);

    @Autowired
    private DatasourceConfigRepository datasourceConfigRepository;

    public Connection getConnection(DatasourceConfig config) throws SQLException, ClassNotFoundException {
        logger.info("Creating connection to datasource: {}", config.getName());
        try {
            Class.forName(config.getDriverClass());
            Connection connection = DriverManager.getConnection(
                    config.getUrl(),
                    config.getUsername(),
                    config.getPassword()
            );
            logger.info("Connection created successfully for datasource: {}", config.getName());
            return connection;
        } catch (ClassNotFoundException e) {
            logger.error("Driver class not found: {}", config.getDriverClass(), e);
            throw e;
        } catch (SQLException e) {
            logger.error("Failed to create connection to datasource: {}", config.getName(), e);
            throw e;
        }
    }

    public Connection getConnectionById(Long id) throws SQLException, ClassNotFoundException {
        logger.info("Getting connection by id: {}", id);
        DatasourceConfig config = datasourceConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Datasource config not found with id: " + id));
        return getConnection(config);
    }
}
