package com.example.datamigration.service;

import com.example.datamigration.entity.DatasourceConfig;
import com.example.datamigration.repository.DatasourceConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ImportServiceTest {

    @Autowired
    private ImportService importService;

    @Autowired
    private DatasourceConfigRepository datasourceConfigRepository;

    @Test
    public void testImportFromCsv() throws Exception {
        // 这里需要先创建一个TDSQL数据源配置
        DatasourceConfig config = new DatasourceConfig();
        config.setName("Test TDSQL-MySQL");
        config.setType("TDSQL_MYSQL");
        config.setUrl("jdbc:mysql://localhost:3306/test");
        config.setUsername("root");
        config.setPassword("123456");
        config.setDriverClass("com.mysql.cj.jdbc.Driver");
        datasourceConfigRepository.save(config);

        // 测试导入功能
        // 注意：这里需要先有一个CSV文件
        String csvFile = "./temp/EMPLOYEES_test.csv";
        importService.importFromCsv(config.getId(), "EMPLOYEES", csvFile);
    }
}
