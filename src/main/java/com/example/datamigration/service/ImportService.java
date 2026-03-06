package com.example.datamigration.service;

import com.example.datamigration.entity.DatasourceConfig;
import com.example.datamigration.repository.DatasourceConfigRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Service
public class ImportService {

    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);

    @Autowired
    private DatasourceService datasourceService;

    @Autowired
    private DatasourceConfigRepository datasourceConfigRepository;

    public void importFromCsv(Long datasourceId, String tableName, String csvFile) throws Exception {
        logger.info("Importing data from CSV file {} to datasource {} table {}", csvFile, datasourceId, tableName);
        DatasourceConfig config = datasourceConfigRepository.findById(datasourceId)
                .orElseThrow(() -> new RuntimeException("Datasource config not found with id: " + datasourceId));
        
        // 根据数据库类型设置驱动类
        String driverClass;
        if (config.getType().equals("TDSQL_MYSQL")) {
            driverClass = "com.mysql.cj.jdbc.Driver";
        } else if (config.getType().equals("TDSQL_PG")) {
            driverClass = "org.postgresql.Driver";
        } else {
            throw new IllegalArgumentException("Unsupported datasource type: " + config.getType());
        }
        config.setDriverClass(driverClass);

        try (Connection connection = datasourceService.getConnection(config);
             FileReader reader = new FileReader(csvFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {

            List<CSVRecord> records = csvParser.getRecords();
            if (records.isEmpty()) {
                logger.warn("CSV file is empty: {}", csvFile);
                return;
            }

            // 获取表头
            CSVRecord headerRecord = records.get(0);
            int columnCount = headerRecord.size();

            // 构建插入SQL语句
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("INSERT INTO " + tableName + " (");
            for (int i = 0; i < columnCount; i++) {
                sqlBuilder.append(headerRecord.get(i));
                if (i < columnCount - 1) {
                    sqlBuilder.append(", ");
                }
            }
            sqlBuilder.append(") VALUES (");
            for (int i = 0; i < columnCount; i++) {
                sqlBuilder.append("?");
                if (i < columnCount - 1) {
                    sqlBuilder.append(", ");
                }
            }
            sqlBuilder.append(")");

            // 批量插入数据
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {
                connection.setAutoCommit(false);

                int rowCount = 0;
                for (int i = 1; i < records.size(); i++) {
                    CSVRecord record = records.get(i);
                    for (int j = 0; j < columnCount; j++) {
                        preparedStatement.setString(j + 1, record.get(j));
                    }
                    preparedStatement.addBatch();
                    rowCount++;

                    // 每1000条提交一次
                    if (i % 1000 == 0) {
                        preparedStatement.executeBatch();
                        connection.commit();
                        logger.info("Imported {} rows to table {}", rowCount, tableName);
                    }
                }

                // 提交剩余的数据
                preparedStatement.executeBatch();
                connection.commit();
                connection.setAutoCommit(true);
                logger.info("Total imported {} rows to table {}", rowCount, tableName);
            }
        } catch (Exception e) {
            logger.error("Failed to import data from CSV file {} to datasource {} table {}", csvFile, datasourceId, tableName, e);
            throw e;
        }
    }
}
