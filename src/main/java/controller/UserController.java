package controller;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.TaskDTO;
import dto.UserDTO;
import exception.ServiceException;
import factory.impl.UserControllerFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;

import java.io.IOException;
import java.util.List;

@WebServlet("/users/*")
public class UserController extends HttpServlet {

    private final transient UserService userService;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public UserController() {
        UserController controller = UserControllerFactory.createUserController();
        this.userService = controller.userService;
        this.objectMapper = controller.objectMapper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
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
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
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
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
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

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {
            LOGGER.error("Error send error response", e);
        }
    }

    void handleError(HttpServletResponse resp, int statusCode, String message) {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {
            LOGGER.error("Error handle error response", e);
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
            } catch (IOException ex) {
                LOGGER.error("Error sending internal server error response", ex);
            }
        }
    }

    void handleSendErrorException(int statusCode, String message, HttpServletResponse resp) {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {
            LOGGER.error("Error sending error response", e);
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

    void handleJsonException(Exception e, HttpServletResponse resp) {
        try {
            LOGGER.error("Error processing JSON", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing JSON");
        } catch (IOException ioException) {
            LOGGER.error("Error handle json error response", ioException);

        }
    }

    void handleIOException(IOException e, HttpServletResponse resp) {
        try {
            LOGGER.error("Error handling request", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error handling request");
        } catch (IOException ioException) {
            LOGGER.error("Error IOException error response", ioException);

        }
    }
}
