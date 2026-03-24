package com.example.datamigration.service;

import com.example.datamigration.entity.DatasourceConfig;
import com.example.datamigration.repository.DatasourceConfigRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    @Autowired
    private DatasourceService datasourceService;

    @Autowired
    private DatasourceConfigRepository datasourceConfigRepository;

    @Value("${application.temp-dir}")
    private String tempDir;

    public String exportToCsv(Long datasourceId, String tableName) throws Exception {
        logger.info("Exporting data from datasource {} table {}", datasourceId, tableName);
        DatasourceConfig config = datasourceConfigRepository.findById(datasourceId)
                .orElseThrow(() -> new RuntimeException("Datasource config not found with id: " + datasourceId));
        
        // 根据数据库类型设置驱动类
        String driverClass;
        if (config.getType().equals("ORACLE")) {
            driverClass = "oracle.jdbc.OracleDriver";
        } else if (config.getType().equals("MYSQL") || config.getType().equals("TDSQL_MYSQL")) {
            driverClass = "com.mysql.cj.jdbc.Driver";
        } else if (config.getType().equals("POSTGRESQL") || config.getType().equals("TDSQL_PG")) {
            driverClass = "org.postgresql.Driver";
        } else {
            throw new IllegalArgumentException("Unsupported datasource type: " + config.getType());
        }
        config.setDriverClass(driverClass);
        
        try (Connection connection = datasourceService.getConnection(config);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = tempDir + "/" + tableName + "_" + timestamp + ".csv";
            logger.info("Generating CSV file: {}", fileName);

            // 确保临时目录存在
            File tempDirectory = new File(tempDir);
            if (!tempDirectory.exists()) {
                tempDirectory.mkdirs();
                logger.info("Created temporary directory: {}", tempDir);
            }

            // 创建 CSV 文件
            try (FileWriter writer = new FileWriter(fileName);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

                // 写入表头
                String[] headers = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    headers[i - 1] = metaData.getColumnName(i);
                }
                csvPrinter.printRecord((Object[]) headers);

                // 写入数据
                int rowCount = 0;
                while (resultSet.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = resultSet.getObject(i);
                    }
                    csvPrinter.printRecord(row);
                    rowCount++;
                }

                csvPrinter.flush();
                logger.info("Exported {} rows to CSV file: {}", rowCount, fileName);
            }

            return fileName;
        } catch (Exception e) {
            logger.error("Failed to export data from datasource {} table {}", datasourceId, tableName, e);
            throw e;
        }
    }
}
