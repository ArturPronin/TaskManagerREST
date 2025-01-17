package config;

import controller.TagController;
import controller.TaskController;
import controller.UserController;
import factory.impl.TagControllerFactory;
import factory.impl.TaskControllerFactory;
import factory.impl.UserControllerFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseConfigTest {
    private static PostgreSQLContainer<?> postgresContainer;
    private static String rootPath;
    private static Path dbConfigPath;
    private static String dbConfigFileName;
    private static Properties properties;
    private static String username;
    private static String password;
    private static String container;
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    @TempDir
    Path tempDir;

    @BeforeAll
    public void setUpContainer() throws IOException, URISyntaxException {
        properties = new Properties();
        dbConfigFileName = "database.properties";
        dbConfigPath = Path.of(Paths.get(classloader.getResource(dbConfigFileName).toURI()).toString().replace("%20", " "));
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(dbConfigPath.toFile())) {
            properties.load(fis);
        }
        username = properties.getProperty("database.username");
        password = properties.getProperty("database.password");
        container = properties.getProperty("container.name");

        postgresContainer = new PostgreSQLContainer<>(container)
                .withUsername(username)
                .withPassword(password);
        postgresContainer.start();


        properties.setProperty("database.url", postgresContainer.getJdbcUrl());
        properties.setProperty("database.username", postgresContainer.getUsername());
        properties.setProperty("database.password", postgresContainer.getPassword());
        properties.setProperty("container.name", container);

        try (FileOutputStream fos = new FileOutputStream(dbConfigPath.toFile())) {
            properties.store(fos, null);
        }


        try (Connection conn = DriverManager.getConnection(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword())) {
            try (Statement statement = conn.createStatement()) {
                try {
                    statement.execute("DROP DATABASE IF EXISTS test_db");
                } catch (SQLException e) {

                }
                statement.execute("CREATE DATABASE test_db");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void tearDownContainer() {
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }

    @BeforeEach
    public void setUp() throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword())) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("CREATE TABLE users (id BIGSERIAL PRIMARY KEY, name VARCHAR(255))");
                statement.execute("CREATE TABLE tasks (id BIGSERIAL PRIMARY KEY, title VARCHAR(255), description TEXT, assigned_user_id BIGINT)");
                statement.execute("CREATE TABLE user_tasks (user_id BIGINT REFERENCES users(id), task_id BIGINT REFERENCES tasks(id), PRIMARY KEY(user_id, task_id))");
                statement.execute("CREATE TABLE tags (id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL)");
                statement.execute("CREATE TABLE task_tag (task_id BIGINT REFERENCES tasks(id), tag_id BIGINT REFERENCES tags(id), PRIMARY KEY (task_id, tag_id))");
            }
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword())) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS user_tasks CASCADE");
                statement.execute("DROP TABLE IF EXISTS tasks CASCADE");
                statement.execute("DROP TABLE IF EXISTS users CASCADE");
                statement.execute("DROP TABLE IF EXISTS tags CASCADE");
                statement.execute("DROP TABLE IF EXISTS task_tag CASCADE");
            }
        }
    }

    @Test
    public void testGetConnection() throws SQLException {

        Connection connection = DatabaseConfig.getConnection();
        assertNotNull(connection, "Connection should not be null");
        connection.close();
    }

    @Test
    public void testCreateUserController() {
        UserController userController = UserControllerFactory.createUserController();
        assertNotNull(userController, "UserController should not be null");
    }

    @Test
    public void testCreateTagController() {
        TagController tagController = TagControllerFactory.createTagController();
        assertNotNull(tagController, "TagController should not be null");
    }

    @Test
    public void testCreateTaskController() {
        TaskController taskController = TaskControllerFactory.createTaskController();
        assertNotNull(taskController, "TaskController should not be null");
    }

    @Test
    public void testGetConnectionWithValidProperties() throws IOException, SQLException {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))) {
            postgres.start();

            Path tempFile = tempDir.resolve("database.properties");
            try (FileWriter writer = new FileWriter(tempFile.toFile())) {
                writer.write("database.url=" + postgres.getJdbcUrl() + "\n");
                writer.write("database.username=" + postgres.getUsername() + "\n");
                writer.write("database.password=" + postgres.getPassword() + "\n");
            }
            Connection connection = DatabaseConfig.getConnection();

            assertNotNull(connection);

            connection.close();
        }
    }

    @Test
    public void testCreateTaskControllerNotParameters() {
        TaskController taskController = new TaskController();
        assertNotNull(taskController, "TaskController should not be null");
    }

    @Test
    public void testCreateUserControllerNotParameters() {
        UserController userController = new UserController();
        assertNotNull(userController, "UserController should not be null");
    }

    @Test
    public void testCreateTagControllerNotParameters() {
        TagController tagController = new TagController();
        assertNotNull(tagController, "TagController should not be null");
    }
}