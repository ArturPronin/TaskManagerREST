package config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import exception.ConfigurationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class DatabaseConfig {
    private static final String ROOT_PATH;
    private static final String DB_CONFIG_PATH;
    private static final Properties PROPERTIES = new Properties();
    private static final HikariConfig HIKARI_CONFIG = new HikariConfig();
    private static final HikariDataSource HIKARI_DATA_SOURCE;
    private static final String JDBC_URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    private DatabaseConfig() {
    }

    static {
        ROOT_PATH = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath().replace("%20", " ");
        DB_CONFIG_PATH = ROOT_PATH + "database.properties";
        try {
            PROPERTIES.load(new FileInputStream(DB_CONFIG_PATH));
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load database configuration", e);
        }
        JDBC_URL = PROPERTIES.getProperty("database.url");
        USERNAME = PROPERTIES.getProperty("database.username");
        PASSWORD = PROPERTIES.getProperty("database.password");
        HIKARI_CONFIG.setDriverClassName(org.postgresql.Driver.class.getName());
        HIKARI_CONFIG.setJdbcUrl(JDBC_URL);
        HIKARI_CONFIG.setUsername(USERNAME);
        HIKARI_CONFIG.setPassword(PASSWORD);
        HIKARI_DATA_SOURCE = new HikariDataSource(HIKARI_CONFIG);
    }

    public static Connection getConnection() throws SQLException {
        return HIKARI_DATA_SOURCE.getConnection();
    }
}
