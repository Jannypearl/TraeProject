package com.example.datamigration.service;

import com.example.datamigration.entity.DatasourceConfig;
import com.example.datamigration.repository.DatasourceConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ExportServiceTest {

    @Autowired
    private ExportService exportService;

    @Autowired
    private DatasourceConfigRepository datasourceConfigRepository;

    @Test
    public void testExportToCsv() throws Exception {
        // 这里需要先创建一个Oracle数据源配置
        DatasourceConfig config = new DatasourceConfig();
        config.setName("Test Oracle");
        config.setType("ORACLE");
        config.setUrl("jdbc:oracle:thin:@localhost:1521:ORCL");
        config.setUsername("system");
        config.setPassword("oracle");
        config.setDriverClass("oracle.jdbc.OracleDriver");
        datasourceConfigRepository.save(config);

        // 测试导出功能
        String csvFile = exportService.exportToCsv(config.getId(), "EMPLOYEES");
        assertNotNull(csvFile, "CSV文件路径不能为空");
    }
}
