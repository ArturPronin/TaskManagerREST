package factory.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.DatabaseConfig;
import controller.UserController;
import dao.UserDAO;
import dao.impl.UserDAOImpl;
import exception.InitializationException;
import service.impl.UserServiceImpl;

import java.sql.Connection;
import java.sql.SQLException;

public class UserControllerFactory {

    public static UserController createUserController() {
        try {
            Connection connection = DatabaseConfig.getConnection();
            UserDAO userDAO = new UserDAOImpl(connection);
            UserServiceImpl userService = new UserServiceImpl(userDAO);
            ObjectMapper objectMapper = new ObjectMapper();
            return new UserController(userService, objectMapper);
        } catch (SQLException e) {
            throw new InitializationException("Failed to initialize components", e);
        }
    }
}
