package com.example.TestTaskNatlex.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:hibernate.properties")
public class PostgresConfig {

    private final Environment environment;

    @Autowired
    public PostgresConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();

        driverManagerDataSource.setDriverClassName(environment.getRequiredProperty("hibernate.driver_class"));
        driverManagerDataSource.setUrl(environment.getRequiredProperty("hibernate.connection.url"));
        driverManagerDataSource.setUsername(environment.getRequiredProperty("hibernate.connection.username"));
        driverManagerDataSource.setPassword(environment.getRequiredProperty("hibernate.connection.password"));

        return driverManagerDataSource;
    }
}
