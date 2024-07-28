package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.TagDTO;
import dto.TaskDTO;
import exception.ServiceException;
import factory.impl.TaskControllerFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.TaskService;

import java.io.IOException;
import java.util.List;

@WebServlet("/tasks/*")
public class TaskController extends HttpServlet {

    private final transient TaskService taskService;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    public TaskController(TaskService taskService, ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.objectMapper = objectMapper;
    }

    public TaskController() {
        TaskController controller = TaskControllerFactory.createTaskController();
        this.taskService = controller.taskService;
        this.objectMapper = controller.objectMapper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllTasks(resp);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetTaskById(pathInfo, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path get");
            }
        } catch (IOException e) {
            LOGGER.error("Error processing GET request", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        if ("/".equals(pathInfo)) {
            try {
                TaskDTO taskDTO = objectMapper.readValue(req.getInputStream(), TaskDTO.class);
                if (taskDTO == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid TaskDTO");
                    return;
                }
                taskService.createTask(taskDTO);
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } catch (IOException e) {
                LOGGER.error("Error processing POST request", e);
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing the request.");
                } catch (IOException ex) {
                    LOGGER.error("Error sending error response post", ex);
                }
            }
        } else if (pathInfo.matches("/\\d+/tags/\\d+")) {
            try {
                String[] pathParts = pathInfo.split("/");
                Long taskId = Long.parseLong(pathParts[1]);
                Long tagId = Long.parseLong(pathParts[3]);
                taskService.assignTagsToTask(taskId, tagId);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } catch (NumberFormatException e) {
                handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag or task ID format", resp);
            }
        } else {
            handleSendErrorException(HttpServletResponse.SC_BAD_REQUEST, "Invalid Path", resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try {
            TaskDTO taskDTO = objectMapper.readValue(req.getInputStream(), TaskDTO.class);
            if (taskDTO == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid TaskDTO");
                return;
            }
            taskService.updateTask(taskDTO);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IOException e) {
            handleIOException(e, resp);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try {
            handleDeleteTask(req, resp);
        } catch (IOException e) {
            LOGGER.error("Error processing DELETE request", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    void handleDeleteTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                try {
                    Long id = Long.parseLong(pathParts[1]);
                    taskService.deleteTask(id);
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } catch (NumberFormatException e) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format");
                } catch (ServiceException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete task");
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
        }
    }

    void handleGetAllTasks(HttpServletResponse resp) {
        try {
            List<TaskDTO> tasks = taskService.getAllTasks();
            writeResponse(resp, tasks);
        } catch (IOException e) {
            LOGGER.error("Error getting all tasks", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    void handleGetTaskById(String pathInfo, HttpServletResponse resp) throws IOException {
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length > 1) {
            try {
                Long id = Long.parseLong(pathParts[1]);
                TaskDTO task = taskService.getTaskById(id);
                if (task != null) {
                    List<TagDTO> tags = taskService.getTagsByTaskId(id);
                    task.setTags(tags);
                    writeResponse(resp, task);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format");
            } catch (IOException e) {
                LOGGER.error("Error getting task by ID", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    void writeResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        objectMapper.writeValue(resp.getOutputStream(), data);
    }

    void handleIOException(IOException e, HttpServletResponse resp) {
        LOGGER.error("An internal error occurred", e);
        try {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred");
        } catch (IOException ex) {
            LOGGER.error("Error sending error response", ex);
        }
    }

    void handleSendErrorException(int statusCode, String message, HttpServletResponse resp) {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {
            LOGGER.error("Error sending error response", e);
        }
    }

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        try {
            resp.sendError(statusCode, message);
        } catch (IOException e) {
            LOGGER.error("Error send error response", e);
        }
    }

}
