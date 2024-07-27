package dao;

import dao.impl.TaskDAOImpl;
import dao.impl.UserDAOImpl;
import entity.Task;
import entity.User;
import exception.DatabaseOperationException;
import factory.impl.TaskFactoryImpl;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Testcontainers
public class TaskDAOImplTest {
    private static PostgreSQLContainer<?> postgresContainer;
    private TaskDAO taskDAO;
    private static Connection connection;
    private UserDAO userDAO;

    @BeforeAll
    public static void setUpBeforeClass() {
        postgresContainer = new PostgreSQLContainer<>("postgres:16").withUsername("test_user").withPassword("test_password");
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
            try (Connection conn = DriverManager.getConnection(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword())) {

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
        userDAO = new UserDAOImpl(connection);

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE users (id BIGSERIAL PRIMARY KEY, name VARCHAR(255))");
            statement.execute("CREATE TABLE tasks (id BIGSERIAL PRIMARY KEY, title VARCHAR(255), description TEXT, assigned_user_id BIGINT)");
            statement.execute("CREATE TABLE user_tasks (user_id BIGINT REFERENCES users(id), task_id BIGINT REFERENCES tasks(id), PRIMARY KEY(user_id, task_id))");
        }
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
    }

    @Test
    public void testCreateTask() throws SQLException {
        User user = new User();
        user.setName("User1");
        userDAO.create(user);

        Task task = new Task();
        task.setTitle("Task1");
        task.setDescription("Test Task Description");
        task.setAssignedUserId(user.getId());
        taskDAO.create(task);

        assertNotNull(task.getId(), "Task ID should not be null after creation");

        Task retrievedTask = taskDAO.findById(task.getId());
        assertNotNull(retrievedTask, "Retrieved task should not be null");
        assertEquals("Task1", retrievedTask.getTitle());
        assertEquals("Test Task Description", retrievedTask.getDescription());
        assertEquals(user.getId(), retrievedTask.getAssignedUserId());
    }

    @Test
    public void testCreateTaskThrowsSQLException() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement insertTaskStmt = mock(PreparedStatement.class);
        PreparedStatement insertUserTaskStmt = mock(PreparedStatement.class);
        ResultSet generatedKeys = mock(ResultSet.class);

        try {
            when(connection.prepareStatement(any(String.class))).thenReturn(insertTaskStmt).thenReturn(insertUserTaskStmt);
            when(insertTaskStmt.executeQuery()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getLong(1)).thenReturn(1L);
            doThrow(new SQLException("Database error")).when(insertTaskStmt).executeQuery();

            TaskDAOImpl taskDAO = new TaskDAOImpl(connection);

            Task task = new Task();
            task.setTitle("Test Task");
            task.setDescription("Test Description");
            task.setAssignedUserId(1L);

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> taskDAO.create(task));
            assertInstanceOf(SQLException.class, thrown.getCause());
            assertEquals("Database error", thrown.getCause().getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("SQLException occurred during test setup");
        }
    }

    @Test
    public void testFindAllTasks() throws SQLException {
        User user = new User();
        user.setName("User1");
        userDAO.create(user);

        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setDescription("Description 1");
        task1.setAssignedUserId(user.getId());
        taskDAO.create(task1);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setAssignedUserId(user.getId());
        taskDAO.create(task2);

        List<Task> tasks = taskDAO.findAll();
        assertEquals(2, tasks.size(), "There should be two tasks");
    }

    @Test
    public void testFindTaskByIdNotFound() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        TaskDAOImpl taskDAO = new TaskDAOImpl(connection);
        Task task = taskDAO.findById(999L);
        assertNull(task, "Task should be null for non-existent ID");
    }

    @Test
    public void testUpdateTask() throws SQLException {
        User user = new User();
        user.setName("User1");
        userDAO.create(user);

        Task task = new Task();
        task.setTitle("Initial Title");
        task.setDescription("Initial Description");
        task.setAssignedUserId(user.getId());
        taskDAO.create(task);

        assertNotNull(task.getId(), "Task ID should not be null after creation");

        task.setTitle("Updated Title");
        task.setDescription("Updated Description");
        task.setAssignedUserId(user.getId());

        taskDAO.update(task);

        Task updatedTask = taskDAO.findById(task.getId());
        assertNotNull(updatedTask, "Updated task should not be null");
        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(updatedTask.getAssignedUserId(), user.getId());
    }

    @Test
    void testUpdateTaskNotFound() throws SQLException {

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
        PreparedStatement mockUpdateStmt = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        TaskDAOImpl dao = new TaskDAOImpl(mockConnection);
        when(mockConnection.prepareStatement(ArgumentMatchers.anyString())).thenReturn(mockSelectStmt);
        when(mockSelectStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        Task taskToUpdate = new Task();
        taskToUpdate.setId(1L);
        DatabaseOperationException thrown = assertThrows(DatabaseOperationException.class, () -> {
            dao.update(taskToUpdate);
        });

        assertTrue(thrown.getMessage().contains("Database exception"));
    }

    @Test
    public void testDeleteTask() throws SQLException {
        User user = new User();
        user.setName("User1");
        userDAO.create(user);

        Task task = new Task();
        task.setTitle("Task to be deleted");
        task.setDescription("Description");
        task.setAssignedUserId(user.getId());
        taskDAO.create(task);

        assertNotNull(task.getId(), "Task ID should not be null after creation");

        taskDAO.delete(task.getId());

        Task deletedTask = taskDAO.findById(task.getId());
        assertNull(deletedTask, "Task should be null after deletion");
    }

    @Test
    public void testDeleteTaskNotFound() throws SQLException {
        taskDAO.delete(999L);

        Task task = taskDAO.findById(999L);
        assertNull(task, "Task should be null for non-existent ID");
    }

    @Test
    public void testFindAllTasksEmpty() throws SQLException {
        List<Task> tasks = taskDAO.findAll();
        assertTrue(tasks.isEmpty(), "Task list should be empty");
    }

    @Test
    public void testFindById() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        TaskFactoryImpl taskFactory = mock(TaskFactoryImpl.class);
        Task task = mock(Task.class);
        when(taskFactory.create()).thenReturn(task);

        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("title")).thenReturn("Title");
        when(rs.getString("description")).thenReturn("Description");
        when(rs.getLong("assigned_user_id")).thenReturn(2L);

        TaskDAOImpl taskDAO = new TaskDAOImpl(connection);

        Task result = taskDAO.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testFindByIdSQLException() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        doThrow(new SQLException("Database error")).when(stmt).executeQuery();

        TaskDAOImpl taskDAO = new TaskDAOImpl(connection);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> taskDAO.findById(1L));
        assertEquals("Database error", thrown.getCause().getMessage());
    }

    @Test
    public void testFindAll() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("title")).thenReturn("Title");
        when(rs.getString("description")).thenReturn("Description");
        when(rs.getLong("assigned_user_id")).thenReturn(2L);

        TaskFactoryImpl taskFactory = mock(TaskFactoryImpl.class);
        Task task = mock(Task.class);
        when(taskFactory.create()).thenReturn(task);

        TaskDAOImpl taskDAO = new TaskDAOImpl(connection);

        List<Task> result = taskDAO.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    public void testFindAllSQLException() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        doThrow(new SQLException("Database error")).when(stmt).executeQuery();

        TaskDAOImpl taskDAO = new TaskDAOImpl(connection);

        RuntimeException thrown = assertThrows(RuntimeException.class, taskDAO::findAll);
        assertEquals("Database error", thrown.getCause().getMessage());
    }

    @Test
    void testHandleDatabaseError() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        TaskDAOImpl dao = new TaskDAOImpl(mockConnection);

        SQLException sqlException = new SQLException("Test SQL exception");
        doThrow(new SQLException("Rollback failed")).when(mockConnection).rollback();
        DatabaseOperationException thrown = assertThrows(DatabaseOperationException.class, () -> {
            dao.handleDatabaseError(sqlException);
        });

        assertEquals("Database error occurred", thrown.getMessage());
    }

    @Test
    void testRestoreAutoCommitState() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        TaskDAOImpl dao = new TaskDAOImpl(mockConnection);
        doThrow(new SQLException("Rollback failed")).when(mockConnection).rollback();

        assertDoesNotThrow(() -> {
            dao.restoreAutoCommitState(false);
        });
        assertDoesNotThrow(() -> {
            dao.restoreAutoCommitState(true);
        });
    }

    @Test
    void testGetAssignedUserIdQuery() {
        Task taskDTO = new Task();
        Task existingTask = new Task();
        StringBuilder updateSql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        taskDTO.setAssignedUserId(1L);
        existingTask.setAssignedUserId(2L);

        TaskDAOImpl.getAssignedUserIdQuery(taskDTO, existingTask, updateSql, parameters);

        assertTrue(updateSql.toString().contains("assigned_user_id = ?, "));
        assertEquals(1, parameters.size());
        assertEquals(1L, parameters.get(0));
    }

    @Test
    void testHandleDatabaseErrorWithRollbackFailure() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        TaskDAOImpl dao = new TaskDAOImpl(mockConnection);

        SQLException sqlException = new SQLException("Test SQL exception");
        SQLException rollbackException = new SQLException("Rollback failed");
        doThrow(rollbackException).when(mockConnection).rollback();
        DatabaseOperationException thrown = assertThrows(DatabaseOperationException.class, () -> {
            dao.handleDatabaseError(sqlException);
        });

        assertEquals("Database error occurred", thrown.getMessage());
        assertEquals(sqlException, thrown.getCause());
    }

    @Test
    void testHandleDatabaseErrorWithoutRollbackFailure() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        TaskDAOImpl dao = new TaskDAOImpl(mockConnection);

        SQLException sqlException = new SQLException("Test SQL exception");
        assertThrows(DatabaseOperationException.class, () -> {
            dao.handleDatabaseError(sqlException);
        });

        verify(mockConnection).rollback();
    }

    @Test
    void testRestoreAutoCommitStateWithSuccess() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        TaskDAOImpl dao = new TaskDAOImpl(mockConnection);
        assertDoesNotThrow(() -> {
            dao.restoreAutoCommitState(true);
        });

        verify(mockConnection).setAutoCommit(true);
    }

    @Test
    void testGetAssignedUserIdQueryWithDifferentValues() {
        Task taskDTO = new Task();
        Task existingTask = new Task();
        StringBuilder updateSql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        taskDTO.setAssignedUserId(1L);
        existingTask.setAssignedUserId(2L);
        TaskDAOImpl.getAssignedUserIdQuery(taskDTO, existingTask, updateSql, parameters);
        assertTrue(updateSql.toString().contains("assigned_user_id = ?, "));
        assertEquals(1, parameters.size());
        assertEquals(1L, parameters.get(0));
        updateSql.setLength(0);
        parameters.clear();
        taskDTO.setAssignedUserId(null);
        existingTask.setAssignedUserId(2L);
        TaskDAOImpl.getAssignedUserIdQuery(taskDTO, existingTask, updateSql, parameters);
        assertFalse(updateSql.toString().contains("assigned_user_id = ?, "));
        assertTrue(parameters.isEmpty());
        updateSql.setLength(0);
        parameters.clear();
        taskDTO.setAssignedUserId(2L);
        existingTask.setAssignedUserId(2L);
        TaskDAOImpl.getAssignedUserIdQuery(taskDTO, existingTask, updateSql, parameters);
        assertFalse(updateSql.toString().contains("assigned_user_id = ?, "));
        assertTrue(parameters.isEmpty());
    }
}