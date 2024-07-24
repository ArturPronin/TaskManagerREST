package controller;

import config.DatabaseConfig;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ControllersInitTest {

    private TaskController taskController;
    private UserController userController;

    @BeforeEach
    void setUp() {
        taskController = new TaskController();
        userController = new UserController();
    }

    @Test
    void testTaskControllerInit() {
        try (MockedStatic<DatabaseConfig> mockedDatabaseConfig = mockStatic(DatabaseConfig.class)) {
            Connection mockConnection = mock(Connection.class);

            mockedDatabaseConfig.when(DatabaseConfig::getConnection).thenReturn(mockConnection);

            taskController.init();

            mockedDatabaseConfig.verify(DatabaseConfig::getConnection, times(1));
        }
    }

    @Test
    void testTaskControllerInitSQLException() {
        try (MockedStatic<DatabaseConfig> mockedDatabaseConfig = mockStatic(DatabaseConfig.class)) {

            mockedDatabaseConfig.when(DatabaseConfig::getConnection).thenThrow(SQLException.class);

            assertThrows(RuntimeException.class, taskController::init);
        }
    }

    @Test
    void testUserControllerInit() {
        try (MockedStatic<DatabaseConfig> mockedDatabaseConfig = mockStatic(DatabaseConfig.class)) {
            Connection mockConnection = mock(Connection.class);

            mockedDatabaseConfig.when(DatabaseConfig::getConnection).thenReturn(mockConnection);

            userController.init();

            mockedDatabaseConfig.verify(DatabaseConfig::getConnection, times(1));
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testUserControllerInitSQLException() {
        try (MockedStatic<DatabaseConfig> mockedDatabaseConfig = mockStatic(DatabaseConfig.class)) {

            mockedDatabaseConfig.when(DatabaseConfig::getConnection).thenThrow(SQLException.class);

            assertThrows(RuntimeException.class, userController::init);
        }
    }
}