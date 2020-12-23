package com.jacky.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author jacky
 * @time 2020-12-23 09:49
 * @discription  Mybatis初始化表
 */

@Component
public class MybatisDatabaseInitializer {

    @Autowired
    DataSource dataSource;

    @PostConstruct
    public void init() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" //
                        + "id BIGINT IDENTITY NOT NULL PRIMARY KEY, " //
                        + "email VARCHAR(100) NOT NULL, " //
                        + "password VARCHAR(100) NOT NULL, " //
                        + "name VARCHAR(100) NOT NULL, " //
                        + "createdAt BIGINT NOT NULL, " //
                        + "UNIQUE (email))");
            }
        }
    }
}
