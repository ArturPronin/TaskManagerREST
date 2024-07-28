package dao;

import dao.impl.TagDAOImpl;
import dao.impl.TaskDAOImpl;
import dao.impl.UserDAOImpl;
import entity.Tag;
import exception.ConfigurationException;
import exception.DatabaseOperationException;
import exception.TaskAssignmentException;
import exception.UserNotFoundException;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TagDAOImplTest {

    private static PostgreSQLContainer<?> postgresContainer;
    private TaskDAO taskDAO;
    private static Connection connection;
    private TagDAO tagDAO;
    private static String rootPath;
    private static String dbConfigPath;
    private static Properties properties;
    private static String username;
    private static String password;
    private static String container;

    @BeforeAll
    public static void setUpBeforeClass() {
        properties = new Properties();
        rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath().replace("%20", " ");
        dbConfigPath = rootPath + "database.properties";
        try {
            properties.load(new FileInputStream(dbConfigPath));
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load database configuration", e);
        }

        username = properties.getProperty("database.username");
        password = properties.getProperty("database.password");
        container = properties.getProperty("container.name");

        postgresContainer = new PostgreSQLContainer<>(container)
                .withUsername(username)
                .withPassword(password);
        postgresContainer.start();

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
    public static void tearDownAfterClass() {
        if (postgresContainer != null) {
            try (Connection conn = DriverManager.getConnection(
                    postgresContainer.getJdbcUrl(),
                    postgresContainer.getUsername(),
                    postgresContainer.getPassword())) {

                try (Statement statement = conn.createStatement()) {
                    statement.execute("DROP DATABASE IF EXISTS test_db");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            postgresContainer.stop();
        }
    }

    @BeforeEach
    public void setUp() throws SQLException {
        postgresContainer.start();

        connection = DriverManager.getConnection(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword());

        taskDAO = new TaskDAOImpl(connection);
        tagDAO = new TagDAOImpl(connection);

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE users (id BIGSERIAL PRIMARY KEY, name VARCHAR(255))");
            statement.execute("CREATE TABLE tasks (id BIGSERIAL PRIMARY KEY, title VARCHAR(255), description TEXT, assigned_user_id BIGINT)");
            statement.execute("CREATE TABLE user_tasks (user_id BIGINT REFERENCES users(id), task_id BIGINT REFERENCES tasks(id), PRIMARY KEY(user_id, task_id))");
            statement.execute("CREATE TABLE tags (id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL)");
            statement.execute("CREATE TABLE task_tag (task_id BIGINT REFERENCES tasks(id), tag_id BIGINT REFERENCES tags(id), PRIMARY KEY (task_id, tag_id))");
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS user_tasks CASCADE");
            statement.execute("DROP TABLE IF EXISTS tasks CASCADE");
            statement.execute("DROP TABLE IF EXISTS users CASCADE");
            statement.execute("DROP TABLE IF EXISTS tags CASCADE");
            statement.execute("DROP TABLE IF EXISTS task_tag CASCADE");
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testCreateTag() throws SQLException {
        Tag tag = new Tag();
        tag.setName("Tag1");
        tagDAO.create(tag);

        assertNotNull(tag.getId(), "Tag ID should not be null after creation");

        Tag retrievedTag = tagDAO.findById(tag.getId());
        assertNotNull(retrievedTag, "Retrieved tag should not be null");
        assertEquals("Tag1", retrievedTag.getName(), "Tag name should match");
    }

    @Test
    public void testCreateTagThrowsSQLException() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(stmt);

        doThrow(new SQLException("Failed to execute update")).when(stmt).executeUpdate();

        TagDAO tagDAO = new TagDAOImpl(connection);

        Tag tag = new Tag();
        tag.setName("Tag1");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            tagDAO.create(tag);
        });

        assertTrue(thrown.getCause() instanceof SQLException);
        assertEquals("Failed to execute update", thrown.getCause().getMessage());
    }

    @Test
    public void testFindAllTags() throws SQLException {
        Tag tag1 = new Tag();
        tag1.setName("Tag1");
        tagDAO.create(tag1);

        Tag tag2 = new Tag();
        tag2.setName("Tag2");
        tagDAO.create(tag2);

        List<Tag> tags = tagDAO.findAll();
        assertEquals(2, tags.size(), "There should be two tags");
    }

    @Test
    public void testFindTagByIdNotFound() throws SQLException {
        Tag tag = tagDAO.findById(999L);
        assertNull(tag, "Tag should be null for non-existent ID");
    }

    @Test
    public void testUpdateTag() throws SQLException {
        Tag tag = new Tag();
        tag.setName("Tag1");
        tagDAO.create(tag);

        assertNotNull(tag.getId(), "Tag ID should not be null after creation");

        tag.setName("Updated Tag");
        tagDAO.update(tag);

        Tag updatedTag = tagDAO.findById(tag.getId());
        assertNotNull(updatedTag, "Updated tag should not be null");
        assertEquals("Updated Tag", updatedTag.getName(), "Tag name should be updated");
    }

    @Test
    public void testUpdateTagNotFound() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);

        when(stmt.executeUpdate()).thenReturn(0);

        UserDAO userDAO = new UserDAOImpl(connection);

        Tag tag = new Tag();
        tag.setId(999L);
        tag.setName("Tag1");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            tagDAO.update(tag);
        });

        assertEquals("Tag not found with ID: 999", thrown.getMessage());
    }

    @Test
    public void testDeleteTag() throws SQLException {
        Tag tag = new Tag();
        tag.setName("Tag1");
        tagDAO.create(tag);

        assertNotNull(tag.getId(), "Tag ID should not be null after creation");

        tagDAO.delete(tag.getId());

        Tag deletedTag = tagDAO.findById(tag.getId());
        assertNull(deletedTag, "Tag should be null after deletion");
    }

    @Test
    public void testDeleteTagNotFound() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(0);

        TagDAO tagDAO = new TagDAOImpl(connection);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            tagDAO.delete(999L);
        });

        assertEquals("Tag not found with ID: 999", thrown.getMessage());
    }

    @Test
    public void testFindAllTagsEmpty() throws SQLException {
        List<Tag> tags = tagDAO.findAll();
        assertTrue(tags.isEmpty(), "Tag list should be empty");
    }

    @Test
    public void testAssignTaskToTagWhenTaskAlreadyAssigned() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(checkStmt);
        when(checkStmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1);

        TagDAO tagDAO = new TagDAOImpl(connection);

        tagDAO.assignTaskToTag(1L, 1L);

        verify(checkStmt, times(1)).executeQuery();
        verify(checkStmt, never()).executeUpdate();
    }

    @Test
    public void testAssignTaskToTagWhenTaskNotAssigned() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString())).thenReturn(checkStmt).thenReturn(insertStmt);
        when(checkStmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        TagDAO tagDAO = new TagDAOImpl(connection);

        tagDAO.assignTaskToTag(1L, 1L);

        verify(checkStmt).executeQuery();
        verify(insertStmt).executeUpdate();
    }

    @Test
    public void testAssignTaskToTagSQLException() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(checkStmt);
        doThrow(new SQLException("Database error")).when(checkStmt).executeQuery();

        TagDAO tagDAO = new TagDAOImpl(connection);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            tagDAO.assignTaskToTag(1L, 1L);
        });

        assertEquals("Database error", thrown.getCause().getMessage());
    }

    @Test
    public void testDeleteTagSQLException() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        SQLException sqlException = new SQLException("Database error while deleting tag");
        doThrow(sqlException).when(stmt).executeUpdate();

        TagDAO tagDAO = new TagDAOImpl(connection);


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            tagDAO.delete(1L);
        });

        assertEquals("Database error while deleting tag", thrown.getMessage());
        assertEquals(sqlException, thrown.getCause());
    }

    @Test
    public void testUpdateTagSQLException() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        doThrow(new SQLException("Database error")).when(stmt).executeUpdate();

        TagDAO tagDAO = new TagDAOImpl(connection);

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Tag Update");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            tagDAO.update(tag);
        });

        assertEquals("Database error", thrown.getCause().getMessage());
    }

    @Test
    public void testGetTasksByTagIdSQLException() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        doThrow(new SQLException("Database error")).when(stmt).executeQuery();

        TagDAO tagDAO = new TagDAOImpl(connection);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            tagDAO.getTasksByTagId(1L);
        });

        assertEquals("Database error", thrown.getCause().getMessage());
    }

    @Test
    void testDeleteTagNotFoundCatch() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);
        assertThrows(UserNotFoundException.class, () -> tagDAO.delete(1L), "Tag not found with ID: 1");
    }

    @Test
    void testDeleteDatabaseError() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Test SQLException"));
        assertThrows(DatabaseOperationException.class, () -> tagDAO.delete(1L), "Database error while deleting tag");
    }

    @Test
    void testAssignTaskToTagCheckSQLException() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(contains("SELECT COUNT(*)"))).thenReturn(checkStmt);
        when(checkStmt.executeQuery()).thenThrow(new SQLException("Test SQLException"));
        assertThrows(TaskAssignmentException.class, () -> tagDAO.assignTaskToTag(1L, 1L), "Error checking task assignment");
    }

    @Test
    void testAssignTaskToTagInsertSQLException() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(contains("SELECT COUNT(*)"))).thenReturn(checkStmt);
        when(checkStmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        PreparedStatement insertStmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(contains("INSERT INTO user_tasks"))).thenReturn(insertStmt);
        doThrow(new SQLException("Test SQLException")).when(insertStmt).executeUpdate();
        assertThrows(TaskAssignmentException.class, () -> tagDAO.assignTaskToTag(1L, 1L), "Error assigning task to tag");
    }
}
