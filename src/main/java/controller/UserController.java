package controller;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.DatabaseConfig;
import dao.UserDAO;
import dao.impl.UserDAOImpl;
import dto.TaskDTO;
import dto.UserDTO;
import exception.InitializationException;
import exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.UserService;
import service.impl.UserServiceImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/users/*")
public class UserController extends HttpServlet {

    transient UserService userService;
    ObjectMapper objectMapper;

    public UserController(UserServiceImpl userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public UserController() {
    }
    @Override
    public void init() throws ServletException {
        try {
            Connection connection = DatabaseConfig.getConnection();
            UserDAO userDAO = new UserDAOImpl(connection);
            userService = new UserServiceImpl(userDAO);
            objectMapper = new ObjectMapper();
        } catch (SQLException e) {
            throw new InitializationException("Failed to initialize resources", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllUsers(resp);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetUserById(pathInfo, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            }
        } catch (JsonGenerationException | JsonMappingException e) {
            handleJsonException(e, resp);
        } catch (IOException e) {
            handleIOException(e, resp);
        }
    }
    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if ("/".equals(pathInfo)) {
            UserDTO userDTO;
            try {
                userDTO = objectMapper.readValue(req.getInputStream(), UserDTO.class);
            } catch (IOException e) {
                handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid UserDTO", resp);
                return;
            }

            if (userDTO == null) {
                handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "UserDTO cannot be null", resp);
                return;
            }

            userService.createUser(userDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } else if (pathInfo.matches("/\\d+/tasks/\\d+")) {
            try {
                String[] pathParts = pathInfo.split("/");
                Long userId = Long.parseLong(pathParts[1]);
                Long taskId = Long.parseLong(pathParts[3]);
                userService.assignTaskToUser(userId, taskId);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } catch (NumberFormatException e) {
                handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid user or task ID format", resp);
            }
        } else {
            handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid Path", resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO userDTO;
        try {
            userDTO = objectMapper.readValue(req.getInputStream(), UserDTO.class);
        } catch (IOException e) {

            handleError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid UserDTO");
            return;
        }

        if (userDTO == null) {

            handleError(resp, HttpServletResponse.SC_BAD_REQUEST, "UserDTO cannot be null");
            return;
        }

        if (userService.getUserById(userDTO.getId()) == null) {

            handleError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        try {

            userService.updateUser(userDTO);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {

            handleError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update user");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                try {
                    Long id = Long.parseLong(pathParts[1]);
                    userService.deleteUser(id);
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } catch (NumberFormatException e) {
                    handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format", resp);
                } catch (ServiceException e) {
                    handleSendErrorException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete user", resp);
                }
            } else {
                handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid path length", resp);
            }
        } else {
            handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Path cannot be null or empty", resp);
        }
    }

    void handleError(HttpServletResponse resp, int statusCode, String message) {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {

            e.printStackTrace();

            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
            } catch (IOException innerEx) {
                innerEx.printStackTrace();

            }
        }
    }

    void handleSendErrorException(int statusCode, String message, HttpServletResponse resp) {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void handleGetAllUsers(HttpServletResponse resp) throws IOException {
        try {
            List<UserDTO> users = userService.getAllUsers();
            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getOutputStream(), users);
        } catch (IOException e) {

            handleIOException(e, resp);
        }
    }

    void handleGetUserById(String pathInfo, HttpServletResponse resp) throws IOException {
        try {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                Long id = Long.parseLong(pathParts[1]);
                UserDTO user = userService.getUserById(id);
                if (user != null) {
                    List<TaskDTO> tasks = userService.getTasksByUserId(id);
                    user.setTasks(tasks);
                    resp.setContentType("application/json");
                    objectMapper.writeValue(resp.getOutputStream(), user);
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found");
                }
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path length");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (IOException e) {

            handleIOException(e, resp);
        }
    }

    private void handleJsonException(Exception e, HttpServletResponse resp) {
        try {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing JSON");
        } catch (IOException ioException) {
            ioException.printStackTrace();

        }
    }

    private void handleIOException(IOException e, HttpServletResponse resp) {
        try {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error handling request");
        } catch (IOException ioException) {
            ioException.printStackTrace();

        }
    }
}
