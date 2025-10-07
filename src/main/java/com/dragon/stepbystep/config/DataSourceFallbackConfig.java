package com.dragon.stepbystep.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"local", "test"})   // <= prod에서는 로드되지 않음
@Configuration
public class DataSourceFallbackConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceFallbackConfig.class);

    private final Environment environment;

    public DataSourceFallbackConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        boolean forceH2 = environment.getProperty("app.datasource.force-h2", Boolean.class, false);
        String primaryUrl = environment.getProperty("app.datasource.primary.url");

        if (forceH2) {
            log.info("환경 설정에 따라 인메모리 H2 데이터베이스를 강제로 사용합니다.");
            return createH2DataSource();
        }

        if (!StringUtils.hasText(primaryUrl)) {
            log.info("외부 데이터베이스 URL이 제공되지 않아 인메모리 H2 데이터베이스를 사용합니다.");
            return createH2DataSource();
        }

        String primaryUsername = environment.getProperty("app.datasource.primary.username");
        String primaryPassword = environment.getProperty("app.datasource.primary.password");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(primaryUrl);
        config.setUsername(primaryUsername);
        config.setPassword(primaryPassword);
        config.setDriverClassName(environment.getProperty("app.datasource.primary.driver", "com.mysql.cj.jdbc.Driver"));
        config.setMaximumPoolSize(environment.getProperty("app.datasource.primary.max-pool-size", Integer.class, 10));
        Long connectionTimeout = environment.getProperty("app.datasource.primary.connect-timeout-millis", Long.class, 3000L);
        if (connectionTimeout != null && connectionTimeout > 0) {
            config.setConnectionTimeout(connectionTimeout);
        }

        String schema = environment.getProperty("app.datasource.primary.schema");
        if (StringUtils.hasText(schema)) {
            config.setSchema(schema);
        }
        HikariDataSource primaryDataSource = null;
        try {
            primaryDataSource = new HikariDataSource(config);
            testConnection(primaryDataSource);
            log.info("외부 데이터베이스({}) 연결에 성공하여 해당 데이터를 사용합니다.", primaryUrl);
            return primaryDataSource;
        } catch (Exception ex) {
            if (primaryDataSource != null) {
                primaryDataSource.close();
            }
            log.warn("외부 데이터베이스({})에 연결할 수 없어 인메모리 H2 데이터베이스로 대체합니다. 오류: {}", primaryUrl, ex.getMessage());
            log.debug("외부 데이터베이스 연결 실패", ex);
            return createH2DataSource();
        }
    }

    private void testConnection(DataSource dataSource) throws SQLException {
        try (Connection ignored = dataSource.getConnection()) {
            // 단순 연결 검증
        }
    }

    private DataSource createH2DataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(environment.getProperty("app.datasource.h2.url", "jdbc:h2:mem:stepbystep;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"));
        config.setUsername(environment.getProperty("app.datasource.h2.username", "sa"));
        config.setPassword(environment.getProperty("app.datasource.h2.password", ""));
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(environment.getProperty("app.datasource.h2.max-pool-size", Integer.class, 4));

        log.info("인메모리 H2 데이터베이스({})를 사용합니다.", config.getJdbcUrl());
        return new HikariDataSource(config);
    }
}