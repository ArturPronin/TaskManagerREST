package factory.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.DatabaseConfig;
import config.InitSQLScheme;
import controller.TaskController;
import dao.TaskDAO;
import dao.impl.TaskDAOImpl;
import exception.InitializationException;
import service.impl.TaskServiceImpl;

import java.sql.Connection;
import java.sql.SQLException;

public class TaskControllerFactory {

    private TaskControllerFactory() {}
    public static TaskController createTaskController() {
        try {
            Connection connection = DatabaseConfig.getConnection();
            TaskDAO taskDAO = new TaskDAOImpl(connection);
            TaskServiceImpl taskService = new TaskServiceImpl(taskDAO);
            ObjectMapper objectMapper = new ObjectMapper();
            InitSQLScheme.initSqlScheme();
            return new TaskController(taskService, objectMapper);
        } catch (SQLException e) {
            throw new InitializationException("Failed to initialize components", e);
        }
    }
}
