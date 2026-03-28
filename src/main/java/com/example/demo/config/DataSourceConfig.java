package com.example.demo.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

	@Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // 1. 드라이버 클래스 변경 (PostgreSQL)
        config.setDriverClassName("org.postgresql.Driver");
        
        // 2. JDBC URL 변경 (Supabase 호스트, 포트, DB명 적용)
        config.setJdbcUrl("jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres");
        
        // 3. 계정 정보 변경
        config.setUsername("postgres.mswxzgweyfmipwtckdxh");
        config.setPassword("Rladbsgh12!@"); // 실제 Supabase 비밀번호를 입력하세요.

        // (선택사항) Supabase는 원격 연결이므로 연결 유지 설정을 추가하면 좋습니다.
        config.addDataSourceProperty("tcpKeepAlive", "true");
        
        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        // Mapper XML 파일 위치 설정 (classpath:/mapper/*.xml)
        org.springframework.core.io.support.PathMatchingResourcePatternResolver resolver =
                new org.springframework.core.io.support.PathMatchingResourcePatternResolver();
        factory.setMapperLocations(resolver.getResources("classpath:/mapper/**/*.xml"));

        // Optional: Type alias 설정
        factory.setTypeAliasesPackage("com.example.demo.dao");

        return factory.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
	
}
