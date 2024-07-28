package factory.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.DatabaseConfig;
import controller.TagController;
import controller.UserController;
import dao.TagDAO;
import dao.UserDAO;
import dao.impl.TagDAOImpl;
import dao.impl.UserDAOImpl;
import exception.InitializationException;
import service.impl.TagServiceImpl;
import service.impl.UserServiceImpl;

import java.sql.Connection;
import java.sql.SQLException;

public class TagControllerFactory {

    private TagControllerFactory() {}
    public static TagController createTagController() {
        try {
            Connection connection = DatabaseConfig.getConnection();
            TagDAO tagDAO = new TagDAOImpl(connection);
            TagServiceImpl tagService = new TagServiceImpl(tagDAO);
            ObjectMapper objectMapper = new ObjectMapper();
            return new TagController(tagService, objectMapper);
        } catch (SQLException e) {
            throw new InitializationException("Failed to initialize components", e);
        }
    }
}
