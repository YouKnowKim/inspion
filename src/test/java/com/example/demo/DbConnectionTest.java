package com.example.demo;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DbConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testConnection() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("보조: DB 연결 성공!");
            System.out.println("연결된 DB 정보: " + conn.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("보조: DB 연결 실패...");
            e.printStackTrace();
        }
    }
}