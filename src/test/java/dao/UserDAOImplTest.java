package dao;

import dao.impl.TaskDAOImpl;
import dao.impl.UserDAOImpl;
import entity.User;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Testcontainers
public class UserDAOImplTest {

    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");
    private UserDAO userDAO;
    private TaskDAO taskDAO;
    private Connection connection;
    private static PostgreSQLContainer<?> postgresContainer;

    @BeforeAll
    public static void setUpBeforeClass() {
        postgresContainer = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("test_db")
                .withUsername("test_user")
                .withPassword("test_password");
        postgresContainer.start();
    }

    @AfterAll
    public static void tearDownAfterClass() {
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }

    @BeforeEach
    public void setUp() throws SQLException {
        POSTGRESQL_CONTAINER.start();

        connection = DriverManager.getConnection(
                POSTGRESQL_CONTAINER.getJdbcUrl(),
                POSTGRESQL_CONTAINER.getUsername(),
                POSTGRESQL_CONTAINER.getPassword()
        );

        userDAO = new UserDAOImpl(connection);
        taskDAO = new TaskDAOImpl(connection);

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE users (id BIGSERIAL PRIMARY KEY, name VARCHAR(255))");
            statement.execute("CREATE TABLE tasks (id BIGSERIAL PRIMARY KEY, title VARCHAR(255), description TEXT, assigned_user_id BIGINT)");
            statement.execute("CREATE TABLE user_tasks (user_id BIGINT REFERENCES users(id), task_id BIGINT REFERENCES tasks(id), PRIMARY KEY(user_id, task_id))");
        }
    }

    @Test
    public void testCreateUser() throws SQLException {
        User user = new User();
        user.setName("User 1");
        userDAO.create(user);

        assertNotNull(user.getId(), "User ID should not be null after creation");

        User retrievedUser = userDAO.findById(user.getId());
        assertNotNull(retrievedUser, "Retrieved user should not be null");
        assertEquals("User 1", retrievedUser.getName(), "User name should match");
    }

    @Test
    public void testCreateUserThrowsSQLException() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(stmt);

        doThrow(new SQLException("Failed to execute update")).when(stmt).executeUpdate();

        UserDAO userDAO = new UserDAOImpl(connection);

        User user = new User();
        user.setName("Test User");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userDAO.create(user);
        });

        assertTrue(thrown.getCause() instanceof SQLException);
        assertEquals("Failed to execute update", thrown.getCause().getMessage());
    }

    @Test
    public void testFindAllUsers() throws SQLException {
        User user1 = new User();
        user1.setName("User 1");
        userDAO.create(user1);

        User user2 = new User();
        user2.setName("User 2");
        userDAO.create(user2);

        List<User> users = userDAO.findAll();
        assertEquals(2, users.size(), "There should be two users");
    }

    @Test
    public void testFindUserByIdNotFound() throws SQLException {
        User user = userDAO.findById(999L);
        assertNull(user, "User should be null for non-existent ID");
    }

    @Test
    public void testUpdateUser() throws SQLException {
        User user = new User();
        user.setName("Initial Name");
        userDAO.create(user);

        assertNotNull(user.getId(), "User ID should not be null after creation");

        user.setName("Updated Name");
        userDAO.update(user);

        User updatedUser = userDAO.findById(user.getId());
        assertNotNull(updatedUser, "Updated user should not be null");
        assertEquals("Updated Name", updatedUser.getName(), "User name should be updated");
    }

    @Test
    public void testUpdateUserNotFound() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);

        when(stmt.executeUpdate()).thenReturn(0);

        UserDAO userDAO = new UserDAOImpl(connection);

        User user = new User();
        user.setId(999L);
        user.setName("Non-existent User");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userDAO.update(user);
        });

        assertEquals("User not found with ID: 999", thrown.getMessage());
    }

    @Test
    public void testDeleteUser() throws SQLException {
        User user = new User();
        user.setName("User to be deleted");
        userDAO.create(user);

        assertNotNull(user.getId(), "User ID should not be null after creation");

        userDAO.delete(user.getId());

        User deletedUser = userDAO.findById(user.getId());
        assertNull(deletedUser, "User should be null after deletion");
    }

    @Test
    public void testDeleteUserNotFound() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(0);

        UserDAO userDAO = new UserDAOImpl(connection);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userDAO.delete(999L);
        });

        assertEquals("User not found with ID: 999", thrown.getMessage());
    }

    @Test
    public void testFindAllUsersEmpty() throws SQLException {
        List<User> users = userDAO.findAll();
        assertTrue(users.isEmpty(), "User list should be empty");
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS user_tasks");
            statement.execute("DROP TABLE IF EXISTS tasks");
            statement.execute("DROP TABLE IF EXISTS users");
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        POSTGRESQL_CONTAINER.stop();
    }

    @Test
    public void testAssignTaskToUserWhenTaskAlreadyAssigned() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(checkStmt);
        when(checkStmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1);

        UserDAOImpl userDAO = new UserDAOImpl(connection);

        userDAO.assignTaskToUser(1L, 1L);

        verify(checkStmt, times(1)).executeQuery();
        verify(checkStmt, never()).executeUpdate();
    }

    @Test
    public void testAssignTaskToUserWhenTaskNotAssigned() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString())).thenReturn(checkStmt).thenReturn(insertStmt);
        when(checkStmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        UserDAOImpl userDAO = new UserDAOImpl(connection);

        userDAO.assignTaskToUser(1L, 1L);

        verify(checkStmt).executeQuery();
        verify(insertStmt).executeUpdate();
    }

    @Test
    public void testAssignTaskToUserSQLException() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(checkStmt);
        doThrow(new SQLException("Database error")).when(checkStmt).executeQuery();

        UserDAOImpl userDAO = new UserDAOImpl(connection);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userDAO.assignTaskToUser(1L, 1L);
        });

        assertEquals("Database error", thrown.getCause().getMessage());
    }

    @Test
    public void testDeleteUserSQLException() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        SQLException sqlException = new SQLException("Database error while deleting user");
        doThrow(sqlException).when(stmt).executeUpdate();

        UserDAOImpl userDAO = new UserDAOImpl(connection);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userDAO.delete(1L);
        });

        assertEquals("Database error while deleting user", thrown.getMessage());
        assertEquals(sqlException, thrown.getCause());
    }
    @Test
    public void testUpdateUserSQLException() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        doThrow(new SQLException("Database error")).when(stmt).executeUpdate();

        UserDAOImpl userDAO = new UserDAOImpl(connection);

        User user = new User();
        user.setId(1L);
        user.setName("Updated Name");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userDAO.update(user);
        });

        assertEquals("Database error", thrown.getCause().getMessage());
    }

    @Test
    public void testGetTasksByUserIdSQLException() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        doThrow(new SQLException("Database error")).when(stmt).executeQuery();

        UserDAOImpl userDAO = new UserDAOImpl(connection);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userDAO.getTasksByUserId(1L);
        });

        assertEquals("Database error", thrown.getCause().getMessage());
    }

}
