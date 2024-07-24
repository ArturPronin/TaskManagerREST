package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.DatabaseConfig;
import dao.TaskDAO;
import dao.impl.TaskDAOImpl;
import dto.TaskDTO;
import exception.InitializationException;
import exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.TaskService;
import service.impl.TaskServiceImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/tasks/*")
public class TaskController extends HttpServlet {

    transient TaskService taskService;
    ObjectMapper objectMapper;

    public TaskController(TaskServiceImpl taskService, ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.objectMapper = objectMapper;
    }

    public TaskController() {
    }
    @Override
    public void init() {
        try {
            Connection connection = DatabaseConfig.getConnection();
            TaskDAO taskDAO = new TaskDAOImpl(connection);
            taskService = new TaskServiceImpl(taskDAO);
            objectMapper = new ObjectMapper();
        } catch (SQLException e) {
            throw new InitializationException("Failed to initialize components", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllTasks(resp);
            } else {
                handleGetTaskById(pathInfo, resp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleGetAllTasks(HttpServletResponse resp) {
        try {
            List<TaskDTO> tasks = taskService.getAllTasks();
            writeResponse(resp, tasks);
        } catch (IOException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            TaskDTO taskDTO = objectMapper.readValue(req.getInputStream(), TaskDTO.class);
            if (taskDTO == null) {

                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid TaskDTO");
                return;
            }
            taskService.createTask(taskDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IOException e) {

            e.printStackTrace();
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing the request.");
            } catch (IOException ex) {

                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
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
            e.printStackTrace();
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

    void handleGetTaskById(String pathInfo, HttpServletResponse resp) throws IOException {
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length > 1) {
            try {
                Long id = Long.parseLong(pathParts[1]);
                TaskDTO task = taskService.getTaskById(id);
                if (task != null) {
                    writeResponse(resp, task);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format");
            } catch (IOException e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void writeResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        objectMapper.writeValue(resp.getOutputStream(), data);
    }

    private void handleIOException(IOException e, HttpServletResponse resp) {
        e.printStackTrace();
        try {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
